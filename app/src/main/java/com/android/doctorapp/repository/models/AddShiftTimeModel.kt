package com.android.doctorapp.repository.models

import java.util.Date

class AddShiftTimeModel(
    var startTime: Date? = null,
    var endTime: Date? = null,
    var isTimeSlotBook: Boolean = false,
    var isTimeClick: Boolean = false
)