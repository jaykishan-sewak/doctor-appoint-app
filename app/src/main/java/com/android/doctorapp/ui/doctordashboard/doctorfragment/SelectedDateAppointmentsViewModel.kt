package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.content.Context
import android.util.Log
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
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class SelectedDateAppointmentsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val appointmentRepository: AppointmentRepository,
    private val context: Context,
) : BaseViewModel() {


    val appointmentList = MutableLiveData<List<AppointmentModel>?>()
    var selectedDate: MutableLiveData<Date> = SingleLiveEvent()
    val isCalender: MutableLiveData<Boolean> = MutableLiveData(false)
    val dataFound: MutableLiveData<Boolean> = MutableLiveData(false)


    fun getAppointmentList() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                appointmentList.value = emptyList()
                setShowProgress(true)
                when (val response =
                    appointmentRepository.getAppointmentsSelectedDateList(
                        selectedDate.value!!, fireStore
                    )) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        appointmentList.value = response.body
                    }

                    is ApiErrorResponse -> {
                        Log.d("TAG", "getAppointmentList: ${response.errorMessage}")
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        setShowProgress(false)
                    }

                    else -> {
                        setShowProgress(false)
                    }
                }
            } else {
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
            }
        }
    }


}