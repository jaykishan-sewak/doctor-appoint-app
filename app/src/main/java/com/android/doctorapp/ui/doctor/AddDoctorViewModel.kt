package com.android.doctorapp.ui.doctor

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AuthRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.local.USER_IS_EMAIL_VERIFIED
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isEmailAddressValid
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddDoctorViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val authRepository: AuthRepository,
    private val context: Context,
    private val session: Session

) : BaseViewModel() {

    val TAG = AddDoctorViewModel::class.java.simpleName

    val name: MutableLiveData<String?> = MutableLiveData()
    val nameError: MutableLiveData<String?> = MutableLiveData()

    val email: MutableLiveData<String> = MutableLiveData()
    val emailError: MutableLiveData<String?> = MutableLiveData()

    val contactNum: MutableLiveData<String> = MutableLiveData()
    val contactNumError: MutableLiveData<String?> = MutableLiveData()

    val dob: MutableLiveData<String> = MutableLiveData()
    val dobError: MutableLiveData<String?> = MutableLiveData()

    val isUserDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    val address: MutableLiveData<String> = MutableLiveData()
    val addressError: MutableLiveData<String?> = MutableLiveData()

    val toggleLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()

    private val _addDoctorResponse = SingleLiveEvent<String>()
    val addDoctorResponse = _addDoctorResponse.asLiveData()

    private val _clickResponse: MutableLiveData<String> = MutableLiveData()
    val clickResponse = _clickResponse.asLiveData()

    val data = MutableLiveData<List<UserDataRequestModel>>()

    val isEmailSent: MutableLiveData<Boolean> = MutableLiveData(false)
    val emailVerifyLabel: MutableLiveData<String> = MutableLiveData("Verify")

    val isEmailVerified: MutableLiveData<Boolean> = MutableLiveData(false)
    val isUserReload: MutableLiveData<Boolean> = MutableLiveData(false)

    val isPhoneVerify: MutableLiveData<Boolean> = MutableLiveData(false)
    val isPhoneVerifyValue: MutableLiveData<String> = MutableLiveData("Verify")

    val isCalendarShow: MutableLiveData<Boolean> = MutableLiveData(false)

    val isDoctor: MutableLiveData<Boolean> = MutableLiveData(true)


    init {
        firebaseUser = firebaseAuth.currentUser!!
    }

    fun onUpdateClick() {

    }

    fun onEmailVerifyClick() {
        if (!firebaseUser.isEmailVerified) {
            emailVerification()
        }
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
                    Log.d(TAG, "emailVerified: ${response.body}")
                    isEmailVerified.postValue(response.body!!)
                    session.putBoolean(USER_IS_EMAIL_VERIFIED, response.body!!)
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
                    Log.d(TAG, "userReload: ${response.body}")
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
        userReload()
    }

    suspend fun checkIsEmailEveryMin() {
        session.getBoolean(USER_IS_EMAIL_VERIFIED).collectLatest {
            if (it == null || !it) {
                isEmailVerified()
            } else {
                isEmailVerified.postValue(true)
            }

        }
    }

    fun getModelUserData(): MutableLiveData<List<UserDataRequestModel>> {
        viewModelScope.launch {
            var recordId = ""
//            viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                Log.d("TAGTest", "Inside collect: ${it}")
                recordId = it.orEmpty()
                val userObj: UserDataRequestModel
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response = authRepository.getRecordById(recordId, fireStore)) {
                        is ApiSuccessResponse -> {
                            isDoctor.value = response.body.isDoctor
                            userObj = UserDataRequestModel(
                                name = response.body.name,
                                email = response.body.email,
                                contactNumber = response.body.contactNumber
                            )
                            isPhoneVerify.value =
                                !firebaseAuth.currentUser?.phoneNumber.isNullOrEmpty()

                            data.value = listOf(userObj)
                            setShowProgress(false)
                        }

                        is ApiErrorResponse -> {
                            context.toast(response.errorMessage)
                            setShowProgress(false)
                        }

                        is ApiNoNetworkResponse -> {
                            context.toast(response.errorMessage)
                            setShowProgress(false)
                        }

                        else -> {
                            context.toast(resourceProvider.getString(R.string.something_went_wrong))
                            setShowProgress(false)
                        }
                    }
                } else {
                    context.toast(resourceProvider.getString(R.string.check_internet_connection))
                }
            }
//            }

        }
        return data
    }


    private fun validateAllField() {
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
        validateAllField()
    }

    fun isValidAddress(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            addressError.value = resourceProvider.getString(R.string.valid_address_desc)
        } else {
            addressError.value = null
        }
    }

    fun isValidEmail(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || text?.toString()?.isEmailAddressValid()
                ?.not() == true
        ) {
            emailError.value = resourceProvider.getString(R.string.enter_valid_email)
        } else {
            emailError.value = null
        }
        validateAllField()
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
        validateAllField()
    }

    fun isValidDob(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            dobError.value = resourceProvider.getString(R.string.valid_dob_desc)
        } else {
            dobError.value = null
        }
        validateAllField()
    }


    fun addDoctorData() {
        if (context.isNetworkAvailable()) {
            addUserToAuthentication()
        } else {
            context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    private fun addUserToAuthentication() {
        firebaseUser = firebaseAuth.currentUser!!
        if (firebaseUser != null) {
            // when firebaseUser is not null then

            viewModelScope.launch {
                setShowProgress(true)
                when (val response = authRepository.register(
                    firebaseAuth,
                    email = email.value!!,
                    password = "Admin@123",
                )) {

                    is ApiSuccessResponse -> {
                        if (!firebaseAuth.currentUser?.uid.isNullOrEmpty()) {
                            addUserData()
                        }
                    }

                    is ApiErrorResponse -> {
                        _addDoctorResponse.value = response.errorMessage
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        _addDoctorResponse.value = response.errorMessage
                        setShowProgress(false)
                    }

                    else -> {
                        setShowProgress(false)
                    }
                }
            }
        }
    }

    private suspend fun addUserData() {
        val userData = UserDataRequestModel(
            userId = firebaseAuth.currentUser?.uid.toString(),
            isDoctor = true,
            email = email.value!!,
            name = name.value!!,
            contactNumber = contactNum.value!!,
            isNotificationEnable = toggleLiveData.value == true
        )

        when (val response = authRepository.addDoctorData(userData, fireStore)) {
            is ApiSuccessResponse -> {
                if (response.body.userId.isNotEmpty()) {
                    name.value = ""
                    email.value = ""
                    contactNum.value = ""
                    setShowProgress(false)
                    _navigationListener.value = R.id.action_addDoctorFragment_to_LoginFragment
                    _addDoctorResponse.value = resourceProvider.getString(R.string.success)
                }
            }

            is ApiErrorResponse -> {
                _addDoctorResponse.value = response.errorMessage
                setShowProgress(false)
            }

            is ApiNoNetworkResponse -> {
                _addDoctorResponse.value = response.errorMessage
                setShowProgress(false)
            }

            else -> {
                setShowProgress(false)
            }
        }
    }

    /*suspend fun updateUserData() {
        val userData = UserDataRequestModel(
            userId = firebaseAuth.currentUser?.uid.toString(),
            name = name.value!!,
            email = email.value!!,
            address = address.value!!,
            contactNumber = contactNum.value!!,
            dob = SimpleDateFormat("dd/MM/yyyy").parse(dob.value!!),
            isUserVerified = true,
            isEmailVerified = true,
            isPhoneNumberVerified = true
        )
        when (val response = authRepository.updateUserData(userData, fireStore)) {
            is ApiSuccessResponse -> {
                if (response.body.userId.isNotEmpty()) {
                    name.value = ""
                    email.value = ""
                    contactNum.value = ""
                    setShowProgress(false)
                    _navigationListener.value = R.id.action_addDoctorFragment_to_LoginFragment
                    _addDoctorResponse.value = resourceProvider.getString(R.string.success)
                }
            }

            is ApiErrorResponse -> {
                _addDoctorResponse.value = response.errorMessage
                setShowProgress(false)
            }

            is ApiNoNetworkResponse -> {
                _addDoctorResponse.value = response.errorMessage
                setShowProgress(false)
            }

            else -> {
                setShowProgress(false)
            }
        }
    }*/

    fun hideProgress() {
        setShowProgress(false)
    }

    fun contactVerify() {
        if (!contactNum.value.isNullOrEmpty()) {
            setShowProgress(true)
            _clickResponse.value = contactNum.value.toString()
        } else {
        }
    }

}
