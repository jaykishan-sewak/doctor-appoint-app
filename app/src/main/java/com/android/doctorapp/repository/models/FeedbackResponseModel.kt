package com.android.doctorapp.repository.models

import java.util.Date

class FeedbackResponseModel(
    var feedbackDocId: String = "",
    var userId: String = "",
    var rating: Float? = null,
    var feedbackMessage: String = "",
    var createdAt: Date? = null,
    var userDetails: UserDataResponseModel? = null
)