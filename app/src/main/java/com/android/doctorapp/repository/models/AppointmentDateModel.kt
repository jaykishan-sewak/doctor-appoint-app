package com.android.doctorapp.repository.models

import java.util.Date

data class AppointmentDateModel(
    var date: Date? = null,
    var isDateSelect: Boolean = false,
    var isDateBook: Boolean = false
)