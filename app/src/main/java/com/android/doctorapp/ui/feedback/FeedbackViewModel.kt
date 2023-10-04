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
    private val session: Session

) : BaseViewModel() {
    val doctorList = MutableLiveData<List<UserDataResponseModel>?>()
    val rating: MutableLiveData<Float> = MutableLiveData(0f)
    val feedbackMsg: MutableLiveData<String> = MutableLiveData()
    val doctorId = MutableLiveData("")
    private val _navigationListener = SingleLiveEvent<Boolean>()
    val navigationListener = _navigationListener.asLiveData()


    fun getUserDoctorList() {
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
                        it!!,
                        FeedbackRequestModel(
                            userId = it,
                            rating = rating.value,
                            feedbackMessage = feedbackMsg.value!!,
                            createdAt = currentDate()
                        ),
                        fireStore
                    )) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
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
}