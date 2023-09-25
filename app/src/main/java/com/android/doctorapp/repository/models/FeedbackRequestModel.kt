package com.android.doctorapp.repository.models


data class FeedbackRequestModel(
    var userId: String = "",
    var id: String = "",
    var doctorId: String = "",
    var rating: Float? = null,
    var feedbackMessage: String = "",
)