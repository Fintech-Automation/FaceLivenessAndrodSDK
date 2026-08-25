package com.fta.sdk.face.liveness.android.views

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.http.SslError
import android.util.AttributeSet
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.fta.sdk.face.liveness.android.FaceLivenessListener
import com.fta.sdk.face.liveness.android.bridge.NativeWebViewBridge
import com.fta.sdk.face.liveness.android.models.*
import org.json.JSONObject

/**
 * WebView based implementation of the same HTML liveness experience used by Flutter.
 *
 * The Flutter SDK passes one verificationToken to the HTML. The HTML decodes the
 * token to resolve environment/backend/tenant and performs the create-session and
 * result calls itself. Android therefore deliberately does not duplicate those APIs.
 */
class FaceLivenessView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var verificationToken: String? = null
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
                pendingPermissionRequest?.let { request ->
                    if (isGranted) {
                        request.grant(request.resources)
                        Log.i(TAG, "Camera permission granted.")
                    } else {
                        request.deny()
                        Log.e(TAG, "Camera permission denied.")
                    }
                }
                pendingPermissionRequest = null
            }
        } else {
            Log.e(TAG, "Host context must be a ComponentActivity.")
        }
    }

    private fun findActivity(context: Context?): ComponentActivity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is ComponentActivity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    private fun initWebViewConfig() {
        settings.apply {
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            useWideViewPort = true
            loadWithOverviewMode = true
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }

        addJavascriptInterface(
            NativeWebViewBridge(object : FaceLivenessListener {
                override fun onSuccess(result: LivenessResultModel?) = listener?.onSuccess(result) ?: Unit
                override fun onFail(result: LivenessResultModel?) = listener?.onFail(result) ?: Unit
                override fun onError(error: LivenessErrorModel?) = listener?.onError(error) ?: Unit
                override fun onCancel() = listener?.onCancel() ?: Unit
                override fun onAnalysisComplete() = listener?.onAnalysisComplete() ?: Unit
                override fun onScreenChange(screenType: LivenessScreenType?) =
                    listener?.onScreenChange(screenType) ?: Unit
                override fun onContinue() = listener?.onContinue() ?: Unit
                override fun onSessionStatusChange(status: LivenessSessionStatus?) =
                    listener?.onSessionStatusChange(status) ?: Unit
            }),
            "flutter_inappwebview"
        )

        webChromeClient = LivenessWebChromeClient()
        webViewClient = LivenessWebViewClient()
    }

    /**
     * Loads the exact Flutter HTML asset, but prefixes the native data before the
     * first HTML script executes. This avoids the race inherent in evaluateJavascript
     * from onPageStarted/onPageFinished.
     */
    fun load() {
        val rawHtml = try {
            context.assets.open("html/face_liveness.html").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            listener?.onError(
                LivenessErrorModel(
                    stage = "config",
                    message = "Unable to load face_liveness.html: ${e.message}",
                    cause = e.message
                )
            )
            return
        }

        val data = buildInitialDataJson()
        val bootstrap = "<script>window.__INITIAL_NATIVE_DATA__=${JSONObject.quote(data)};</script>"
        val html = rawHtml.replaceFirst("<head>", "<head>$bootstrap")

        loadDataWithBaseURL(
            "file:///android_asset/html/",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun buildInitialDataJson(): String {
        return JSONObject().apply {
            put("verificationToken", verificationToken)
            put("brand", brand?.toJson() ?: JSONObject.NULL)
            put("flow", flow?.toJson() ?: JSONObject.NULL)
            put("theme", theme?.toJson() ?: JSONObject.NULL)
            put("localization", localization?.toJson() ?: JSONObject.NULL)
            if (captureText != null) {
                put("captureText", JSONObject(captureText as Map<*, *>))
            } else {
                put("captureText", JSONObject.NULL)
            }
        }.toString()
    }

    inner class LivenessWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d(TAG, "Loading liveness HTML: $url")
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler,
            error: SslError
        ) {
            // Mirrors Flutter's ServerTrustAuthResponseAction.PROCEED behavior.
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
                return
            }

            pendingPermissionRequest = request
            cameraPermissionLauncher?.launch(Manifest.permission.CAMERA)
                ?: run {
                    request.deny()
                    pendingPermissionRequest = null
                    Toast.makeText(
                        context,
                        "Camera permission is required for liveness detection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            Log.d(TAG, "[LIVENESS_WEB] ${consoleMessage?.message()}")
            return true
        }
    }

    fun destroyView() {
        stopLoading()
        pendingPermissionRequest?.deny()
        pendingPermissionRequest = null
        cameraPermissionLauncher = null
        removeJavascriptInterface("flutter_inappwebview")
        webViewClient = WebViewClient()
        webChromeClient = WebChromeClient()
        removeAllViews()
        destroy()
    }

    companion object {
        private const val TAG = "FaceLivenessView"
    }
}
