package com.android.doctorapp.repository.models

data class NotificationRequestModel(
    var to: String?,
    var data: DataRequestModel,
)

data class DataRequestModel(
    var body: String = "",
    var title: String = "",
    var type: Boolean? = null,
    var documentId: String? = null,
    var isBookAppointment: Boolean = false
)
