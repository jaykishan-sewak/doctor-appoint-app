package com.android.doctorapp.ui.admin

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AdminRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.ImageUriAndGender
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.launch
import javax.inject.Inject


class AdminDashboardViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val adminRepository: AdminRepository,
    private val context: Context,
    private val session: Session

) : BaseViewModel() {


    val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()
    val doctorDetails: MutableLiveData<UserDataResponseModel?> = MutableLiveData()
    val userId: MutableLiveData<String> = MutableLiveData("")
    val itemPosition: MutableLiveData<Int> = MutableLiveData()
    val deleteId: MutableLiveData<String?> = MutableLiveData(null)
    val callClick = MutableLiveData("")
    private val _navigateToLogin = SingleLiveEvent<Boolean>()
    val navigateToLogin = _navigateToLogin.asLiveData()

    val isLogoutClick: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDeletedSuccess: MutableLiveData<Boolean> = MutableLiveData(false)

    var imageOrGenderObj: MutableLiveData<ImageUriAndGender> = MutableLiveData()

    private val items = MutableLiveData<List<UserDataResponseModel>?>()
    private val tempData = MutableLiveData<List<UserDataResponseModel>>()
    var doctorList = items.asLiveData()

    init {
        getItems()
    }

    fun lengthChecked(text: CharSequence) {
        if (text.toString().length >= 3) {
            items.value = tempData.value
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
                items.value = emptyList()
                when (val response = adminRepository.adminGetDoctorList(fireStore)) {
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

    fun deleteDoctor(id: String?) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = adminRepository.deleteDoctor(fireStore, id!!)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        if (response.body) {
                            val currentList = items.value?.toMutableList() ?: mutableListOf()
                            if (itemPosition.value in 0 until currentList.size) {
                                currentList.removeAt(itemPosition.value!!)
                                items.postValue(currentList)
                                isDeletedSuccess.postValue(true)
                            }
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


    fun getDoctorDetail() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)

                when (val response = adminRepository.getDoctorDetails(userId.value!!, fireStore)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        doctorDetails.value = response.body
                        imageOrGenderObj.value = ImageUriAndGender(
                            if (!response.body.images.isNullOrEmpty()) response.body.images?.toUri() else null,
                            response.body.gender
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
            } else
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    fun moveToUpdateScreen() {
        viewModelScope.launch {
            session.putString(USER_ID, doctorDetails.value?.userId!!)
        }
        _navigationListener.value = R.id.admin_to_update_doctor
    }

    fun deleteDoctorData(id: String?) {
        deleteId.value = id
    }

    fun clickOnCall(text: String) {
        callClick.value = text
    }

    fun signOut() {
        viewModelScope.launch {
            adminRepository.clearLoggedInSession()
        }
        _navigateToLogin.value = true

    }
}