package com.fta.sdk.identity.verification.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.fta.sdk.identity.verification.android.IdentityVerificationListener
import com.fta.sdk.identity.verification.android.models.*
import com.fta.sdk.identity.verification.android.views.IdentityVerificationView
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONObject

class IdentityVerificationActivity : AppCompatActivity(), IdentityVerificationListener {

    private lateinit var identityVerification: IdentityVerificationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_verification)

        identityVerification = findViewById(R.id.identityVerificationView)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Identity Verification"
        toolbar.setNavigationIcon(com.fta.sdk.identity.verification.sample.R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { onCancel() }

        identityVerification.verificationToken = intent.getStringExtra("verificationToken") ?: ""

        identityVerification.brand = IdentityVerificationBrand(
            name = intent.getStringExtra("brandName"),
            logoUrl = intent.getStringExtra("brandLogoUrl")?.takeIf { it.isNotEmpty() },
            secureLabel = intent.getStringExtra("brandSecureLabel")
        )

        identityVerification.flow = IdentityVerificationFlow(
            skipIntro = intent.getBooleanExtra("skipIntro", false),
            skipPrepare = intent.getBooleanExtra("skipPrepare", false)
        )

        identityVerification.theme = IdentityVerificationTheme(
            colors = IdentityVerificationThemeColors(
                primary = intent.getStringExtra("primaryColor"),
                secondary = intent.getStringExtra("secondaryColor"),
                heading = intent.getStringExtra("headingColor")
            ),
            shape = IdentityVerificationThemeShape(radius = 100),
            typography = IdentityVerificationThemeTypography(fontFamily = "Inter, system-ui, sans-serif")
        )

        intent.getStringExtra("localization")?.takeIf { it.isNotBlank() }?.let {
            identityVerification.localization = IdentityVerificationLocalization.fromJson(JSONObject(it))
        }

        identityVerification.listener = this
        identityVerification.load()
    }

    override fun onSuccess(result: IdentityVerificationResultModel?) {
        Log.d("Identity Verification", "Identity Verification check succeeded: ${result?.toJson()}")
    }

    override fun onFail(result: IdentityVerificationResultModel?) {
        Log.d("Identity Verification", "Identity Verification check failed: ${result?.toJson()}")
    }

    override fun onError(error: IdentityVerificationErrorModel?) {
        Log.d("Identity Verification", "Identity Verification check error: ${error?.toJson()}")
    }

    override fun onCancel() {
        Log.d("Identity Verification", "Identity Verification check canceled")
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onContinue() {
        Log.d("Identity Verification", "Identity Verification check Continue")
        setResult(RESULT_OK)
        finish()
    }

    override fun onAnalysisComplete() {
        Log.d("Identity Verification", "Identity Verification check Analysis Complete")
    }

    override fun onScreenChange(screenType: IdentityVerificationScreenType?) {
        Log.d("Identity Verification", "to Identity Verification Screen: $screenType")
    }

    override fun onSessionStatusChange(status: IdentityVerificationSessionStatus?) {
        Log.d("Identity Verification", "Identity Verification check Session Status: ${status?.toJson()}")
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
        identityVerification.destroyView()
        super.onDestroy()
    }
}
