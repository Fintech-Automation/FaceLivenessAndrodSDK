package com.fta.sdk.identity.verification.android.models

import org.json.JSONObject

data class IdentityVerificationBrand(
    val name: String? = null,
    val logoUrl: String? = null,
    val secureLabel: String? = null
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("logoUrl", logoUrl)
            put("secureLabel", secureLabel)
        }
    }
}