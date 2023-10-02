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
import com.android.doctorapp.repository.models.AddShiftTimeModel
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.DateSlotModel
import com.android.doctorapp.repository.models.SymptomModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey.DATE_MM_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.DATE_MONTH_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.DAY_NAME_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.FIELD_PENDING
import com.android.doctorapp.util.constants.ConstantKey.FIELD_REJECTED
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_DATE
import com.android.doctorapp.util.constants.ConstantKey.FULL_DATE_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.FULL_DAY_NAME_FORMAT
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.convertToFormatDate
import com.android.doctorapp.util.extension.currentDate
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

    private val _timeSlotList = MutableLiveData<ArrayList<AddShiftTimeModel>>()
    val timeSlotList = _timeSlotList.asLiveData()
    private val timeList = ArrayList<AddShiftTimeModel>()

    private val _holidayDateList = MutableLiveData<ArrayList<Date>>()
    private val holidayList = ArrayList<Date>()

    private val weekOfDayList = ArrayList<String>()

    val isBookAppointmentDataValid: MutableLiveData<Boolean> = MutableLiveData(false)
    var isTimeSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    var isDateSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    val isBookAppointmentClick: MutableLiveData<Boolean> = MutableLiveData(false)
    val onlineBookingToggleData: MutableLiveData<Boolean> = MutableLiveData(false)

    val doctorId: MutableLiveData<String> = MutableLiveData("")
    val doctorName: MutableLiveData<String> = MutableLiveData()
    private val doctorSpecialities: MutableLiveData<String> = MutableLiveData()
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
    var appointmentObj = MutableLiveData<AppointmentModel>()

    val doctorDetails: MutableLiveData<UserDataResponseModel?> = MutableLiveData()
    var appointmentResponse: MutableLiveData<AppointmentModel?> = MutableLiveData()
    var userDataResponse: MutableLiveData<UserDataResponseModel?> = MutableLiveData()
    var symptomResponse: MutableLiveData<SymptomModel?> = MutableLiveData()

    private fun get15DaysList() {
        val currentDate: String = getCurrentDate()
        val dateList = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        calendar.time = dateFormatFull.parse(currentDate) as Date

        // Add the current date to the list
        dateList.add(dateFormatFull.parse(currentDate) as Date)

        for (i in 1 until 15) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            dateList.add(calendar.time)
        }

        dateList.forEach {
            daysList.add(DateSlotModel(date = it, disable = false))
        }

        daysList.forEachIndexed { index, dateSlotModel ->
            holidayList.forEachIndexed { _, data ->
                if (dateFormatter(dateSlotModel.date!!, DATE_MM_FORMAT) == dateFormatter(
                        data,
                        DATE_MM_FORMAT
                    )
                ) {
                    daysList[index] = DateSlotModel(date = dateSlotModel.date, disable = true)
                    return@forEachIndexed
                }
            }
            weekOfDayList.forEachIndexed { _, str ->
                if (dateFormatter(dateSlotModel.date!!, DAY_NAME_FORMAT) == convertToFormatDate(
                        str,
                        FULL_DAY_NAME_FORMAT,
                        DAY_NAME_FORMAT
                    )
                ) {
                    daysList[index] = DateSlotModel(date = dateSlotModel.date, disable = true)
                    return@forEachIndexed
                }
            }
        }
        _daysDateList.value = daysList
    }

    // Function to get the current date as a Date object
    private fun getCurrentDate(): String {
        val currentCal = Calendar.getInstance()
        return dateFormatFull.format(currentCal.time)
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
                    status = FIELD_PENDING,
                    name = userName.value.toString(),
                    age = age.value.toString(),
                    contactNumber = contactNumber.value.toString(),
                    userId = it.toString(),
                    doctorId = doctorId.value.toString()
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
                    appointmentRepository.getDoctorById(doctorId.value.toString(), fireStore)) {
                    is ApiSuccessResponse -> {
                        doctorName.value = response.body.name
                        val weekOffDbList = response.body.weekOffList
                        weekOffDbList?.forEachIndexed { index, s ->
                            weekOfDayList.add(s)
                        }
                        response.body.holidayList?.forEachIndexed { index, holidayModel ->
                            holidayList.add(holidayModel)
                        }
                        doctorSpecialities.value = response.body.specialities.toString()

                        val tempList = response.body.availableTime?.sortedBy {
                            it.startTime
                        }

                        tempList?.forEachIndexed { index, addShiftResponseModel ->
                            timeList.add(
                                AddShiftTimeModel(
                                    startTime = addShiftResponseModel.startTime,
                                    endTime = addShiftResponseModel.endTime,
                                    isTimeSlotBook = addShiftResponseModel.isTimeSlotBook
                                )
                            )
                        }

                        doctorDetails.value = response.body

                        _timeSlotList.value = timeList
                        _holidayDateList.value = holidayList

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
                            age.value = calculateAge(response.body.dob)
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

    private fun calculateAge(dateOfBirth: Date?): String {
        try {
            val formattedDateOfBirth = dateFormatter(dateOfBirth, FORMATTED_DATE)
            val dateFormatter = SimpleDateFormat(DATE_MONTH_FORMAT, Locale.getDefault())
            val dob: Date = dateFormatter.parse(formattedDateOfBirth)
            val calendarDob = Calendar.getInstance()
            calendarDob.time = dob

            val currentDate = Calendar.getInstance()

            val years = currentDate.get(Calendar.YEAR) - calendarDob.get(Calendar.YEAR)
            if (currentDate.get(Calendar.DAY_OF_YEAR) < calendarDob.get(Calendar.DAY_OF_YEAR)) {
                // Adjust age if birthday hasn't occurred yet this year
                return "${years - 1}"
            }
            return years.toString()
        } catch (e: Exception) {
            Log.d("TAG", "calculateAge: ${e.fillInStackTrace()}")
            return ""
        }
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


    fun appointmentRejectApiCall(text: String) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response =
                    appointmentRepository.updateAppointmentData(
                        appointmentObj.value!!.apply {
                            reason = text
                            status = FIELD_REJECTED
                        },
                        fireStore
                    )) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        _navigationListener.value = true
                    }

                    is ApiErrorResponse -> {
                        context.toast(response.errorMessage)
                        setShowProgress(false)
                        _navigationListener.value = true
                    }

                    is ApiNoNetworkResponse -> {
                        context.toast(response.errorMessage)
                        setShowProgress(false)
                        _navigationListener.value = true
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

    fun getAppointmentDetails() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                when (val response =
                    appointmentRepository.getAppointmentDetails(
                        appointmentObj.value?.id!!,
                        fireStore
                    )) {
                    is ApiSuccessResponse -> {
                        appointmentResponse.postValue(response.body)
                        getAppointmentUserDetails()
                        getUserSymptomDetails()
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

    private fun getAppointmentUserDetails() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                when (val response =
                    appointmentRepository.getAppointmentUserDetails(
                        appointmentObj.value?.userId!!,
                        fireStore
                    )) {
                    is ApiSuccessResponse -> {
                        userDataResponse.value = response.body
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

    fun updateAppointmentStatus(appointmentStatus: String) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response =
                    appointmentRepository.updateAppointmentData(
                        appointmentObj.value!!.apply {
                            status = appointmentStatus
                        },
                        fireStore
                    )) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        _navigationListener.value = true
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

    fun checkAppointmentDate(): Boolean {
        val currentDate = currentDate()
        return appointmentObj.value?.bookingDateTime!! > currentDate
    }

    private fun getUserSymptomDetails() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                when (val response =
                    appointmentRepository.getUserSymptomDetails(
                        appointmentObj.value?.userId!!,
                        fireStore
                    )) {
                    is ApiSuccessResponse -> {
                        symptomResponse.value = response.body
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