package com.android.doctorapp.repository.models

import java.util.Date

data class AppointmentModel (
    var bookingDateTime: Date? = null,
    var isOnline: Boolean = false,
    var reason: String = "",
    var status: String = "",
    var userId: String = ""
)