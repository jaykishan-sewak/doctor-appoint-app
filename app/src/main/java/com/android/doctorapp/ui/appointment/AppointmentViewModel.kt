package com.android.doctorapp.ui.appointment

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AppointmentRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.DateSlotModel
import com.android.doctorapp.repository.models.TimeSlotModel
import com.android.doctorapp.util.constants.ConstantKey.DATE_MM_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.DAY_NAME_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.FULL_DATE_FORMAT
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


class AppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val session: Session,
    private val context: Context,
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {

    private val dateFormatFull = SimpleDateFormat(FULL_DATE_FORMAT)

    private val _daysDateList = MutableLiveData<ArrayList<DateSlotModel>>()
    val daysDateList = _daysDateList.asLiveData()
    private val daysList = ArrayList<DateSlotModel>()

    private val _timeSlotList = MutableLiveData<ArrayList<TimeSlotModel>>()
    val timeSlotList = _timeSlotList.asLiveData()
    private val timeList = ArrayList<TimeSlotModel>()

    private val _holidayDateList = MutableLiveData<ArrayList<Date>>()
    private val holidayList = ArrayList<Date>()

    private val weekOfDayList = ArrayList<String>()

    val isBookAppointmentDataValid: MutableLiveData<Boolean> = MutableLiveData(false)
    var isTimeSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    var isDateSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    val isBookAppointmentClick: MutableLiveData<Boolean> = MutableLiveData(false)
    val onlineBookingToggleData: MutableLiveData<Boolean> = MutableLiveData(false)

    val userId: MutableLiveData<String> = MutableLiveData("")
    val doctorName: MutableLiveData<String> = MutableLiveData()
    val doctorSpecialities: MutableLiveData<String> = MutableLiveData()
    private val userName = MutableLiveData<String>()
    private val contactNumber = MutableLiveData<String>()
    private lateinit var ageDate: Date
    private val age = MutableLiveData<String>()

    private val _navigationListener: MutableLiveData<Boolean> = MutableLiveData(false)
    val navigationListener = _navigationListener.asLiveData()

    var isShowBothButton = MutableLiveData(false)
    var cancelClick = MutableLiveData(false)
    var confirmClick = MutableLiveData(false)
    var rejectClick = MutableLiveData(false)
    var reasonValue = MutableLiveData<String>()
    var appoinmentObj = MutableLiveData<AppointmentModel>()

    init {
//        getHolidayList()
//        getWeekOfDayList()
//        get15DaysList()
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
//        getTimeSlot()

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
                isTimeSlotBook = false
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
                isTimeSlotBook = false
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
            val inputFormat = SimpleDateFormat(FULL_DATE_FORMAT)
            val outputFormat = SimpleDateFormat(DATE_MM_FORMAT)

            val date = inputFormat.parse(inputDateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            e.fillInStackTrace()
            ""
        }
    }

    private fun convertDayName(inputDateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat(FULL_DATE_FORMAT)
            val outputFormat = SimpleDateFormat(DAY_NAME_FORMAT)

            val date = inputFormat.parse(inputDateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun validateDateTime() {
        isBookAppointmentDataValid.value = isDateSelected.value == true
                && isTimeSelected.value == true
    }

    fun bookAppointment() {
        isBookAppointmentClick.value = true
    }

    fun addBookingAppointmentData(selectedTime: Date) {
        setShowProgress(true)
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                val appointmentModel = AppointmentModel(
                    bookingDateTime = selectedTime,
                    isOnline = onlineBookingToggleData.value == true,
                    status = "PROGRESS",
                    name = userName.value.toString(),
                    age = age.value.toString(),
                    contactNumber = contactNumber.value.toString(),
                    userId = it.toString()
                )
                when (val response =
                    appointmentRepository.addBookingAppointment(appointmentModel, fireStore)) {
                    is ApiSuccessResponse -> {
                        context.toast(resourceProvider.getString(R.string.appointment_booking_success))
                        _navigationListener.value = true
                        setShowProgress(false)

                    }

                    is ApiErrorResponse -> {
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        setShowProgress(false)
                    }

                    else -> {
                        setShowProgress(false)
                    }
                }
            }
        }
    }

    fun getDoctorData() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response =
                    appointmentRepository.getDoctorById(userId.value.toString(), fireStore)) {
                    is ApiSuccessResponse -> {
                        doctorName.value = response.body.name
                        val weekOffDbList = response.body.weekOffList
                        weekOffDbList?.forEachIndexed { index, s ->
                            weekOfDayList.add(s)
                        }
                        doctorSpecialities.value = response.body.specialities.toString()
                        val timeSlotDbList = response.body.availableTime
                        timeSlotDbList?.forEachIndexed { index, timeSlotRequestModel ->
                            timeList.add(TimeSlotModel(timeSlotRequestModel.timeSlot, timeSlotRequestModel.isTimeSlotBook))
                        }
                        _timeSlotList.value = timeList

                        getUserData()
                        get15DaysList()
                    }

                    is ApiErrorResponse -> {
                        context.toast(response.errorMessage)
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        context.toast(response.errorMessage)
                        setShowProgress(false)
                    }

                    else -> {
                        context.toast(resourceProvider.getString(R.string.something_went_wrong))
                        setShowProgress(false)
                    }
                }
            } else {
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
            }
        }
    }

    private fun getUserData() {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    when (val response =
                        appointmentRepository.getUserById(it.toString(), fireStore)) {
                        is ApiSuccessResponse -> {
                            userName.value = response.body.name
                            contactNumber.value = response.body.contactNumber
                            ageDate = response.body.dob!!
                            age.value = calculateAge(response.body.dob.toString())
                            setShowProgress(false)
                        }

                        is ApiErrorResponse -> {
                            context.toast(response.errorMessage)
                            setShowProgress(false)
                        }

                        is ApiNoNetworkResponse -> {
                            context.toast(response.errorMessage)
                            setShowProgress(false)
                        }

                        else -> {
                            context.toast(resourceProvider.getString(R.string.something_went_wrong))
                            setShowProgress(false)
                        }
                    }
                } else {
                    context.toast(resourceProvider.getString(R.string.check_internet_connection))
                }

            }
        }
    }

    private fun calculateAge(dateOfBirth: String): String {
        val dateFormatter = SimpleDateFormat(FULL_DATE_FORMAT)
        val dob: Date = dateFormatter.parse(dateOfBirth)
        val calendarDob = Calendar.getInstance()
        calendarDob.time = dob

        val currentDate = Calendar.getInstance()

        val years = currentDate.get(Calendar.YEAR) - calendarDob.get(Calendar.YEAR)
        if (currentDate.get(Calendar.DAY_OF_YEAR) < calendarDob.get(Calendar.DAY_OF_YEAR)) {
            // Adjust age if birthday hasn't occurred yet this year
            return "${years - 1}"
        }
        return years.toString()
    }

    fun onCancelClick() {
        cancelClick.value = true
    }

    fun onConfirmClick() {
        confirmClick.value = true
    }

    fun onRejectClick() {
        rejectClick.value = true
    }

    fun onSubmit() {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                if (context.isNetworkAvailable()) {
                    when (val response =
                        appointmentRepository.updateAppointmentData(
                            appoinmentObj.value!!.apply {
                                reason = reasonValue.value!!
                            },
                            fireStore
                        )) {
                        is ApiSuccessResponse -> {
                            setShowProgress(false)
                        }

                        is ApiErrorResponse -> {
                            context.toast(response.errorMessage)
                            setShowProgress(false)
                        }

                        is ApiNoNetworkResponse -> {
                            context.toast(response.errorMessage)
                            setShowProgress(false)
                        }

                        else -> {
                            context.toast(resourceProvider.getString(R.string.something_went_wrong))
                            setShowProgress(false)
                        }
                    }
                } else {
                    context.toast(resourceProvider.getString(R.string.check_internet_connection))
                }

            }
        }
    }
}