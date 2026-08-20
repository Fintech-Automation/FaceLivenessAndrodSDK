package com.fta.sdk.face.liveness.android.bridge

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import com.fta.sdk.face.liveness.android.FaceLivenessListener
import com.fta.sdk.face.liveness.android.models.*
import org.json.JSONArray
import org.json.JSONObject

class FlutterInAppWebViewBridge(private val listener: FaceLivenessListener?) {

    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun callHandler(handlerName: String?, argsJson: String? = null): Any? {
        Log.d("LIVENESS_BRIDGE", "Bridge event: $handlerName, args: $argsJson")

        mainHandler.post {
            try {
                var firstArg: JSONObject? = null
                if (!argsJson.isNullOrEmpty() && argsJson != "undefined") {
                    val trimmed = argsJson.trim()
                    if (trimmed.startsWith("[")) {
                        val jsonArray = JSONArray(trimmed)
                        if (jsonArray.length() > 0) {
                            firstArg = jsonArray.optJSONObject(0)
                        }
                    } else if (trimmed.startsWith("{")) {
                        firstArg = JSONObject(trimmed)
                    }
                }

                when (handlerName) {
                    "onSuccess" -> {
                        val resultObj = firstArg?.optJSONObject("result") ?: firstArg
                        val model = resultObj?.let { LivenessResultModel.fromJson(it) }
                        listener?.onSuccess(model)
                    }
                    "onFail" -> {
                        val resultObj = firstArg?.optJSONObject("result") ?: firstArg
                        val model = resultObj?.let { LivenessResultModel.fromJson(it) }

                        Log.e("LIVENESS_BRIDGE", "onFail model: $model, raw JSON: $firstArg")
                        listener?.onFail(model)
                    }
                    "onError" -> {
                        val errorObj = firstArg?.optJSONObject("error") ?: firstArg
                        val model = errorObj?.let { LivenessErrorModel.fromJson(it) }
                        listener?.onError(model)
                    }
                    "onCancel" -> listener?.onCancel()
                    "onAnalysisComplete" -> listener?.onAnalysisComplete()
                    "onScreenChange" -> {
                        val screenName = firstArg?.optString("screen")
                        listener?.onScreenChange(LivenessScreenType.from(screenName))
                    }
                    "onContinue" -> listener?.onContinue()
                    else -> Log.w("LIVENESS_BRIDGE", "Unhandled event: $handlerName")
                }
            } catch (e: Exception) {
                Log.e("LIVENESS_BRIDGE", "Error parsing Javascript bridge message: $handlerName", e)
            }
        }
        return null
    }
}