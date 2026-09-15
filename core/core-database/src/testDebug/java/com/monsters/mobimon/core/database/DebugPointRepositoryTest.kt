package com.monsters.mobimon.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.domain.CurrentAppUse
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.UtcClock
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class DebugPointRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var databaseName: String
    private val ids = AtomicInteger()
    private var appUse = AppUseState.ALLOWED

    @Before
    fun setUp() {
        runBlocking {
            context = ApplicationProvider.getApplicationContext()
            databaseName = "debug-points-${System.nanoTime()}.db"
            database = openDatabase()
            database.companionDao().insertProfile(PetProfileEntity("profile", "GOLDEN", 0))
            database.economyDao().insertAccount(PointAccountEntity("profile", 0))
        }
    }

    @After
    fun tearDown() {
        if (database.isOpen) database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun `debug adjustments and ledger remain consistent after reopening`() =
        runBlocking {
            val repository = subject()

            assertEquals(DebugPointResult.UPDATED, repository.add(100))
            assertEquals(DebugPointResult.UPDATED, repository.subtract(25))
            reopenDatabase()

            assertEquals(75L, database.economyDao().account("profile")?.balance)
            assertEquals(75L, database.economyDao().ledger("profile").sumOf { it.amount })

            assertEquals(DebugPointResult.UPDATED, subject().reset())
            reopenDatabase()

            assertEquals(0L, database.economyDao().account("profile")?.balance)
            assertEquals(0L, database.economyDao().ledger("profile").sumOf { it.amount })
        }

    @Test
    fun `restricted app use rejects debug adjustment inside transaction`() =
        runBlocking {
            appUse = AppUseState.RESTRICTED

            assertEquals(DebugPointResult.INTERACTION_RESTRICTED, subject().add(100))
            assertEquals(0L, database.economyDao().account("profile")?.balance)
            assertEquals(emptyList<PointLedgerEntity>(), database.economyDao().ledger("profile"))
        }

    @Test
    fun `ledger failure rolls back debug balance change`() =
        runBlocking {
            val duplicateIds = IdGenerator { "duplicate" }
            val repository = subject(duplicateIds)

            assertEquals(DebugPointResult.UPDATED, repository.add(10))
            assertEquals(DebugPointResult.STORAGE_FAILURE, repository.add(20))

            assertEquals(10L, database.economyDao().account("profile")?.balance)
            assertEquals(listOf(10L), database.economyDao().ledger("profile").map { it.amount })
        }

    private fun subject(idGenerator: IdGenerator = IdGenerator { "debug-${ids.incrementAndGet()}" }) =
        DebugPointRepository(
            database = database,
            profileId = "profile",
            utcClock = UtcClock { 1_800_000_000_000L },
            ids = idGenerator,
            appUse = CurrentAppUse { appUse },
        )

    private fun openDatabase(): AppDatabase =
        Room
            .databaseBuilder(context, AppDatabase::class.java, databaseName)
            .allowMainThreadQueries()
            .build()

    private fun reopenDatabase() {
        database.close()
        database = openDatabase()
    }
}
