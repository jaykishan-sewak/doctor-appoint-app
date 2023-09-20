package com.android.doctorapp.repository.models

import java.util.Date

data class AppointmentModel(
    var id: String = "",
    var bookingDateTime: Date? = null,
    var isOnline: Boolean = false,
    var reason: String = "",
    var status: String = "",
    var userId: String = "",
    var name: String = "",
    var contactNumber: String = "",
    var age: String = "",
    var doctorId: String = ""
)

data class Header(val date: Date?)