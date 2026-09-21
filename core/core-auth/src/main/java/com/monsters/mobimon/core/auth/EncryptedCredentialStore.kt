package com.monsters.mobimon.core.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Credentials never enter preferences, backups, saved state, or logs. */
internal class EncryptedCredentialStore(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CredentialStore {
    private val file = AtomicFile(File(context.noBackupFilesDir, "github-session.enc"))
    private val alias = "${context.packageName}.github-session.v1"
    private val associatedData = alias.toByteArray(Charsets.UTF_8)

    override suspend fun read(): StoredCredential? =
        withContext(dispatcher) {
            if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists()) return@withContext null
            val bytes = file.openRead().use { it.readNBytes(32_769) }
            check(bytes.size in 30..32_768 && bytes[0] == 1.toByte())
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(create = false), GCMParameterSpec(128, bytes.copyOfRange(1, 13)))
            cipher.updateAAD(associatedData)
            val plaintext = cipher.doFinal(bytes, 13, bytes.size - 13)
            try {
                val json = JSONObject(plaintext.toString(Charsets.UTF_8))
                StoredCredential(
                    json.getString("clientId"),
                    GitHubTokens(
                        json.getString("accessToken").also { check(it.isNotBlank()) },
                        json.optionalLong("expiresAt"),
                        if (json.has("refreshToken")) json.getString("refreshToken") else null,
                        json.optionalLong("refreshExpiresAt"),
                    ),
                )
            } finally {
                plaintext.fill(0)
            }
        }

    override suspend fun write(credential: StoredCredential) =
        withContext(dispatcher) {
            val tokens = credential.tokens
            val plaintext =
                JSONObject()
                    .put("clientId", credential.clientId)
                    .put("accessToken", tokens.accessToken)
                    .put("expiresAt", tokens.expiresAtMillis)
                    .put("refreshToken", tokens.refreshToken)
                    .put("refreshExpiresAt", tokens.refreshExpiresAtMillis)
                    .toString()
                    .toByteArray(Charsets.UTF_8)
            val encrypted =
                try {
                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    cipher.init(Cipher.ENCRYPT_MODE, key(create = true))
                    cipher.updateAAD(associatedData)
                    check(cipher.iv.size == 12)
                    byteArrayOf(1) + cipher.iv + cipher.doFinal(plaintext)
                } finally {
                    plaintext.fill(0)
                }
            val stream = file.startWrite()
            try {
                stream.write(encrypted)
                file.finishWrite(stream)
            } catch (error: Exception) {
                file.failWrite(stream)
                throw error
            }
        }

    override suspend fun clear() =
        withContext(dispatcher) {
            file.delete()
            check(listOf("", ".bak", ".new").none { File(file.baseFile.path + it).exists() })
            keyStore().deleteEntry(alias)
        }

    private fun key(create: Boolean): SecretKey {
        val store = keyStore()
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        check(create) { "Credential key unavailable" }
        return KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            .apply {
                init(
                    KeyGenParameterSpec
                        .Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setKeySize(256)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(true)
                        .build(),
                )
            }.generateKey()
    }

    private fun keyStore() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun JSONObject.optionalLong(name: String) = if (has(name)) getLong(name) else null
}
