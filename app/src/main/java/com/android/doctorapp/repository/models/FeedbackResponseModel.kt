package com.android.doctorapp.repository.models

import java.util.Date

class FeedbackResponseModel(
    var feedbackId: String = "",
    var userId: String = "",
    var rating: Float? = null,
    var feedbackMessage: String = "",
    var createdAt: Date? = null
)