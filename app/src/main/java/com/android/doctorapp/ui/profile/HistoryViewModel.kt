package com.android.doctorapp.ui.profile

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
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class HistoryViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val appointmentRepository: AppointmentRepository,
    private val context: Context,
    private val session: Session
) : BaseViewModel() {

    val appointmentHistoryList = MutableLiveData<List<AppointmentModel>?>()
    val dataFound: MutableLiveData<Boolean> = MutableLiveData(false)


    init {
        getAppointmentHistoryList()
    }

    private fun getAppointmentHistoryList() {
        dataFound.value = true
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                session.getString(USER_ID).collectLatest {
                    if (it?.isNotEmpty() == true) {
                        setShowProgress(true)
                        when (val response =
                            appointmentRepository.getAppointmentsHistoryList(
                                it, fireStore
                            )) {
                            is ApiSuccessResponse -> {
                                setShowProgress(false)
                                appointmentHistoryList.value = response.body
                                Log.d(
                                    TAG,
                                    "getHistoryAppointmentHistoryList: ${Gson().toJson(response.body)} "
                                )
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