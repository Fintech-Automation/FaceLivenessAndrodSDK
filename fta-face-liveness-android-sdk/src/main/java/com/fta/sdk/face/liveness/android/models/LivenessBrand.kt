package com.fta.sdk.face.liveness.android.models

import org.json.JSONObject

data class LivenessBrand(
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