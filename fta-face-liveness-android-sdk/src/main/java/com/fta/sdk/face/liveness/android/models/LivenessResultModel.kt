package com.fta.sdk.face.liveness.android.models

import org.json.JSONObject

data class LivenessResultModel(
    val id: String? = null,
    val status: String? = null,
    val failReason: String? = null,
    val createdTime: String? = null,
    val completedTime: String? = null,
    val rawJson: JSONObject? = null
) {
    fun toJson(): JSONObject {
        return rawJson ?: JSONObject().apply {
            put("id", id)
            put("status", status)
            put("fail_reason", failReason)
            put("created_time", createdTime)
            put("completed_time", completedTime)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): LivenessResultModel {
            return LivenessResultModel(
                id = json.optNullableString("id"),
                status = json.optNullableString("status"),
                failReason = json.optNullableString("fail_reason"),
                createdTime = json.optNullableString("created_time"),
                completedTime = json.optNullableString("completed_time"),
                rawJson = json
            )
        }

        private fun JSONObject.optNullableString(key: String): String? {
            return if (has(key) && !isNull(key)) optString(key) else null
        }
    }
}