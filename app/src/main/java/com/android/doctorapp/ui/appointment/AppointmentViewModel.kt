package com.android.doctorapp.ui.appointment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.models.AppointmentDateTimeModel
import com.android.doctorapp.repository.models.DateModel
import com.android.doctorapp.repository.models.TimeModel
import com.android.doctorapp.util.extension.asLiveData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class AppointmentViewModel @Inject constructor(): BaseViewModel() {

    private val _scheduleDateTimeList = MutableLiveData<AppointmentDateTimeModel>()
    val scheduleDateTimeList = _scheduleDateTimeList.asLiveData()
    private val scheduleDateTimeList1 = ArrayList<AppointmentDateTimeModel>()

    private val _dateModelList = MutableLiveData<DateModel>()
    val dateModelList = _dateModelList.asLiveData()
    private val dateModelList1 = ArrayList<DateModel>()

    private val _timeModelList = MutableLiveData<ArrayList<TimeModel>>()
    val timeModelList = _timeModelList.asLiveData()
    private val timeModelList1 = ArrayList<TimeModel>()

//    private val _scheduleDateList = MutableLiveData<ArrayList<AppointmentDateTimeModel>>()
//    val scheduleDateList = _scheduleDateList.asLiveData()

//    private val _scheduleTimeList = MutableLiveData<ArrayList<AppointmentTimeModel>>()
//    val scheduleTimeList = _scheduleTimeList.asLiveData()

    private val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
//    private val dateFormat = SimpleDateFormat("dd-MM-yyyy EE")

    val isBookAppointmentDataValid: MutableLiveData<Boolean> = MutableLiveData(false)
    var isTimeSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    var isDateSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    private val appointmentDateList = ArrayList<AppointmentDateTimeModel>()
//    private val appointmentTimeModel = ArrayList<AppointmentTimeModel>()

    init {
        get15DaysList()
        getDoctorTime()
    }

    private fun getDoctorTime() {

        val calendar = Calendar.getInstance()
        val hoursList = mutableListOf<Date>()
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())

        for (i in 0 until 10) {
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            hoursList.add(calendar.time)
        }

//        hoursList.forEach {
//            appointmentTimeModel.add(AppointmentTimeModel(time = it, isTimeSelect = false, isTimeBook = false))
//        }

//        appointmentTimeModel.add(AppointmentTimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 15:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
//        appointmentTimeModel.add(AppointmentTimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 16:00:00 GMT+05:30 2023"), isTimeSelect = true, isTimeBook = false))
//        appointmentTimeModel.add(AppointmentTimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 17:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
//        appointmentTimeModel.add(AppointmentTimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 18:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
//        appointmentTimeModel.add(AppointmentTimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 19:00:00 GMT+05:30 2023"), isTimeSelect = true, isTimeBook = false))
//        appointmentTimeModel.add(AppointmentTimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 20:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
//        appointmentTimeModel.add(AppointmentTimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 21:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
//        appointmentTimeModel.add(AppointmentTimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 22:00:00 GMT+05:30 2023"), isTimeSelect = true, isTimeBook = false))
//        appointmentTimeModel.add(AppointmentTimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 23:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))

        timeModelList1.add(TimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 12:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
        timeModelList1.add(TimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 13:00:00 GMT+05:30 2023"), isTimeSelect = true, isTimeBook = false))
        timeModelList1.add(TimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 14:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
        timeModelList1.add(TimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 15:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
        timeModelList1.add(TimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 16:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
        timeModelList1.add(TimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 17:00:00 GMT+05:30 2023"), isTimeSelect = true, isTimeBook = false))
        timeModelList1.add(TimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 18:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
        timeModelList1.add(TimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 19:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
        timeModelList1.add(TimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 20:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))
        timeModelList1.add(TimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 21:00:00 GMT+05:30 2023"), isTimeSelect = true, isTimeBook = false))
        timeModelList1.add(TimeModel(time = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse("Tue Sep 05 22:00:00 GMT+05:30 2023"), isTimeSelect = false, isTimeBook = false))


//        _scheduleTimeList.value = appointmentTimeModel
    }

    private fun get15DaysList() {
        val currentDate: String = getCurrentDate().toString()
        val dateList = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(currentDate)

        // Add the current date to the list
        dateList.add(dateFormat.parse(currentDate))

        for (i in 1 until 15) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            dateList.add(calendar.time)
        }

        getDoctorTime()

        dateList.forEach{
//            appointmentDateList.add(AppointmentDateTimeModel(date = it, isDateSelect = false, isDateBook = false))
            dateModelList1.add(DateModel(date = it, isDateSelect = false, isDateBook = false, timeList = timeModelList1))
//            scheduleDateTimeList1.add(AppointmentDateTimeModel(list = dateModelList1))
        }

//        dateModelList1.forEach {
//            Log.d("TAG", "get15DaysList: $it")
//            _dateModelList.value = it
//        }


        _scheduleDateTimeList.value = AppointmentDateTimeModel(dateModelList1)
    }

    // Function to get the current date as a Date object
    private fun getCurrentDate(): String {
        val currentCal = Calendar.getInstance()
        return dateFormat.format(currentCal.time)
    }

    fun validateDateTime() {
        isBookAppointmentDataValid.value = isTimeSelected.value == true
                && isTimeSelected.value == true
        Log.d("TAG", "validateDateTime: ${isTimeSelected.value}  --->    ${isDateSelected.value}")
    }

    fun onBookAppointmentClick() {
        Log.d("TAG", "onBookAppointmentClick: ${isBookAppointmentDataValid.value}")
    }

}