package com.android.doctorapp.ui.authentication.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AuthRepository
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.LoginResponseModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isEmailAddressValid
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel() {
    private val _loginResponse = SingleLiveEvent<LoginResponseModel?>()
    val loginResponse = _loginResponse.asLiveData()

    val email: MutableLiveData<String> = MutableLiveData()
    val emailError: MutableLiveData<String?> = MutableLiveData()
    val password: MutableLiveData<String> = MutableLiveData()
    val passwordError: MutableLiveData<String?> = MutableLiveData()

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()
    private var auth: FirebaseAuth? = null

    init {
        auth = FirebaseAuth.getInstance()
    }
    fun onClick() {
        if (isValidateInput()) {
            callLoginAPI()
        }
    }

    /**
     *  Validate the user input fields.
     */
    private fun isValidateInput(): Boolean {
        passwordError.postValue(null)
        emailError.postValue(null)
        if (email.value.isNullOrEmpty()) {
            emailError.postValue(resourceProvider.getString(R.string.error_enter_email))
        } else if (email.value.toString().isEmailAddressValid().not()) {
            emailError.postValue(resourceProvider.getString(R.string.enter_valid_email))
        } else if (password.value.isNullOrEmpty()) {
            passwordError.postValue(resourceProvider.getString(R.string.error_enter_password))
        } else return true
        return false
    }

    private fun callLoginAPI() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.login(
                auth!!,
                email = email.value.toString(),
                password = password.value.toString(),
            )) {
                is ApiSuccessResponse -> {
                    Log.d("response.body---", Gson().toJson(response.body.user?.email))
                    email.value = ""
                    password.value = ""
                    setShowProgress(false)
                    _loginResponse.postValue(LoginResponseModel("test","10","test1","test123"))
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

    fun onRegisterClick() {
        _navigationListener.postValue(R.id.action_loginFragment_to_registerFragment)
    }

}