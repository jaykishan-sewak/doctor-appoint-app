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
import com.android.doctorapp.repository.models.FeedbackResponseModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class AllFeedbackViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val feedRepository: FeedbackRepository,
    private val context: Context,
    val session: Session

) : BaseViewModel() {

    var doctorDataObj = MutableLiveData<UserDataResponseModel>()
    var feedbackList = MutableLiveData<List<FeedbackResponseModel>?>()
    val dataFound: MutableLiveData<Boolean> = MutableLiveData(false)


    fun getAllFeedbackList() {
        dataFound.value = true
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response =
                        feedRepository.getAllFeedbackList(doctorDataObj.value?.userId, fireStore)) {
                        is ApiSuccessResponse -> {
                            feedbackList.value = response.body
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