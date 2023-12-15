package com.android.doctorapp.ui.profile

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.ProfileRepository
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.local.IS_NEW_USER_TOKEN
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val profileRepository: ProfileRepository,
    private val context: Context,
    val session: Session
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

    val isCameraClick: MutableLiveData<Boolean> = MutableLiveData(false)
    val isGalleryClick: MutableLiveData<Boolean> = MutableLiveData(false)
    var clinicImgArrayList = ArrayList<String>()
    val clinicImgList = MutableLiveData<ArrayList<String>?>()
    var isUserViewClinicImg = MutableLiveData<Boolean>()
    val noClinicImgFound: MutableLiveData<Boolean> = MutableLiveData(false)

    val myDoctorsList: MutableLiveData<List<UserDataResponseModel?>?> = MutableLiveData()
    val dataNotFound: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDarkThemeClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isTokenEmptySuccessFully: MutableLiveData<Boolean> = MutableLiveData(false)
    val isClearSessionSuccessFully: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        emailClick.postValue("")
    }

    fun getUserProfileData() {
        viewModelScope.launch {
            var recordId = ""
            val userId = session.getString(USER_ID).firstOrNull()
            if (!userId.isNullOrEmpty()) {
                recordId = userId
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
                            clinicImgArrayList = response.body.clinicImg ?: ArrayList()
                            clinicImgList.value = clinicImgArrayList
                            setShowProgress(false)
                        }

                        is ApiErrorResponse -> {
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

    fun clickOnMyDoctors() {
        _navigationListener.value = R.id.action_user_profile_to_myDoctors
    }

    fun clickedOnDarkTheme() {
        if (isDarkThemeClicked.value == true) {
            viewModelScope.launch {
                session.putBoolean(IS_ENABLED_DARK_MODE, true)
            }
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            viewModelScope.launch {
                session.putBoolean(IS_ENABLED_DARK_MODE, false)
            }
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
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

    fun updateUserData(token: String?) {
        viewModelScope.launch {
            val userId = session.getString(USER_ID).firstOrNull()
            if (!userId.isNullOrEmpty()) {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response =
                        profileRepository.emptyUserToken(token, userId, fireStore)) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            session.putBoolean(IS_NEW_USER_TOKEN, false)
                            isTokenEmptySuccessFully.value = true
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
                } else context.toast(resourceProvider.getString(R.string.check_internet_connection))
            }
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            isClearSessionSuccessFully.value = profileRepository.clearLoggedInSession()
        }
    }

    fun clickOnCamera() {
        isCameraClick.value = true
    }

    fun clickOnGallery() {
        isGalleryClick.value = true
    }

    fun uploadImage(image: Uri) {
        noClinicImgFound.value = false
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = profileRepository.uploadImage(image, storage)) {
                    is ApiSuccessResponse -> {
                        if (response.body.isNotEmpty()) {
                            clinicImgArrayList.add(response.body)
                            clinicImgList.value = clinicImgArrayList
                            updateClinicImageList(clinicImgList.value)
                        } else
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

    private fun updateClinicImageList(clinicImgArrayList: ArrayList<String>?) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response =
                    profileRepository.updateClinicImgById(
                        clinicImgArrayList,
                        fireStore,
                        userProfileDataResponse.value?.userId
                    )) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        clinicImgList.value = clinicImgArrayList
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

    fun viewClinicClicked() {
        _navigationListener.value = R.id.action_doctor_profile_to_viewClinic
    }

    fun deleteImage(position: Int) {
        updateClinicImageList(clinicImgArrayList)
        clinicImgArrayList.removeAt(position)
    }

    fun getMyDoctors(currentDate: Date) {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response =
                        profileRepository.getMyDoctorsList(it, currentDate, fireStore)) {
                        is ApiSuccessResponse -> {
                            if (response.body.isNotEmpty()) {
                                myDoctorsList.value = response.body
                                setShowProgress(false)
                            } else {
                                setShowProgress(false)
                                dataNotFound.value = true
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

}