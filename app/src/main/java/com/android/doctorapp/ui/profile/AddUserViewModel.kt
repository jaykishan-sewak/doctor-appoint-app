package com.android.doctorapp.ui.profile

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AuthRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_IS_EMAIL_VERIFIED
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddUserViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val authRepository: AuthRepository,
    val session: Session
) : BaseViewModel() {

    val isUserDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    val name: MutableLiveData<String> = MutableLiveData()
    val nameError: MutableLiveData<String?> = MutableLiveData()

    val email: MutableLiveData<String> = MutableLiveData()

    val address: MutableLiveData<String> = MutableLiveData()
    val addressError: MutableLiveData<String?> = MutableLiveData()

    val dob: MutableLiveData<String> = MutableLiveData()
    val dobError: MutableLiveData<String?> = MutableLiveData()

    val contactNum: MutableLiveData<String> = MutableLiveData()
    val contactNumError: MutableLiveData<String?> = MutableLiveData()

    val isCalendarShow: MutableLiveData<Boolean> = MutableLiveData(false)

    val isEmailSent: MutableLiveData<Boolean> = MutableLiveData(false)
    val emailVerifyLabel: MutableLiveData<String> = MutableLiveData("Verify")

    val isEmailVerified: MutableLiveData<Boolean> = MutableLiveData(false)
    val isUserReload: MutableLiveData<Boolean> = MutableLiveData(false)
    val contactVerifyLabel: MutableLiveData<String> = MutableLiveData("Verify")

    init {
        fetchEmail()
        firebaseUser = firebaseAuth.currentUser!!
    }

    fun onUpdateClick() {

    }

    fun onEmailVerifyClick() {
        if (!firebaseUser.isEmailVerified) {
            emailVerification()
        }
    }

    fun onContactVerifyClick() {

    }

    private fun fetchEmail() {
        if (firebaseAuth.currentUser?.uid != null) {
            email.value = firebaseAuth.currentUser!!.email
        }
    }

    private fun isAllValidate() {
        isUserDataValid.value = (!name.value.isNullOrEmpty() && !address.value.isNullOrEmpty()
                && !dob.value.isNullOrEmpty() && !contactNum.value.isNullOrEmpty()
                && nameError.value.isNullOrEmpty() && addressError.value.isNullOrEmpty()
                && dobError.value.isNullOrEmpty() && contactNumError.value.isNullOrEmpty())
    }

    fun isValidName(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            nameError.value = resourceProvider.getString(R.string.valid_name_desc)
        } else if (text?.get(0)?.isLetter() != true) {
            nameError.value = resourceProvider.getString(R.string.valid_name_start_with_char)
        } else {
            nameError.value = null
        }
        isAllValidate()
    }

    fun isValidAddress(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            addressError.value = resourceProvider.getString(R.string.valid_address_desc)
        } else {
            addressError.value = null
        }
        isAllValidate()
    }

    fun isValidContact(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            contactNumError.value =
                resourceProvider.getString(R.string.error_valid_phone_number)
        } else {
            if (Patterns.PHONE.matcher(text ?: "").matches()) {
                if (text?.toString()?.length == 10) {
                    contactNumError.value = null
                } else {
                    contactNumError.value =
                        resourceProvider.getString(R.string.error_valid_phone_number)
                }
            } else {
                contactNumError.value =
                    resourceProvider.getString(R.string.error_valid_phone_number)
            }
        }
        isAllValidate()
    }

    fun isValidDob(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            dobError.value = resourceProvider.getString(R.string.valid_dob_desc)
        } else {
            dobError.value = null
        }
        isAllValidate()
    }

    fun onCalenderClick() {
        isCalendarShow.postValue(true)
    }

    private fun emailVerification() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.emailVerification(
                firebaseUser,
            )) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    isEmailSent.postValue(true)
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

    fun emailVerified() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.emailVerified(
                firebaseUser,
            )) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    isEmailVerified.postValue(response.body!!)
                    session.putBoolean(USER_IS_EMAIL_VERIFIED, true)
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

    private fun userReload() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.userReload(
                firebaseUser,
            )) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    isUserReload.postValue(response.body!!)
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

    private fun isEmailVerified() {
//        firebaseAuth.currentUser!!.reload()
//            .addOnCompleteListener { task ->
        userReload()
//                if (task.isSuccessful){
//                    if (firebaseAuth.currentUser?.isEmailVerified == true) {
//                        isEmailVerified.postValue(true)
//                        viewModelScope.launch {
//                            session.putBoolean(USER_IS_EMAIL_VERIFIED, true)
//                        }
//                    }
//                }
        // }
    }

    suspend fun checkIsEmailEveryMin() {
        if (!session.getBoolean(USER_IS_EMAIL_VERIFIED).equals(true)) {
            viewModelScope.launch {
                isEmailVerified()
            }
        }
    }

}

