package com.android.doctorapp.ui.doctor

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AddDoctorRepository
import com.android.doctorapp.repository.AuthRepository
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isEmailAddressValid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddDoctorViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val addRepository: AddDoctorRepository
) : BaseViewModel() {

    private val TAG: String = AddDoctorViewModel::class.java.simpleName

    private val doctorName: MutableLiveData<String?> = MutableLiveData()
    val doctorNameError: MutableLiveData<String?> = MutableLiveData()
    val doctorNameErrorTrue: MutableLiveData<Boolean> = MutableLiveData()

    private val doctorEmail: MutableLiveData<String> = MutableLiveData()
    val doctorEmailError: MutableLiveData<String?> = MutableLiveData()
    val doctorEmailErrorTrue: MutableLiveData<Boolean> = MutableLiveData()

    private val doctorContactNumber: MutableLiveData<String> = MutableLiveData()
    val doctorContactNumberError: MutableLiveData<String?> = MutableLiveData()
    val doctorContactNumberErrorTrue: MutableLiveData<Boolean> = MutableLiveData()

    val toggleLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private lateinit var firebaseAuth: FirebaseAuth

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()

    fun isValidateName(text: CharSequence?) {
        doctorNameError.postValue(null)
        doctorNameErrorTrue.postValue(false)
        if (text?.toString().isNullOrEmpty()) {
            doctorNameErrorTrue.postValue(false)
            doctorNameError.postValue(resourceProvider.getString(R.string.valid_name_desc))
        } else if ((text?.toString()?.length!! >= 1) && ((text?.toString()?.length ?: 0) < 3)) {
            doctorNameErrorTrue.postValue(false)
            doctorNameError.postValue(resourceProvider.getString(R.string.valid_name_desc))
        } else {
            doctorNameError.postValue(null)
            doctorNameErrorTrue.postValue(true)
            doctorName.postValue(text.toString())
        }
    }

    fun isValidEmail(text: CharSequence?) {
        doctorEmailError.postValue(null)
        if (text?.toString().isNullOrEmpty()) {
            doctorEmailErrorTrue.postValue(false)
            doctorEmailError.postValue(resourceProvider.getString(R.string.enter_valid_email))
        } else if (text?.toString()?.isEmailAddressValid()?.not()!!) {
            doctorEmailError.postValue(resourceProvider.getString(R.string.enter_valid_email))
            doctorEmailErrorTrue.postValue(false)
        } else {
            doctorEmailError.postValue(null)
            doctorEmailErrorTrue.postValue(true)
            doctorEmail.postValue(text.toString())
        }
    }

    fun isValidContact(text: CharSequence?) {
        doctorContactNumberError.postValue(null)
        if (text?.toString().isNullOrEmpty()) {
            doctorContactNumberErrorTrue.postValue(false)
            doctorContactNumberError.postValue(resourceProvider.getString(R.string.error_valid_phone_number))
        } else {
            if (Patterns.PHONE.matcher(text?.toString()).matches()) {
                if (text?.toString()?.length == 10) {
                    doctorContactNumberErrorTrue.postValue(true)
                    doctorContactNumberError.postValue(null)
                    doctorContactNumber.postValue(text.toString())
                } else {
                    doctorContactNumberErrorTrue.postValue(false)
                    doctorContactNumberError.postValue(resourceProvider.getString(R.string.error_valid_phone_number))
                }
            } else {
                doctorContactNumberErrorTrue.postValue(false)
                doctorContactNumberError.postValue(resourceProvider.getString(R.string.error_valid_phone_number))
            }
        }
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

                    is DocumentReference -> {
                        doctorName.value = ""
                        doctorEmail.value = ""
                        doctorContactNumber.value = ""
                        Log.d(TAG, "addDoctorData: DocumentReference")
                        setShowProgress(false)
                         _navigationListener.postValue(R.id.action_loginFragment_to_registerFragment)

                    }

                    is FirebaseFirestoreException -> {
                        Log.d(TAG, "addDoctorData: ${response.fillInStackTrace()}")
                        setShowProgress(false)
                    }

                    else -> {
                        Log.d(TAG, "addDoctorData: else")
                        setShowProgress(false)
                    }
                }


            }
        }
    }
}