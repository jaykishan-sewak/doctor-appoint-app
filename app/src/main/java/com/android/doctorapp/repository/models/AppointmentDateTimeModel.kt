package com.android.doctorapp.repository.models

import java.util.Date

data class AppointmentDateTimeModel(
    var list: ArrayList<DateModel> = arrayListOf()
)

data class DateModel(
    var date: Date? = null,
    var isDateSelect: Boolean = false,
    var isDateBook: Boolean = false,
    var timeList: ArrayList<TimeModel> = arrayListOf()
)

data class TimeModel(
    var time: Date? = null,
    var isTimeSelect: Boolean = false,
    var isTimeBook: Boolean = false
)