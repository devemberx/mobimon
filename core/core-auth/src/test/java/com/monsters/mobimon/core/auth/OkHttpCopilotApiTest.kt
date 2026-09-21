package com.monsters.mobimon.core.auth

import com.monsters.mobimon.core.domain.ConversationProblem
import com.monsters.mobimon.core.domain.ConversationTurn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OkHttpCopilotApiTest {
    private val server = MockWebServer()
    private lateinit var api: OkHttpCopilotApi
    private lateinit var access: CopilotAccess
    private val session =
        CopilotAutoSession(CopilotModel("current-default", CopilotChatApi.CHAT_COMPLETIONS), "auto-secret", 4_600_000)

    @Before fun setup() {
        server.start()
        api = OkHttpCopilotApi(OkHttpClient(), { 1_000_000 }, server.url("/copilot_internal/user"))
        access = CopilotAccess("copilot-secret", 4_600_000, server.url("/"))
    }

    @After fun close() = server.shutdown()

    @Test fun authorizationRequiresChatAccessAndAcceptsOnlyTrustedCopilotHosts() =
        runBlocking {
            enqueue(
                """{"chat_enabled":true,
                   "endpoints":{"api":"https://api.individual.githubcopilot.com"}}""",
            )
            val token = api.authorize("github-secret")
            assertEquals("api.individual.githubcopilot.com", token.endpoint.host)
            assertEquals(1_300_000, token.expiresAtMillis)
            assertEquals("Bearer github-secret", server.takeRequest().getHeader("Authorization"))
            for (endpoint in listOf(
                "https://attacker.example/",
                "https://api.githubcopilot.com.attacker.example/",
                "http://api.githubcopilot.com/",
                "https://api.githubcopilot.com/private",
                "https://user@api.githubcopilot.com/",
            )) {
                enqueue(
                    JSONObject()
                        .put("chat_enabled", true)
                        .put("endpoints", JSONObject().put("api", endpoint))
                        .toString(),
                )
                assertProblem(ConversationProblem.PROVIDER) { api.authorize("github-secret") }
            }
            assertFalse(token.toString().contains("github-secret"))
            for (body in listOf("{}", "{\"chat_enabled\":false}")) {
                enqueue(body)
                assertProblem(ConversationProblem.ACCESS) { api.authorize("github-secret") }
            }
        }

    @Test fun modelDiscoveryRetainsAutoOnlyModelsWithoutSelectingAManualDefault() =
        runBlocking {
            enqueue(
                """{"data":[
          {"id":"blocked","is_chat_default":true,"policy":{"state":"disabled"},"capabilities":{"type":"chat"}},
          {"id":"responses-only","capabilities":{"type":"chat"},"supported_endpoints":["/responses"]},
          {"id":"auto-only","model_picker_enabled":false,"capabilities":{"type":"chat"}},
          {"id":"current-default","is_chat_default":true,"capabilities":{"type":"chat"},
           "supported_endpoints":["/chat/completions"]}
        ]}""",
            )
            val models = api.models(access)
            assertFalse(models.first { it.id == "blocked" }.enabled)
            assertEquals(CopilotChatApi.RESPONSES, models.first { it.id == "responses-only" }.api)
            assertTrue(models.first { it.id == "auto-only" }.enabled)
            val request = server.takeRequest()
            assertEquals("/models", request.path)
            assertEquals("Bearer copilot-secret", request.getHeader("Authorization"))
            enqueue("""{"data":[]}""")
            assertTrue(api.models(access).isEmpty())
        }

    @Test fun chatSendsBoundedDialogueAndCompanionOnlyWithNoToolsOrOAuthToken() =
        runBlocking {
            enqueue("""{"choices":[{"finish_reason":"stop","message":{"role":"assistant","content":"안녕하세요"}}]}""")
            assertEquals(
                "안녕하세요",
                api.complete(access, session, "friend:luna", listOf(ConversationTurn("안녕", true))),
            )
            val request = server.takeRequest()
            assertEquals("/chat/completions", request.path)
            assertEquals("user", request.getHeader("X-Initiator"))
            assertEquals("auto-secret", request.getHeader("Copilot-Session-Token"))
            val body = JSONObject(request.body.readUtf8())
            assertEquals("current-default", body.getString("model"))
            assertFalse(body.getBoolean("stream"))
            assertFalse(body.has("tools"))
            assertEquals(2, body.getJSONArray("messages").length())
            assertTrue(
                body
                    .getJSONArray("messages")
                    .getJSONObject(0)
                    .getString("content")
                    .contains("Luna"),
            )
            assertEquals("안녕", body.getJSONArray("messages").getJSONObject(1).getString("content"))
            assertFalse(body.toString().contains("github-secret"))
        }

    @Test fun statusErrorsAreRedactedAndNeverAutomaticallyRetried() =
        runBlocking {
            for ((status, problem) in listOf(
                401 to ConversationProblem.ACCOUNT,
                403 to ConversationProblem.ACCESS,
                402 to ConversationProblem.USAGE,
                429 to ConversationProblem.USAGE,
                504 to ConversationProblem.TIMEOUT,
                503 to ConversationProblem.SERVICE,
                400 to ConversationProblem.PROVIDER,
            )) {
                val before = server.requestCount
                server.enqueue(MockResponse().setResponseCode(status).setBody("private-provider-error"))
                assertProblem(
                    problem,
                ) { api.complete(access, session, "friend:mobi", listOf(ConversationTurn("hello", true))) }
                assertEquals(before + 1, server.requestCount)
            }
        }

    @Test fun providerRejectionsAreClassifiedWithoutExposingProviderText() {
        for (body in listOf(
            """{"error":"no_eligible_models"}""",
            """{"error":{"code":"no_available_models","message":"private-provider-error"}}""",
            """{"message":"No models available for this request"}""",
        )) {
            assertEquals(CopilotRejection.NO_ELIGIBLE_MODELS, CopilotRejection.from(JSONObject(body)))
        }
        assertEquals(
            CopilotRejection.UNSUPPORTED_INTEGRATION,
            CopilotRejection.from(JSONObject("""{"error":{"code":"unsupported_integration"}}""")),
        )
        assertEquals(
            CopilotRejection.UNKNOWN,
            CopilotRejection.from(JSONObject("""{"error":{"message":"private-provider-error"}}""")),
        )
        assertEquals(
            CopilotRejection.NO_HEALTHY_UPSTREAM,
            CopilotRejection.from(JSONObject("""{"message":"no healthy upstream"}""")),
        )
    }

    @Test fun serviceRejectionsRemainBoundedRedactedAndNeverReplayed() =
        runBlocking {
            for ((body, expected) in listOf(
                """{"error":{"code":"no_available_models","message":"private-provider-error"}}""" to
                    ConversationProblem.AUTO_UNAVAILABLE,
                "No eligible models" to ConversationProblem.AUTO_UNAVAILABLE,
                "no healthy upstream" to ConversationProblem.SERVICE,
                "x".repeat(16_385) to ConversationProblem.SERVICE,
                "not json" to ConversationProblem.SERVICE,
            )) {
                val before = server.requestCount
                server.enqueue(MockResponse().setResponseCode(503).setBody(body))
                assertProblem(expected) { api.auto(access, "hello", listOf(session.model)) }
                assertEquals(before + 1, server.requestCount)
            }
            server.enqueue(MockResponse().setResponseCode(503).setBody("No eligible models"))
            assertProblem(ConversationProblem.SERVICE) {
                api.complete(access, session, "friend:mobi", listOf(ConversationTurn("hello", true)))
            }
        }

    @Test fun redirectsMalformedOversizedAndToolResponsesFailClosed() =
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", server.url("/leak")))
            assertProblem(ConversationProblem.PROVIDER) { api.authorize("github-secret") }
            assertEquals(1, server.requestCount)
            for (body in listOf(
                "not json",
                "x".repeat(1_048_577),
                """{"choices":[{"finish_reason":"tool_calls","message":{"role":"assistant","tool_calls":[]}}]}""",
                """{"choices":[{"finish_reason":"stop","message":{"role":"assistant","content":""}}]}""",
            )) {
                enqueue(body)
                assertProblem(ConversationProblem.PROVIDER) {
                    api.complete(access, session, "friend:mobi", listOf(ConversationTurn("hello", true)))
                }
            }
        }

    @Test fun cancellationStopsWaitingForHttpResponse() =
        runBlocking {
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val pending = async(Dispatchers.Default) { api.auto(access, "hello", listOf(session.model)) }
            assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            pending.cancelAndJoin()
            assertTrue(pending.isCancelled)
        }

    @Test fun autoUsesServerSelectedModelAndSessionTokenInsteadOfCatalogDefault() =
        runBlocking {
            enqueue(
                """{"data":[
            {"id":"manual-default","is_chat_default":true,"capabilities":{"type":"chat"}},
            {"id":"auto-only","model_picker_enabled":false,
             "capabilities":{"type":"chat","limits":{"max_output_tokens":1024}}}
        ]}""",
            )
            val models = api.models(access)
            server.takeRequest()
            enqueue(autoResponse(JSONObject().put("id", "auto-only")))
            val routed = api.auto(access, "안녕", models)
            assertEquals("auto-only", routed.model.id)
            assertFalse(routed.toString().contains("auto-secret"))
            val routing = server.takeRequest()
            assertEquals("POST", routing.method)
            assertEquals("/auto", routing.path)
            assertEquals("Bearer copilot-secret", routing.getHeader("Authorization"))
            assertEquals(null, routing.getHeader("Copilot-Session-Token"))
            assertEquals("MobiMon/0.1", routing.getHeader("Editor-Version"))
            val payload = JSONObject(routing.body.readUtf8())
            assertEquals(setOf("prompt"), payload.keys().asSequence().toSet())
            assertEquals("안녕", payload.getString("prompt"))
            assertEquals("2026-08-01", routing.getHeader("X-GitHub-Api-Version"))
            assertEquals("mobimon", routing.getHeader("Copilot-Integration-Id"))
            enqueue("""{"choices":[{"finish_reason":"stop","message":{"role":"assistant","content":"안녕하세요"}}]}""")
            api.complete(access, routed, "friend:mobi", listOf(ConversationTurn("안녕", true)))
            val completion = server.takeRequest()
            assertEquals("auto-secret", completion.getHeader("Copilot-Session-Token"))
            assertEquals("Bearer copilot-secret", completion.getHeader("Authorization"))
            val body = JSONObject(completion.body.readUtf8())
            assertEquals("auto-only", body.getString("model"))
            assertEquals(1024, body.getInt("max_tokens"))
        }

    @Test fun autoAcceptsEmbeddedMetadataWhenCatalogLagsAndUsesResponsesFormat() =
        runBlocking {
            enqueue(
                autoResponse(
                    JSONObject("""{"id":"routed-new","supported_endpoints":["/responses"],"capabilities":{}}"""),
                ),
            )
            val routed = api.auto(access, "hello", listOf(session.model))
            assertEquals(CopilotChatApi.RESPONSES, routed.model.api)
            server.takeRequest()
            enqueue(
                """{"status":"completed","output":[
            {"type":"reasoning","summary":[{"type":"summary_text","text":"private reasoning"}]},
            {"type":"message","role":"assistant","content":[{"type":"output_text","text":"answer"}]}
        ]}""",
            )
            val history =
                listOf(
                    ConversationTurn("first", true),
                    ConversationTurn("prior reply", false),
                    ConversationTurn("hello", true),
                )
            assertEquals("answer", api.complete(access, routed, "friend:luna", history))
            val completion = server.takeRequest()
            assertEquals("/responses", completion.path)
            assertEquals("auto-secret", completion.getHeader("Copilot-Session-Token"))
            val body = JSONObject(completion.body.readUtf8())
            assertEquals("routed-new", body.getString("model"))
            assertTrue(body.getString("instructions").contains("Luna"))
            assertFalse(body.getBoolean("store"))
            assertFalse(body.getBoolean("stream"))
            assertFalse(body.has("tools"))
            assertFalse(body.has("previous_response_id"))
            assertEquals("disabled", body.getString("truncation"))
            assertEquals(3, body.getJSONArray("input").length())
            assertEquals(
                "output_text",
                body
                    .getJSONArray("input")
                    .getJSONObject(1)
                    .getJSONArray("content")
                    .getJSONObject(0)
                    .getString("type"),
            )
        }

    @Test fun invalidOrDeniedAutoResponsesNeverChooseAnUnrelatedModel() =
        runBlocking {
            val catalog =
                listOf(session.model, CopilotModel("disabled", CopilotChatApi.CHAT_COMPLETIONS, enabled = false))
            val invalid =
                listOf(
                    JSONObject(autoResponse(JSONObject().put("id", "current-default"))).put("session_token", ""),
                    JSONObject(
                        autoResponse(JSONObject().put("id", "current-default")),
                    ).put("session_token", "bad\nheader"),
                    JSONObject(autoResponse(JSONObject().put("id", "current-default"))).put("expires_at", 1100),
                    JSONObject(autoResponse(JSONObject().put("id", "current-default"))).put("expires_at", 100000),
                    JSONObject(autoResponse(JSONObject().put("id", "unknown"))),
                    JSONObject(autoResponse(JSONObject().put("id", "bad id").put("capabilities", JSONObject()))),
                )
            for (body in invalid) {
                enqueue(body.toString())
                val before = server.requestCount
                assertProblem(ConversationProblem.PROVIDER) { api.auto(access, "hello", catalog) }
                assertEquals(before + 1, server.requestCount)
            }
            enqueue(autoResponse(JSONObject().put("id", "disabled")))
            assertProblem(ConversationProblem.ACCESS) { api.auto(access, "hello", catalog) }
            for (status in listOf(403, 404, 429)) {
                server.enqueue(MockResponse().setResponseCode(status).setBody("private-provider-error"))
                val before = server.requestCount
                val problem =
                    when (status) {
                        403 -> ConversationProblem.ACCESS
                        429 -> ConversationProblem.USAGE
                        else -> ConversationProblem.AUTO_UNAVAILABLE
                    }
                assertProblem(problem) { api.auto(access, "hello", catalog) }
                assertEquals(before + 1, server.requestCount)
            }
        }

    @Test fun responsesRejectToolsFailuresAndEmptyOrOversizedText() =
        runBlocking {
            val routed =
                CopilotAutoSession(CopilotModel("responses", CopilotChatApi.RESPONSES), "auto-secret", 4_600_000)
            val message =
                JSONObject(
                    """{"type":"message","role":"assistant","content":[{"type":"output_text","text":"answer"}]}""",
                )
            for (body in listOf(
                """{"status":"failed","output":[]}""",
                """{"status":"completed","output":[{"type":"function_call"}]}""",
                """{"status":"completed","output":[]}""",
                JSONObject()
                    .put(
                        "status",
                        "incomplete",
                    ).put(
                        "incomplete_details",
                        JSONObject().put("reason", "content_filter"),
                    ).put("output", org.json.JSONArray().put(message))
                    .toString(),
                JSONObject()
                    .put(
                        "status",
                        "completed",
                    ).put(
                        "output",
                        org.json.JSONArray().put(
                            JSONObject(
                                message.toString(),
                            ).put(
                                "content",
                                org.json.JSONArray().put(
                                    JSONObject().put("type", "output_text").put("text", "x".repeat(12001)),
                                ),
                            ),
                        ),
                    ).toString(),
            )) {
                enqueue(body)
                assertProblem(ConversationProblem.PROVIDER) {
                    api.complete(access, routed, "friend:mobi", listOf(ConversationTurn("hello", true)))
                }
            }
            enqueue(
                JSONObject()
                    .put(
                        "status",
                        "incomplete",
                    ).put(
                        "incomplete_details",
                        JSONObject().put("reason", "max_output_tokens"),
                    ).put("output", org.json.JSONArray().put(message))
                    .toString(),
            )
            assertEquals("answer", api.complete(access, routed, "friend:mobi", listOf(ConversationTurn("hello", true))))
        }

    private fun autoResponse(model: JSONObject): String =
        JSONObject()
            .put("session_token", "auto-secret")
            .put("expires_at", 4600)
            .put("selected_model", model)
            .toString()

    private fun enqueue(body: String) {
        server.enqueue(MockResponse().setBody(body))
    }

    private suspend fun assertProblem(
        expected: ConversationProblem,
        block: suspend () -> Unit,
    ) {
        try {
            block()
            throw AssertionError("Expected $expected")
        } catch (error: ConversationException) {
            assertEquals(expected, error.problem)
            assertEquals(expected.name, error.message)
        }
    }
}
