package com.android.doctorapp.ui.authentication.login


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
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {
    private val _loginResponse = SingleLiveEvent<LoginResponseModel?>()
    val loginResponse = _loginResponse.asLiveData()

    val email: MutableLiveData<String> = MutableLiveData()
    val emailError: MutableLiveData<String?> = MutableLiveData()

    val password: MutableLiveData<String> = MutableLiveData()
    val passwordError: MutableLiveData<String?> = MutableLiveData()

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()
    var auth: FirebaseAuth? = null

    val isGoogleClick: MutableLiveData<Boolean> = MutableLiveData(false)
    private val googleResponse: MutableLiveData<Boolean> = MutableLiveData(false)




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
        } else if (password.value.toString().isPassWordValid().not()) {
            passwordError.postValue(resourceProvider.getString(R.string.password_should_contain_at_least_8_characters))
        } else return true
        return false
    }



    /**
     * This method is used for email and password
     * firebase authentication. It returns response of ApiResponse type.
     */
    private fun callLoginAPI() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.login(
                auth!!,
                email = email.value.toString(),
                password = password.value.toString(),
            )) {
                is ApiSuccessResponse -> {
                    email.value = ""
                    password.value = ""
                    setShowProgress(false)
                    _navigationListener.postValue(R.id.action_loginFragment_to_addUserProfileFragment)
                    //_loginResponse.postValue(LoginResponseModel("test", "10", "test1", "test123"))
                }

                is ApiErrorResponse -> {
                    setApiError(response.errorMessage)
                    setShowProgress(false)
                }
                
                is ApiNoNetworkResponse -> {
                    setNoNetworkError(response.errorMessage)
                    setShowProgress(false)
                }
                else -> {}
            }
        }
    }

    /**
     * This method is used for google sign in
     * firebase authentication. It returns response of ApiResponse type.
     */
    fun callGoogleAPI(authCredential : AuthCredential){
        viewModelScope.launch {
            setShowProgress(true)
            when(val response = authRepository.googleLogin(
                auth!!,
                authCredential,
            )) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    googleResponse.postValue(true)
                    _navigationListener.postValue(R.id.action_loginFragment_to_addUserProfileFragment)
                }

                is ApiErrorResponse -> {
                    setApiError(response.errorMessage)
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setNoNetworkError(response.errorMessage)
                    setShowProgress(false)
                }
                else -> {}
            }
        }
    }
    /**
     * This method is used for navigation to Register Screen
     *  onclick of SignUp text. Directly called in xml
     */
    fun onRegisterClick() {
        _navigationListener.postValue(R.id.action_loginFragment_to_registerFragment)
    }

    /**
     * This method is used for googleSign image click or not.
     * Directly called in xml
     */
    fun onGoogleSignClick() {
        isGoogleClick.postValue(true)
    }



}