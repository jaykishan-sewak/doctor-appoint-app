package com.android.doctorapp.repository.models

import java.util.Date

data class SymptomModel(
    var id: String = "",
    var doctorName: String = "",
    var userId: String = "",
    var lastVisitDay: Date? = null,
    var lastPrescription: String = "",
    var sufferingDay: String = "",
    var doctorId: String ?= "",
    var symptomDetails: String = ""
) {
}