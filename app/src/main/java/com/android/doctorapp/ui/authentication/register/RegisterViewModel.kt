package com.android.doctorapp.ui.authentication.register

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
import com.android.doctorapp.util.extension.isPassWordValid
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

    val confirmPassword: MutableLiveData<String> = MutableLiveData()
    val confirmPasswordError: MutableLiveData<String?> = MutableLiveData()

    val isSignUpDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _registerResponse = MutableLiveData<LoginResponseModel?>()
    val registerResponse = _registerResponse.asLiveData()

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()


    init {

    }

    fun onRegisterClick() {
        callRegisterApi()
    }

    private fun isAllValidate() {
        isSignUpDataValid.value = (!email.value.isNullOrEmpty() && !password.value.isNullOrEmpty() && !confirmPassword.value.isNullOrEmpty()
                && emailError.value.isNullOrEmpty() && passwordError.value.isNullOrEmpty() && confirmPasswordError.value.isNullOrEmpty())
    }

    fun isValidEmail(text: CharSequence) {
        if (text.toString().isNotEmpty() && text.toString().isEmailAddressValid().not()) {
            emailError.postValue(resourceProvider.getString(R.string.enter_valid_email))
        } else {
            emailError.postValue(null)
        }
        isAllValidate()
    }

    fun isValidPassword(text: CharSequence) {
        if (text.toString().isNotEmpty() && text.toString().isPassWordValid().not()) {
            passwordError.postValue(resourceProvider.getString(R.string.error_enter_password))
        } else {
            passwordError.postValue(null)
        }
        isAllValidate()
    }

    fun isConfirmPassword(text: CharSequence) {
        if (text.toString().isNotEmpty() && text.toString() != password.value) {
            confirmPasswordError.postValue(resourceProvider.getString(R.string.confirm_password_doesn_t_match_try_again))
        } else {
            confirmPasswordError.postValue(null)
        }
        isAllValidate()
    }

    /**
     * This method is used for create user with Email and Password
     * firebase authentication. It returns response of ApiResponse type.
     */
    private fun callRegisterApi() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.register(
                firebaseAuth,
                email = email.value.toString(),
                password = password.value.toString(),
            )) {
                is ApiSuccessResponse -> {
                    Log.d("response.body---", Gson().toJson(response.body.user?.email))
                    email.value = ""
                    password.value = ""
                    setShowProgress(false)
                    _navigationListener.postValue(R.id.action_registerFragment_to_loginFragment)
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

    fun onLogInClick() {
        email.postValue("")
        emailError.postValue(null)
        password.postValue("")
        passwordError.postValue(null)
        confirmPassword.postValue("")
        confirmPasswordError.postValue(null)
        _navigationListener.postValue(R.id.action_registerFragment_to_loginFragment)

    }

}