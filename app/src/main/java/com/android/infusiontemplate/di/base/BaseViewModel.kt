package com.android.infusiontemplate.di.base

import androidx.lifecycle.ViewModel
import com.android.infusiontemplate.util.SingleLiveEvent
import com.android.infusiontemplate.util.extension.asLiveData

open class BaseViewModel : ViewModel() {

    private val _apiError = SingleLiveEvent<String>()
    val apiError = _apiError.asLiveData()

    private val _noNetwork = SingleLiveEvent<String>()
    val noNetwork = _noNetwork.asLiveData()

    private val _showProgress = SingleLiveEvent<Boolean>()
    val showProgress = _showProgress.asLiveData()

    fun setApiError(message: String) {
        _apiError.postValue(message)
    }

    fun setNoNetworkError(message: String) {
        _noNetwork.postValue(message)
    }

    fun setShowProgress(value: Boolean) {
        _showProgress.postValue(value)
    }
}