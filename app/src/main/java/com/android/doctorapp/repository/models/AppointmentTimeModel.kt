package com.android.doctorapp.repository.models

import java.util.Date

data class AppointmentTimeModel(
    var time: Date? = null,
    var isTimeSelect: Boolean = false,
    var isTimeBook: Boolean = false
)