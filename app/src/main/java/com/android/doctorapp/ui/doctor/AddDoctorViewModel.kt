package com.android.doctorapp.ui.doctor

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AddDoctorRepository
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isEmailAddressValid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddDoctorViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val addRepository: AddDoctorRepository
) : BaseViewModel() {

    val doctorName: MutableLiveData<String?> = MutableLiveData()
    val doctorNameError: MutableLiveData<String?> = MutableLiveData()

    val doctorEmail: MutableLiveData<String> = MutableLiveData()
    val doctorEmailError: MutableLiveData<String?> = MutableLiveData()

    val doctorContactNumber: MutableLiveData<String> = MutableLiveData()
    val doctorContactNumberError: MutableLiveData<String?> = MutableLiveData()

    val toggleLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    private lateinit var firebaseAuth: FirebaseAuth
    val isDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()

    private val _addDoctorResponse = SingleLiveEvent<String>()
    val addDoctorResponse = _addDoctorResponse.asLiveData()

    private fun isAllValidate() {
        isDataValid.value = (!doctorName.value.isNullOrEmpty() && !doctorEmail.value.isNullOrEmpty()
                && !doctorContactNumber.value.isNullOrEmpty() && doctorNameError.value.isNullOrEmpty()
                && doctorEmailError.value.isNullOrEmpty() && doctorContactNumberError.value.isNullOrEmpty())
    }

    fun isValidateName(text: CharSequence?) {
        if (text?.toString()
                .isNullOrEmpty() || ((text?.toString()?.length
                ?: 0) < 3)
        ) {
            doctorNameError.value = resourceProvider.getString(R.string.valid_name_desc)
        } else {
            doctorNameError.value = null

        }
        isAllValidate()
    }

    fun isValidEmail(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || text?.toString()?.isEmailAddressValid()
                ?.not() == true
        ) {
            doctorEmailError.value = resourceProvider.getString(R.string.enter_valid_email)
        } else {
            doctorEmailError.value = null
        }
        isAllValidate()
    }

    fun isValidContact(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            doctorContactNumberError.value =
                resourceProvider.getString(R.string.error_valid_phone_number)
        } else {
            if (Patterns.PHONE.matcher(text ?: "").matches()) {
                if (text?.toString()?.length == 10) {
                    doctorContactNumberError.value = null
                } else {
                    doctorContactNumberError.value =
                        resourceProvider.getString(R.string.error_valid_phone_number)
                }
            } else {
                doctorContactNumberError.value =
                    resourceProvider.getString(R.string.error_valid_phone_number)
            }
        }
        isAllValidate()
    }


    fun addDoctorData() {
        // Initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // Initialize firebase user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            // when firebaseUser is not null then
            val userData = UserDataRequestModel(
                userId = firebaseUser.uid,
                isDoctor = true,
                email = doctorEmail.value,
                name = doctorName.value,
                address = "",
                contactNumber = doctorContactNumber.value,
                degree = "",
                specialities = "",
                availableDays = "",
                isEmailVerified = false,
                isPhoneNumberVerified = false,
                availableTime = "",
                imagesval = "",
                isAdmin = false,
                isNotificationEnable = toggleLiveData.value,
                dob = "",
                isUserVerified = false,
                onlineAvailabilityDateTime = "",
                offlineAvailabilityDateTime = ""
            )

            viewModelScope.launch {
                setShowProgress(true)
                when (val response = addRepository.addDoctorData(userData, firestore)) {

                    is ApiSuccessResponse -> {
                        doctorName.value = ""
                        doctorEmail.value = ""
                        doctorContactNumber.value = ""
                        setShowProgress(false)
                        _navigationListener.value = R.id.action_addDoctorFragment_to_LoginFragment
                        _addDoctorResponse.value = resourceProvider.getString(R.string.success)
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
}