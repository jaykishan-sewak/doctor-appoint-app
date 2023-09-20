package com.android.doctorapp.ui.userdashboard.userfragment

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AppointmentRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class UserRequestViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val appointmentRepository: AppointmentRepository,
    private val session: Session,
    private val context: Context
) : BaseViewModel() {

    val userAppointmentData = MutableLiveData<List<AppointmentModel>>()
    val isDoctorRequestCalendar: MutableLiveData<Boolean> = MutableLiveData(false)
    var requestSelectedDate: MutableLiveData<Date> = SingleLiveEvent()
    val dataFound : MutableLiveData<Boolean> = MutableLiveData(false)

    fun getRequestAppointmentList() {
        viewModelScope.launch {
            var recordId: String = ""
            session.getString(USER_ID).collectLatest {
                recordId = it.orEmpty()
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response =
                        appointmentRepository.getBookAppointmentDetailsList(
                            recordId, fireStore
                        )) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            userAppointmentData.value = response.body!!
                            Log.d( "List------", Gson().toJson(response.body))
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
}