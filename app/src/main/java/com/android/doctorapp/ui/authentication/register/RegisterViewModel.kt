package com.android.doctorapp.ui.authentication.register

import android.content.Context
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AuthRepository
import com.android.doctorapp.repository.models.*
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isEmailAddressValid
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.launch
import javax.inject.Inject

class RegisterViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    val email: MutableLiveData<String> = MutableLiveData()
    val emailError: MutableLiveData<String?> = MutableLiveData()
    val password: MutableLiveData<String> = MutableLiveData()
    val passwordError: MutableLiveData<String?> = MutableLiveData()
    private val _registerResponse = MutableLiveData<LoginResponseModel?>()
    val registerResponse = _registerResponse.asLiveData()

    private val _navigation = SingleLiveEvent<Int>()
    val navigation = _navigation.asLiveData()

    private var auth: FirebaseAuth? = null

    init {
        auth = FirebaseAuth.getInstance()
    }


    fun onRegisterClick() {

        if (isValidInput()) {
            callRegisterApi()
        }
    }

    private fun callRegisterApi() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.register(
                auth!!,
                email = email.value.toString(),
                password = password.value.toString(),
            )) {
                is ApiSuccessResponse -> {
                    Log.d("response.body---", Gson().toJson(response.body.user?.email))
                    email.value = ""
                    password.value = ""
                    setShowProgress(false)
                    _navigation.postValue(R.id.action_registerFragment_to_loginFragment)
                }

                is ApiErrorResponse -> {
                    Log.d("ApiErrorResponse.body---", response.errorMessage)
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    Log.d("ApiNoNetworkResponse.body---", response.errorMessage)
                    setShowProgress(false)
                }

                else -> {}
            }
        }
    }

    private fun isValidInput(): Boolean {
        emailError.postValue(null)
        passwordError.postValue(null)
        if (email.value.isNullOrEmpty()) {
            emailError.postValue(resourceProvider.getString(R.string.error_enter_email))
        } else if (email.value.toString().isEmailAddressValid().not()) {
            emailError.postValue(resourceProvider.getString(R.string.enter_valid_email))
        } else if (password.value.isNullOrEmpty()) {
            passwordError.postValue(resourceProvider.getString(R.string.error_enter_password))
        } else return true
        return false
    }
}