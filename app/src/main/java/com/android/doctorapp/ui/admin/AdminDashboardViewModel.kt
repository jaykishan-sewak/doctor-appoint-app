package com.android.doctorapp.ui.admin

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AdminRepository
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import com.google.gson.Gson
import kotlinx.coroutines.launch
import javax.inject.Inject


class AdminDashboardViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val adminRepository: AdminRepository,
    private val context: Context

) : BaseViewModel() {
    private val items = SingleLiveEvent<List<UserDataResponseModel>>()
    val doctorList = items.asLiveData()

    val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()

    fun getItems() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = adminRepository.getDoctorList(fireStore)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        if (response.body.isNotEmpty()) {
                            items.value = response.body!!
                            Log.d("Data----", Gson().toJson(response.body))
                        }
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

    fun deleteDoctor(id: String, index: Int) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = adminRepository.deleteDoctor(fireStore, id)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        if (response.body) {
                            val currentList = items.value?.toMutableList() ?: mutableListOf()
                            if (index in 0 until currentList.size) {
                                currentList.removeAt(index)
                                items.postValue(currentList)
                            }
                            Log.d("Data----", "${response.body}")
                        }
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

    fun addDoctor() {
        _navigationListener.value = R.id.admin_to_add_doctor
    }

}