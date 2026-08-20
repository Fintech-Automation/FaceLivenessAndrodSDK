package com.fta.sdk.face.liveness.android.models

import org.json.JSONObject

data class LivenessFlow(
    val skipIntro: Boolean = false,
    val skipPrepare: Boolean = false
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("skipIntro", skipIntro)
            put("skipPrepare", skipPrepare)
        }
    }
}