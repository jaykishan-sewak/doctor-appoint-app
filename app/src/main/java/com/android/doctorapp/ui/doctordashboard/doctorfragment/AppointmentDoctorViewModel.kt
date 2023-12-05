package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AppointmentRepository
import com.android.doctorapp.repository.local.IS_NEW_USER_TOKEN
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.local.USER_TOKEN
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.Header
import com.android.doctorapp.util.constants.ConstantKey.DATE_MM_FORMAT
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class AppointmentDoctorViewModel @Inject constructor(
    val resourceProvider: ResourceProvider,
    val appointmentRepository: AppointmentRepository,
    val session: Session,
    val context: Context
) : BaseViewModel() {

    val finalAppointmentList = MutableLiveData<List<Any>>()
    private var mainList = mutableListOf<Any>()
    private val appointmentList = MutableLiveData<List<AppointmentModel>?>()
    private val sortedAppointmentList = MutableLiveData<List<AppointmentModel>>()

    val dataFound: MutableLiveData<Boolean> = MutableLiveData(false)
    private val fcmToken: MutableLiveData<String?> = MutableLiveData()
    val isDarkThemeEnable: MutableLiveData<Boolean?> = MutableLiveData(false)


    init {
        viewModelScope.launch {
            isTokenEmpty()
        }
    }

    private suspend fun isTokenEmpty() {
        session.getString(USER_TOKEN).collectLatest {
            if (it.isNullOrEmpty()) {
                firebaseToken()
            } else
                if (appointmentList.value == null)
                    getAppointmentList()
        }
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
                if (count < 2) mainList.add(
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

    fun getAppointmentList() {
        dataFound.value = true
        mainList = mutableListOf()
        appointmentList.value = emptyList()
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                val userId = session.getString(USER_ID).firstOrNull()
                if (!userId.isNullOrEmpty()) {
                    setShowProgress(true)
                    when (val response =
                        appointmentRepository.getAppointmentsList(userId, fireStore)) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            if (response.body.isNotEmpty()) {
                                appointmentList.value = response.body
                                sortedAppointmentList.value =
                                    appointmentList.value!!.sortedByDescending { it1 ->
                                        it1.bookingDateTime
                                    }
                                addData()
                            } else dataFound.value = false
                        }

                        is ApiErrorResponse -> {
                            context.toast(response.errorMessage)
                            setShowProgress(false)
                        }

                        is ApiNoNetworkResponse -> {
                            setShowProgress(false)
                        }

                        else -> {
                            context.toast(context.getString(R.string.something_went_wrong))
                            setShowProgress(false)
                        }
                    }
                }

            } else {
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
            }
        }
    }

    private fun firebaseToken() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = appointmentRepository.fetchFCMToken()) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        if (response.body.isNotEmpty()) {
                            updateUserData(
                                response.body
                            )
                            session.putString(USER_TOKEN, response.body)
                            fcmToken.value = response.body
                        }

                    }

                    is ApiErrorResponse -> {
                        context.toast(response.errorMessage)
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        setShowProgress(false)
                    }

                    else -> {
                        setShowProgress(false)
                    }
                }
            } else context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    fun updateUserData(token: String?) {
        viewModelScope.launch {
            val userId = session.getString(USER_ID).firstOrNull()
            if (!userId.isNullOrEmpty()) {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response =
                        appointmentRepository.updateUserData(token, userId, fireStore)) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            session.putBoolean(IS_NEW_USER_TOKEN, false)
                        }

                        is ApiErrorResponse -> {
                            context.toast(response.errorMessage)
                            setShowProgress(false)
                        }

                        is ApiNoNetworkResponse -> {
                            setShowProgress(false)
                        }

                        else -> {
                            setShowProgress(false)
                        }
                    }
                } else context.toast(resourceProvider.getString(R.string.check_internet_connection))
            }
        }
    }


}


