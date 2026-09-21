package com.monsters.mobimon.core.auth

@Suppress("UNUSED_PARAMETER")
internal object CopilotDiagnostics {
    fun http(
        stage: CopilotRequestStage,
        status: Int,
    ) = Unit

    fun rejection(reason: CopilotRejection) = Unit

    fun models(
        total: Int,
        compatible: Int,
    ) = Unit
}
