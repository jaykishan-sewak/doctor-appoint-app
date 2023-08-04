package com.android.infusiontemplate.ui.authentication.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.infusiontemplate.R
import com.android.infusiontemplate.di.ResourceProvider
import com.android.infusiontemplate.di.base.BaseViewModel
import com.android.infusiontemplate.repository.AuthRepository
import com.android.infusiontemplate.repository.models.ApiErrorResponse
import com.android.infusiontemplate.repository.models.ApiSuccessResponse
import com.android.infusiontemplate.repository.models.LoginRequestModel
import com.android.infusiontemplate.repository.models.LoginResponseModel
import com.android.infusiontemplate.util.SingleLiveEvent
import com.android.infusiontemplate.util.extension.asLiveData
import com.android.infusiontemplate.util.extension.isEmailAddressValid
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
            val response = authRepository.login(
                LoginRequestModel(
                    email = email.value.toString(),
                    password = password.value.toString(),
                )
            )
            if (response is ApiSuccessResponse) {
                _loginResponse.postValue(response.body)
            } else if (response is ApiErrorResponse) {
                setApiError(response.errorMessage)
            }
            setShowProgress(false)
        }
    }

    fun onRegisterClick() {
        _navigationListener.postValue(R.id.action_loginFragment_to_registerFragment)
    }

}