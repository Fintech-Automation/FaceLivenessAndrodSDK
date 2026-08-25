package com.fta.sdk.face.liveness.android.bridge

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import com.fta.sdk.face.liveness.android.FaceLivenessListener
import com.fta.sdk.face.liveness.android.models.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Receives the same handler calls emitted by face_liveness.html for Flutter's
 * InAppWebView bridge.
 */
class NativeWebViewBridge(private val listener: FaceLivenessListener?) {

    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun callHandler(handlerName: String?, argsJson: String? = null): Any? {
        mainHandler.post {
            try {
                val firstArg = parseFirstArg(argsJson)

                when (handlerName) {
                    "onSuccess" -> {
                        val obj = firstArg?.optJSONObject("result") ?: firstArg
                        listener?.onSuccess(obj?.let { LivenessResultModel.fromJson(it) })
                    }

                    "onFail" -> {
                        val obj = firstArg?.optJSONObject("result") ?: firstArg
                        listener?.onFail(obj?.let { LivenessResultModel.fromJson(it) })
                    }

                    "onError" -> {
                        val obj = firstArg?.optJSONObject("error") ?: firstArg
                        listener?.onError(obj?.let { LivenessErrorModel.fromJson(it) })
                    }

                    "onCancel" -> listener?.onCancel()

                    "onAnalysisComplete" -> listener?.onAnalysisComplete()

                    "onScreenChange" -> {
                        listener?.onScreenChange(
                            LivenessScreenType.from(firstArg?.optString("screen", null))
                        )
                    }

                    "onContinue" -> listener?.onContinue()

                    "onSessionStatusChange" -> {
                        val obj = firstArg?.optJSONObject("result") ?: firstArg
                        listener?.onSessionStatusChange(
                            obj?.let { LivenessSessionStatus.fromJson(it) }
                        )
                    }

                    else -> Log.w(TAG, "Unhandled event: $handlerName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing JavaScript bridge event: $handlerName", e)
            }
        }
        return null
    }

    private fun parseFirstArg(argsJson: String?): JSONObject? {
        if (argsJson.isNullOrBlank() || argsJson == "undefined" || argsJson == "null") {
            return null
        }

        val trimmed = argsJson.trim()
        return when {
            trimmed.startsWith("[") -> JSONArray(trimmed).optJSONObject(0)
            trimmed.startsWith("{") -> JSONObject(trimmed)
            else -> null
        }
    }

    companion object {
        private const val TAG = "NativeWebViewBridge"
    }
}
