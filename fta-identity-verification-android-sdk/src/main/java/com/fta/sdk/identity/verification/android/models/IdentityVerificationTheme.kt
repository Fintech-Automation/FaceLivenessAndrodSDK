package com.fta.sdk.identity.verification.android.models

import org.json.JSONObject

data class IdentityVerificationTheme(
    val colors: IdentityVerificationThemeColors? = null,
    val shape: IdentityVerificationThemeShape? = null,
    val typography: IdentityVerificationThemeTypography? = null
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("colors", colors?.toJson())
            put("shape", shape?.toJson())
            put("typography", typography?.toJson())
        }
    }
}

data class IdentityVerificationThemeColors(
    val primary: String? = null,
    val secondary: String? = null,
    val heading: String? = null,
    val brandTint: String? = null,
    val body: String? = null,
    val muted: String? = null,
    val line: String? = null,
    val bg: String? = null,
    val card: String? = null,
    val primaryText: String? = null
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("primary", primary)
            put("secondary", secondary)
            put("heading", heading)
            put("brandTint", brandTint)
            put("body", body)
            put("muted", muted)
            put("line", line)
            put("bg", bg)
            put("card", card)
            put("primaryText", primaryText)
        }
    }
}

data class IdentityVerificationThemeShape(
    val radius: Number? = null
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("radius", radius)
        }
    }
}

data class IdentityVerificationThemeTypography(
    val fontFamily: String? = null
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("fontFamily", fontFamily)
        }
    }
}