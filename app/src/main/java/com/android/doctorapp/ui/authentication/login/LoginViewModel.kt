package com.android.doctorapp.ui.authentication.login


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
import com.android.doctorapp.repository.models.LoginResponseModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isEmailAddressValid
import com.android.doctorapp.util.extension.isPassWordValid
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
    context: Context
) : BaseViewModel() {
    private val _loginResponse = SingleLiveEvent<LoginResponseModel?>()
    val loginResponse = _loginResponse.asLiveData()

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

    val signInAccountTask: MutableLiveData<Task<GoogleSignInAccount>> = MutableLiveData()
    val googleSignInAccount: MutableLiveData<GoogleSignInAccount> = MutableLiveData()
    val authCredential: MutableLiveData<AuthCredential> = MutableLiveData()





    init {
        // googleSignInOptions initialization
        googleInitialization(context)
        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    }


    fun onClick() {
            callLoginAPI()
    }

    /**
     *  Validate the user input fields.
     */
    private fun isAllValidate(){
        isDataValid.value = (!email.value.isNullOrEmpty() && !password.value.isNullOrEmpty()
                && emailError.value.isNullOrEmpty() && passwordError.value.isNullOrEmpty())
    }

    fun isValidateEmail(text: CharSequence) {
        if (text.toString().isEmpty() || text.toString().isEmailAddressValid().not()) {
            emailError.postValue(resourceProvider.getString(R.string.enter_valid_email))
        }
        else {
            emailError.postValue(null)
        }
        isAllValidate()
    }

    fun isValidPassword(text: CharSequence){
        if (text.toString().isEmpty() || text.toString().isPassWordValid().not()) {
            passwordError.postValue(resourceProvider.getString(R.string.error_enter_password))
        }
        else {
            passwordError.postValue(null)
        }
        isAllValidate()
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
                firebaseAuth,
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

    fun callSignInAccountTaskAPI(result: ActivityResult){
        viewModelScope.launch {
            setShowProgress(true)
            when(val response = authRepository.signInAccountTask(
                result,
            )) {
                is  ApiSuccessResponse -> {
                    setShowProgress(false)
                    signInAccountTask.postValue(response.body!!)
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

    fun callGoogleSignInAccountAPI(signInAccountTask: Task<GoogleSignInAccount>){
        viewModelScope.launch {
            setShowProgress(true)
            when(val response = authRepository.googleSignInAccount(
                signInAccountTask,
            )) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    googleSignInAccount.postValue(response.body!!)
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

    fun callAuthCredentialsAPI( idToken: String){
        viewModelScope.launch {
            setShowProgress(true)
            when(val response = authRepository.authCredentials(
                idToken
            )){
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    authCredential.postValue(response.body!!)
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