package com.monsters.mobimon.core.auth

import com.monsters.mobimon.core.domain.ConversationProblem
import com.monsters.mobimon.core.domain.ConversationResult
import com.monsters.mobimon.core.domain.ConversationTurn
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Test

class CopilotConversationProviderTest {
    private var now = 1_000L
    private var allowed = true
    private var current = true
    private var revision = 1L
    private var accessLifetime = 120_000L
    private val api = FakeApi()
    private val provider =
        CopilotConversationProvider(
            { ConversationCredential(it, revision, "github-token") },
            { current },
            { allowed },
            api,
            { now },
        )

    @Test fun readinessRequiresExchangeAndModelAndRefreshesBeforeExpiryOrAccountRevisionChange() =
        runTest {
            assertEquals(ConversationResult.Success("auto"), provider.connect(1))
            assertEquals(
                ConversationResult.Success("answer"),
                provider.reply(1, "conversation", "friend:mobi", listOf(ConversationTurn("hello", true))),
            )
            assertEquals(1, api.exchanges)
            now = 100_000
            provider.connect(1)
            assertEquals(2, api.exchanges)
            revision++
            provider.connect(1)
            assertEquals(3, api.exchanges)
            provider.connect(2)
            assertEquals(4, api.exchanges)
        }

    @Test fun parkingAndAccountAreRecheckedBetweenNetworkCallsAndBeforeReturningReply() =
        runTest {
            api.afterExchange = { allowed = false }
            assertEquals(ConversationResult.Failure(ConversationProblem.RESTRICTED), provider.connect(1))
            assertEquals(0, api.models)
            allowed = true
            api.afterExchange = {}
            api.afterComplete = { current = false }
            assertEquals(
                ConversationResult.Failure(ConversationProblem.ACCOUNT),
                provider.reply(1, "conversation", "friend:mobi", listOf(ConversationTurn("hello", true))),
            )
        }

    @Test fun invalidHistoryAndRestrictionsDoNotStartNetworkCalls() =
        runTest {
            for (messages in listOf(
                emptyList(),
                listOf(ConversationTurn("assistant-first", false)),
                listOf(ConversationTurn(" ", true)),
                listOf(ConversationTurn("x".repeat(4001), true)),
            )) {
                assertEquals(
                    ConversationResult.Failure(ConversationProblem.LIMIT),
                    provider.reply(1, "conversation", "friend:mobi", messages),
                )
            }
            allowed = false
            assertEquals(ConversationResult.Failure(ConversationProblem.RESTRICTED), provider.connect(1))
            assertEquals(0, api.exchanges)
        }

    @Test fun autoIsDeferredUntilSendAndSessionsStayWithinConversationCompanionAndAccount() =
        runTest {
            accessLifetime = 3_600_000
            val messages = listOf(ConversationTurn("same prompt", true))
            provider.connect(1)
            assertEquals(0, api.routes)
            provider.reply(1, "first", "friend:mobi", messages)
            provider.reply(1, "first", "friend:mobi", messages)
            assertEquals(1, api.routes)
            provider.reply(1, "new", "friend:mobi", messages)
            assertEquals(2, api.routes)
            provider.reply(1, "new", "friend:luna", messages)
            assertEquals(3, api.routes)
            provider.reply(2, "new", "friend:luna", messages)
            assertEquals(4, api.routes)
            now += 310_000
            provider.reply(2, "new", "friend:luna", messages)
            assertEquals(5, api.routes)
            assertEquals(2, api.exchanges)
        }

    @Test fun failedAutoNeverFallsBackToManualSelectionAndGuardsBlockCompletionAfterRouting() =
        runTest {
            val messages = listOf(ConversationTurn("hello", true))
            api.autoProblem = ConversationProblem.ACCESS
            assertEquals(
                ConversationResult.Failure(ConversationProblem.ACCESS),
                provider.reply(1, "first", "friend:mobi", messages),
            )
            assertEquals(0, api.completions)
            api.autoProblem = null
            api.afterAuto = { allowed = false }
            assertEquals(
                ConversationResult.Failure(ConversationProblem.RESTRICTED),
                provider.reply(1, "first", "friend:mobi", messages),
            )
            assertEquals(0, api.completions)
            allowed = true
            api.afterAuto = { current = false }
            assertEquals(
                ConversationResult.Failure(ConversationProblem.ACCOUNT),
                provider.reply(1, "first", "friend:mobi", messages),
            )
            assertEquals(0, api.completions)
        }

    private inner class FakeApi : CopilotApi {
        var exchanges = 0
        var models = 0
        var routes = 0
        var completions = 0
        var afterAuto: () -> Unit = {}
        var autoProblem: ConversationProblem? = null
        var afterExchange: () -> Unit = {}
        var afterComplete: () -> Unit = {}

        override suspend fun authorize(githubToken: String): CopilotAccess {
            exchanges++
            afterExchange()
            return CopilotAccess("short-lived", now + accessLifetime, "https://api.githubcopilot.com/".toHttpUrl())
        }

        override suspend fun models(access: CopilotAccess): List<CopilotModel> {
            models++
            return listOf(CopilotModel("model", CopilotChatApi.CHAT_COMPLETIONS))
        }

        override suspend fun auto(
            access: CopilotAccess,
            prompt: String,
            models: List<CopilotModel>,
        ): CopilotAutoSession {
            routes++
            afterAuto()
            autoProblem?.let { throw ConversationException(it) }
            return CopilotAutoSession(models.first(), "auto-token", now + 600_000)
        }

        override suspend fun complete(
            access: CopilotAccess,
            session: CopilotAutoSession,
            friendId: String,
            messages: List<ConversationTurn>,
        ): String {
            completions++
            afterComplete()
            return "answer"
        }
    }
}
