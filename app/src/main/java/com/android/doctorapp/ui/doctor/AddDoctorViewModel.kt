package com.android.doctorapp.ui.doctor

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AuthRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
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
import java.text.SimpleDateFormat
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

    val contactNumber: MutableLiveData<String> = MutableLiveData()
    val contactNumberError: MutableLiveData<String?> = MutableLiveData()

    val toggleLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    val isDataValid: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDataValid1: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()

    private val _addDoctorResponse = SingleLiveEvent<String>()
    val addDoctorResponse = _addDoctorResponse.asLiveData()

    private val _clickResponse: MutableLiveData<String> = SingleLiveEvent()
    val clickResponse = _clickResponse.asLiveData()

    val data = MutableLiveData<List<UserDataRequestModel>>()

    val address: MutableLiveData<String> = MutableLiveData()
    val addressError: MutableLiveData<String?> = MutableLiveData()

    val dob: MutableLiveData<String> = MutableLiveData()
    private val dobError: MutableLiveData<String?> = MutableLiveData()

    val isCalender: MutableLiveData<View> = SingleLiveEvent()
    val isAvailableDate: MutableLiveData<String?> = MutableLiveData()
    private val isAvailableDateError: MutableLiveData<String?> = MutableLiveData()

    val isTimeShow: MutableLiveData<Boolean> = MutableLiveData(false)
    val availableTime: MutableLiveData<String> = MutableLiveData()
    private val availableTimeError: MutableLiveData<String?> = MutableLiveData()

    val isPhoneVerify: MutableLiveData<Boolean> = MutableLiveData(true)
    val isPhoneVerifyValue: MutableLiveData<String> = MutableLiveData("Verify")

    val isDoctor: MutableLiveData<Boolean> = MutableLiveData(false)


    fun getModelUserData(): MutableLiveData<List<UserDataRequestModel>> {
        viewModelScope.launch {
            var recordId: String = ""
            session.getString(USER_ID).collectLatest {
                recordId = it.orEmpty()
                var userObj: UserDataRequestModel
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response = authRepository.getRecordById(recordId, fireStore)) {
                        is ApiSuccessResponse -> {
                            userObj = UserDataRequestModel(
                                name = response.body.name,
                                email = response.body.email,
                                contactNumber = response.body.contactNumber
                            )
                            isDoctor.value = response.body.isDoctor
                            if (firebaseAuth.currentUser?.phoneNumber.isNullOrEmpty()) {
                                isPhoneVerify.value = true
                                isPhoneVerifyValue.value = "Verify"
                            } else {
                                isPhoneVerify.value = false
                                isPhoneVerifyValue.value = "Verified"
                            }
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
        }
        return data
    }

    private fun validateAllField() {
        isDataValid.value = (!name.value.isNullOrEmpty() && !email.value.isNullOrEmpty()
                && !contactNumber.value.isNullOrEmpty() && nameError.value.isNullOrEmpty()
                && emailError.value.isNullOrEmpty() && contactNumberError.value.isNullOrEmpty())
    }

    fun validateAllUpdateField() {
        isDataValid1.value = (!name.value.isNullOrEmpty() && !email.value.isNullOrEmpty()
                && !contactNumber.value.isNullOrEmpty() && nameError.value.isNullOrEmpty()
                && emailError.value.isNullOrEmpty() && contactNumberError.value.isNullOrEmpty()
                && !address.value.isNullOrEmpty() && addressError.value.isNullOrEmpty()
                && !dob.value.isNullOrEmpty() && dobError.value.isNullOrEmpty()
                && !isAvailableDate.value.isNullOrEmpty() && isAvailableDateError.value.isNullOrEmpty()
                && !availableTime.value.isNullOrEmpty() && availableTimeError.value.isNullOrEmpty()
                && isPhoneVerify.value == false
                )
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
        validateAllUpdateField()
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
        validateAllUpdateField()
    }

    fun isValidContact(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            contactNumberError.value =
                resourceProvider.getString(R.string.error_valid_phone_number)
        } else {
            if (Patterns.PHONE.matcher(text ?: "").matches()) {
                if (text?.toString()?.length == 10) {
                    contactNumberError.value = null
                } else {
                    contactNumberError.value =
                        resourceProvider.getString(R.string.error_valid_phone_number)
                }
            } else {
                contactNumberError.value =
                    resourceProvider.getString(R.string.error_valid_phone_number)
            }
        }
        validateAllField()
    }

    fun isValidAddress(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            addressError.value = resourceProvider.getString(R.string.valid_address_desc)
        } else {
            addressError.value = null
        }
        validateAllUpdateField()
    }

    fun isValidDob(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            dobError.value = resourceProvider.getString(R.string.valid_dob_desc)
        } else {
            dobError.value = null
        }
        validateAllUpdateField()
    }

    fun isValidDate(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            isAvailableDateError.value = resourceProvider.getString(R.string.valid_date_desc)
        } else {
            isAvailableDateError.value = null
        }
        validateAllUpdateField()
    }

    fun isValidTime(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            availableTimeError.value = resourceProvider.getString(R.string.valid_time_desc)
        } else {
            availableTimeError.value = null
        }
        validateAllUpdateField()
    }

    fun calenderClick(text_dob: View) {
        isCalender.value = text_dob
    }

    fun timeClick() {
        isTimeShow.value = true
    }

    fun addDoctorData() {
        if (context.isNetworkAvailable()) {
            addUserToAuthentication()
        } else {
            context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    fun onUpdateClick() {
        if (context.isNetworkAvailable()) {
            updateUser()
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

    private fun updateUser() {
        val userData: UserDataRequestModel
        if (isDoctor.value == true) {

            var degreeArray: ArrayList<String> = arrayListOf("MS", "B.VSc")
            var specialitiesArray: ArrayList<String> =
                arrayListOf("Physicians", "Gastroenterologists")

            userData = UserDataRequestModel(
                userId = firebaseAuth.currentUser?.uid.toString(),
                isDoctor = true,
                email = email.value.toString(),
                name = name.value.toString(),
                gender = "MALE",
                address = address.value.toString(),
                contactNumber = contactNumber.value.toString(),
                degree = degreeArray,
                specialities = specialitiesArray,
                availableDays = "",
                isEmailVerified = true,
                isPhoneNumberVerified = true,
                availableTime = "",
                isAdmin = false,
                dob = SimpleDateFormat("dd-MM-yyyy").parse(dob.value.toString()),
                isUserVerified = true
            )
        } else {
            //Here Code for User Update
            userData = UserDataRequestModel()
        }

        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.updateUserData(userData, fireStore)) {
                is ApiSuccessResponse -> {
                    if (response.body.userId.isNotEmpty()) {
                        name.value = ""
                        email.value = ""
                        address.value = ""
                        contactNumber.value = ""
                        dob.value = ""
                        isAvailableDate.value = ""
                        availableTime.value = ""
                        setShowProgress(false)
                        _navigationListener.value =
                            R.id.action_updateDoctorFragment_to_LoginFragment
                        _addDoctorResponse.value = resourceProvider.getString(R.string.success)
                    }
                }

                is ApiErrorResponse -> {
                    Log.d(TAG, "updateUser: ${response.errorMessage}")
                    _addDoctorResponse.value = response.errorMessage
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    Log.d(TAG, "updateUser: ${response.errorMessage}")
                    _addDoctorResponse.value = response.errorMessage
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
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
            contactNumber = contactNumber.value!!,
            isNotificationEnable = toggleLiveData.value == true
        )

        when (val response = authRepository.addDoctorData(userData, fireStore)) {
            is ApiSuccessResponse -> {
                if (response.body.userId.isNotEmpty()) {
                    name.value = ""
                    email.value = ""
                    contactNumber.value = ""
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

    fun hideProgress() {
        setShowProgress(false)
    }

    fun contactVerify() {
        if (!contactNumber.value.isNullOrEmpty()) {
            setShowProgress(true)
            _clickResponse.value = contactNumber.value.toString()
        } else {
        }
    }
}