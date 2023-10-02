package com.android.doctorapp.ui.userdashboard.userfragment

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AppointmentRepository
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.FIELD_REJECTED
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.currentDate
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookingDetailViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val appointmentRepository: AppointmentRepository,
    private val context: Context
) : BaseViewModel() {

    var appointmentObj = MutableLiveData<AppointmentModel>()
    var cancelClick = MutableLiveData(false)

    private val _navigationListener: MutableLiveData<Boolean> = MutableLiveData(false)
    val navigationListener = _navigationListener.asLiveData()


    fun checkAppointmentDate(): Boolean {
        val currentDate = currentDate()
        return appointmentObj.value?.bookingDateTime!! > currentDate && appointmentObj.value?.status != FIELD_REJECTED
    }

    fun appointmentRejectApiCall(text: String) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response =
                    appointmentRepository.updateAppointmentDataById(
                        appointmentObj.value!!.apply {
                            reason = text
                            status = FIELD_REJECTED
                        },
                        fireStore
                    )) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        _navigationListener.value = true
                    }

                    is ApiErrorResponse -> {
                        context.toast(response.errorMessage)
                        setShowProgress(false)
                        _navigationListener.value = true
                    }

                    is ApiNoNetworkResponse -> {
                        context.toast(response.errorMessage)
                        setShowProgress(false)
                        _navigationListener.value = true
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

    fun onCancelClick() {
        cancelClick.value = true
    }
}