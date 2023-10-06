package com.android.doctorapp.repository.models

import java.util.Date


data class FeedbackRequestModel(
    var userId: String? = "",
    var rating: Float? = null,
    var feedbackMessage: String? = "",
    var createdAt: Date? = null
)