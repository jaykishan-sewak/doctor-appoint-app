package com.android.doctorapp.repository.models

class FeedbackResponseModel(
    var userId: String = "",
    var id: String = "",
    var doctorId: String = "",
    var rating: Float? = null,
    var feedbackMessage: String = "",
)