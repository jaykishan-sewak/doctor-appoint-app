package com.android.doctorapp.ui.doctor

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AuthRepository
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.DegreeResponseModel
import com.android.doctorapp.repository.models.SpecializationResponseModel
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isEmailAddressValid
import com.google.gson.Gson
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddDoctorViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    val TAG = AddDoctorViewModel::class.java.simpleName

    val doctorName: MutableLiveData<String?> = MutableLiveData()
    val doctorNameError: MutableLiveData<String?> = MutableLiveData()

    val doctorEmail: MutableLiveData<String> = MutableLiveData()
    val doctorEmailError: MutableLiveData<String?> = MutableLiveData()

    val doctorContactNumber: MutableLiveData<String> = MutableLiveData()
    val doctorContactNumberError: MutableLiveData<String?> = MutableLiveData()

    val toggleLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    val isDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()

    private val _addDoctorResponse = SingleLiveEvent<String>()
    val addDoctorResponse = _addDoctorResponse.asLiveData()

    private val _clickResponse: MutableLiveData<String> = MutableLiveData()
    val clickResponse = _clickResponse.asLiveData()
    private val degreeItems = SingleLiveEvent<DegreeResponseModel?>()
    val degreeList = degreeItems.asLiveData()
    private val specializationItems = SingleLiveEvent<SpecializationResponseModel?>()
    val specializationList = specializationItems.asLiveData()


    private fun validateAllField() {
        isDataValid.value = (!doctorName.value.isNullOrEmpty() && !doctorEmail.value.isNullOrEmpty()
                && !doctorContactNumber.value.isNullOrEmpty() && doctorNameError.value.isNullOrEmpty()
                && doctorEmailError.value.isNullOrEmpty() && doctorContactNumberError.value.isNullOrEmpty())
    }

    fun isValidName(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            doctorNameError.value = resourceProvider.getString(R.string.valid_name_desc)
        } else if (text?.get(0)?.isLetter() != true) {
            doctorNameError.value = resourceProvider.getString(R.string.valid_name_start_with_char)
        } else {
            doctorNameError.value = null
        }
        validateAllField()
    }

    fun isValidEmail(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || text?.toString()?.isEmailAddressValid()
                ?.not() == true
        ) {
            doctorEmailError.value = resourceProvider.getString(R.string.enter_valid_email)
        } else {
            doctorEmailError.value = null
        }
        validateAllField()
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
        validateAllField()
    }


    fun addDoctorData() {
        firebaseUser = firebaseAuth.currentUser!!
        if (firebaseUser != null) {
            // when firebaseUser is not null then

            viewModelScope.launch {
                setShowProgress(true)

                when (val response = authRepository.register(
                    firebaseAuth,
                    email = doctorEmail.value!!,
                    password = "Admin@123",
                )) {

                    is ApiSuccessResponse -> {
                        if (!firebaseAuth.currentUser?.uid.isNullOrEmpty()) {
                            addUserData()
                        }
                    }

                    is ApiErrorResponse -> {
                        Log.d(TAG, "addDoctorData: ${response.errorMessage}")
                        _addDoctorResponse.value = response.errorMessage
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        Log.d(TAG, "addDoctorData: ${response.errorMessage}")
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
            email = doctorEmail.value!!,
            name = doctorName.value!!,
            contactNumber = doctorContactNumber.value!!,
            isNotificationEnable = toggleLiveData.value == true
        )


        when (val response = authRepository.addDoctorData(userData, fireStore)) {

            is ApiSuccessResponse -> {
                if (response.body.userId.isNotEmpty()) {
                    doctorName.value = ""
                    doctorEmail.value = ""
                    doctorContactNumber.value = ""
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

    fun contactVerify() {
        if (!doctorContactNumber.value.isNullOrEmpty()) {
            _clickResponse.value = doctorContactNumber.value.toString()
        } else {
        }
    }

    fun getDegreeItems() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.getDegreeList(fireStore)) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    degreeItems.value = response.body
                    Log.d("Data----", Gson().toJson(response.body))
                }

                is ApiErrorResponse -> {
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
                }
            }
        }
    }

    fun getSpecializationItems() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.getSpecializationList(fireStore)) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    specializationItems.value = response.body
                    Log.d("Data----", Gson().toJson(response.body))
                }

                is ApiErrorResponse -> {
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
                }
            }
        }
    }

    fun addDegreeItems(data:String) {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.addDegree(fireStore,data)) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    Log.d("Add Data----", Gson().toJson(response.body))
                }

                is ApiErrorResponse -> {
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
                }
            }
        }
    }

    fun addSpecializationItems(data:String) {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.addSpecialization(fireStore,data)) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    Log.d("Add Data----", Gson().toJson(response.body))
                }

                is ApiErrorResponse -> {
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
                }
            }
        }
    }

}