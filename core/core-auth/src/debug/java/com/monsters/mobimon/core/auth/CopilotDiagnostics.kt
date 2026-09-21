package com.monsters.mobimon.core.auth

import android.util.Log

/** Fixed metadata only: never accept credentials, headers, prompts, or provider response bodies. */
internal object CopilotDiagnostics {
    fun http(
        stage: CopilotRequestStage,
        status: Int,
    ) {
        Log.i("MobiMonCopilot", "stage=$stage status=$status")
    }

    fun rejection(reason: CopilotRejection) {
        Log.i("MobiMonCopilot", "rejection=$reason")
    }

    fun models(
        total: Int,
        compatible: Int,
    ) {
        Log.i("MobiMonCopilot", "stage=MODELS total=$total compatible=$compatible")
    }
}
