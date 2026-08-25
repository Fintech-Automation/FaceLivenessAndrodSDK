# FTA Face Liveness Android SDK

The FTA Face Liveness Android SDK provides face liveness verification for Android applications through an idiomatic Kotlin API. It includes the liveness UI, configuration models, JavaScript bridge, callbacks, session handling, and result/error models required to integrate face liveness into an Android application.

## Flow

The liveness flow is:

```text
intro -> prepare -> capture -> processing -> success | fail | error
```

The SDK loads its bundled liveness interface and provides the initial configuration through the native Android layer. JavaScript bridge events are translated into Android callbacks.

## Installation

Add the Android SDK module to your application project, or consume the published AAR/Maven artifact when available.

For a local module:

```kotlin
dependencies {
    implementation(project(":fta-face-liveness-android-sdk"))
}
```

The SDK requires:

- compileSdk 36
- minSdk 24
- Kotlin 2.0.21
- Android Gradle Plugin 8.11.2
- Java/Kotlin target 11

## Usage

Create a `FaceLivenessView` in your Activity or Fragment and configure the required SDK parameters.

```kotlin
val view = findViewById<FaceLivenessView>(R.id.faceLivenessView)

view.verificationToken = "YOUR_TOKEN"

view.brand = LivenessBrand(
    name = "Fintech Automation",
    logoUrl = "https://example.com/logo.png",
    secureLabel = "Bank-grade liveness detection"
)

view.flow = LivenessFlow(
    skipIntro = false,
    skipPrepare = false
)

view.theme = LivenessTheme(
    colors = LivenessThemeColors(
        primary = "#1634A4",
        secondary = "#1A3DBF",
        heading = "#111827"
    ),
    shape = LivenessThemeShape(radius = 22),
    typography = LivenessThemeTypography(
        fontFamily = "Inter, system-ui, sans-serif"
    )
)

view.listener = object : FaceLivenessListener {
    override fun onSuccess(result: LivenessResultModel?) {
        println("Liveness succeeded: ${result?.toJson()}")
    }

    override fun onFail(result: LivenessResultModel?) {
        println("Liveness failed: ${result?.toJson()}")
    }

    override fun onError(error: LivenessErrorModel?) {
        println("Liveness error: ${error?.toJson()}")
    }

    override fun onCancel() {
        println("Liveness canceled")
    }

    override fun onContinue() {
        println("Liveness continue")
    }

    override fun onAnalysisComplete() {
        println("Liveness analysis complete")
    }

    override fun onScreenChange(screenType: LivenessScreenType?) {
        println("Liveness screen: $screenType")
    }

    override fun onSessionStatusChange(status: LivenessSessionStatus?) {
        println("Session status: ${status?.status}")
    }
}

view.load()
```

## Authentication Token

Obtain a `verificationToken` from your backend before starting the liveness flow.

Pass the complete token to `FaceLivenessView.verificationToken` without decoding, prefixing, or modifying it.

The SDK handles token decoding, session validation, session creation, capture, result lookup, and session-status handling internally. Applications only need to provide the complete verification token.

For API request and response details, refer to the [UniFi Face Liveness API documentation](https://api-docs.accelerationcloud.com/resource/unifi-face-liveness).

## Configuration

### Verification Token

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `verificationToken` | `String` | Yes | none | Bearer token used by the liveness backend. |

### Brand

```kotlin
LivenessBrand(
    name = "Fintech Automation",
    logoUrl = "https://example.com/logo.png",
    secureLabel = "Bank-grade liveness detection"
)
```

| Field | Default | Description |
| --- | --- | --- |
| `name` | `null` / hidden | Business or brand name. |
| `logoUrl` | none | Optional hosted brand image URL. |
| `secureLabel` | SDK default | Security/trust label. Pass an empty string to hide it. |

Brand rendering uses the following priority: `logoUrl`, then `name`, with the SDK fallback mark when neither is available.

### Flow

```kotlin
LivenessFlow(
    skipIntro = false,
    skipPrepare = false
)
```

| Field | Default | Description |
| --- | --- | --- |
| `skipIntro` | `false` | Starts at Prepare instead of Intro. |
| `skipPrepare` | `false` | Skips Prepare and starts Capture. |

### Theme

```kotlin
LivenessTheme(
    colors = LivenessThemeColors(
        primary = "#1634A4",
        secondary = "#1A3DBF",
        heading = "#111827"
    ),
    shape = LivenessThemeShape(radius = 22),
    typography = LivenessThemeTypography(
        fontFamily = "Inter, system-ui, sans-serif"
    )
)
```

| Field | Default | Description |
| --- | --- | --- |
| `colors.primary` | `#1634A4` | Main brand color. |
| `colors.secondary` | `#1A3DBF` | Secondary brand accent. |
| `colors.heading` | `#111827` | Main heading and strong text color. |
| `shape.radius` | `22` | Root/card corner radius. |
| `typography.fontFamily` | `Inter, system-ui, sans-serif` | Font family used by wrapper/capture theme. |

The Android model also exposes the additional color tokens used by the shared HTML: `brandTint`, `body`, `muted`, `line`, `bg`, `card`, and `primaryText`.

### Localization

`LivenessLocalization` provides localized text for each SDK screen:

```kotlin
LivenessLocalization(
    intro = LivenessLocalizationIntro(
        eyebrow = "Identity check",
        title = "Let's confirm it's really you",
        body = "A quick face scan helps protect your account.",
        cta = "Start face scan",
        trustLabel = "Bank-grade liveness detection"
    ),
    prepare = LivenessLocalizationPrepare(
        eyebrow = "Before we start",
        title = "Three things for a clean scan",
        tips = listOf(
            LivenessLocalizationPageElements("Find good light", "Avoid strong backlight."),
            LivenessLocalizationPageElements("Clear your face", "Remove sunglasses or masks."),
            LivenessLocalizationPageElements("Hold steady", "Keep the device at eye level.")
        ),
        cta = "I'm ready",
        backLabel = "Back"
    ),
    starting = LivenessLocalizationPageElements("Starting camera", "Creating a secure liveness session."),
    processing = LivenessLocalizationPageElements("Verifying your scan", "This usually takes just a moment."),
    success = LivenessLocalizationResultElements("You're verified", "Thanks. The liveness check was completed.", "Continue"),
    fail = LivenessLocalizationResultElements("We couldn't complete the scan", "Move somewhere brighter and try again.", "Retry"),
    cameraPermission = LivenessLocalizationPageElements("Camera access is required", "Allow camera access, then try again.")
)
```

Supported paths:

| Path | Type | Description |
| --- | --- | --- |
| `intro.eyebrow` | `String` | Intro overline. |
| `intro.title` | `String` | Intro heading. |
| `intro.body` | `String` | Intro explanation. |
| `intro.cta` | `String` | Intro action. |
| `intro.trustLabel` | `String` | Intro trust/security label. |
| `prepare.eyebrow` | `String` | Prepare overline. |
| `prepare.title` | `String` | Prepare heading. |
| `prepare.tips` | `List<LivenessLocalizationPageElements>` | Prepare checklist. |
| `prepare.cta` | `String` | Prepare action. |
| `prepare.backLabel` | `String` | Prepare back label. |
| `starting.title/body` | `String` | Camera/session startup copy. |
| `processing.title/body` | `String` | Result-processing copy. |
| `success.title/body/cta` | `String` | Success screen copy. |
| `fail.title/body/cta` | `String` | Failure/retry copy. |
| `cameraPermission.title/body` | `String` | Camera permission copy. |

### Capture Text

The capture interface accepts customizable capture guidance text. Configure it from Android with:

```kotlin
view.captureText = mapOf(
    "faceTooFar" to "Move closer",
    "faceTooClose" to "Move back"
)
```

Keys are passed through unchanged to the shared HTML runtime.

## Callbacks

Implement `FaceLivenessListener` to receive liveness lifecycle events:

| Callback | Description |
| --- | --- |
| `onSuccess` | Liveness verification completed successfully. |
| `onFail` | Liveness verification completed but did not pass. |
| `onError` | An SDK or runtime error occurred. |
| `onCancel` | The user canceled the liveness flow. |
| `onAnalysisComplete` | Capture analysis has completed. |
| `onSessionStatusChange` | The verification session status changed. |
| `onScreenChange` | The liveness screen changed. |
| `onContinue` | The user continued to the next stage. |

## Session Status

`LivenessSessionStatus.status` supports the following values:

- `COMPLETED` — the token already completed the liveness check successfully.
- `EXPIRED` — the token or session has expired.
- `INVALID` — the token/session is invalid or rejected.
- `READY` — the session is valid and ready for liveness detection.
- `RETRY_LIMIT_EXCEEDED` — no further verification attempts are allowed.

The sample app closes the liveness screen after terminal session statuses that require exiting the flow.

## Result

`LivenessResultModel` represents the liveness verification result:

```kotlin
data class LivenessResultModel(
    val id: String? = null,
    val status: String? = null,
    val failReason: String? = null,
    val createdTime: String? = null,
    val completedTime: String? = null
)
```

The Android implementation also keeps `rawJson` internally so fields returned by the shared runtime are not unnecessarily discarded.

## Error

`LivenessErrorModel` represents an SDK or liveness runtime error:

```kotlin
data class LivenessErrorModel(
    val stage: String? = null,
    val message: String? = null,
    val cause: Any? = null
)
```

## Android Permissions

The SDK requires:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
```

The SDK library declares these permissions. The host application must still comply with Android runtime permission requirements. The SDK requests camera permission when the WebView requests camera access.

The host Activity should be a `ComponentActivity` (including `AppCompatActivity`) so the SDK can register the runtime camera-permission launcher.

## WebView / Runtime Requirements

The capture runtime requires:

- Camera access.
- JavaScript and DOM storage enabled.
- Network access.
- WebGL support.
- A production environment with appropriate HTTPS/network configuration.

The Android SDK loads the bundled liveness interface through a WebView and injects the native configuration at document start. The JavaScript bridge is initialized before the liveness flow begins.

## Sample App

The `app` module is a complete Android sample application for testing SDK integration.

The sample uses the recommended parameter order and SDK defaults:

```text
Input Parameters
├── Verification Token
├── Parameters
│   ├── Backend Url
│   ├── Tenant
│   └── Localization
├── Brand
│   ├── Name
│   ├── Logo Url
│   └── Secure Label
├── Flow
│   ├── Skip Intro
│   └── Skip Prepare
├── Theme
│   ├── Primary
│   ├── Secondary Color
│   └── Heading Color
└── Continue
```

Backend URL and Tenant are included in the sample UI for configuration and testing. The SDK uses the complete `verificationToken` to determine the verification environment and session configuration.

## API Overview

| Component | Purpose |
| --- | --- |
| `FaceLivenessView` | Main Android liveness view. |
| `verificationToken` | Authentication and verification token. |
| `brand` | Brand and trust presentation. |
| `flow` | Intro and preparation flow configuration. |
| `theme` | Colors, shape, and typography configuration. |
| `localization` | Screen and action text configuration. |
| `captureText` | Capture guidance text configuration. |
| `FaceLivenessListener` | Liveness lifecycle callbacks. |

## Notes

- The camera capture step owns the camera oval geometry and liveness model flow. The SDK themes the surrounding UI and supported capture theme tokens.
- The underlying capture runtime uses process-global client configuration. If a host app also configures the same provider runtime, mount this SDK with that shared global behavior in mind.
- Bundled runtime identifiers are public client identifiers. Privileged operations stay on the FTA backend.
- The bundled liveness interface is part of the SDK runtime and should be updated together with the SDK when runtime behavior changes.

## License

This repository includes the FinTech Face Liveness SDK, which is licensed under a Commercial License Agreement. See the repository license files for the applicable terms.

Use of this SDK requires explicit permission from FinTech Automation.
