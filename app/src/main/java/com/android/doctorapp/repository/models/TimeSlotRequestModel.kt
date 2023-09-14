package com.android.doctorapp.repository.models

import java.util.Date

data class TimeSlotRequestModel(
    var timeSlot: Date? = null,
    var isTimeSlotBook: Boolean = false
)
