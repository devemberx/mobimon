package com.devemberx.rivo.core.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.devemberx.rivo.core.domain.CompanionSettings
import com.devemberx.rivo.core.domain.WriteResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.concurrent.CancellationException

class DataStoreSettingsRepositoryTest {
    @Test
    fun `settings defaults and successful writes persist after reopening`() =
        runBlocking {
            val file = File(System.getProperty("java.io.tmpdir"), "settings-${System.nanoTime()}.preferences_pb")
            var scope = dataStoreScope()
            try {
                var repository = DataStoreSettingsRepository(preferenceStore(file, scope))
                assertEquals(CompanionSettings(), repository.settings.first())
                assertEquals(WriteResult.Success, repository.setShowOnVehicleHome(false))
                assertEquals(WriteResult.Success, repository.setReducedMotion(true))
                assertEquals(CompanionSettings(false, true), repository.settings.first())
                scope.stopDataStore()

                scope = dataStoreScope()
                repository = DataStoreSettingsRepository(preferenceStore(file, scope))
                assertEquals(CompanionSettings(false, true), repository.settings.first())
            } finally {
                scope.stopDataStore()
                file.delete()
            }
        }

    @Test
    fun `settings write reports io failure`() =
        runBlocking {
            val repository = DataStoreSettingsRepository(FailingDataStore(IOException("disk unavailable")))

            assertEquals(WriteResult.Failure, repository.setReducedMotion(true))
        }

    @Test
    fun `settings write rethrows cancellation`() =
        runBlocking {
            val repository = DataStoreSettingsRepository(FailingDataStore(CancellationException("cancelled")))

            var cancelled = false
            try {
                repository.setShowOnVehicleHome(false)
            } catch (_: CancellationException) {
                cancelled = true
            }
            assertTrue(cancelled)
        }

    private fun dataStoreScope() = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private suspend fun CoroutineScope.stopDataStore() {
        coroutineContext[Job]?.cancelAndJoin()
    }

    private fun preferenceStore(
        file: File,
        scope: CoroutineScope,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        )
}

private class FailingDataStore(
    private val failure: Throwable,
) : DataStore<Preferences> {
    override val data: Flow<Preferences> = flowOf(emptyPreferences())

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences = throw failure
}
