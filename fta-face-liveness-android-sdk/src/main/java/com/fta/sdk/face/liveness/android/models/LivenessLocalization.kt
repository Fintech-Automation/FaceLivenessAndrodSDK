package com.fta.sdk.face.liveness.android.models

import org.json.JSONArray
import org.json.JSONObject

data class LivenessLocalization(
    val intro: LivenessLocalizationIntro? = null,
    val prepare: LivenessLocalizationPrepare? = null,
    val starting: LivenessLocalizationPageElements? = null,
    val processing: LivenessLocalizationPageElements? = null,
    val success: LivenessLocalizationResultElements? = null,
    val fail: LivenessLocalizationResultElements? = null,
    val cameraPermission: LivenessLocalizationPageElements? = null
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("intro", intro?.toJson())
            put("prepare", prepare?.toJson())
            put("starting", starting?.toJson())
            put("processing", processing?.toJson())
            put("success", success?.toJson())
            put("fail", fail?.toJson())
            put("cameraPermission", cameraPermission?.toJson())
        }
    }

    companion object {
        fun fromJson(json: JSONObject): LivenessLocalization {
            return LivenessLocalization(
                intro = json.optJSONObject("intro")?.let {
                    LivenessLocalizationIntro(
                        eyebrow = it.optString("eyebrow"),
                        title = it.optString("title"),
                        body = it.optString("body"),
                        cta = it.optString("cta"),
                        trustLabel = it.optString("trustLabel")
                    )
                },
                prepare = json.optJSONObject("prepare")?.let {
                    val tipsArr = it.optJSONArray("tips")
                    val tipsList = mutableListOf<LivenessLocalizationPageElements>()
                    if (tipsArr != null) {
                        for (i in 0 until tipsArr.length()) {
                            val tipObj = tipsArr.getJSONObject(i)
                            tipsList.add(
                                LivenessLocalizationPageElements(
                                    title = tipObj.optString("title"),
                                    body = tipObj.optString("body")
                                )
                            )
                        }
                    }
                    LivenessLocalizationPrepare(
                        eyebrow = it.optString("eyebrow"),
                        title = it.optString("title"),
                        tips = tipsList,
                        cta = it.optString("cta"),
                        backLabel = it.optString("backLabel")
                    )
                },
                starting = json.optJSONObject("starting")?.let {
                    LivenessLocalizationPageElements(it.optString("title"), it.optString("body"))
                },
                processing = json.optJSONObject("processing")?.let {
                    LivenessLocalizationPageElements(it.optString("title"), it.optString("body"))
                },
                success = json.optJSONObject("success")?.let {
                    LivenessLocalizationResultElements(it.optString("title"), it.optString("body"), it.optString("cta"))
                },
                fail = json.optJSONObject("fail")?.let {
                    LivenessLocalizationResultElements(it.optString("title"), it.optString("body"), it.optString("cta"))
                },
                cameraPermission = json.optJSONObject("cameraPermission")?.let {
                    LivenessLocalizationPageElements(it.optString("title"), it.optString("body"))
                }
            )
        }
    }
}

data class LivenessLocalizationIntro(
    val eyebrow: String? = null,
    val title: String? = null,
    val body: String? = null,
    val cta: String? = null,
    val trustLabel: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("eyebrow", eyebrow)
        put("title", title)
        put("body", body)
        put("cta", cta)
        put("trustLabel", trustLabel)
    }
}

open class LivenessLocalizationPageElements(
    open val title: String? = null,
    open val body: String? = null
) {
    open fun toJson(): JSONObject = JSONObject().apply {
        put("title", title)
        put("body", body)
    }
}

data class LivenessLocalizationPrepare(
    val eyebrow: String? = null,
    val title: String? = null,
    val tips: List<LivenessLocalizationPageElements>? = null,
    val cta: String? = null,
    val backLabel: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("eyebrow", eyebrow)
        put("title", title)
        tips?.let { list ->
            val arr = JSONArray()
            list.forEach { arr.put(it.toJson()) }
            put("tips", arr)
        }
        put("cta", cta)
        put("backLabel", backLabel)
    }
}

data class LivenessLocalizationResultElements(
    override val title: String? = null,
    override val body: String? = null,
    val cta: String? = null
) : LivenessLocalizationPageElements(title, body) {
    override fun toJson(): JSONObject = super.toJson().apply {
        put("cta", cta)
    }
}