package com.android.doctorapp.ui.userdashboard.userfragment

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AppointmentRepository
import com.android.doctorapp.repository.ItemsRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.DataRequestModel
import com.android.doctorapp.repository.models.NotificationRequestModel
import com.android.doctorapp.util.constants.ConstantKey.APPOINTMENT_REJECTED_BY
import com.android.doctorapp.util.constants.ConstantKey.FIELD_REJECTED
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.currentDate
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookingDetailViewModel @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val resourceProvider: ResourceProvider,
    private val appointmentRepository: AppointmentRepository,
    val session: Session,
    private val context: Context
) : BaseViewModel() {

    var appointmentObj = MutableLiveData<AppointmentModel?>()
    var cancelClick = MutableLiveData(false)
    val imageUri = MutableLiveData<Uri?>()
    var selectedTab: MutableLiveData<String> = MutableLiveData()
    val documentId: MutableLiveData<String?> = MutableLiveData()

    private val _navigationListener: MutableLiveData<Boolean> = MutableLiveData(false)
    val navigationListener = _navigationListener.asLiveData()
    val isCancelEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDarkThemeEnable: MutableLiveData<Boolean?> = MutableLiveData(false)

    fun checkAppointmentDate(): Boolean {
        val currentDate = currentDate()
        return if (appointmentObj.value != null)
            appointmentObj.value?.bookingDateTime!! > currentDate && appointmentObj.value?.status != FIELD_REJECTED
        else
            false
    }


    fun appointmentRejectApiCall(text: String) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = appointmentRepository.updateAppointmentDataById(
                    appointmentObj.value!!.apply {
                        reason = text
                        status = FIELD_REJECTED
                    }, fireStore
                )) {
                    is ApiSuccessResponse -> {
                        if (appointmentObj.value?.doctorDetails?.isNotificationEnable == true) sendRejectedNotification(
                            APPOINTMENT_REJECTED_BY, appointmentObj.value!!.id
                        )
                        else {
                            setShowProgress(false)
                            _navigationListener.value = true
                        }
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

    private fun sendRejectedNotification(msg: String, documentId: String) {
        viewModelScope.launch {
            setShowProgress(true)
            val data = DataRequestModel(
                " $msg ${appointmentObj.value?.name}",
                "Appointment",
                appointmentObj.value?.doctorDetails?.isDoctor, documentId
            )
            val notificationRequest =
                NotificationRequestModel(appointmentObj.value?.doctorDetails?.token!!, data)
            when (val response = itemsRepository.sendNotification(notificationRequest)) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    _navigationListener.value = true
                }

                is ApiErrorResponse -> {
                    setShowProgress(false)
                    setApiError(response.errorMessage)
                    context.toast(response.errorMessage)
                }

                is ApiNoNetworkResponse -> {
                    setShowProgress(false)
                    setNoNetworkError(response.errorMessage)
                    context.toast(response.errorMessage)
                }

                else -> {}
            }
            setShowProgress(false)
        }
    }

    fun getAppointmentDetails() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response =
                    appointmentRepository.getAppointmentDetails(
                        documentId.value!!,
                        fireStore
                    )) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        appointmentObj.value = response.body
                        isCancelEnabled.value = checkAppointmentDate()
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


}