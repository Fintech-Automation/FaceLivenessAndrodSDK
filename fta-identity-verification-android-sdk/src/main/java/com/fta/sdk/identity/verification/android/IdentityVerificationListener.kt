package com.fta.sdk.identity.verification.android

import com.fta.sdk.identity.verification.android.models.IdentityVerificationErrorModel
import com.fta.sdk.identity.verification.android.models.IdentityVerificationResultModel
import com.fta.sdk.identity.verification.android.models.IdentityVerificationScreenType
import com.fta.sdk.identity.verification.android.models.IdentityVerificationSessionStatus

/**
 * Callbacks exposed by the Android SDK. The callback contract mirrors the Flutter SDK.
 */
interface IdentityVerificationListener {
    fun onSuccess(result: IdentityVerificationResultModel?) {}
    fun onFail(result: IdentityVerificationResultModel?) {}
    fun onError(error: IdentityVerificationErrorModel?) {}
    fun onCancel() {}
    fun onAnalysisComplete() {}
    fun onScreenChange(screenType: IdentityVerificationScreenType?) {}
    fun onContinue() {}
    fun onSessionStatusChange(status: IdentityVerificationSessionStatus?) {}
}
