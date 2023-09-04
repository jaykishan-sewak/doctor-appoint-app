package com.android.doctorapp.ui.appointment

import android.util.Log
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.models.AppointmentDateModel
import com.android.doctorapp.repository.models.AppointmentTimeModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class AppointmentViewModel @Inject constructor(): BaseViewModel() {

    private val _scheduleDateList = SingleLiveEvent<ArrayList<AppointmentDateModel>>()
    val scheduleDateList = _scheduleDateList.asLiveData()
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy EE")
    private val simpleTime = SimpleDateFormat("hh:mm")

    private val _scheduleTimeList = SingleLiveEvent<ArrayList<AppointmentTimeModel>>()
    val scheduleTimeList = _scheduleTimeList.asLiveData()

    init {
        get15DaysList()
        getDoctorTime()
    }

    private fun getDoctorTime() {
        val appointmentTimeModel = ArrayList<AppointmentTimeModel>()

        val calendar = Calendar.getInstance()
        val hoursList = mutableListOf<Date>()
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())

        for (i in 0 until 10) {
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            hoursList.add(calendar.time)
        }

        hoursList.forEach {
            appointmentTimeModel.add(AppointmentTimeModel(time = it, isTimeSelect = false, isTimeBook = false))
        }

        _scheduleTimeList.value = appointmentTimeModel
    }

    private fun get15DaysList() {
        val appointmentDateList = ArrayList<AppointmentDateModel>()
        val currentDate: String = getCurrentDate().toString()
        val dateList = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(currentDate)

        // Add the current date to the list

        dateList.add(dateFormat.parse(currentDate))

        // Calculate and add dates for the next 14 days
        for (i in 1 until 15) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            dateList.add(calendar.time)
        }
        dateList.forEach{
            appointmentDateList.add(AppointmentDateModel(date = it, isDateSelect = false, isDateBook = false))
        }
        _scheduleDateList.value = appointmentDateList
    }

    // Function to get the current date as a Date object
    private fun getCurrentDate(): String {
        val currentCal = Calendar.getInstance()
        return dateFormat.format(currentCal.time)
    }

    private fun getCurrentTime(): String {
        return simpleTime.format(Date())
    }

    private fun getCurrentTime1(): Calendar {
        return Calendar.getInstance()
    }





    /*fun get15DaysList() {
        val currentDate = getCurrentDate()
        val dateList = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate

        // Add the current date to the list
        dateList.add(currentDate)

        // Calculate and add dates for the next 14 days
        for (i in 1 until 15) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            dateList.add(calendar.time)
        }
    }

    // Function to get the current date as a Date object
    private fun getCurrentDate(): Date {
        val dateFormat = SimpleDateFormat("dd-mm-yyyy")
        val currentCal = Calendar.getInstance()
        val currentDate = dateFormat.format(currentCal.time)
        return dateFormat.parse(currentDate)
    }*/

}