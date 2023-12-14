package com.android.doctorapp.ui.appointment

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AppointmentRepository
import com.android.doctorapp.repository.ItemsRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.models.AddShiftTimeModel
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.DataRequestModel
import com.android.doctorapp.repository.models.DateSlotModel
import com.android.doctorapp.repository.models.NotificationRequestModel
import com.android.doctorapp.repository.models.SymptomModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.APPOINTMENT_APPROVED_BY
import com.android.doctorapp.util.constants.ConstantKey.APPOINTMENT_BOOKED_BY
import com.android.doctorapp.util.constants.ConstantKey.APPOINTMENT_REJECTED_BY
import com.android.doctorapp.util.constants.ConstantKey.DATE_MM_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.DATE_MONTH_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.DAY_NAME_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.FIELD_PENDING
import com.android.doctorapp.util.constants.ConstantKey.FIELD_REJECTED
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_DATE
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_TIME
import com.android.doctorapp.util.constants.ConstantKey.FULL_DAY_NAME_FORMAT
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.convertToFormatDate
import com.android.doctorapp.util.extension.currentDate
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.getCurrentDate
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
    private val itemsRepository: ItemsRepository,
    private val appointmentRepository: AppointmentRepository,
    val session: Session,
    private val context: Context,
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {

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
    val imageUri = MutableLiveData<Uri?>()


    val doctorDetails: MutableLiveData<UserDataResponseModel?> = MutableLiveData()
    var appointmentResponse: MutableLiveData<AppointmentModel?> = MutableLiveData()
    var userDataResponse: MutableLiveData<UserDataResponseModel?> = MutableLiveData()
    var symptomResponse: MutableLiveData<SymptomModel?> = MutableLiveData()

    val symptomDetails: MutableLiveData<String> = MutableLiveData()
    val sufferingDays: MutableLiveData<String> = MutableLiveData()
    var isSymptomDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    val symptomDetailsError: MutableLiveData<String?> = MutableLiveData()
    val sufferingDaysError: MutableLiveData<String?> = MutableLiveData()
    private val currentDate: String = getCurrentDate()

    val isVisitedToggleData: MutableLiveData<Boolean> = MutableLiveData(false)

    var updateClick = MutableLiveData(false)

    var dateStr: MutableLiveData<String> = MutableLiveData()

    val phoneClick: MutableLiveData<String> = MutableLiveData()
    val emailClick: MutableLiveData<String> = MutableLiveData()
    val isDirectionClick: MutableLiveData<Boolean> = MutableLiveData(false)

    var doctorDataObj = MutableLiveData<UserDataResponseModel>()

    val documentId: MutableLiveData<String> = MutableLiveData()

    val viewClinicClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val clinicImageList = MutableLiveData<ArrayList<String>?>()
    val isDarkThemeEnable: MutableLiveData<Boolean?> = MutableLiveData(false)
    val btnEnableOrNot: MutableLiveData<Boolean> = MutableLiveData(false)


    init {
        emailClick.postValue("")
        getUserData()
    }

    private fun get15DaysList() {
        val dateList = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        calendar.time =
            SimpleDateFormat(FORMATTED_DATE, Locale.getDefault()).parse(currentDate) as Date

        // Add the current date to the list
        dateList.add(
            SimpleDateFormat(
                FORMATTED_DATE, Locale.getDefault()
            ).parse(currentDate) as Date
        )

        for (i in 1 until 15) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            dateList.add(calendar.time)
        }

        daysList.clear()
        dateList.forEach {
            daysList.add(DateSlotModel(date = it, disable = false))
        }

        daysList.forEachIndexed { index, dateSlotModel ->
            if (dateSlotModel.date == SimpleDateFormat(FORMATTED_DATE, Locale.getDefault()).parse(
                    currentDate
                ) as Date
            ) {
                timeList.forEachIndexed { timeIndex, addShiftTimeModel ->
                    val currentTime = dateFormatter(Date(), FORMATTED_TIME)
                    if (currentTime > dateFormatter(addShiftTimeModel.startTime, FORMATTED_TIME)) {
                        timeList.get(timeIndex).isTimeSlotBook = true
                    }
                }
                dateStr.value = dateFormatter(currentDate(), ConstantKey.FORMATTED_DATE_MONTH_YEAR)
                isDateSelected.value = true
                daysList[index].dateSelect = true
            }


            holidayList.forEachIndexed { _, data ->
                if (dateFormatter(dateSlotModel.date!!, DATE_MM_FORMAT) == dateFormatter(
                        data, DATE_MM_FORMAT
                    )
                ) {
                    daysList[index] = DateSlotModel(date = dateSlotModel.date, disable = true)
                    return@forEachIndexed
                }
            }
            weekOfDayList.forEachIndexed { _, str ->
                if (dateFormatter(dateSlotModel.date!!, DAY_NAME_FORMAT) == convertToFormatDate(
                        str, FULL_DAY_NAME_FORMAT, DAY_NAME_FORMAT
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

    fun validateDateTime() {
        isBookAppointmentDataValid.value =
            isDateSelected.value == true && isTimeSelected.value == true && isSymptomDataValid.value == true
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
                    doctorId = doctorDataObj.value?.userId.toString(),
                    symptomDetails = symptomDetails.value.toString(),
                    sufferingDay = sufferingDays.value.toString()
                )
                when (val response =
                    appointmentRepository.addBookingAppointment(appointmentModel, fireStore)) {
                    is ApiSuccessResponse -> {
                        if (doctorDataObj.value?.isNotificationEnable == true) sendNotification(
                            doctorDataObj.value?.token,
                            APPOINTMENT_BOOKED_BY,
                            doctorDataObj.value?.isDoctor,
                            response.body,
                            true
                        )
                        else {
                            setShowProgress(false)
                            _navigationListener.value = true
                        }
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
                getAppointmentData()
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
            val formattedDateOfBirth = dateFormatter(dateOfBirth, DATE_MONTH_FORMAT)
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
                when (val response = appointmentRepository.updateAppointmentData(
                    appointmentObj.value!!.apply {
                        reason = text
                        status = FIELD_REJECTED
                    }, fireStore
                )) {
                    is ApiSuccessResponse -> {
                        if (userDataResponse.value?.isNotificationEnable == true) sendNotification(
                            userDataResponse.value?.token,
                            APPOINTMENT_REJECTED_BY,
                            userDataResponse.value?.isDoctor,
                            appointmentObj.value?.id,
                            false
                        )
                        else {
                            setShowProgress(false)
                            _navigationListener.value = true
                        }

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
                when (val response = appointmentRepository.getAppointmentDetails(
                    appointmentObj.value?.id!!, fireStore
                )) {
                    is ApiSuccessResponse -> {
                        appointmentResponse.postValue(response.body)
                        isVisitedToggleData.postValue(response.body.isVisited)
                        getAppointmentUserDetails()
                        getUserSymptomDetails()
                        setShowProgress(false)
                    }

                    is ApiErrorResponse -> {
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
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
                when (val response = appointmentRepository.getAppointmentUserDetails(
                    appointmentObj.value?.userId!!, fireStore
                )) {
                    is ApiSuccessResponse -> {
                        userDataResponse.value = response.body
                        imageUri.value = response.body.images?.toUri()
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
                when (val response = appointmentRepository.updateAppointmentData(
                    appointmentObj.value!!.apply {
                        status = appointmentStatus
                    }, fireStore
                )) {
                    is ApiSuccessResponse -> {
                        if (userDataResponse.value?.isNotificationEnable == true) {
                            if (appointmentStatus == FIELD_REJECTED) sendNotification(
                                userDataResponse.value?.token,
                                APPOINTMENT_REJECTED_BY,
                                userDataResponse.value?.isDoctor,
                                appointmentObj.value!!.id,
                                false
                            )
                            else sendNotification(
                                userDataResponse.value?.token,
                                APPOINTMENT_APPROVED_BY,
                                userDataResponse.value?.isDoctor,
                                appointmentObj.value!!.id,
                                false
                            )
                        } else {
                            setShowProgress(false)
                            _navigationListener.value = true
                        }
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
        btnEnableOrNot.value =
            if (appointmentObj.value != null) appointmentObj.value?.bookingDateTime!! > currentDate && appointmentObj.value?.status != FIELD_REJECTED
            else false
        return btnEnableOrNot.value!!
    }

    private fun getUserSymptomDetails() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                when (val response = appointmentRepository.getUserSymptomDetails(
                    appointmentObj.value?.userId!!, fireStore
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

    private fun validatesAllFields(): Boolean {
        isSymptomDataValid.value =
            (!symptomDetails.value.isNullOrEmpty() && !sufferingDays.value.isNullOrEmpty() && symptomDetailsError.value.isNullOrEmpty() && sufferingDaysError.value.isNullOrEmpty())
        validateDateTime()
        return isSymptomDataValid.value!!
    }

    fun isValidSymptomDetails(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            symptomDetailsError.value = resourceProvider.getString(R.string.valid_symptom_desc)
        } else if (text?.get(0)?.isLetter() != true) {
            symptomDetailsError.value =
                resourceProvider.getString(R.string.valid_symptom_start_with_char)
        } else {
            symptomDetailsError.value = null
        }
        validatesAllFields()
    }

    fun isValidNumberOfDays(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            sufferingDaysError.value =
                resourceProvider.getString(R.string.valid_number_of_days_desc)
        } else {
            sufferingDaysError.value = null
        }
        validatesAllFields()
    }

    fun onUpdateClick() {
        updateClick.value = true
    }

    fun appointmentUpdateApiCall() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = appointmentRepository.updateAppointmentDataById(
                    appointmentObj.value!!.apply {
                        isVisited = isVisitedToggleData.value!!
                    }, fireStore
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

    fun getAppointmentData(
        selectedDate: Date = SimpleDateFormat(
            FORMATTED_DATE, Locale.getDefault()
        ).parse(currentDate) as Date
    ) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = appointmentRepository.getDoctorAppointmentByDate(
                    doctorDataObj.value?.userId, selectedDate, fireStore
                )) {
                    is ApiSuccessResponse -> {
                        if (timeList.isEmpty()) {
                            getDoctorDataDetails(response.body)
                        } else {
                            timeList.forEachIndexed { index, addShiftTimeModel ->
                                addShiftTimeModel.isTimeSlotBook =
                                    currentTime(selectedDate, addShiftTimeModel.startTime)
                            }

                            response.body.forEachIndexed { index, appointmentModel ->
                                timeList.forEachIndexed { timeIndex, addShiftTimeModel ->
                                    if (dateFormatter(
                                            appointmentModel.bookingDateTime, FORMATTED_TIME
                                        ) == dateFormatter(
                                            addShiftTimeModel.startTime, FORMATTED_TIME
                                        ) || currentTime(selectedDate, addShiftTimeModel.startTime)
                                    ) {
                                        timeList[timeIndex].isTimeSlotBook = true
                                    }
                                }
                            }
                            _timeSlotList.value = timeList
                            setShowProgress(false)
                        }
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

    private fun getDoctorDataDetails(appointmentList: ArrayList<AppointmentModel>) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                when (val doctorResponse = appointmentRepository.getDoctorById(
                    doctorDataObj.value?.docId!!, fireStore
                )) {

                    is ApiSuccessResponse -> {
                        doctorName.value = doctorResponse.body.name
                        clinicImageList.value = doctorResponse.body.clinicImg
                        val weekOffDbList = doctorResponse.body.weekOffList
                        weekOffDbList?.forEachIndexed { index, s ->
                            weekOfDayList.add(s)
                        }
                        doctorResponse.body.holidayList?.forEachIndexed { index, holidayModel ->
                            holidayList.add(holidayModel)
                        }
                        doctorSpecialities.value = doctorResponse.body.specialities.toString()

                        val tempList = doctorResponse.body.availableTime?.sortedBy {
                            it.startTime
                        }

                        timeList.clear()

                        tempList?.forEachIndexed { index, addShiftResponseModel ->
                            timeList.add(
                                AddShiftTimeModel(
                                    startTime = addShiftResponseModel.startTime,
                                    endTime = addShiftResponseModel.endTime,
                                    isTimeSlotBook = addShiftResponseModel.isTimeSlotBook
                                )
                            )
                        }

                        appointmentList.forEachIndexed { index, appointmentModel ->
                            timeList.forEachIndexed { timeIndex, addShiftTimeModel ->
                                if (dateFormatter(
                                        appointmentModel.bookingDateTime, FORMATTED_TIME
                                    ) == dateFormatter(addShiftTimeModel.startTime, FORMATTED_TIME)
                                ) {
                                    timeList.get(timeIndex).isTimeSlotBook = true
                                }
                            }
                        }

                        doctorDetails.value = doctorResponse.body
                        imageUri.value = doctorResponse.body.images?.toUri()
                        _timeSlotList.value = timeList
                        _holidayDateList.value = holidayList
                        getUserData()
                        get15DaysList()

                        setShowProgress(false)
                    }

                    is ApiErrorResponse -> {
                        context.toast(doctorResponse.errorMessage)
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        context.toast(doctorResponse.errorMessage)
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

    fun onClickPhoneIcon(contact: String) {
        phoneClick.postValue(contact)
    }

    fun onClickEmailIcon(email: String) {
        emailClick.postValue(email)
    }

    fun onClickDirectionIcon() {
        isDirectionClick.postValue(true)
    }

    private fun currentTime(selectedDate: Date, time: Date?): Boolean {
        val currentDate = Date()
        val currentTime = dateFormatter(Date(), FORMATTED_TIME)
        if (dateFormatter(
                selectedDate, ConstantKey.DD_MM_FORMAT
            ) == dateFormatter(
                currentDate, ConstantKey.DD_MM_FORMAT
            )
        ) {
            return (currentTime > dateFormatter(
                time, FORMATTED_TIME
            ))
        }
        return false
    }

    private fun sendNotification(
        token: String?, msg: String, type: Boolean?, documentId: String?, isBookAppointment: Boolean
    ) {
        viewModelScope.launch {
            setShowProgress(true)
            val data = DataRequestModel(
                "$msg ${userName.value}", "Appointment", type, documentId, isBookAppointment
            )
            val notificationRequest = NotificationRequestModel(token, data)
            when (val response = itemsRepository.sendNotification(notificationRequest)) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    _navigationListener.value = true
                    context.toast(resourceProvider.getString(R.string.appointment_booking_success))
                }

                is ApiErrorResponse -> {
                    setApiError(response.errorMessage)
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setNoNetworkError(response.errorMessage)
                    setShowProgress(false)
                }

                else -> {}
            }
            setShowProgress(false)
        }
    }

    fun getNotificationAppointmentDetails() {
        setShowProgress(true)
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                when (val response = appointmentRepository.getNotificationAppointmentDetails(
                    documentId.value!!, fireStore
                )) {
                    is ApiSuccessResponse -> {
                        appointmentResponse.postValue(response.body)
                        appointmentObj.value = response.body!!
                        checkAppointmentDate()
                        isVisitedToggleData.postValue(response.body.isVisited)
                        userDataResponse.postValue(response.body.doctorDetails)
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

    fun viewClinicClicked() {
        viewClinicClicked.value = true
    }


}
