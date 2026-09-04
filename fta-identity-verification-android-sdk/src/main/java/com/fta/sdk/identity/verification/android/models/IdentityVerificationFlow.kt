package com.fta.sdk.identity.verification.android.models

import org.json.JSONObject

data class IdentityVerificationFlow(
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