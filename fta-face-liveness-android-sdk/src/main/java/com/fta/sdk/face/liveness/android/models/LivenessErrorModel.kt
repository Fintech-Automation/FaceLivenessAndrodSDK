package com.fta.sdk.face.liveness.android.models

import org.json.JSONObject

data class LivenessErrorModel(
    val code: String? = null,
    val message: String? = null,
    val rawJson: JSONObject? = null
) {
    fun toJson(): JSONObject = rawJson ?: JSONObject().apply {
        put("code", code)
        put("message", message)
    }

    companion object {
        fun fromJson(json: JSONObject): LivenessErrorModel {
            return LivenessErrorModel(
                code = json.optString("code"),
                message = json.optString("message"),
                rawJson = json
            )
        }
    }
}