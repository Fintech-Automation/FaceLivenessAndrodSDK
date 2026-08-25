package com.fta.sdk.face.liveness.android

import com.fta.sdk.face.liveness.android.models.LivenessErrorModel
import com.fta.sdk.face.liveness.android.models.LivenessResultModel
import com.fta.sdk.face.liveness.android.models.LivenessScreenType
import com.fta.sdk.face.liveness.android.models.LivenessSessionStatus

/**
 * Callbacks exposed by the Android SDK. The callback contract mirrors the Flutter SDK.
 */
interface FaceLivenessListener {
    fun onSuccess(result: LivenessResultModel?) {}
    fun onFail(result: LivenessResultModel?) {}
    fun onError(error: LivenessErrorModel?) {}
    fun onCancel() {}
    fun onAnalysisComplete() {}
    fun onScreenChange(screenType: LivenessScreenType?) {}
    fun onContinue() {}
    fun onSessionStatusChange(status: LivenessSessionStatus?) {}
}
