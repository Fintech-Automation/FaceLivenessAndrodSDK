package com.fta.sdk.face.liveness.android.views

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.http.SslError
import android.util.AttributeSet
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.fta.sdk.face.liveness.android.FaceLivenessListener
import com.fta.sdk.face.liveness.android.bridge.FlutterInAppWebViewBridge
import com.fta.sdk.face.liveness.android.models.*
import org.json.JSONObject

class FaceLivenessView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var launchToken: String? = null
    var backendUrl: String? = null
    var tenant: String? = null

    var brand: LivenessBrand? = null
    var flow: LivenessFlow? = null
    var theme: LivenessTheme? = null
    var localization: LivenessLocalization? = null
    var captureText: Map<String, String>? = null

    var listener: FaceLivenessListener? = null

    private var pendingPermissionRequest: PermissionRequest? = null

    private var cameraPermissionLauncher: ActivityResultLauncher<String>? = null

    init {
        initWebViewConfig()
        setupPermissionLauncher()
    }

    private fun setupPermissionLauncher() {
        val activity = findActivity(context)
        if (activity is ComponentActivity) {
            cameraPermissionLauncher = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    pendingPermissionRequest?.let { req ->
                        req.grant(req.resources)
                        Log.i("LIVENESS", "Camera permission granted. Permission applied to H5.")
                    }
                } else {
                    pendingPermissionRequest?.deny()
                    Toast.makeText(
                        context,
                        "Camera permission is required for liveness detection.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("LIVENESS", "User denied system CAMERA permission.")
                }
                pendingPermissionRequest = null
            }
        } else {
            Log.e(
                "LIVENESS",
                "Host context is not a ComponentActivity. Failed to register permission launcher."
            )
        }
    }

    private fun findActivity(context: Context?): ComponentActivity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is ComponentActivity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

    private fun initWebViewConfig() {
        val settings = settings
        settings.javaScriptEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE

        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        addJavascriptInterface(
            FlutterInAppWebViewBridge(object : FaceLivenessListener {
                override fun onSuccess(result: LivenessResultModel?) {
                    listener?.onSuccess(result)
                }

                override fun onFail(result: LivenessResultModel?) {
                    listener?.onFail(result)
                }

                override fun onError(error: LivenessErrorModel?) {
                    listener?.onError(error)
                }

                override fun onCancel() {
                    listener?.onCancel()
                }

                override fun onAnalysisComplete() {
                    listener?.onAnalysisComplete()
                }

                override fun onScreenChange(screenType: LivenessScreenType?) {
                    listener?.onScreenChange(screenType)
                }

                override fun onContinue() {
                    listener?.onContinue()
                }
            }),
            "flutter_inappwebview"
        )

        webChromeClient = LivenessWebChromeClient()
        webViewClient = LivenessWebViewClient()
    }

    fun load() {
        loadUrl("file:///android_asset/html/face_liveness.html")
    }

    private fun buildInitialDataJson(): String {
        val jsonObject = JSONObject()
        jsonObject.put("launchToken", launchToken)
        jsonObject.put("backendUrl", backendUrl)
        jsonObject.put("tenant", tenant)

        jsonObject.put("brand", brand?.toJson() ?: JSONObject.NULL)
        jsonObject.put("flow", flow?.toJson() ?: JSONObject.NULL)
        jsonObject.put("theme", theme?.toJson() ?: JSONObject.NULL)
        jsonObject.put("localization", localization?.toJson() ?: JSONObject.NULL)

        if (captureText != null) {
            jsonObject.put("captureText", JSONObject(captureText as Map<*, *>))
        } else {
            jsonObject.put("captureText", JSONObject.NULL)
        }

        return jsonObject.toString()
    }

    private fun injectInitData() {
        val rawJson = buildInitialDataJson()
        val safeJsonString = JSONObject.quote(rawJson)

        val script = """
        (function() {
            try {
                window.__INITIAL_NATIVE_DATA__ = $safeJsonString;
                console.log("Injected __INITIAL_NATIVE_DATA__ as valid JSON string.");
            } catch (e) {
                console.error("Failed to set __INITIAL_NATIVE_DATA__", e);
            }
        })();
        """.trimIndent()

        evaluateJavascript(script) { res ->
            Log.i("LIVENESS_WEB", "[Injection Execution Result]: $res")
        }
    }

    inner class LivenessWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            injectInitData()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            injectInitData()
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError) {
            handler.proceed()
        }
    }

    inner class LivenessWebChromeClient : WebChromeClient() {

        override fun onPermissionRequest(request: PermissionRequest?) {
            request ?: return

            val hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasCameraPermission) {
                request.grant(request.resources)
            } else {
                pendingPermissionRequest = request
                if (cameraPermissionLauncher != null) {
                    cameraPermissionLauncher?.launch(Manifest.permission.CAMERA)
                } else {
                    request.deny()
                    Log.e("LIVENESS", "Failed to request dynamic permission. cameraPermissionLauncher was not properly initialized.")
                }
            }
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            val msg = consoleMessage?.message() ?: ""
            Log.i("LIVENESS_CONSOLE", "【H5】$msg")
            return super.onConsoleMessage(consoleMessage)
        }
    }

    fun destroyView() {
        stopLoading()
        pendingPermissionRequest = null
        cameraPermissionLauncher = null
        webViewClient = WebViewClient()
        webChromeClient = WebChromeClient()
        removeAllViews()
        destroy()
    }
}