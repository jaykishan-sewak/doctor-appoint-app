package com.android.doctorapp.ui.appointment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.models.DateSlotModel
import com.android.doctorapp.repository.models.TimeSlotModel
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_DATE
import com.android.doctorapp.util.constants.ConstantKey.FULL_DATE_FORMAT
import com.android.doctorapp.util.extension.asLiveData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class AppointmentViewModel @Inject constructor() : BaseViewModel() {

    private val dateFormatFull = SimpleDateFormat(FULL_DATE_FORMAT)
    val dateFormat = SimpleDateFormat(FORMATTED_DATE)

    private val _daysDateList = MutableLiveData<ArrayList<DateSlotModel>>()
    val daysDateList = _daysDateList.asLiveData()
    private val daysList = ArrayList<DateSlotModel>()

    private val _timeSlotList = MutableLiveData<ArrayList<TimeSlotModel>>()
    val timeSlotList = _timeSlotList.asLiveData()
    private val timeList = ArrayList<TimeSlotModel>()

    private val _holidayDateList = MutableLiveData<ArrayList<Date>>()
    val holidayDateList = _holidayDateList.asLiveData()
    private val holidayList = ArrayList<Date>()
    
    private val weekOfDayList = ArrayList<String>()

    val isBookAppointmentDataValid: MutableLiveData<Boolean> = MutableLiveData(false)
    var isTimeSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    var isDateSelected: MutableLiveData<Boolean> = MutableLiveData(false)


    init {
        getHolidayList()
        getWeekOfDayList()
        get15DaysList()
    }
    
    private fun getWeekOfDayList() {
        weekOfDayList.add("Sun")
        weekOfDayList.add("Sat")
    }

    private fun getHolidayList() {

        holidayList.add(dateFormatFull.parse("Sat Jan 14 12:00:00 GMT+05:30 2023") as Date)
        holidayList.add(dateFormatFull.parse("Thu Jan 26 12:00:00 GMT+05:30 2023") as Date)
        holidayList.add(dateFormatFull.parse("Wed Mar 08 12:00:00 GMT+05:30 2023") as Date)
        holidayList.add(dateFormatFull.parse("Tue Aug 15 12:00:00 GMT+05:30 2023") as Date)
        holidayList.add(dateFormatFull.parse("Wed Aug 30 12:00:00 GMT+05:30 2023") as Date)
        holidayList.add(dateFormatFull.parse("Thu Sep 07 12:00:00 GMT+05:30 2023") as Date)
        holidayList.add(dateFormatFull.parse("Tue Oct 24 12:00:00 GMT+05:30 2023") as Date)
        holidayList.add(dateFormatFull.parse("Sun Nov 12 12:00:00 GMT+05:30 2023") as Date)
        holidayList.add(dateFormatFull.parse("Mon Nov 13 12:00:00 GMT+05:30 2023") as Date)
        holidayList.add(dateFormatFull.parse("Tue Nov 14 12:00:00 GMT+05:30 2023") as Date)

        _holidayDateList.value = holidayList

    }

    private fun get15DaysList() {
        val currentDate: String = getCurrentDate().toString()
        val dateList = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        calendar.time = dateFormatFull.parse(currentDate) as Date

        // Add the current date to the list
        dateList.add(dateFormatFull.parse(currentDate) as Date)

        for (i in 1 until 15) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            dateList.add(calendar.time)
        }
        getTimeSlot()

        dateList.forEach {
            daysList.add(DateSlotModel(date = it, disable = false))
        }

        daysList.forEachIndexed { index, dateSlotModel ->
            holidayList.forEachIndexed { _, data ->
                if (convertDate(dateSlotModel.date.toString()) == convertDate(data.toString())) {
                    daysList[index] = DateSlotModel(date = dateSlotModel.date, disable = true)
                    return@forEachIndexed
                }
            }
            weekOfDayList.forEachIndexed { _, str ->
                if (convertDayName(dateSlotModel.date.toString()) == str) {
                    daysList[index] = DateSlotModel(date = dateSlotModel.date, disable = true)
                    return@forEachIndexed
                }
            }
        }
        _daysDateList.value = daysList
    }

    private fun getTimeSlot() {
        val calendar = Calendar.getInstance()
        val hoursList = mutableListOf<Date>()
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())

        for (i in 0 until 10) {
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            hoursList.add(calendar.time)
        }

        timeList.add(
            TimeSlotModel(
                timeSlot = dateFormatFull.parse("Tue Sep 05 12:00:00 GMT+05:30 2023"),
                isTimeSlotBook = false
            )
        )
        timeList.add(
            TimeSlotModel(
                timeSlot = dateFormatFull.parse("Tue Sep 05 13:00:00 GMT+05:30 2023"),
                isTimeSlotBook = true
            )
        )
        timeList.add(
            TimeSlotModel(
                timeSlot = dateFormatFull.parse("Tue Sep 05 14:00:00 GMT+05:30 2023"),
                isTimeSlotBook = false
            )
        )
        timeList.add(
            TimeSlotModel(
                timeSlot = dateFormatFull.parse("Tue Sep 05 15:00:00 GMT+05:30 2023"),
                isTimeSlotBook = false
            )
        )
        timeList.add(
            TimeSlotModel(
                timeSlot = dateFormatFull.parse("Tue Sep 05 16:00:00 GMT+05:30 2023"),
                isTimeSlotBook = false
            )
        )
        timeList.add(
            TimeSlotModel(
                timeSlot = dateFormatFull.parse("Tue Sep 05 17:00:00 GMT+05:30 2023"),
                isTimeSlotBook = false,
            )
        )
        timeList.add(
            TimeSlotModel(
                timeSlot = dateFormatFull.parse("Tue Sep 05 18:00:00 GMT+05:30 2023"),
                isTimeSlotBook = false
            )
        )
        timeList.add(
            TimeSlotModel(
                timeSlot = dateFormatFull.parse("Tue Sep 05 19:00:00 GMT+05:30 2023"),
                isTimeSlotBook = true
            )
        )
        timeList.add(
            TimeSlotModel(
                timeSlot = dateFormatFull.parse("Tue Sep 05 20:00:00 GMT+05:30 2023"),
                isTimeSlotBook = false
            )
        )
        timeList.add(
            TimeSlotModel(
                timeSlot = dateFormatFull.parse("Tue Sep 05 21:00:00 GMT+05:30 2023"),
                isTimeSlotBook = false,
            )
        )
        timeList.add(
            TimeSlotModel(
                timeSlot = dateFormatFull.parse("Tue Sep 05 22:00:00 GMT+05:30 2023"),
                isTimeSlotBook = true
            )
        )

        _timeSlotList.value = timeList
    }


    // Function to get the current date as a Date object
    private fun getCurrentDate(): String {
        val currentCal = Calendar.getInstance()
        return dateFormatFull.format(currentCal.time)
    }

    private fun convertDate(inputDateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
            val outputFormat = SimpleDateFormat("dd-MM-yyyy")

            val date = inputFormat.parse(inputDateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.d("Format issue--", e.message.toString())
            ""
        }
    }
    
    private fun convertDayName(inputDateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
            val outputFormat = SimpleDateFormat("EEE")

            val date = inputFormat.parse(inputDateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.d("TAG", "convertDayName: ${e.message}")
            ""
        }
    }

     fun validateDateTime() {
        isBookAppointmentDataValid.value = isDateSelected.value == true
                && isTimeSelected.value == true
    }

}