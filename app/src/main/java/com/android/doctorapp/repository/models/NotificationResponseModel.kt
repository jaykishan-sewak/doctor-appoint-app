package com.android.doctorapp.repository.models

data class NotificationResponseModel(
    val multicast_id : String,
    val success: Int,
    val failure: Int,
    val canonical_ids: Int,
    val results: List<ResultsModel>
)

data class ResultsModel(
    val message_id: String
)
