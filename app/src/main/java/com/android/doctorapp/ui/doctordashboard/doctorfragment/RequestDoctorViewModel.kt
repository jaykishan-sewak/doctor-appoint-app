package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.content.Context
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
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_DATE
import com.android.doctorapp.util.extension.getCurrentDate
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class RequestDoctorViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val appointmentRepository: AppointmentRepository,
    private val context: Context,
    val session: Session,

    ) : BaseViewModel() {

    val requestAppointmentList = MutableLiveData<List<AppointmentModel>?>()
    var requestSelectedDate: MutableLiveData<Date> = SingleLiveEvent()
    val isRequestCalender: MutableLiveData<Boolean> = MutableLiveData(false)
    val dataFound: MutableLiveData<Boolean> = MutableLiveData(false)
    val startDate: MutableLiveData<Date?> = MutableLiveData()
    val endDate: MutableLiveData<Date?> = MutableLiveData()
    private val currentDate: String = getCurrentDate()
    val rangeDate: MutableLiveData<String> = MutableLiveData()
    val isDarkThemeEnable: MutableLiveData<Boolean?> = MutableLiveData(false)

    fun getRequestAppointmentList() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                session.getString(USER_ID).collectLatest {
                    if (it?.isNotEmpty() == true) {
                        if (startDate.value == null || endDate.value == null) {
                            startDate.value =
                                SimpleDateFormat(FORMATTED_DATE, Locale.getDefault()).parse(
                                    currentDate
                                )
                            val nextDate = Calendar.getInstance()
                            nextDate.time = startDate.value!!
                            nextDate.add(Calendar.DATE, 1)
                            endDate.value = nextDate.time
                        }
                        dataFound.value = true
                        setShowProgress(true)
                        when (val response =
                            appointmentRepository.getAppointmentsProgressList(
                                it,
                                startDate.value!!,
                                endDate.value!!,
                                fireStore
                            )) {
                            is ApiSuccessResponse -> {
                                setShowProgress(false)
                                requestAppointmentList.value = response.body
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
            } else {
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
            }
        }
    }
}