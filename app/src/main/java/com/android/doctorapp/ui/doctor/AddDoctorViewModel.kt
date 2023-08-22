package com.android.doctorapp.ui.doctor

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddDoctorViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val authRepository: AuthRepository,
    private val context: Context,
    private val session: Session

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

    val data = MutableLiveData<List<UserDataRequestModel>>()

    val doctorAddress: MutableLiveData<String> = MutableLiveData()
    val doctorAddressError: MutableLiveData<String?> = MutableLiveData()

    fun getModelUserData(): MutableLiveData<List<UserDataRequestModel>> {
        viewModelScope.launch {
            var recordId: String = ""
//            viewModelScope.launch {
                session.getString(USER_ID).collectLatest {
                    Log.d("TAGTest", "Inside collect            : ${it}")
                    recordId = it.orEmpty()
                    Log.d("TAGTest", "objGetUserData current Id : ${firebaseAuth.currentUser?.uid.toString()}")
                    Log.d("TAGTest", "shared id                 : $recordId")
                    var userObj: UserDataRequestModel
                    if (context.isNetworkAvailable()) {
                        setShowProgress(true)
                        when (val response = authRepository.getRecordById(recordId, fireStore)) {
                            is ApiSuccessResponse -> {
                                Log.d("TAGMy", "objGetUserData: success ${response.body.name}")
                                userObj = UserDataRequestModel(
                                    name = response.body.name,
                                    email = response.body.email,
                                    contactNumber = response.body.contactNumber
                                )
                                data.value = listOf(userObj)
                                setShowProgress(false)
                            }

                            is ApiErrorResponse -> {
                                Log.d("TAGMy", "objGetUserData: apiError")
                                context.toast(response.errorMessage)
                                setShowProgress(false)
                            }

                            is ApiNoNetworkResponse -> {
                                Log.d("TAGMy", "objGetUserData: api no network")
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

    fun isValidAddress(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            doctorAddressError.value = resourceProvider.getString(R.string.valid_address_desc)
        } else {
            doctorAddressError.value = null
        }
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

    suspend fun objGetUserData(): MutableLiveData<List<UserDataRequestModel>> {
        var recordId: String = ""
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                Log.d("TAGTest", "Inside collect            : ${it}")
                recordId = it.orEmpty()

                Log.d(
                    "TAGTest",
                    "objGetUserData current Id : ${firebaseAuth.currentUser?.uid.toString()}"
                )
                Log.d("TAGTest", "shared id                 : $recordId")


                var userObj: UserDataRequestModel
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response = authRepository.getRecordById(recordId, fireStore)) {
                        is ApiSuccessResponse -> {
                            Log.d("TAGMy", "objGetUserData: success ${response.body.name}")
                            userObj = UserDataRequestModel(
                                name = response.body.name,
                                email = response.body.email,
                                contactNumber = response.body.contactNumber
                            )
                            data.value = listOf(userObj)
                            setShowProgress(false)
                        }

                        is ApiErrorResponse -> {
                            Log.d("TAGMy", "objGetUserData: apiError")
                            context.toast(response.errorMessage)
                            setShowProgress(false)
                        }

                        is ApiNoNetworkResponse -> {
                            Log.d("TAGMy", "objGetUserData: api no network")
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
                    email = doctorEmail.value!!,
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

    fun hideProgress() {
        setShowProgress(false)
    }

    fun contactVerify() {
        if (!doctorContactNumber.value.isNullOrEmpty()) {
            setShowProgress(true)
            _clickResponse.value = doctorContactNumber.value.toString()
        } else {
        }
    }

    fun ttt() {
        Log.d(TAG, "ttt: ${doctorAddress.value}")
    }

}