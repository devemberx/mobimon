package com.monsters.mobimon.core.auth

import com.monsters.mobimon.core.domain.ConversationProblem
import com.monsters.mobimon.core.domain.ConversationTurn
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.io.InterruptedIOException
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class OkHttpCopilotApi(
    client: OkHttpClient,
    private val nowMillis: () -> Long,
    private val userUrl: HttpUrl = "https://api.github.com/copilot_internal/user".toHttpUrl(),
) : CopilotApi {
    // Never follow a provider redirect with a credential or automatically replay a paid request.
    private val client =
        client
            .newBuilder()
            .followRedirects(
                false,
            ).followSslRedirects(false)
            .retryOnConnectionFailure(false)
            .build()

    override suspend fun authorize(githubToken: String): CopilotAccess {
        val json =
            request(
                CopilotRequestStage.AUTHORIZATION,
                Request
                    .Builder()
                    .url(userUrl)
                    .header("Authorization", "Bearer $githubToken")
                    .header("X-GitHub-Api-Version", "2022-11-28"),
            )
        if (json.opt("chat_enabled") != true) fail(ConversationProblem.ACCESS)
        val endpoint =
            json.optJSONObject("endpoints")?.opt("api")?.let {
                (it as? String)?.toHttpUrlOrNull() ?: fail(ConversationProblem.PROVIDER)
            } ?: "https://api.githubcopilot.com/".toHttpUrl()
        if (endpoint.scheme != "https" ||
            endpoint.host !in
            setOf(
                "api.githubcopilot.com",
                "api.individual.githubcopilot.com",
                "api.business.githubcopilot.com",
                "api.enterprise.githubcopilot.com",
            ) ||
            endpoint.port != 443 ||
            endpoint.username.isNotEmpty() ||
            endpoint.password.isNotEmpty() ||
            endpoint.encodedPath != "/" ||
            endpoint.query != null ||
            endpoint.fragment != null
        ) {
            fail(ConversationProblem.PROVIDER)
        }
        // Cache the access check briefly, not the OAuth token's actual lifetime. The owner
        // refreshes/validates its credential on every operation before this cache is used.
        return CopilotAccess(githubToken, nowMillis() + 300_000, endpoint)
    }

    override suspend fun models(access: CopilotAccess): List<CopilotModel> {
        val data =
            request(CopilotRequestStage.MODELS, builder(access, "models")).optJSONArray("data")
                ?: fail(ConversationProblem.PROVIDER)
        val models = (0 until data.length()).mapNotNull { data.optJSONObject(it)?.let { item -> parseModel(item) } }
        CopilotDiagnostics.models(data.length(), models.count { it.enabled && it.api != null })
        return models
    }

    override suspend fun auto(
        access: CopilotAccess,
        prompt: String,
        models: List<CopilotModel>,
    ): CopilotAutoSession {
        val json =
            request(
                CopilotRequestStage.AUTO,
                builder(access, "auto").post(
                    JSONObject().put("prompt", prompt).toString().toRequestBody(JSON_MEDIA_TYPE),
                ),
            )
        val token = json.opt("session_token") as? String ?: fail(ConversationProblem.PROVIDER)
        val expiry = (json.opt("expires_at") as? Number)?.toLong() ?: fail(ConversationProblem.PROVIDER)
        if (token.isBlank() ||
            token.length > 16_384 ||
            token.any { it.code !in 33..126 } ||
            expiry <= nowMillis() / 1000 + 300 ||
            expiry > nowMillis() / 1000 + 86_700
        ) {
            fail(ConversationProblem.PROVIDER)
        }
        val selected = json.optJSONObject("selected_model") ?: fail(ConversationProblem.PROVIDER)
        val id = selected.opt("id") as? String ?: fail(ConversationProblem.PROVIDER)
        if (selected.optJSONObject("policy")?.optString("state")?.let { it != "enabled" } == true) {
            fail(ConversationProblem.ACCESS)
        }
        val model =
            models.firstOrNull { it.id == id } ?: parseModel(selected, embedded = true)
                ?: fail(ConversationProblem.PROVIDER)
        if (!model.enabled) fail(ConversationProblem.ACCESS)
        if (model.api == null) fail(ConversationProblem.PROVIDER)
        return CopilotAutoSession(model, token, expiry * 1000)
    }

    override suspend fun complete(
        access: CopilotAccess,
        session: CopilotAutoSession,
        friendId: String,
        messages: List<ConversationTurn>,
    ): String {
        val model = session.model
        val endpoint = model.api ?: fail(ConversationProblem.PROVIDER)
        val body = CopilotMessageCodec.request(model, friendId, messages)
        val json =
            request(
                CopilotRequestStage.COMPLETION,
                builder(access, endpoint.path)
                    .header("X-Initiator", "user")
                    .header("Copilot-Session-Token", session.token)
                    .post(body.toString().toRequestBody(JSON_MEDIA_TYPE)),
            )
        return CopilotMessageCodec.reply(endpoint, json)
    }

    private fun validModelId(id: String) = id.matches(Regex("[A-Za-z0-9._:/-]{1,128}")) && id != "auto"

    private fun parseModel(
        item: JSONObject,
        embedded: Boolean = false,
    ): CopilotModel? {
        val id = item.opt("id") as? String ?: return null
        if (!validModelId(id)) return null
        val capabilities = item.optJSONObject("capabilities") ?: return null
        if (capabilities.optString("type", if (embedded) "chat" else "") != "chat") return null
        val endpoints = item.optJSONArray("supported_endpoints")
        val paths = endpoints?.let { (0 until it.length()).map(it::optString) }
        val endpoint =
            when {
                paths == null || "/chat/completions" in paths -> CopilotChatApi.CHAT_COMPLETIONS
                "/responses" in paths -> CopilotChatApi.RESPONSES
                else -> null
            }
        val limit = capabilities.optJSONObject("limits")?.optInt("max_output_tokens", 2048) ?: 2048
        // Picker visibility controls manual selection, not eligibility for a server-issued Auto session.
        return CopilotModel(
            id,
            endpoint,
            (item.optJSONObject("policy")?.optString("state") ?: "enabled") == "enabled",
            limit.coerceIn(1, 2048),
        )
    }

    private fun builder(
        access: CopilotAccess,
        path: String,
    ) = Request
        .Builder()
        .url(access.endpoint.resolve(path)!!)
        .header("Authorization", "Bearer ${access.token}")
        .header("X-GitHub-Api-Version", "2026-08-01")
        .header("Copilot-Integration-Id", "mobimon")

    private suspend fun request(
        stage: CopilotRequestStage,
        builder: Request.Builder,
    ): JSONObject =
        suspendCancellableCoroutine { continuation ->
            val call =
                client.newCall(
                    builder
                        .header("Accept", "application/json")
                        .header("User-Agent", "MobiMon/0.1")
                        .header("Editor-Version", "MobiMon/0.1")
                        .header("Editor-Plugin-Version", "mobimon/0.1")
                        .header("X-Request-Id", UUID.randomUUID().toString())
                        .build(),
                )
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(
                object : Callback {
                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                ConversationException(
                                    if (e is InterruptedIOException) {
                                        ConversationProblem.TIMEOUT
                                    } else {
                                        ConversationProblem.NETWORK
                                    },
                                ),
                            )
                        }
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        try {
                            val json =
                                response.use {
                                    CopilotDiagnostics.http(stage, it.code)
                                    val rejection =
                                        if (!it.isSuccessful) {
                                            val source = it.body?.source()
                                            val error =
                                                if (source != null && !source.request(16_385)) {
                                                    val body = source.readUtf8()
                                                    runCatching { JSONObject(body) }
                                                        .getOrElse { JSONObject().put("message", body) }
                                                } else {
                                                    null
                                                }
                                            CopilotRejection.from(error).also(CopilotDiagnostics::rejection)
                                        } else {
                                            CopilotRejection.UNKNOWN
                                        }
                                    when (it.code) {
                                        401 -> fail(ConversationProblem.ACCOUNT)
                                        403 -> {
                                            fail(
                                                if (rejection ==
                                                    CopilotRejection.RATE_LIMIT
                                                ) {
                                                    ConversationProblem.USAGE
                                                } else {
                                                    ConversationProblem.ACCESS
                                                },
                                            )
                                        }
                                        402, 429 -> fail(ConversationProblem.USAGE)
                                        408, 504 -> fail(ConversationProblem.TIMEOUT)
                                        in 500..599 ->
                                            fail(
                                                if (stage == CopilotRequestStage.AUTO &&
                                                    rejection == CopilotRejection.NO_ELIGIBLE_MODELS
                                                ) {
                                                    ConversationProblem.AUTO_UNAVAILABLE
                                                } else {
                                                    ConversationProblem.SERVICE
                                                },
                                            )
                                    }
                                    if (it.code == 404 && stage == CopilotRequestStage.AUTO) {
                                        fail(ConversationProblem.AUTO_UNAVAILABLE)
                                    }
                                    if (!it.isSuccessful) fail(ConversationProblem.PROVIDER)
                                    val source = it.body?.source() ?: fail(ConversationProblem.PROVIDER)
                                    if (source.request(1_048_577)) fail(ConversationProblem.PROVIDER)
                                    JSONObject(source.readUtf8())
                                }
                            if (continuation.isActive) continuation.resume(json)
                        } catch (error: Exception) {
                            val problem =
                                when (error) {
                                    is ConversationException -> error.problem
                                    is InterruptedIOException -> ConversationProblem.TIMEOUT
                                    is IOException -> ConversationProblem.NETWORK
                                    else -> ConversationProblem.PROVIDER
                                }
                            if (continuation.isActive) continuation.resumeWithException(ConversationException(problem))
                        }
                    }
                },
            )
        }

    private fun fail(problem: ConversationProblem): Nothing = throw ConversationException(problem)

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
