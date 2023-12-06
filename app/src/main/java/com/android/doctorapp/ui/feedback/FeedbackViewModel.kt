package com.android.doctorapp.ui.feedback

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.FeedbackRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.FeedbackRequestModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.currentDate
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class FeedbackViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val feedRepository: FeedbackRepository,
    private val context: Context,
    val session: Session

) : BaseViewModel() {

    var doctorList: MutableLiveData<List<UserDataResponseModel>?> = MutableLiveData()
    val rating: MutableLiveData<Float?> = MutableLiveData(0f)
    val feedbackMsg: MutableLiveData<String> = MutableLiveData()
    val doctorId = MutableLiveData("")
    private val _navigationListener = SingleLiveEvent<Boolean>()
    val navigationListener = _navigationListener.asLiveData()
    var userDataObj = MutableLiveData<UserDataResponseModel?>()
    var doctorFeedbackObj = MutableLiveData<UserDataResponseModel?>()
    val isEditClick: MutableLiveData<Boolean> = MutableLiveData(false)
    val dataFound: MutableLiveData<Boolean> = MutableLiveData()


    fun getUserDoctorList() {
        dataFound.value = true
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response = feedRepository.getUserDoctorList(fireStore, it)) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            doctorList.value = response.body
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

    fun onRatingChanged(rating: Float) {
        this.rating.value = rating
    }

    fun submitFeedback() {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response = feedRepository.addFeedbackData(
                        userDataObj.value?.docId,
                        FeedbackRequestModel(
                            userId = it,
                            rating = rating.value,
                            feedbackMessage = feedbackMsg.value,
                            createdAt = currentDate()
                        ),
                        fireStore
                    )) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            doctorList.postValue(null)
                            rating.value = 0f
                            feedbackMsg.value = ""
                            _navigationListener.value = response.body.isNotEmpty()

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

    fun getUserFeedbackData() {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response = feedRepository.getUserFeedbackData(
                        userDataObj.value?.userId,
                        fireStore,
                        it
                    )) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            doctorFeedbackObj.value = response.body
                            rating.value = response.body.feedbackDetails?.rating
                            feedbackMsg.value = response.body.feedbackDetails?.feedbackMessage
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

    fun updateFeedback() {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response = feedRepository.getUpdateFeedbackData(
                        userDataObj.value?.userId,
                        fireStore,
                        FeedbackRequestModel(
                            userId = it,
                            rating = rating.value,
                            feedbackMessage = feedbackMsg.value,
                            createdAt = currentDate()
                        ),
                    )) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            rating.value = 0f
                            feedbackMsg.value = ""
                            _navigationListener.value = true

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

    fun deleteFeedback(item: UserDataResponseModel, position: Int) {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response = feedRepository.deleteFeedbackData(
                        item.docId,
                        fireStore,
                        item.feedbackDetails!!.feedbackDocId
                    )) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            val currentList = doctorList.value?.toMutableList() ?: mutableListOf()
                            if (position in 0 until currentList.size) {
                                currentList.removeAt(position)
                                doctorList.postValue(currentList)
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