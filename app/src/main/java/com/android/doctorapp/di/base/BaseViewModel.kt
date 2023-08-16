package com.android.doctorapp.di.base

import androidx.lifecycle.ViewModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

open class BaseViewModel : ViewModel() {

    private val _apiError = SingleLiveEvent<String>()
    val apiError = _apiError.asLiveData()

    private val _noNetwork = SingleLiveEvent<String>()
    val noNetwork = _noNetwork.asLiveData()

    private val _showProgress = SingleLiveEvent<Boolean>()
    val showProgress = _showProgress.asLiveData()

    var fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var firebaseUser: FirebaseUser
    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

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