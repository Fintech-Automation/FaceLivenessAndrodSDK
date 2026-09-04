package com.fta.sdk.identity.verification.sample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import org.json.JSONObject

class InputParameterActivity : AppCompatActivity() {
    private val defaultLogoUrl = "https://accloud-public-storage-dev1.s3.us-east-2.amazonaws.com/REx0xk8bC8_tenants/GBX/Fintech_6_pwo4ga.png"
    private val defaultLocalization = """{
    "intro": {"eyebrow":"Identity check","title":"Let's confirm it's really you","body":"A quick face scan helps protect your account.","cta":"Start face scan","trustLabel":"Bank-grade liveness detection"},
    "prepare": {"eyebrow":"Before we start","title":"Three things for a clean scan","tips":[{"title":"Find good light","body":"Avoid strong backlight."},{"title":"Clear your face","body":"Remove sunglasses or masks."},{"title":"Hold steady","body":"Keep the device at eye level."}],"cta":"I'm ready","backLabel":"Back"},
    "starting": {"title":"Starting camera","body":"Creating a secure liveness session."},
    "processing": {"title":"Verifying your scan","body":"This usually takes just a moment."},
    "success": {"title":"You're verified","body":"Thanks. The liveness check was completed.","cta":"Thanks. The liveness check was completed."},
    "fail": {"title":"We couldn't complete the scan","body":"Move somewhere brighter and try again.","cta":"Move somewhere brighter and try again."},
    "cameraPermission": {"title":"Camera access is required","body":"Allow camera access, then try again."}
  }"""

    private var primaryHex: String? = null
    private var secondaryHex: String? = null
    private var headingHex: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_parameter)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Input Parameters"

        val vToken = findViewById<EditText>(R.id.etVerificationToken)
        val backend = findViewById<EditText>(R.id.etBackendUrl)
        val tenant = findViewById<EditText>(R.id.etTenant)
        val localization = findViewById<EditText>(R.id.etLocalization)
        val brandName = findViewById<EditText>(R.id.etBrandName)
        val logoUrl = findViewById<EditText>(R.id.etBrandLogoUrl)
        val secureLabel = findViewById<EditText>(R.id.etBrandSecureLabel)
        val skipIntro = findViewById<SwitchMaterial>(R.id.swSkipIntro)
        val skipPrepare = findViewById<SwitchMaterial>(R.id.swSkipPrepare)
        val continueButton = findViewById<MaterialButton>(R.id.btnContinue)
        val error = findViewById<TextView>(R.id.tvErrorLog)
        val primary = findViewById<LinearLayout>(R.id.btnPickPrimary)
        val secondary = findViewById<LinearLayout>(R.id.btnPickSecondary)
        val heading = findViewById<LinearLayout>(R.id.btnPickHeading)

        backend.setText("https://api-dev.accelerationcloud.info")
        tenant.setText("unifi")
        logoUrl.setText(defaultLogoUrl)
        localization.setText(defaultLocalization)

        continueButton.isEnabled = vToken.text.isNotBlank()
        vToken.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                continueButton.isEnabled = !s.isNullOrBlank()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        primary.setOnClickListener { chooseColor("Primary") { primaryHex = it; paint(primary, it) } }
        secondary.setOnClickListener { chooseColor("Secondary Color") { secondaryHex = it; paint(secondary, it) } }
        heading.setOnClickListener { chooseColor("Heading Color") { headingHex = it; paint(heading, it) } }

        continueButton.setOnClickListener {
            error.visibility = View.GONE
            val json = localization.text.toString().trim()
            try {
                JSONObject(json)
            } catch (e: Exception) {
                error.text = "Error: ${e.message}"
                error.visibility = View.VISIBLE
                return@setOnClickListener
            }
            startActivity(Intent(this, IdentityVerificationActivity::class.java).apply {
                putExtra("verificationToken", vToken.text.toString().trim())
                putExtra("brandName", brandName.text.toString().trim().ifEmpty { null })
                putExtra("brandLogoUrl", logoUrl.text.toString().trim().ifEmpty { null })
                putExtra("brandSecureLabel", secureLabel.text.toString().trim().ifEmpty { null })
                putExtra("skipIntro", skipIntro.isChecked)
                putExtra("skipPrepare", skipPrepare.isChecked)
                putExtra("primaryColor", primaryHex)
                putExtra("secondaryColor", secondaryHex)
                putExtra("headingColor", headingHex)
                putExtra("localization", json)
            })
        }
    }

    private fun paint(view: View, hex: String) {
        view.setBackgroundColor(Color.parseColor(hex))
    }

    private fun chooseColor(title: String, onSelected: (String) -> Unit) {
        val colors = listOf("#FFFFFF", "#FF9800", "#FFEB3B", "#4CAF50", "#00BCD4", "#2196F3", "#3F51B5", "#9C27B0", "#E91E63", "#000000", "#FFFFFF")
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 8)
        }
        var selected = colors[0]
        val hex = EditText(this).apply { hint = "Hex color, e.g. #1D4ED8"; setText(selected) }
        container.addView(hex)
        val grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
        colors.chunked(6).forEach { rowColors ->
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER }
            rowColors.forEach { value ->
                val swatch = View(this).apply {
                    setBackgroundColor(Color.parseColor(value))
                    setOnClickListener { selected = value; hex.setText(value) }
                }
                row.addView(swatch, LinearLayout.LayoutParams(42, 42).apply { setMargins(5, 5, 5, 5) })
            }
            grid.addView(row)
        }
        container.addView(grid)
        AlertDialog.Builder(this).setTitle("Pick a color!").setView(container)
            .setPositiveButton("Got it") { _, _ ->
                val value = hex.text.toString().trim()
                if (Regex("^#[0-9A-Fa-f]{6}$").matches(value)) onSelected(value)
                else onSelected(selected)
            }.show()
    }
}
