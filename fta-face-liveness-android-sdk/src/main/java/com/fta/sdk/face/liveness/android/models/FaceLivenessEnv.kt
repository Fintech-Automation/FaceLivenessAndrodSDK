package com.fta.sdk.face.liveness.android.models

/**
 * Environments understood by the verification token.
 */
enum class FaceLivenessEnv {
    dev, staging, uat, prod
}

enum class LivenessScreenType {
    intro,
    prepare,
    capture,
    processing,
    success,
    fail,
    error;

    companion object {
        fun from(name: String?): LivenessScreenType? =
            values().firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}

enum class SessionStatus(val displayName: String) {
    COMPLETED("COMPLETED"),
    EXPIRED("EXPIRED"),
    INVALID("INVALID"),
    READY("READY"),
    RETRY_LIMIT_EXCEEDED("RETRY_LIMIT_EXCEEDED");

    companion object {
        fun from(value: String?): SessionStatus? =
            values().firstOrNull { it.displayName == value }
    }
}

data class LivenessSessionStatus(
    val status: SessionStatus? = null,
    val stage: String? = null,
    val message: String? = null,
    val isEligible: Boolean? = null
) {
    fun toJson(): org.json.JSONObject = org.json.JSONObject().apply {
        put("status", status?.displayName)
        put("stage", stage)
        put("message", message)
        put("isEligible", isEligible)
    }

    companion object {
        fun fromJson(json: org.json.JSONObject): LivenessSessionStatus =
            LivenessSessionStatus(
                status = SessionStatus.from(json.optString("status", null)),
                stage = json.optString("stage", null),
                message = json.optString("message", null),
                isEligible = if (json.has("isEligible") && !json.isNull("isEligible")) {
                    json.optBoolean("isEligible")
                } else null
            )
    }
}
