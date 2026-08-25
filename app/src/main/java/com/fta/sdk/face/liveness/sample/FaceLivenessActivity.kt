package com.fta.sdk.face.liveness.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.fta.sdk.face.liveness.android.FaceLivenessListener
import com.fta.sdk.face.liveness.android.models.*
import com.fta.sdk.face.liveness.android.views.FaceLivenessView
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONObject

class FaceLivenessActivity : AppCompatActivity(), FaceLivenessListener {

    private lateinit var livenessView: FaceLivenessView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_liveness)

        livenessView = findViewById(R.id.faceLivenessView)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Face Liveness"
        toolbar.setNavigationIcon(com.fta.sdk.face.liveness.sample.R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { onCancel() }

        livenessView.verificationToken = intent.getStringExtra("verificationToken") ?: ""

        livenessView.brand = LivenessBrand(
            name = intent.getStringExtra("brandName"),
            logoUrl = intent.getStringExtra("brandLogoUrl")?.takeIf { it.isNotEmpty() },
            secureLabel = intent.getStringExtra("brandSecureLabel")
        )

        livenessView.flow = LivenessFlow(
            skipIntro = intent.getBooleanExtra("skipIntro", false),
            skipPrepare = intent.getBooleanExtra("skipPrepare", false)
        )

        livenessView.theme = LivenessTheme(
            colors = LivenessThemeColors(
                primary = intent.getStringExtra("primaryColor"),
                secondary = intent.getStringExtra("secondaryColor"),
                heading = intent.getStringExtra("headingColor")
            ),
            shape = LivenessThemeShape(radius = 100),
            typography = LivenessThemeTypography(fontFamily = "Inter, system-ui, sans-serif")
        )

        intent.getStringExtra("localization")?.takeIf { it.isNotBlank() }?.let {
            livenessView.localization = LivenessLocalization.fromJson(JSONObject(it))
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
        Log.d("LIVENESS", "Liveness check Continue")
        setResult(RESULT_OK)
        finish()
    }

    override fun onAnalysisComplete() {
        Log.d("LIVENESS", "Liveness check Analysis Complete")
    }

    override fun onScreenChange(screenType: LivenessScreenType?) {
        Log.d("LIVENESS", "to Liveness Screen: $screenType")
    }

    override fun onSessionStatusChange(status: LivenessSessionStatus?) {
        Log.d("LIVENESS", "Liveness check Session Status: ${status?.toJson()}")
        val terminal = status?.status == SessionStatus.RETRY_LIMIT_EXCEEDED ||
            status?.status == SessionStatus.EXPIRED ||
            status?.status == SessionStatus.INVALID

        if (terminal) {
            window.decorView.postDelayed({
                if (!isFinishing) {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }, 2500L)
        }
    }

    override fun onDestroy() {
        livenessView.destroyView()
        super.onDestroy()
    }
}
