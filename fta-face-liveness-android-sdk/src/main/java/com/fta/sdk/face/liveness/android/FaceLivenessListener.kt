package com.fta.sdk.face.liveness.android

import com.fta.sdk.face.liveness.android.models.*

interface FaceLivenessListener {
    fun onSuccess(result: LivenessResultModel?) {}
    fun onFail(result: LivenessResultModel?) {}
    fun onError(error: LivenessErrorModel?) {}
    fun onCancel() {}
    fun onAnalysisComplete() {}
    fun onScreenChange(screenType: LivenessScreenType?) {}
    fun onContinue() {}
}