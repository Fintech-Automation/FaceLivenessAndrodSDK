package com.fta.sdk.face.liveness.android.models

import org.json.JSONObject

/**
 * Error contract mirrors Flutter's LivenessErrorModel:
 * stage | message | cause.
 */
data class LivenessErrorModel(
    val stage: String? = null,
    val message: String? = null,
    val cause: Any? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("stage", stage)
        put("message", message)
        put("cause", cause)
    }

    companion object {
        fun fromJson(json: JSONObject): LivenessErrorModel =
            LivenessErrorModel(
                stage = json.optString("stage", null),
                message = json.optString("message", null),
                cause = if (json.has("cause") && !json.isNull("cause")) json.opt("cause") else null
            )
    }
}
