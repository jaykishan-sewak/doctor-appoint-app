package com.android.infusiontemplate.ui.profile

import androidx.lifecycle.viewModelScope
import com.android.infusiontemplate.di.base.BaseViewModel
import com.android.infusiontemplate.repository.ProfileRepository
import com.android.infusiontemplate.repository.models.ApiErrorResponse
import com.android.infusiontemplate.repository.models.ApiNoNetworkResponse
import com.android.infusiontemplate.repository.models.ApiSuccessResponse
import com.android.infusiontemplate.repository.models.ProfileResponseModel
import com.android.infusiontemplate.util.SingleLiveEvent
import com.android.infusiontemplate.util.extension.asLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : BaseViewModel() {

    private val _profileResponse = SingleLiveEvent<ProfileResponseModel?>()
    val profileResponse = _profileResponse.asLiveData()

    private val _navigateToLogin = SingleLiveEvent<Boolean>()
    val navigateToLogin = _navigateToLogin.asLiveData()

    private val _onProfilePictureClicked = SingleLiveEvent<Unit>()
    val onProfilePictureClicked = _onProfilePictureClicked.asLiveData()

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
}