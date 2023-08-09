package com.android.doctorapp.ui.home

import androidx.lifecycle.viewModelScope
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.ItemsRepository
import com.android.doctorapp.repository.ProfileRepository
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.ItemsResponseModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(private val itemsRepository: ItemsRepository,
                                        private val profileRepository: ProfileRepository) :
    BaseViewModel() {

    private val _itemResponse = SingleLiveEvent<ItemsResponseModel?>()
    val itemResponse = _itemResponse.asLiveData()

    private val _navigateToLogin = SingleLiveEvent<Unit>()
    val navigateToLogin = _navigateToLogin.asLiveData()

    init {
        getItems()
    }

    private fun getItems() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = itemsRepository.getItems()) {
                is ApiSuccessResponse -> {
                    _itemResponse.postValue(response.body)
                }
                is ApiErrorResponse -> {
                    setApiError(response.errorMessage)
                }
                is ApiNoNetworkResponse -> {
                    setNoNetworkError(response.errorMessage)
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
        _navigateToLogin.postValue(Unit)
    }
}