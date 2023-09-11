package com.android.doctorapp.repository.models

import java.util.Date

data class DateSlotModel(
    var date: Date? = null,
    var disable: Boolean = false,
    var dateSelect: Boolean = false
)