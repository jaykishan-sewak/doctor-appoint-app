package com.android.doctorapp.ui.profile

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.ProfileRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.ProfileResponseModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.dateListFormatter
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val profileRepository: ProfileRepository,
    private val context: Context,
    private val session: Session
) : BaseViewModel() {

    private val _profileResponse = SingleLiveEvent<ProfileResponseModel?>()
    val profileResponse = _profileResponse.asLiveData()

    private val _navigateToLogin = SingleLiveEvent<Boolean>()
    val navigateToLogin = _navigateToLogin.asLiveData()

    private val _onProfilePictureClicked = SingleLiveEvent<Unit>()
    val onProfilePictureClicked = _onProfilePictureClicked.asLiveData()

    var userProfileDataResponse: MutableLiveData<UserDataResponseModel> = MutableLiveData()
    val isEdit: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDoctorEdit: MutableLiveData<Boolean> = MutableLiveData(false)

    val phoneClick: MutableLiveData<String> = MutableLiveData()
    val emailClick: MutableLiveData<String> = MutableLiveData()
    val dateList: MutableLiveData<List<String>> = MutableLiveData()


    init {
        getUserProfile()
    }

    private fun getUserProfile() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val result = profileRepository.getUserProfile()) {
                is ApiSuccessResponse -> {
                    _profileResponse.postValue(result.body)
                }

                is ApiErrorResponse -> {
                    setApiError(result.errorMessage)
                }

                is ApiNoNetworkResponse -> {
                    setNoNetworkError(result.errorMessage)
                }

                else -> {}
            }
            setShowProgress(false)
        }
    }

    fun getUserProfileData() {
        viewModelScope.launch {
            var recordId: String = ""
            session.getString(USER_ID).collectLatest {
                recordId = it.orEmpty()
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response =
                        profileRepository.getProfileRecordById(recordId, fireStore)) {
                        is ApiSuccessResponse -> {
                            userProfileDataResponse.value = response.body!!
                            dateList.value = dateListFormatter(
                                response.body.holidayList,
                                ConstantKey.DATE_MM_FORMAT
                            )
                            Log.d(TAG, "DateList: ${response.body}")
                            setShowProgress(false)
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


    fun signOut() {
        viewModelScope.launch {
            profileRepository.clearLoggedInSession()
        }
        _navigateToLogin.postValue(true)
    }

    fun profilePictureClicked() {
        _onProfilePictureClicked.postValue(Unit)
    }

    fun setImage(path: String?) {
        _profileResponse.postValue(_profileResponse.value?.also {
            it.data?.profile = path
        })
    }

    fun onClickPhoneIcon(contact: String) {
        phoneClick.postValue(contact)
    }

    fun onClickEmailIcon(email: String) {
        emailClick.postValue(email)
    }
}