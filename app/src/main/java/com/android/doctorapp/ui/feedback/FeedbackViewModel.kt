package com.android.doctorapp.ui.feedback

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
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
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.FeedbackRequestModel
import com.android.doctorapp.repository.models.FeedbackResponseModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.currentDate
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import com.google.gson.Gson
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
    val rating: MutableLiveData<Float?> = MutableLiveData(0f)
    val feedbackMsg: MutableLiveData<String> = MutableLiveData()
    val doctorId = MutableLiveData("")
    private val _navigationListener = SingleLiveEvent<Boolean>()
    val navigationListener = _navigationListener.asLiveData()
    var userDataObj = MutableLiveData<UserDataResponseModel>()
    var userFeedbackObj = MutableLiveData<FeedbackResponseModel>()
    var isEditClicked : MutableLiveData<Boolean> = MutableLiveData(false)


    fun getUserDoctorList() {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response = feedRepository.getUserDoctorList(fireStore, it)) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            Log.d(TAG, "getUserDoctorList: ${Gson().toJson(response.body)}")
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
                        userDataObj.value!!.docId,
                        FeedbackRequestModel(
                            userId = it!!,
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

    fun getUserFeedbackData() {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response = feedRepository.getUserFeedbackData(
                        userDataObj.value!!.userId,
                        fireStore,
                        it
                    )) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                            userFeedbackObj.value = response.body!!
                            rating.value = response.body.rating
                            feedbackMsg.value = response.body.feedbackMessage
                            Log.d(TAG, "UserDataFeedback: ${Gson().toJson(response.body)} ")
                            Log.d(TAG, "getUserFeedbackData: ${response.body}")
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