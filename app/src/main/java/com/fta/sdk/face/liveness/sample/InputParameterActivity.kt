package com.fta.sdk.face.liveness.sample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class InputParameterActivity : AppCompatActivity() {

    private val defaultToken = "Bearer eyJraWQiOiJxRGZiUElwWE4ySDhVUlNmQUZ4OFZUUXg0TkVjdnlSU0s3anpON0NjVmtVIiwidHlwIjoiYXBwbGljYXRpb24vb2t0YS1pbnRlcm5hbC1hdCtqd3QiLCJhbGciOiJSUzI1NiJ9.eyJ2ZXIiOjEsImp0aSI6IkFULnJab1A5dUVpd0F3Zm4yZFVRWVRCa21SSGhmekVtRTUxQy1uRFJzRWxiOE0ub2FyMXJzZWY0dVBwcERGWW8xZDciLCJpc3MiOiJodHRwczovL2ZpbnRlY2hzc28ub2t0YXByZXZpZXcuY29tIiwiYXVkIjoiaHR0cHM6Ly9maW50ZWNoc3NvLm9rdGFwcmV2aWV3LmNvbSIsInN1YiI6ImJ5YW5AZmludGVjaGF1dG9tYXRpb24uY29tIiwiaWF0IjoxNzg1NDMyMDUzLCJleHAiOjE3ODU0MzU2NTMsImNpZCI6IjBvYWhlNTd2Y2VrQWtIaTlQMWQ3IiwidWlkIjoiMDB1ZTc1ejF2MHRtYjRFaU4xZDciLCJzY3AiOlsib2ZmbGluZV9hY2Nlc3MiLCJvcGVuaWQiXSwiYXV0aF90aW1lIjoxNzg1NDMyMDUxfQ.JR80oewA2D34ASnsHME9zh64xB_8UJSZZZSspSh9uO0ClWmQKN-P5Br23yjWnExw_N6-xx5Um_1njPHiWEwDRonEPW3YhQ55s48tnCK3-WA0bWwURVy0woDBbqnlnVwXb6NIs2fA7807HTVGtKX9k0G-vtLRdAkPGQKTegklkNG-vFpoB7GX0RTqf3nmdvdczf0BYtwV9xTmHjsPPJPXvn_Qc_twI8hrz3ufQtC42dGfJDZo7iCaKTOiscm7OosxJVE6iq_XOexbKvGiVpKoU2CufBh4c4mMbxzPBdu5XOW4Ge5F1EJdY5YLwn-gr4q_outhLz7q-cK8pCwagQ5dbg"
    private val defaultLogoUrl = "https://accloud-public-storage-dev1.s3.us-east-2.amazonaws.com/REx0xk8bC8_tenants/GBX/Fintech_6_pwo4ga.png"

    private val defaultLocalization = """{
  "intro": {
    "eyebrow": "Identity check",
    "title": "Let's confirm it's really you",
    "body": "A quick face scan helps protect your account.",
    "cta": "Start face scan",
    "trustLabel": "Bank-grade liveness detection"
  },
  "prepare": {
    "eyebrow": "Before we start",
    "title": "Three things for a clean scan",
    "tips": [
      {
        "title": "Find good light",
        "body": "Avoid strong backlight."
      },
      {
        "title": "Clear your face",
        "body": "Remove sunglasses or masks."
      },
      {
        "title": "Hold steady",
        "body": "Keep the device at eye level."
      }
    ],
    "cta": "I'm ready",
    "backLabel": "Back"
  },
  "starting": {
    "title": "Starting camera",
    "body": "Creating a secure liveness session."
  },
  "processing": {
    "title": "Verifying your scan",
    "body": "This usually takes just a moment."
  },
  "success": {
    "title": "You're verified",
    "body": "Thanks. The liveness check was completed.",
    "cta": "Thanks. The liveness check was completed."
  },
  "fail": {
    "title": "We couldn't complete the scan",
    "body": "Move somewhere brighter and try again.",
    "cta": "Move somewhere brighter and try again."
  },
  "cameraPermission": {
    "title": "Camera access is required",
    "body": "Allow camera access, then try again."
  }
}"""

    private val defaultCaptureText = """{
    "hintCenterFaceText": "Center your face",
    "hintTooCloseText": "Move back",
    "hintTooFarText": "Move closer",
    "hintHoldFaceForFreshnessText": "Hold still"
  }"""

    private var primaryHex: String? = null
    private var secondaryHex: String? = null
    private var headingHex: String? = null

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_parameter)
        title = "Input Parameters"

        val etToken = findViewById<EditText>(R.id.etToken)
        val etBackendUrl = findViewById<EditText>(R.id.etBackendUrl)
        val etTenant = findViewById<EditText>(R.id.etTenant)
        val etLocalization = findViewById<EditText>(R.id.etLocalization)
        val etCaptureText = findViewById<EditText>(R.id.etCaptureText)
        val etBrandName = findViewById<EditText>(R.id.etBrandName)
        val etBrandLogoUrl = findViewById<EditText>(R.id.etBrandLogoUrl)
        val etBrandSecureLabel = findViewById<EditText>(R.id.etBrandSecureLabel)
        val swSkipIntro = findViewById<SwitchMaterial>(R.id.swSkipIntro)
        val swSkipPrepare = findViewById<SwitchMaterial>(R.id.swSkipPrepare)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvErrorLog = findViewById<TextView>(R.id.tvErrorLog)

        val btnPickPrimary = findViewById<LinearLayout>(R.id.btnPickPrimary)
        val btnPickSecondary = findViewById<LinearLayout>(R.id.btnPickSecondary)
        val btnPickHeading = findViewById<LinearLayout>(R.id.btnPickHeading)

        val viewPrimaryColor = findViewById<View>(R.id.viewPrimaryColor)
        val viewSecondaryColor = findViewById<View>(R.id.viewSecondaryColor)
        val viewHeadingColor = findViewById<View>(R.id.viewHeadingColor)

//        etToken.setText(defaultToken)
        etBackendUrl.setText("https://api-dev.accelerationcloud.info")
        etTenant.setText("unifi")
        etBrandLogoUrl.setText(defaultLogoUrl)
        etLocalization.setText(defaultLocalization)
        etCaptureText.setText(defaultCaptureText)

        btnPickPrimary.setOnClickListener {
            showColorPicker("Pick Primary Color") { newHex ->
                primaryHex = newHex
                viewPrimaryColor.setBackgroundColor(Color.parseColor(newHex))
            }
        }

        btnPickSecondary.setOnClickListener {
            showColorPicker("Pick Secondary Color") { newHex ->
                secondaryHex = newHex
                viewSecondaryColor.setBackgroundColor(Color.parseColor(newHex))
            }
        }

        btnPickHeading.setOnClickListener {
            showColorPicker("Pick Heading Color") { newHex ->
                headingHex = newHex
                viewHeadingColor.setBackgroundColor(Color.parseColor(newHex))
            }
        }

        btnContinue.setOnClickListener {
            tvErrorLog?.visibility = View.GONE
            tvErrorLog?.text = ""

            progressBar.visibility = View.VISIBLE
            btnContinue.isEnabled = false

            scope.launch {
                val tokenVal = etToken.text.toString().trim()
                val backendUrlVal = etBackendUrl.text.toString().trim()
                val tenantVal = etTenant.text.toString().trim()

                val result = createLivenessLink(
                    token = tokenVal,
                    backendUrl = backendUrlVal,
                    tenant = tenantVal,
                    brandName = etBrandName.text.toString().trim(),
                    brandLogoUrl = etBrandLogoUrl.text.toString().trim(),
                    primaryColorHex = primaryHex
                )

                progressBar.visibility = View.GONE
                btnContinue.isEnabled = true
                if (result.isSuccess && !result.launchToken.isNullOrEmpty()) {
                    val intent = Intent(this@InputParameterActivity, FaceLivenessActivity::class.java).apply {
                        putExtra("backendUrl", backendUrlVal)
                        putExtra("tenant", tenantVal)
                        putExtra("launchToken", result.launchToken)
                        putExtra("brandName", valueOrDefault(etBrandName.text.toString().trim()))
                        putExtra("brandLogoUrl", valueOrDefault(etBrandLogoUrl.text.toString().trim()))
                        putExtra("brandSecureLabel", valueOrDefault(etBrandSecureLabel.text.toString().trim()))
                        putExtra("skipIntro", swSkipIntro.isChecked)
                        putExtra("skipPrepare", swSkipPrepare.isChecked)
                        putExtra("primaryColor", primaryHex)
                        putExtra("secondaryColor", secondaryHex)
                        putExtra("headingColor", headingHex)
                        putExtra("localization", etLocalization.text.toString())
                        putExtra("captureText", etCaptureText.text.toString())
                    }
                    startActivity(intent)
                } else {
                    tvErrorLog?.let {
                        it.text = "Error: ${result.errorLog}"
                        it.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun valueOrDefault(value: String, defaultValue: String? = null): String? {
        return if (value.isNotEmpty()) value else defaultValue
    }

    private data class LivenessLinkResult(
        val isSuccess: Boolean,
        val launchToken: String? = null,
        val errorLog: String? = null
    )

    private suspend fun createLivenessLink(
        token: String,
        backendUrl: String,
        tenant: String,
        brandName: String,
        brandLogoUrl: String,
        primaryColorHex: String?
    ): LivenessLinkResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("$backendUrl/api/v1/cores/$tenant/facial-liveness")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", token)
                doOutput = true
            }

            val jsonBody = JSONObject().apply {
                put("external_id", System.currentTimeMillis() * 1000)
                put("brandingConfig", JSONObject().apply {
                    put("tenantName", valueOrDefault(brandName, "Fintech Automation"))
                    put("logoUrl", valueOrDefault(brandLogoUrl, defaultLogoUrl))
                    put("bannerUrl", "https://accloud-public-storage-dev1.s3.us-east-2.amazonaws.com/REx0xk8bC8_tenants/GBX/banner.png")
                    put("themeColor", primaryColorHex ?: "#1d4ed8")
                })
            }

            OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }
            val responseCode = conn.responseCode
            Log.i("API", "Liveness link statusCode: $responseCode")

            if (responseCode == 200) {
                val responseStr = conn.inputStream.bufferedReader().readText()
                val jsonObj = JSONObject(responseStr)
                Log.i("API", "Liveness link response Data : $jsonObj")

                if (jsonObj.optInt("code") == 200) {
                    val launchToken = jsonObj.optString("data")
                    return@withContext LivenessLinkResult(isSuccess = true, launchToken = launchToken)
                } else {
                    val errMsg = jsonObj.optString("error_message", responseStr)
                    return@withContext LivenessLinkResult(isSuccess = false, errorLog = errMsg)
                }
            } else {
                val errorStreamText = conn.errorStream?.bufferedReader()?.readText() ?: conn.responseMessage
                return@withContext LivenessLinkResult(isSuccess = false, errorLog = errorStreamText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext LivenessLinkResult(isSuccess = false, errorLog = e.localizedMessage ?: e.message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun showColorPicker(initialTitle: String, onColorPicked: (String) -> Unit) {
        var currentHex = "#1D4ED8"

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val previewView = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            ).apply {
                bottomMargin = 24
            }
            try {
                setBackgroundColor(Color.parseColor(currentHex))
            } catch (_: Exception) {}
        }
        layout.addView(previewView)

        val hexInput = EditText(this).apply {
            hint = "#1D4ED8"
            setText(currentHex)
            setSingleLine()
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val input = s?.toString()?.trim() ?: ""
                    if (input.startsWith("#") && (input.length == 7 || input.length == 9)) {
                        try {
                            val color = Color.parseColor(input)
                            previewView.setBackgroundColor(color)
                            currentHex = input
                        } catch (_: Exception) {}
                    }
                }
            })
        }
        layout.addView(hexInput)

        val paletteGrid = GridLayout(this).apply {
            columnCount = 4
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 24
            }
        }

        val presetColors = arrayOf(
            "#1D4ED8", "#2563EB", "#0284C7", "#10B981", "#EF4444",
            "#F59E0B", "#64748B", "#0F172A", "#FFFFFF", "#000000",
            "#8B5CF6", "#EC4899", "#14B8A6", "#84CC16", "#EAB308", "#EAB3FF",
        )

        val size = (resources.displayMetrics.widthPixels - 200) / 5
        presetColors.forEach { colorHex ->
            val tile = View(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(8, 8, 8, 8)
                }
                setBackgroundColor(Color.parseColor(colorHex))
                setOnClickListener {
                    currentHex = colorHex
                    hexInput.setText(colorHex)
                    previewView.setBackgroundColor(Color.parseColor(colorHex))
                }
            }
            paletteGrid.addView(tile)
        }
        layout.addView(paletteGrid)

        AlertDialog.Builder(this)
            .setTitle(initialTitle)
            .setView(layout)
            .setPositiveButton("Got it") { _, _ ->
                onColorPicked(currentHex)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}