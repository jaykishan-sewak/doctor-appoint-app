package com.android.doctorapp.ui.profile

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AdminRepository
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import com.google.gson.Gson
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserDashboardViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val adminRepository: AdminRepository,
    private val context: Context

) : BaseViewModel() {

    private val items = MutableLiveData<List<UserDataResponseModel>>()
    val doctorList = items.asLiveData()

    val searchData: MutableLiveData<String> = MutableLiveData()
    private val tempData = MutableLiveData<List<UserDataResponseModel>>()

    fun lengthChecked(text: CharSequence) {
        if (text.toString().length >= 3) {
            Log.d(TAG, "lengthChecked: Called")
            searchStarts()
        } else
            items.value = tempData.value
    }

    private fun searchStarts() {
        if (!searchData.value.isNullOrEmpty()) {
            val filterList = doctorList.value?.filter { data ->
                data.name.lowercase().contains(searchData.value!!) || data.gender.lowercase()
                    .contains(searchData.value!!) || (data.degree != null && data.degree!!.contains(
                    searchData.value!!.uppercase()
                ))
            }
            if (filterList!!.isNotEmpty())
                items.value = filterList!!
            Log.d("filter list---", Gson().toJson(filterList))
        }

    }

    fun getItems() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = adminRepository.getDoctorList(fireStore)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        if (response.body.isNotEmpty()) {
                            items.value = response.body!!
                            tempData.value = items.value
                            Log.d("DoctorData----", Gson().toJson(response.body))
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
}