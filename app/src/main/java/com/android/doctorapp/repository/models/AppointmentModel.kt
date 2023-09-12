package com.android.doctorapp.repository.models

import java.util.Date

data class AppointmentModel (
    var bookingDateTime: Date? = null,
    var isOnline: Boolean = false,
    var reason: String = "",
    var status: String = "",
    var name: String = "",
    var age: String = "",
    var contactNumber: String = "",
    var userId: String = ""
)