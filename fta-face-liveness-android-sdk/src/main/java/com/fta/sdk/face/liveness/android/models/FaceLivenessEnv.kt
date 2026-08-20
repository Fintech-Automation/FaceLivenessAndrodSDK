package com.fta.sdk.face.liveness.android.models

import org.json.JSONObject

enum class FaceLivenessEnv {
    dev, staging, prod
}

enum class LivenessScreenType {
    intro, prepare, starting, processing, success, fail, cameraPermission;

    companion object {
        fun from(name: String?): LivenessScreenType? {
            return values().firstOrNull { it.name.equals(name, ignoreCase = true) }
        }
    }
}