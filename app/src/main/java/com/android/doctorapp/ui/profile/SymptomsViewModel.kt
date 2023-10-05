package com.android.doctorapp.ui.profile

import android.content.Context
import android.view.View
import android.widget.RadioGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.ProfileRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.SymptomModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.convertDateToFull
import com.android.doctorapp.util.extension.hideKeyboard
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class SymptomsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val profileRepository: ProfileRepository,
    private val context: Context,
    private val session: Session
) : BaseViewModel() {

    val selectYesOrNo: MutableLiveData<Boolean> =
        MutableLiveData(false)
    val isCalender: MutableLiveData<View> = SingleLiveEvent()
    val lastVisitDate: MutableLiveData<String> = MutableLiveData()
    val lastVisitTime: MutableLiveData<String> = MutableLiveData()
    val symptomDetails: MutableLiveData<String> = MutableLiveData()
    val numberOfDays: MutableLiveData<String> = MutableLiveData()
    val consultDoctorName: MutableLiveData<String> = MutableLiveData()

    var isUpdateDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    val symptomDetailsError: MutableLiveData<String?> = MutableLiveData()
    val numberOfDaysError: MutableLiveData<String?> = MutableLiveData()
    private val consultDoctorNameError: MutableLiveData<String?> = MutableLiveData()
    private val lastVisitDateError: MutableLiveData<String?> = MutableLiveData()
    private val lastVisitTimeError: MutableLiveData<String?> = MutableLiveData()
    var doctorObj: MutableLiveData<UserDataResponseModel>? = MutableLiveData()

    var doctorList = MutableLiveData<List<UserDataResponseModel>?>()

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()


    fun consultOrNOt(group: RadioGroup, checkedId: Int) {
        selectYesOrNo.value = R.id.radioButtonNo != checkedId
        isUpdateDataValid.value = !selectYesOrNo.value!!
    }

    fun calenderClick(textLastVisitDate: View) {
        isCalender.value = textLastVisitDate
        textLastVisitDate.hideKeyboard()
    }

    fun onSubmit() {
        updateUser()
    }

    private fun updateUser() {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                //Here Code for User Update
                val userData = SymptomModel(
                    id = "",
                    doctorName = consultDoctorName.value.toString(),
                    userId = it!!,
                    lastVisitDay = convertDateToFull(lastVisitDate.value.toString()),
                    lastPrescription = lastVisitTime.value.toString(),
                    sufferingDay = numberOfDays.value.toString(),
                    doctorId = doctorObj?.value?.userId,
                    symptomDetails = symptomDetails.value.toString(),
                )
                setShowProgress(true)
                when (val response =
                    profileRepository.submitUserSymptomsData(userData, fireStore)) {
                    is ApiSuccessResponse -> {
                        if (response.body.userId.isNotEmpty()) {
                            consultDoctorName.value = ""
                            lastVisitDate.value = ""
                            lastVisitTime.value = ""
                            numberOfDays.value = ""
                            symptomDetails.value = ""
                            setShowProgress(false)
                            _navigationListener.value = R.id.navigation_user_profile
                        }
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

    fun getDoctorList() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = profileRepository.getDoctorList(fireStore)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        if (response.body.isNotEmpty()) {
                            doctorList.value = response.body
                        }
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
            } else
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    private fun validatesAllFields(): Boolean {
        if (selectYesOrNo.value!!) {
            isUpdateDataValid.value =
                (!symptomDetails.value.isNullOrEmpty() && !numberOfDays.value.isNullOrEmpty()
                        && !consultDoctorName.value.isNullOrEmpty() && symptomDetailsError.value.isNullOrEmpty()
                        && numberOfDaysError.value.isNullOrEmpty() && consultDoctorNameError.value.isNullOrEmpty()
                        && !lastVisitDate.value.isNullOrEmpty() && lastVisitDateError.value.isNullOrEmpty()
                        && !lastVisitTime.value.isNullOrEmpty() && lastVisitTimeError.value.isNullOrEmpty()
                        )
        } else {
            isUpdateDataValid.value =
                (!symptomDetails.value.isNullOrEmpty() && !numberOfDays.value.isNullOrEmpty()
                        && symptomDetailsError.value.isNullOrEmpty()
                        && numberOfDaysError.value.isNullOrEmpty()
                        )
        }
        return isUpdateDataValid.value!!
    }

    fun isValidSymptomDetails(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            symptomDetailsError.value = resourceProvider.getString(R.string.valid_symptom_desc)
        } else if (text?.get(0)?.isLetter() != true) {
            symptomDetailsError.value =
                resourceProvider.getString(R.string.valid_symptom_start_with_char)
        } else {
            symptomDetailsError.value = null
        }
        validatesAllFields()
    }

    fun isValidNumberOfDays(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            numberOfDaysError.value = resourceProvider.getString(R.string.valid_number_of_days_desc)
        } else {
            numberOfDaysError.value = null
        }
        validatesAllFields()
    }

}