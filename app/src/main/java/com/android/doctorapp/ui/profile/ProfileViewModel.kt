package com.android.doctorapp.ui.profile

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
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

    var userProfileDataResponse: MutableLiveData<UserDataResponseModel?> = MutableLiveData()
    val isDoctorEdit: MutableLiveData<Boolean> = MutableLiveData(false)

    val phoneClick: MutableLiveData<String> = MutableLiveData()
    val emailClick: MutableLiveData<String> = MutableLiveData()
    val dateList: MutableLiveData<List<String>> = MutableLiveData()
    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()
    val imageUri = MutableLiveData<Uri>()
    val notificationToggleData: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        emailClick.postValue("")
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
                            userProfileDataResponse.value = response.body
                            notificationToggleData.value = response.body.isNotificationEnable
                            imageUri.value = userProfileDataResponse.value!!.images?.toUri()
                            dateList.value = dateListFormatter(
                                response.body.holidayList,
                                ConstantKey.DATE_MM_FORMAT
                            )
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

    fun editClick(isDoctor: Boolean) {
        if (isDoctor)
            isDoctorEdit.postValue(true)
        else
            _navigationListener.value = R.id.action_user_profile_to_updateUserProfile
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

    fun clickOnSymptom() {
        _navigationListener.postValue(R.id.action_user_profile_to_symptoms)
    }

    fun clickOnFeedback() {
        _navigationListener.value = R.id.action_user_profile_to_feedBack
    }

    fun clickOnHistory() {
        _navigationListener.value = R.id.action_user_profile_to_history
    }

    fun clickOnNotification() {
        updateNotificationStatus(notificationToggleData.value!!)
    }

    private fun updateNotificationStatus(notificationEnable: Boolean) {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response =
                        profileRepository.updateUserData(notificationEnable, it, fireStore)) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
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