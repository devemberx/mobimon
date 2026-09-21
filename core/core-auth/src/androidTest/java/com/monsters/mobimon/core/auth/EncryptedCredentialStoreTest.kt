package com.monsters.mobimon.core.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class EncryptedCredentialStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val store = EncryptedCredentialStore(context)

    @After fun cleanup() = runBlocking { store.clear() }

    @Test fun keystoreEncryptsBothTokensAndNewStoreRestoresThem() =
        runBlocking {
            store.write(StoredCredential("client", GitHubTokens("access-secret", 12345, "refresh-secret", 98765)))
            val bytes = File(context.noBackupFilesDir, "github-session.enc").readBytes()
            assertFalse(bytes.toString(Charsets.UTF_8).contains("access-secret"))
            assertFalse(bytes.toString(Charsets.UTF_8).contains("refresh-secret"))
            val reopened = EncryptedCredentialStore(context).read()!!
            assertEquals("access-secret", reopened.tokens.accessToken)
            assertEquals("refresh-secret", reopened.tokens.refreshToken)
            assertEquals(12345L, reopened.tokens.expiresAtMillis)
            store.clear()
            assertNull(EncryptedCredentialStore(context).read())
        }

    @Test fun tamperingIsRejectedAndFreshApprovalCanReplaceIt() =
        runBlocking {
            store.write(StoredCredential("client", GitHubTokens("original")))
            val file = File(context.noBackupFilesDir, "github-session.enc")
            val bytes = file.readBytes()
            bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
            file.writeBytes(bytes)
            var rejected = false
            try {
                store.read()
            } catch (_: Exception) {
                rejected = true
            }
            assertTrue(rejected)
            store.clear()
            store.write(StoredCredential("client", GitHubTokens("replacement")))
            assertEquals("replacement", store.read()!!.tokens.accessToken)
        }
}
