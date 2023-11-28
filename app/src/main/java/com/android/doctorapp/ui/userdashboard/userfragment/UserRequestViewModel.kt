package com.android.doctorapp.ui.userdashboard.userfragment

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
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import com.google.firebase.firestore.DocumentSnapshot
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

    val userAppointmentData: MutableList<AppointmentModel> = mutableListOf()
    val _pastAppointments = MutableLiveData<List<AppointmentModel>?>()
    val pastAppointments: MutableLiveData<List<AppointmentModel>?> get() = _pastAppointments

    val isDoctorRequestCalendar: MutableLiveData<Boolean> = MutableLiveData(false)
    var requestSelectedDate: MutableLiveData<Date> = SingleLiveEvent()
    val dataFound: MutableLiveData<Boolean> = MutableLiveData(false)
    val upcomingOrPast: MutableLiveData<String> = MutableLiveData(ConstantKey.UPCOMING_LABEL)
    var selectedTabPosition: MutableLiveData<Int> = MutableLiveData(0)
    private val toSortAppointmentList = MutableLiveData<List<AppointmentModel>?>()

    var lastDocument: DocumentSnapshot? = null
    val loadingPB: MutableLiveData<Boolean> = MutableLiveData(false)
    var lastAppointment: MutableLiveData<AppointmentModel?> = MutableLiveData()
    val dataLoaded: MutableLiveData<Boolean> = MutableLiveData(false)

    fun getUpcomingAppointmentList() {
        dataFound.value = true
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
                            setShowProgress(false)
                            if (response.body.data.isNotEmpty()) {
                                val newList = response.body.data
                                Log.d(
                                    TAG,
                                    "getUpcomingAppointmentList: ${Gson().toJson(response.body.data)}"
                                )
                                userAppointmentData.addAll(newList)
                                _pastAppointments.value = newList
                                lastDocument = response.body.lastDocument
                                dataLoaded.postValue(true)
                            } else {
                                if (!_pastAppointments.value.isNullOrEmpty()) {
                                    context.toast("That's all the data..")
                                    loadingPB.value = false
                                } else {
                                    _pastAppointments.value = response.body.data
                                    userAppointmentData.addAll(response.body.data)
                                }

                            }

                        }

                        is ApiErrorResponse -> {
                            Log.d(TAG, "getPastAppointmentList: ${response.errorMessage}")
                            context.toast(response.errorMessage)
                            loadingPB.value = false
                            setShowProgress(false)
                        }

                        is ApiNoNetworkResponse -> {
                            context.toast(response.errorMessage)
                            loadingPB.value = false
                            setShowProgress(false)
                        }

                        else -> {
                            context.toast(resourceProvider.getString(R.string.something_went_wrong))
                            setShowProgress(false)
                        }
                    }
//                    }
                } else {
                    context.toast(resourceProvider.getString(R.string.check_internet_connection))
                }
            }
        }
    }

    fun getPastAppointmentList() {
        dataFound.value = true
        viewModelScope.launch {
            var recordId = ""
            session.getString(USER_ID).collectLatest {
                recordId = it.orEmpty()
                if (context.isNetworkAvailable()) {
//                    if (lastAppointment.value?.id == lastDocument?.id) {
//                        // checking if the page number is greater than limit.
//                        // displaying toast message in this case when page>limit.
//                        context.toast("That's all the data..")
//                        // hiding our progress bar.
//                        loadingPB.value = false
//                    } else {
                    when (val response =
                        appointmentRepository.getPastBookAppointmentDetailsList(
                            recordId,
                            fireStore,
                            lastDocument
                        )) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
//                                toSortAppointmentList.value = response.body.data
                            if (response.body.data.isNotEmpty()) {
                                val newList = response.body.data
                                Log.d(
                                    TAG,
                                    "getPastAppointmentList: ${Gson().toJson(response.body.data)}"
                                )
//                                _pastAppointments.value =
//                                    toSortAppointmentList.value!!.sortedByDescending {
//                                        it.bookingDateTime
//                                    }
                                userAppointmentData.addAll(newList)
                                _pastAppointments.value = newList
                                lastDocument = response.body.lastDocument
                                dataLoaded.postValue(true)
//                            userAppointmentData.value = toSortAppointmentList.value!!.sortedByDescending {
//                                it.bookingDateTime
//                            }
                            } else {
                                if (!_pastAppointments.value.isNullOrEmpty()) {
                                    context.toast("That's all the data..")
                                    loadingPB.value = false
                                } else {
                                    _pastAppointments.value = response.body.data
                                    userAppointmentData.addAll(response.body.data)
                                }
                            }
                        }

                        is ApiErrorResponse -> {
                            Log.d(TAG, "getPastAppointmentList: ${response.errorMessage}")
                            context.toast(response.errorMessage)
                            loadingPB.value = false
                            setShowProgress(false)
                        }

                        is ApiNoNetworkResponse -> {
                            context.toast(response.errorMessage)
                            loadingPB.value = false
                            setShowProgress(false)
                        }

                        else -> {
                            context.toast(resourceProvider.getString(R.string.something_went_wrong))
                            setShowProgress(false)
                        }
                    }
//                    }
                } else {
                    context.toast(resourceProvider.getString(R.string.check_internet_connection))
                }
            }
        }
    }

    fun getLastPastAppointment() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                session.getString(USER_ID).collectLatest {
                    if (it?.isNotEmpty() == true) {
                        setShowProgress(true)
                        when (val response =
                            appointmentRepository.getLastElementOfPastList(
                                it,
                                fireStore
                            )) {
                            is ApiSuccessResponse -> {
                                setShowProgress(false)
                                lastAppointment.value = response.body
                                Log.d(TAG, "getLastPastAppointment: ${response.body}")
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