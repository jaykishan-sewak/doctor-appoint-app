package com.android.doctorapp.ui.admin

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AdminRepository
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.google.gson.Gson
import kotlinx.coroutines.launch
import javax.inject.Inject


class AdminDashboardViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val adminRepository: AdminRepository

) : BaseViewModel() {
    private val items = SingleLiveEvent<List<UserDataResponseModel>>()
    val doctorList = items.asLiveData()

    fun getItems() {
        viewModelScope.launch {
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
        }
    }

    fun deleteDoctor(id: String, index: Int) {
        viewModelScope.launch {
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
        }

    }
}