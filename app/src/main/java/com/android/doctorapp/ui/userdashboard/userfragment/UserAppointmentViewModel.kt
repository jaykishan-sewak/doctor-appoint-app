package com.android.doctorapp.ui.userdashboard.userfragment

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
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserAppointmentViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val appointmentRepository: AppointmentRepository,
    val session: Session,
    private val context: Context
) : BaseViewModel() {

    private val items = MutableLiveData<List<UserDataResponseModel>?>()
    val doctorList = items.asLiveData()
    val dataFound: MutableLiveData<Boolean> = MutableLiveData(false)

    private val tempData = MutableLiveData<List<UserDataResponseModel>>()
    val locationCity: MutableLiveData<String> =
        MutableLiveData(resourceProvider.getString(R.string.nearest_doctor))
    private val fcmToken: MutableLiveData<String?> = MutableLiveData()


    init {
        viewModelScope.launch {
            isTokenEmpty()
        }
    }

    private suspend fun isTokenEmpty() {
        session.getString(USER_TOKEN).collectLatest {
            if (it.isNullOrEmpty()) {
                firebaseToken()
            }
        }
    }

    fun lengthChecked(text: CharSequence) {
        if (text.toString().length >= 3) {
            searchStarts(text.toString().lowercase())
        } else
            items.value = tempData.value
    }

    private fun searchStarts(text: String) {
        val filterList = doctorList.value?.filter { data ->
            data.name.lowercase().contains(text) || data.gender.lowercase()
                .contains(text) || (data.degree != null && data.degree!!.contains(
                text.uppercase()
            ))
        }
        items.value = filterList!!
    }

    fun getItems(latitude: Double, longitude: Double) {
        setShowProgress(true)
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                when (val response =
                    appointmentRepository.getLatLngDoctorList(fireStore, latitude, longitude)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        if (response.body.isNotEmpty()) {
                            items.value = response.body
                            tempData.value = items.value
                        } else
                            dataFound.value = true
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
            } else
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    private fun firebaseToken() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = appointmentRepository.fetchFCMToken()) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        fcmToken.value = response.body
                        session.putString(USER_TOKEN, response.body)
                        updateUserData(response.body)
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
            } else
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    fun updateUserData(token: String?) {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response =
                        appointmentRepository.updateUserData(token, it, fireStore)) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            session.putBoolean(IS_NEW_USER_TOKEN, false)
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
                } else
                    context.toast(resourceProvider.getString(R.string.check_internet_connection))
            }
        }
    }


}