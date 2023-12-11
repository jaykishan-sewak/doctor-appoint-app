package com.android.doctorapp.ui.userdashboard.userfragment

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
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class UserRequestViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val appointmentRepository: AppointmentRepository,
    val session: Session,
    private val context: Context
) : BaseViewModel() {

    val userAppointmentData: MutableList<AppointmentModel> = mutableListOf()
    val _pastAppointments = MutableLiveData<List<AppointmentModel>?>()
    val pastAppointments: MutableLiveData<List<AppointmentModel>?> get() = _pastAppointments

    val isDoctorRequestCalendar: MutableLiveData<Boolean> = MutableLiveData(false)
    var requestSelectedDate: MutableLiveData<Date> = SingleLiveEvent()
    val dataFound: MutableLiveData<Boolean> = MutableLiveData(false)
    val upcomingOrPast: MutableLiveData<String> = MutableLiveData(ConstantKey.UPCOMING_LABEL)
    var selectedTabPosition: MutableLiveData<Int> = MutableLiveData(0)

    var lastDocument: DocumentSnapshot? = null

    fun getUpcomingAppointmentList() {
        viewModelScope.launch {
            var recordId = ""
            session.getString(USER_ID).collectLatest {
                recordId = it.orEmpty()
                if (context.isNetworkAvailable()) {
                    when (val response =
                        appointmentRepository.getUpcomingBookAppointmentDetailsList(
                            recordId,
                            fireStore,
                            lastDocument
                        )) {
                        is ApiSuccessResponse -> {
                            if (response.body.data.isNotEmpty()) {
                                val newList = response.body.data
                                userAppointmentData.addAll(newList)
                                lastDocument = response.body.lastDocument
                                _pastAppointments.value = newList
                            } else {
                                if (userAppointmentData.isEmpty()) {
                                    setShowProgress(false)
                                    dataFound.value = false
                                } else {
                                    _pastAppointments.value = response.body.data
                                    userAppointmentData.addAll(response.body.data)
                                }
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

    fun getPastAppointmentList() {
        viewModelScope.launch {
            var recordId = ""
            session.getString(USER_ID).collectLatest {
                recordId = it.orEmpty()
                if (context.isNetworkAvailable()) {
                    when (val response =
                        appointmentRepository.getPastBookAppointmentDetailsList(
                            recordId,
                            fireStore,
                            lastDocument
                        )) {
                        is ApiSuccessResponse -> {
                            if (response.body.data.isNotEmpty()) {
                                val newList = response.body.data
                                userAppointmentData.addAll(newList)
                                lastDocument = response.body.lastDocument
                                _pastAppointments.value = newList
                            } else {
                                if (userAppointmentData.isEmpty()) {
                                    setShowProgress(false)
                                    dataFound.value = false
                                } else {
                                    _pastAppointments.value = response.body.data
                                    userAppointmentData.addAll(response.body.data)
                                }
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