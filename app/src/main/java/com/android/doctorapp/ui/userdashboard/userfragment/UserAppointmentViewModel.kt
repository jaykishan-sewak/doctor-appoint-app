package com.android.doctorapp.ui.userdashboard.userfragment

import android.content.Context
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
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserAppointmentViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val adminRepository: AdminRepository,
    private val context: Context
) : BaseViewModel() {

    private val items = MutableLiveData<List<UserDataResponseModel>?>()
    val doctorList = items.asLiveData()
    val dataFound: MutableLiveData<Boolean> = MutableLiveData(false)

    private val tempData = MutableLiveData<List<UserDataResponseModel>>()
    val locationCity: MutableLiveData<String> =
        MutableLiveData(resourceProvider.getString(R.string.nearest_doctor))


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

    fun getItems() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = adminRepository.getDoctorList(fireStore)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        if (response.body.isNotEmpty()) {
                            items.value = response.body
                            tempData.value = items.value
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