package com.android.doctorapp.di.base

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.repository.AppointmentRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

open class BaseViewModel(
) : ViewModel() {

    private val _apiError = SingleLiveEvent<String>()
    val apiError = _apiError.asLiveData()

    private val _noNetwork = SingleLiveEvent<String>()
    val noNetwork = _noNetwork.asLiveData()

    private val _showProgress = SingleLiveEvent<Boolean>()
    val showProgress = _showProgress.asLiveData()

    var fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var firebaseUser: FirebaseUser
    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var  googleSignInOptions : GoogleSignInOptions
    val storage = FirebaseStorage.getInstance()

    fun googleInitialization(context:Context){
        googleSignInOptions= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.client_id))
            .requestEmail()
            .build()
    }
    fun setApiError(message: String) {
        _apiError.postValue(message)
    }

    fun setNoNetworkError(message: String) {
        _noNetwork.postValue(message)
    }

    fun setShowProgress(value: Boolean) {
        _showProgress.postValue(value)
    }
    fun updateUserData(token: String?,resourceProvider:ResourceProvider,session:Session,context:Context,appointmentRepository:AppointmentRepository) {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response =
                        appointmentRepository.updateUserData(token, it, fireStore)) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            context.toast("Token stored successfully")
                        }

                        is ApiErrorResponse -> {
                            setShowProgress(false)
                        }

                        is ApiNoNetworkResponse -> {
                            setShowProgress(false)
                        }

                        else -> {
                            setShowProgress(false)
                        }
                    }
                } else
                    context.toast(resourceProvider.getString(R.string.check_internet_connection))
            }
        }
    }
}