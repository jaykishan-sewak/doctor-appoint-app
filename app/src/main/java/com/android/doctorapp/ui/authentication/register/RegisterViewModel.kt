package com.android.doctorapp.ui.authentication.register

import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AuthRepository
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isEmailAddressValid
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.isPassWordValid
import com.android.doctorapp.util.extension.toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.launch
import javax.inject.Inject

class RegisterViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val authRepository: AuthRepository,
    private val context: Context
) : BaseViewModel() {

    val email: MutableLiveData<String> = MutableLiveData()
    val emailError: MutableLiveData<String?> = MutableLiveData()

    val password: MutableLiveData<String> = MutableLiveData()
    val passwordError: MutableLiveData<String?> = MutableLiveData()

    val confirmPassword: MutableLiveData<String> = MutableLiveData()
    val confirmPasswordError: MutableLiveData<String?> = MutableLiveData()

    val isSignUpDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()

    private val _addUserResponse = SingleLiveEvent<String>()
    val addUserResponse = _addUserResponse.asLiveData()

    var googleSignInClient: GoogleSignInClient

    val isGoogleClick: MutableLiveData<Boolean> = MutableLiveData(false)
    private val googleResponse: MutableLiveData<Boolean> = MutableLiveData(false)

    val signInAccountTask: MutableLiveData<Task<GoogleSignInAccount>?> = MutableLiveData()
    val googleSignInAccount: MutableLiveData<GoogleSignInAccount?> = MutableLiveData()
    val authCredential: MutableLiveData<AuthCredential?> = MutableLiveData()


    init {
        // googleSignInOptions initialization
        googleInitialization(context)
        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
    }

    fun onRegisterClick() {
        if (context.isNetworkAvailable()) {
            callRegisterApi()
        } else {
            context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    private fun isAllValidate() {
        isSignUpDataValid.value = (!email.value.isNullOrEmpty() && !password.value.isNullOrEmpty()
                && !confirmPassword.value.isNullOrEmpty() && emailError.value.isNullOrEmpty()
                && passwordError.value.isNullOrEmpty() && confirmPasswordError.value.isNullOrEmpty())
    }

    fun isValidEmail(text: CharSequence) {
        if (text.toString().isNotEmpty() && text.toString().isEmailAddressValid().not()) {
            emailError.value = resourceProvider.getString(R.string.enter_valid_email)
        } else {
            emailError.value = null
        }
        isAllValidate()
    }

    fun isValidPassword(text: CharSequence) {
        if (text.toString().isNotEmpty() && text.toString().isPassWordValid().not()) {
            passwordError.value = resourceProvider.getString(R.string.error_enter_password)
        } else {
            if (!confirmPassword.value.isNullOrEmpty() && text.toString() != confirmPassword.value) {
                passwordError.value =
                    resourceProvider.getString(R.string.password_doesn_t_match_try_again)
            } else {
                passwordError.value = null
                confirmPasswordError.value = null
            }
        }
        isAllValidate()
    }

    fun isConfirmPassword(text: CharSequence) {
        if (text.toString().isNotEmpty() && text.toString() != password.value) {
            confirmPasswordError.value =
                resourceProvider.getString(R.string.confirm_password_doesn_t_match_try_again)
        } else {
            passwordError.value = null
            confirmPasswordError.value = null

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
                    addUserData()
                }

                is ApiErrorResponse -> {
                    setShowProgress(false)
                    context.toast(response.errorMessage)
                }

                is ApiNoNetworkResponse -> {
                    setShowProgress(false)
                }

                else -> {}
            }
        }
    }

    private fun addUserData() {

        firebaseUser = firebaseAuth.currentUser!!

        val userData = UserDataRequestModel(
            userId = firebaseUser.uid,
            isDoctor = false,
            email = firebaseUser.email!!,
            isNotificationEnable = true
        )

        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.addUserData(
                userData,
                fireStore,
            )) {
                is ApiSuccessResponse -> {
                    if (response.body.userId.isNotEmpty()) {
                        email.value = ""
                        password.value = ""
                        confirmPassword.value = ""
                        setShowProgress(false)
                        _navigationListener.postValue(R.id.action_registerFragment_to_loginFragment)
                        _addUserResponse.value = resourceProvider.getString(R.string.success)
                    }
                }

                is ApiErrorResponse -> {
                    _addUserResponse.value = response.errorMessage
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    _addUserResponse.value = response.errorMessage
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
                }
            }
        }

    }

    fun callGoogleAPI(authCredential: AuthCredential) {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.googleLogin(
                firebaseAuth,
                authCredential,
            )) {
                is ApiSuccessResponse -> {
                    addUserData()
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

                    else -> {
                        setShowProgress(false)
                    }
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


    fun onLogInClick() {
        email.postValue("")
        emailError.postValue(null)
        password.postValue("")
        passwordError.postValue(null)
        confirmPassword.postValue("")
        confirmPasswordError.postValue(null)
        _navigationListener.postValue(R.id.action_registerFragment_to_loginFragment)

    }

    fun onGoogleSignClick() {
        isGoogleClick.postValue(true)
    }

}