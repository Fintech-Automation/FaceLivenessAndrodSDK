package com.fta.sdk.identity.verification.android.models

import org.json.JSONArray
import org.json.JSONObject

data class IdentityVerificationLocalization(
    val intro: IdentityVerificationLocalizationIntro? = null,
    val prepare: IdentityVerificationLocalizationPrepare? = null,
    val starting: IdentityVerificationLocalizationPageElements? = null,
    val processing: IdentityVerificationLocalizationPageElements? = null,
    val success: IdentityVerificationLocalizationResultElements? = null,
    val fail: IdentityVerificationLocalizationResultElements? = null,
    val cameraPermission: IdentityVerificationLocalizationPageElements? = null
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
        fun fromJson(json: JSONObject): IdentityVerificationLocalization {
            return IdentityVerificationLocalization(
                intro = json.optJSONObject("intro")?.let {
                    IdentityVerificationLocalizationIntro(
                        eyebrow = it.optString("eyebrow"),
                        title = it.optString("title"),
                        body = it.optString("body"),
                        cta = it.optString("cta"),
                        trustLabel = it.optString("trustLabel")
                    )
                },
                prepare = json.optJSONObject("prepare")?.let {
                    val tipsArr = it.optJSONArray("tips")
                    val tipsList = mutableListOf<IdentityVerificationLocalizationPageElements>()
                    if (tipsArr != null) {
                        for (i in 0 until tipsArr.length()) {
                            val tipObj = tipsArr.getJSONObject(i)
                            tipsList.add(
                                IdentityVerificationLocalizationPageElements(
                                    title = tipObj.optString("title"),
                                    body = tipObj.optString("body")
                                )
                            )
                        }
                    }
                    IdentityVerificationLocalizationPrepare(
                        eyebrow = it.optString("eyebrow"),
                        title = it.optString("title"),
                        tips = tipsList,
                        cta = it.optString("cta"),
                        backLabel = it.optString("backLabel")
                    )
                },
                starting = json.optJSONObject("starting")?.let {
                    IdentityVerificationLocalizationPageElements(it.optString("title"), it.optString("body"))
                },
                processing = json.optJSONObject("processing")?.let {
                    IdentityVerificationLocalizationPageElements(it.optString("title"), it.optString("body"))
                },
                success = json.optJSONObject("success")?.let {
                    IdentityVerificationLocalizationResultElements(it.optString("title"), it.optString("body"), it.optString("cta"))
                },
                fail = json.optJSONObject("fail")?.let {
                    IdentityVerificationLocalizationResultElements(it.optString("title"), it.optString("body"), it.optString("cta"))
                },
                cameraPermission = json.optJSONObject("cameraPermission")?.let {
                    IdentityVerificationLocalizationPageElements(it.optString("title"), it.optString("body"))
                }
            )
        }
    }
}

data class IdentityVerificationLocalizationIntro(
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

open class IdentityVerificationLocalizationPageElements(
    open val title: String? = null,
    open val body: String? = null
) {
    open fun toJson(): JSONObject = JSONObject().apply {
        put("title", title)
        put("body", body)
    }
}

data class IdentityVerificationLocalizationPrepare(
    val eyebrow: String? = null,
    val title: String? = null,
    val tips: List<IdentityVerificationLocalizationPageElements>? = null,
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

data class IdentityVerificationLocalizationResultElements(
    override val title: String? = null,
    override val body: String? = null,
    val cta: String? = null
) : IdentityVerificationLocalizationPageElements(title, body) {
    override fun toJson(): JSONObject = super.toJson().apply {
        put("cta", cta)
    }
}