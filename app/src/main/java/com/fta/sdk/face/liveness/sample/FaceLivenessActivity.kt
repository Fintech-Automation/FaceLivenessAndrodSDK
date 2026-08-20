package com.fta.sdk.face.liveness.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fta.sdk.face.liveness.android.FaceLivenessListener
import com.fta.sdk.face.liveness.android.models.*
import com.fta.sdk.face.liveness.android.views.FaceLivenessView
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONObject

class FaceLivenessActivity : AppCompatActivity(), FaceLivenessListener {
    companion object {
        private const val REQUEST_CAMERA_CODE = 1001
    }
    private lateinit var livenessView: FaceLivenessView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_liveness)

        livenessView = findViewById(R.id.faceLivenessView)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Face Liveness"

        toolbar.setNavigationOnClickListener {
            onCancel()
        }

        val launchToken = intent.getStringExtra("launchToken") ?: ""
        val backendUrl = intent.getStringExtra("backendUrl")
        val tenant = intent.getStringExtra("tenant")
        val brandName = intent.getStringExtra("brandName")
        val brandLogoUrl = intent.getStringExtra("brandLogoUrl")
        val brandSecureLabel = intent.getStringExtra("brandSecureLabel")
        val skipIntro = intent.getBooleanExtra("skipIntro", false)
        val skipPrepare = intent.getBooleanExtra("skipPrepare", false)

        val primaryColor = intent.getStringExtra("primaryColor")
        val secondaryColor = intent.getStringExtra("secondaryColor")
        val headingColor = intent.getStringExtra("headingColor")

        val localizationStr = intent.getStringExtra("localization")
        val captureTextStr = intent.getStringExtra("captureText")

        livenessView.launchToken = launchToken
        livenessView.backendUrl = backendUrl
        livenessView.tenant = tenant

        val effectiveLogoUrl = if (!brandLogoUrl.isNullOrEmpty()) brandLogoUrl else null

        livenessView.brand = LivenessBrand(
            name = brandName,
            logoUrl = effectiveLogoUrl,
            secureLabel = brandSecureLabel
        )

        livenessView.flow = LivenessFlow(
            skipIntro = skipIntro,
            skipPrepare = skipPrepare
        )

        livenessView.theme = LivenessTheme(
            colors = LivenessThemeColors(
                primary = primaryColor,
                secondary = secondaryColor,
                heading = headingColor
            ),
            shape = LivenessThemeShape(radius = 100),
            typography = LivenessThemeTypography(fontFamily = "Inter, system-ui, sans-serif")
        )

        if (!localizationStr.isNullOrEmpty()) {
            livenessView.localization = LivenessLocalization.fromJson(JSONObject(localizationStr))
        }

        if (!captureTextStr.isNullOrEmpty()) {
            val map = mutableMapOf<String, String>()
            val jsonObj = JSONObject(captureTextStr)
            jsonObj.keys().forEach { key -> map[key] = jsonObj.getString(key) }
            livenessView.captureText = map
        }

        livenessView.listener = this
        livenessView.load()
    }


    override fun onSuccess(result: LivenessResultModel?) {
        Log.d("LIVENESS", "Liveness check succeeded: ${result?.toJson()}")
    }

    override fun onFail(result: LivenessResultModel?) {
        Log.d("LIVENESS", "Liveness check failed: ${result?.toJson()}")
    }

    override fun onError(error: LivenessErrorModel?) {
        Log.d("LIVENESS", "Liveness check error: ${error?.toJson()}")
    }

    override fun onCancel() {
        Log.d("LIVENESS", "Liveness check canceled")
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onContinue() {
        Log.d("LIVENESS", "User clicked Continue, finishing Activity")
        setResult(RESULT_OK)
        finish()
    }

    override fun onAnalysisComplete() {
        Log.d("LIVENESS", "Liveness check Analysis Complete")
    }

    override fun onScreenChange(screenType: LivenessScreenType?) {
        Log.d("LIVENESS", "to Liveness Screen: $screenType")
    }

    override fun onDestroy() {
        super.onDestroy()
        livenessView.destroyView()
    }
}