package com.android.doctorapp.ui.authentication.login

import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AuthRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.local.USER_IS_EMAIL_VERIFIED
import com.android.doctorapp.repository.local.USER_TYPE
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.LoginResponseModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.constants.ConstantKey.DOCTOR
import com.android.doctorapp.util.constants.ConstantKey.USER
import com.android.doctorapp.util.constants.ConstantKey.USER_TYPE_ADMIN
import com.android.doctorapp.util.constants.ConstantKey.USER_TYPE_DOCTOR
import com.android.doctorapp.util.constants.ConstantKey.USER_TYPE_USER
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isEmailAddressValid
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider,
    private val context: Context,
    private val session: Session
) : BaseViewModel() {
    private val _loginResponse = SingleLiveEvent<LoginResponseModel?>()
    val loginResponse = _loginResponse.asLiveData()

    val doctorChecked: MutableLiveData<Boolean> = MutableLiveData(false)
    val userChecked: MutableLiveData<Boolean> = MutableLiveData(false)
    val adminChecked: MutableLiveData<Boolean> = MutableLiveData(false)

    var googleSignInClient: GoogleSignInClient

    val email: MutableLiveData<String> = MutableLiveData()
    val emailError: MutableLiveData<String?> = MutableLiveData()

    val password: MutableLiveData<String> = MutableLiveData()
    val passwordError: MutableLiveData<String?> = MutableLiveData()

    val isDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()

    val isGoogleClick: MutableLiveData<Boolean> = MutableLiveData(false)
    private val googleResponse: MutableLiveData<Boolean> = MutableLiveData(false)

    val signInAccountTask: MutableLiveData<Task<GoogleSignInAccount>?> = MutableLiveData()
    val googleSignInAccount: MutableLiveData<GoogleSignInAccount?> = MutableLiveData()
    val authCredential: MutableLiveData<AuthCredential?> = MutableLiveData()

    val isUserVerified: MutableLiveData<String> = SingleLiveEvent()
    val isResetDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    val forgotPassEmailSend: MutableLiveData<Boolean> = MutableLiveData(false)


    init {
        // googleSignInOptions initialization
        googleInitialization(context)
        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    }


    fun onClick() {
        if (context.isNetworkAvailable()) {
            callLoginAPI()
        } else {
            context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }

    }

    /**
     *  Validate the user input fields.
     */
    private fun isAllValidate() {
        isDataValid.value =
            (!email.value.isNullOrEmpty() && !password.value.isNullOrEmpty() && emailError.value.isNullOrEmpty() && passwordError.value.isNullOrEmpty())
    }

    fun isValidateEmail(text: CharSequence) {
        if (text.toString().isNotEmpty() && text.toString().isEmailAddressValid().not()) {
            emailError.value = resourceProvider.getString(R.string.enter_valid_email)
        } else {
            emailError.value = null
        }
        isAllValidate()
        isResetAllValidate()
    }

    fun isValidPassword(text: CharSequence?) {
        if (text.toString().isEmpty() && text.toString() != "") {
            passwordError.value = resourceProvider.getString(R.string.enter_password)
        } else {
            passwordError.value = null
        }
        isAllValidate()
    }

    private fun isResetAllValidate() {
        isResetDataValid.value =
            (!email.value.isNullOrEmpty() && emailError.value.isNullOrEmpty())
    }


    /**
     * This method is used for email and password
     * firebase authentication. It returns response of ApiResponse type.
     */
    private fun callLoginAPI() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.login(
                firebaseAuth,
                email = email.value.toString(),
                password = password.value.toString(),
            )) {
                is ApiSuccessResponse -> {
                    if (!firebaseAuth.currentUser?.uid.isNullOrEmpty()) {
                        getUserData()

                    }
                }

                is ApiErrorResponse -> {
                    setApiError(response.errorMessage)
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setNoNetworkError(response.errorMessage)
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
                }
            }
        }
    }

    private suspend fun getUserData() {

        when (val response =
            authRepository.getRecordById(firebaseAuth.currentUser?.uid.toString(), fireStore)) {

            is ApiSuccessResponse -> {
                email.value = ""
                password.value = ""
                setShowProgress(false)
                session.putString(USER_ID, response.body.userId)
                session.putBoolean(USER_IS_EMAIL_VERIFIED, false)
                if (response.body.isAdmin) {
                    session.putString(USER_TYPE, USER_TYPE_ADMIN)
                    adminChecked.value = true
                } else if (response.body.isDoctor) {

                    if (response.body.isUserVerified) {
                        session.putString(USER_TYPE, USER_TYPE_DOCTOR)
                        doctorChecked.value = true
                    } else {
                        isUserVerified.postValue(DOCTOR)
                    }
                } else {
                    if (response.body.isUserVerified) {
                        session.putString(USER_TYPE, USER_TYPE_USER)
                        userChecked.value = true
                    } else {
                        isUserVerified.postValue(USER)
                    }
                }
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

    /**
     * This method is used for google sign in
     * firebase authentication. It returns response of ApiResponse type.
     */
    fun callGoogleAPI(authCredential: AuthCredential) {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.googleLogin(
                firebaseAuth,
                authCredential,
            )) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    if (!firebaseAuth.currentUser?.uid.isNullOrEmpty()) {
                        getUserData()
                    }
                    googleResponse.postValue(true)
                }

                is ApiErrorResponse -> {
                    setApiError(response.errorMessage)
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setNoNetworkError(response.errorMessage)
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
                }
            }
        }
    }

    fun callSignInAccountTaskAPI(result: ActivityResult) {
        viewModelScope.launch {
            setShowProgress(true)
            if (context.isNetworkAvailable()) {
                when (val response = authRepository.signInAccountTask(
                    result,
                )) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        signInAccountTask.postValue(response.body)
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
            } else {
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
            }
        }
    }

    fun callGoogleSignInAccountAPI(signInAccountTask: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.googleSignInAccount(
                signInAccountTask,
            )) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    googleSignInAccount.postValue(response.body)
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

    fun callAuthCredentialsAPI(idToken: String) {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.authCredentials(
                idToken
            )) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    authCredential.postValue(response.body)
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
        email.postValue("")
        emailError.postValue(null)
        password.postValue("")
        passwordError.postValue(null)
        _navigationListener.postValue(R.id.action_loginFragment_to_registerFragment)

    }

    fun onForgotPassClick() {
        email.postValue("")
        emailError.postValue(null)
        password.postValue("")
        passwordError.postValue(null)
        _navigationListener.postValue(R.id.action_loginFragment_to_forgotPasswordFragment)

    }

    fun onLoginClick() {
        _navigationListener.postValue(R.id.action_forgot_pass_to_login)

    }

    /**
     * This method is used for googleSign image click or not.
     * Directly called in xml
     */
    fun onGoogleSignClick() {
        isGoogleClick.postValue(true)
    }

    private fun callForgotPassAPI() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.forgotPassword(
                firebaseAuth,
                email.value.toString(),
            )) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    forgotPassEmailSend.postValue(true)
                }

                is ApiErrorResponse -> {
                    setApiError(response.errorMessage)
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setNoNetworkError(response.errorMessage)
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
                }
            }
        }
    }

    fun onResetPassClick() {
        if (context.isNetworkAvailable()) {
            callForgotPassAPI()
        } else {
            context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }

    }

}