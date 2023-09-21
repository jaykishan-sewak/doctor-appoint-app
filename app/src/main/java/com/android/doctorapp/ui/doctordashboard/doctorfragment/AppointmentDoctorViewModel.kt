package com.android.doctorapp.ui.doctordashboard.doctorfragment

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
import com.android.doctorapp.repository.models.Header
import com.android.doctorapp.util.constants.ConstantKey.DATE_MM_FORMAT
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class AppointmentDoctorViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val appointmentRepository: AppointmentRepository,
    private val context: Context,
) : BaseViewModel() {

    val finalAppointmentList = MutableLiveData<List<Any>>()
    private val mainList = mutableListOf<Any>()
    private val appointmentList = MutableLiveData<List<AppointmentModel>>()
    private val sortedAppointmentList = MutableLiveData<List<AppointmentModel>>()

    val dataFound: MutableLiveData<Boolean> = MutableLiveData(false)


    init {
        getAppointmentList()
    }

    private fun addData() {
        var count = 0
        sortedAppointmentList.value?.forEachIndexed { index, appointmentModel ->
            if (mainList.isNotEmpty() && mainList.contains(
                    Header(
                        SimpleDateFormat(DATE_MM_FORMAT, Locale.getDefault()).parse(
                            dateFormatter(appointmentModel.bookingDateTime!!, DATE_MM_FORMAT)
                        )
                    )
                )
            ) {
                count++
                if (count < 2)
                    mainList.add(
                        AppointmentModel(
                            appointmentModel.id,
                            appointmentModel.bookingDateTime,
                            appointmentModel.isOnline,
                            appointmentModel.reason,
                            appointmentModel.status,
                            appointmentModel.userId,
                            appointmentModel.name,
                            appointmentModel.contactNumber,
                            appointmentModel.age
                        )
                    )

            } else {
                mainList.add(
                    Header(
                        SimpleDateFormat(DATE_MM_FORMAT, Locale.getDefault()).parse(
                            dateFormatter(appointmentModel.bookingDateTime!!, DATE_MM_FORMAT)
                        )
                    )
                )
                mainList.add(
                    AppointmentModel(
                        appointmentModel.id,
                        appointmentModel.bookingDateTime,
                        appointmentModel.isOnline,
                        appointmentModel.reason,
                        appointmentModel.status,
                        appointmentModel.userId,
                        appointmentModel.name,
                        appointmentModel.contactNumber,
                        appointmentModel.age
                    )
                )
                count = 0
            }

        }
        finalAppointmentList.value = mainList
    }

    private fun getAppointmentList() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = appointmentRepository.getAppointmentsList(fireStore)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        if (response.body.isNotEmpty()) {
                            appointmentList.value = response.body!!
                            sortedAppointmentList.value =
                                appointmentList.value!!.sortedByDescending {
                                    it.bookingDateTime
                                }
                            addData()
                        }
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
                        context.toast(context.getString(R.string.something_went_wrong))
                        setShowProgress(false)
                    }
                }
            } else {
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
            }
        }
    }

}


