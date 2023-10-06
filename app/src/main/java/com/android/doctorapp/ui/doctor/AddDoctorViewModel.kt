package com.android.doctorapp.ui.doctor

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.RadioGroup
import androidx.core.net.toUri
import androidx.core.view.children
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUpdateDoctorProfileBinding
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AuthRepository
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_ID
import com.android.doctorapp.repository.local.USER_IS_EMAIL_VERIFIED
import com.android.doctorapp.repository.models.AddShiftRequestModel
import com.android.doctorapp.repository.models.AddShiftTimeModel
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.DegreeResponseModel
import com.android.doctorapp.repository.models.HolidayModel
import com.android.doctorapp.repository.models.SpecializationResponseModel
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.repository.models.WeekOffModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.constants.ConstantKey.DATE_MM_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.FEMALE_GENDER
import com.android.doctorapp.util.constants.ConstantKey.KEY_GEO_HASH
import com.android.doctorapp.util.constants.ConstantKey.KEY_LATITUDE
import com.android.doctorapp.util.constants.ConstantKey.KEY_LONGITUDE
import com.android.doctorapp.util.constants.ConstantKey.MALE_GENDER
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.isEmailAddressValid
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AddDoctorViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val authRepository: AuthRepository,
    private val context: Context,
    private val session: Session

) : BaseViewModel() {

    val TAG = AddDoctorViewModel::class.java.simpleName

    val name: MutableLiveData<String?> = MutableLiveData()
    val nameError: MutableLiveData<String?> = MutableLiveData()

    val email: MutableLiveData<String> = MutableLiveData()
    val emailError: MutableLiveData<String?> = MutableLiveData()

    val contactNumber: MutableLiveData<String> = MutableLiveData()
    val contactNumberError: MutableLiveData<String?> = MutableLiveData()

    val notificationToggleData: MutableLiveData<Boolean> = MutableLiveData(false)

    val isAddDataValid: MutableLiveData<Boolean> = MutableLiveData(false)
    val isUpdateDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()

    private val _addDoctorResponse = SingleLiveEvent<String>()
    val addDoctorResponse = _addDoctorResponse.asLiveData()

    private val _clickResponse: MutableLiveData<String> = SingleLiveEvent()
    val clickResponse = _clickResponse.asLiveData()
    private val degreeItems = SingleLiveEvent<DegreeResponseModel?>()
    val degreeList = degreeItems.asLiveData()
    private val specializationItems = SingleLiveEvent<SpecializationResponseModel?>()
    val specializationList = specializationItems.asLiveData()

    private val data = MutableLiveData<UserDataResponseModel>()

    val address: MutableLiveData<String> = MutableLiveData()
    val addressError: MutableLiveData<String?> = MutableLiveData()
//    val addressLat: MutableLiveData<Double?> = MutableLiveData()
//    val addressLng: MutableLiveData<Double?> = MutableLiveData()

    val dob: MutableLiveData<String> = MutableLiveData()
    val dobError: MutableLiveData<String?> = MutableLiveData()

    val isCalender: MutableLiveData<View> = SingleLiveEvent()
    val isAvailableDate: MutableLiveData<String?> = MutableLiveData()

    val isPhoneVerify: MutableLiveData<Boolean> = MutableLiveData(false)
    val isPhoneVerifyValue: MutableLiveData<String> =
        MutableLiveData(resourceProvider.getString(R.string.verify))

    val isDoctor: MutableLiveData<Boolean> = MutableLiveData(false)
    val isEmailSent: MutableLiveData<Boolean> = MutableLiveData(false)
    val emailVerifyLabel: MutableLiveData<String> =
        MutableLiveData(resourceProvider.getString(R.string.verify))
    val isEmailVerified: MutableLiveData<Boolean?> = MutableLiveData(false)

    val isEmailEnable: MutableLiveData<Boolean> = MutableLiveData(true)

    val isUserReload: MutableLiveData<Boolean?> = MutableLiveData(false)
    var binding: FragmentUpdateDoctorProfileBinding? = null

    val degreeLiveList = MutableLiveData<List<String>>()
    val specializationLiveList = MutableLiveData<List<String>>()
    private val selectGenderValue: MutableLiveData<String> =
        MutableLiveData(MALE_GENDER)
    val userId: MutableLiveData<String?> = MutableLiveData(null)
    private val tempEmail: MutableLiveData<String?> = MutableLiveData()
    private val tempContactNumber: MutableLiveData<String?> = MutableLiveData()


    private val weekDayList = ArrayList<WeekOffModel>()
    val weekDayNameList = MutableLiveData<ArrayList<WeekOffModel>>()

    val holidayList = MutableLiveData<ArrayList<HolidayModel>>()

    val addShiftTimeSlotList = MutableLiveData<ArrayList<AddShiftTimeModel>>()

    private val _dataResponse = SingleLiveEvent<UserDataRequestModel?>()
    val userResponse = _dataResponse.asLiveData()

    val isProfileNavigation: MutableLiveData<Boolean> = MutableLiveData(false)

    var gender: MutableLiveData<Int> = MutableLiveData()
    val isCameraClick: MutableLiveData<Boolean> = MutableLiveData(false)
    val isGalleryClick: MutableLiveData<Boolean> = MutableLiveData(false)
    val imageUri: MutableLiveData<Uri> = MutableLiveData<Uri>()

    val fees: MutableLiveData<String> = MutableLiveData()
    val feesError: MutableLiveData<String?> = MutableLiveData()
    val geoHash: MutableLiveData<String?> = MutableLiveData()
    var addressLatLngList = MutableLiveData<Map<String, Any>?>()

    fun setBindingData(binding: FragmentUpdateDoctorProfileBinding) {
        this.binding = binding
    }

    init {
        firebaseUser = firebaseAuth.currentUser!!
        getWeekDayList()

    }

    fun getUserData(): MutableLiveData<UserDataResponseModel> {
        viewModelScope.launch {
            var recordId = ""
            session.getString(USER_ID).collectLatest {
                recordId = it.orEmpty()
                val userObj: UserDataResponseModel
                if (context.isNetworkAvailable()) {
                    setShowProgress(true)
                    when (val response = authRepository.getRecordById(recordId, fireStore)) {
                        is ApiSuccessResponse -> {
                            userObj = UserDataResponseModel(
                                name = response.body.name,
                                email = response.body.email,
                                contactNumber = response.body.contactNumber,
                                isNotificationEnable = response.body.isNotificationEnable
                            )
                            isDoctor.value = response.body.isDoctor
                            isPhoneVerify.value = response.body.isPhoneNumberVerified
                            if (!response.body.isPhoneNumberVerified) {
                                if (firebaseAuth.currentUser?.phoneNumber.isNullOrEmpty()) {
                                    isPhoneVerify.value = false
                                    isPhoneVerifyValue.value =
                                        resourceProvider.getString(R.string.verify)
                                } else {
                                    isPhoneVerify.value = true
                                    isPhoneVerifyValue.value =
                                        resourceProvider.getString(R.string.verified)
                                }
                            } else {
                                isPhoneVerify.value = true
                                isPhoneVerifyValue.value =
                                    resourceProvider.getString(R.string.verified)
                                isEmailVerified.value = response.body.isEmailVerified
                            }
                            notificationToggleData.value = response.body.isNotificationEnable
                            degreeLiveList.value = response.body.degree?.toList()
                            specializationLiveList.value = response.body.specialities?.toList()
                            holidayList.value =
                                if (response.body.holidayList?.isNotEmpty() == true) response.body.holidayList?.map { holidayDate ->
                                    HolidayModel(
                                        holidayDate = holidayDate
                                    )
                                } as ArrayList<HolidayModel> else null


                            addShiftTimeSlotList.value =
                                if (response.body.availableTime?.isNotEmpty() == true)
                                    response.body.availableTime?.map { shiftModel ->
                                        AddShiftTimeModel(
                                            startTime = shiftModel.startTime,
                                            endTime = shiftModel.endTime,
                                            isTimeSlotBook = shiftModel.isTimeSlotBook
                                        )
                                    } as ArrayList<AddShiftTimeModel> else null

                            getDBWeekDayList(response.body.weekOffList)

                            data.value = userObj
                            _dataResponse.value = response.body
                            if (!response.body.images.isNullOrEmpty()) {
                                imageUri.value = response.body.images?.toUri()
                            }
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
        return data
    }

    fun validateAllUpdateField() {

        if (isDoctor.value!!) {

            if (addShiftTimeSlotList.value.isNullOrEmpty()) {
                isUpdateDataValid.value = false
            } else {
                isUpdateDataValid.value =
                    (!name.value.isNullOrEmpty() && !email.value.isNullOrEmpty()
                            && !contactNumber.value.isNullOrEmpty() && nameError.value.isNullOrEmpty()
                            && emailError.value.isNullOrEmpty() && contactNumberError.value.isNullOrEmpty()
                            && !address.value.isNullOrEmpty() && addressError.value.isNullOrEmpty()
                            && !dob.value.isNullOrEmpty() && dobError.value.isNullOrEmpty()
                            && addShiftTimeSlotList.value?.filter { shiftTIme ->
                        shiftTIme.startTime == null || shiftTIme.endTime == null
                    }.isNullOrEmpty()
                            && isPhoneVerify.value!!
                            && isEmailVerified.value!!
                            && binding?.chipGroup?.children?.toList()?.size!! > 0
                            && binding?.chipGroupSpec?.children?.toList()?.size!! > 0
                            )
            }

        } else {
            isUpdateDataValid.value = (!name.value.isNullOrEmpty() && !email.value.isNullOrEmpty()
                    && !contactNumber.value.isNullOrEmpty() && nameError.value.isNullOrEmpty()
                    && emailError.value.isNullOrEmpty() && contactNumberError.value.isNullOrEmpty()
                    && !address.value.isNullOrEmpty() && addressError.value.isNullOrEmpty()
                    && !dob.value.isNullOrEmpty() && dobError.value.isNullOrEmpty()
                    && isPhoneVerify.value!! && isEmailVerified.value!!
                    )
        }
    }

    private fun validateAllField() {
        isAddDataValid.value = (!name.value.isNullOrEmpty() && !email.value.isNullOrEmpty()
                && !contactNumber.value.isNullOrEmpty() && nameError.value.isNullOrEmpty()
                && emailError.value.isNullOrEmpty() && contactNumberError.value.isNullOrEmpty())
    }

    fun isValidName(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            nameError.value = resourceProvider.getString(R.string.valid_name_desc)
        } else if (text?.get(0)?.isLetter() != true) {
            nameError.value = resourceProvider.getString(R.string.valid_name_start_with_char)
        } else {
            nameError.value = null
        }
        validateAllField()
        validateAllUpdateField()
    }

    fun isValidEmail(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || text?.toString()?.isEmailAddressValid()
                ?.not() == true
        ) {
            emailError.value = resourceProvider.getString(R.string.enter_valid_email)
        } else {
            emailError.value = null
        }
        validateAllField()
        validateAllUpdateField()
    }

    fun isValidContact(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            contactNumberError.value =
                resourceProvider.getString(R.string.error_valid_phone_number)
        } else {
            if (Patterns.PHONE.matcher(text ?: "").matches()) {
                if (text?.toString()?.length == 10) {
                    contactNumberError.value = null
                } else {
                    contactNumberError.value =
                        resourceProvider.getString(R.string.error_valid_phone_number)
                }
            } else {
                contactNumberError.value =
                    resourceProvider.getString(R.string.error_valid_phone_number)
            }
        }
        validateAllField()

    }

    fun isValidAddress(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            addressError.value = resourceProvider.getString(R.string.valid_address_desc)
        } else {
            addressError.value = null
        }
        validateAllUpdateField()
    }

    fun isValidDob(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            dobError.value = resourceProvider.getString(R.string.valid_dob_desc)
        } else {
            dobError.value = null
        }
        validateAllUpdateField()
    }

    fun isDobGreater22() {
        dobError.value = resourceProvider.getString(R.string.age_validate)
        validateAllUpdateField()
    }

    fun genderSelect(group: RadioGroup, checkedId: Int) {
        if (R.id.radioButtonMale == checkedId) {
            selectGenderValue.value = MALE_GENDER
        } else {
            selectGenderValue.value = FEMALE_GENDER
        }
    }


    fun calenderClick(text_dob: View) {
        isCalender.value = text_dob
    }

    fun addDoctorData() {
        if (context.isNetworkAvailable()) {
            if (userId.value.isNullOrEmpty())
                addUserToAuthentication()
            else
                updateDoctorData()
        } else {
            context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    fun onUpdateClick() {
        if (context.isNetworkAvailable()) {
            if (imageUri.value != null && !imageUri.value.toString().startsWith("https:"))
                uploadImage(imageUri.value!!)
            else
                this.updateUser("")
        } else {
            context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    private fun addUserToAuthentication() {
        firebaseUser = firebaseAuth.currentUser!!
        if (firebaseUser != null) {
            // when firebaseUser is not null then

            viewModelScope.launch {
                setShowProgress(true)
                when (val response = authRepository.register(
                    firebaseAuth,
                    email = email.value!!,
                    password = "Admin@123",
                )) {

                    is ApiSuccessResponse -> {
                        if (!firebaseAuth.currentUser?.uid.isNullOrEmpty()) {
                            addUserData()
                        }
                    }

                    is ApiErrorResponse -> {
                        _addDoctorResponse.value = response.errorMessage
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        _addDoctorResponse.value = response.errorMessage
                        setShowProgress(false)
                    }

                    else -> {
                        setShowProgress(false)
                    }
                }
            }
        }
    }

    fun test() {
        viewModelScope.launch {
            /*when (val response = authRepository.testGeoHash(fireStore)) {
                else -> {

                }
            }*/

            val latitude = 23.0225
            val longitude = 72.5714

            val geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude))
            val data1 = hashMapOf(
                KEY_GEO_HASH to geohash,
                KEY_LATITUDE to latitude,
                KEY_LONGITUDE to longitude,
//                "geopoint" to GeoPoint(latitude, longitude)

                // ... other data fields
            )

            Log.d(TAG, "test: $data1")

//            val myData: HashMap<String, Any> = data1

            var recordId: String = ""
            session.getString(USER_ID).collectLatest {
                val userData: UserDataRequestModel
                isDoctor.value = true
                if (isDoctor.value == true) {
                    userData = UserDataRequestModel(
                        userId = it.toString(),
                        isDoctor = true,
                        email = email.value.toString(),
                        name = name.value.toString(),
                        gender = selectGenderValue.value.toString(),
                        address = address.value.toString(),
                        contactNumber = contactNumber.value.toString(),
                        doctorFees = fees.value?.toInt(),
                        degree = binding?.chipGroup?.children?.toList()
                            ?.map { (it as Chip).text.toString() } as ArrayList<String>?,
                        specialities = binding?.chipGroupSpec?.children?.toList()
                            ?.map { (it as Chip).text.toString() } as ArrayList<String>?,
                        isEmailVerified = true,
                        isPhoneNumberVerified = true,
                        availableTime = null /*addShiftTimeSlotList.value?.toList()
                            ?.map { newData ->
                                AddShiftRequestModel(
                                    startTime = newData.startTime,
                                    endTime = newData.endTime,
                                    isTimeSlotBook = newData.isTimeSlotBook
                                )
                            } as ArrayList<AddShiftRequestModel>*/,
                        isAdmin = false,
                        isNotificationEnable = notificationToggleData.value == true,
                        dob = null/*SimpleDateFormat(
                            DATE_MM_FORMAT,
                            Locale.getDefault()
                        ).parse(dob.value.toString())*/,
                        isUserVerified = true,
                        holidayList = null/*if (holidayList.value?.isNotEmpty() == true) holidayList.value?.toList()
                            ?.map { holidayDate -> holidayDate.holidayDate } as ArrayList<Date> else null*/,
                        weekOffList = null/*if (weekDayNameList.value?.isNotEmpty() == true) weekDayNameList.value?.toList()
                            ?.filter { it.isWeekOff == true }
                            ?.map { weekOffModel -> weekOffModel.dayName }
                                as ArrayList<String> else null*/,
//                            addressLatLng = data1
//                        addressLatLng = addressLatLngList.value
                    )
                } else {
                    //Here Code for User Update
                    userData = UserDataRequestModel(
                        userId = it.toString(),
                        isDoctor = false,
                        email = email.value.toString(),
                        name = name.value.toString(),
                        gender = selectGenderValue.value.toString(),
                        address = address.value.toString(),
                        contactNumber = contactNumber.value.toString(),
                        isEmailVerified = true,
                        isPhoneNumberVerified = true,
                        isAdmin = false,
                        dob = SimpleDateFormat(
                            DATE_MM_FORMAT,
                            Locale.getDefault()
                        ).parse(dob.value.toString()),
                        isUserVerified = true,

                        )

                }
                setShowProgress(true)
                when (val response = authRepository.testGeoHash(userData, fireStore)) {
                    is ApiSuccessResponse -> {
                        if (response.body.userId.isNotEmpty()) {
                            name.value = ""
                            email.value = ""
                            address.value = ""
                            contactNumber.value = ""
                            dob.value = ""
                            isAvailableDate.value = ""
                            setShowProgress(false)
                            if (isDoctor.value == true) {
                                _addDoctorResponse.value =
                                    resourceProvider.getString(R.string.success)
                            } else {
                                _addDoctorResponse.value =
                                    resourceProvider.getString(R.string.success)
                            }
                        }
                    }

                    is ApiErrorResponse -> {
                        _addDoctorResponse.value = response.errorMessage
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        _addDoctorResponse.value = response.errorMessage
                        setShowProgress(false)
                    }

                    else -> {
                        setShowProgress(false)
                    }
                }
            }

        }
    }

    private fun updateUser(imageUrl: String) {
        viewModelScope.launch {
            session.getString(USER_ID).collectLatest {
                val userData: UserDataRequestModel
                if (isDoctor.value == true) {
                    userData = UserDataRequestModel(
                        userId = it.toString(),
                        isDoctor = true,
                        email = email.value.toString(),
                        name = name.value.toString(),
                        gender = selectGenderValue.value.toString(),
                        address = address.value.toString(),
                        contactNumber = contactNumber.value.toString(),
                        doctorFees = fees.value?.toInt(),
                        degree = binding?.chipGroup?.children?.toList()
                            ?.map { (it as Chip).text.toString() } as ArrayList<String>?,
                        specialities = binding?.chipGroupSpec?.children?.toList()
                            ?.map { (it as Chip).text.toString() } as ArrayList<String>?,
                        isEmailVerified = true,
                        isPhoneNumberVerified = true,
                        availableTime = addShiftTimeSlotList.value?.toList()
                            ?.map { newData ->
                                AddShiftRequestModel(
                                    startTime = newData.startTime,
                                    endTime = newData.endTime,
                                    isTimeSlotBook = newData.isTimeSlotBook
                                )
                            } as ArrayList<AddShiftRequestModel>,
                        isAdmin = false,
                        isNotificationEnable = notificationToggleData.value == true,
                        dob = SimpleDateFormat(
                            DATE_MM_FORMAT,
                            Locale.getDefault()
                        ).parse(dob.value.toString()),
                        isUserVerified = true,
                        holidayList = if (holidayList.value?.isNotEmpty() == true) holidayList.value?.toList()
                            ?.map { holidayDate -> holidayDate.holidayDate } as ArrayList<Date> else null,
                        weekOffList = if (weekDayNameList.value?.isNotEmpty() == true) weekDayNameList.value?.toList()
                            ?.filter { it.isWeekOff == true }
                            ?.map { weekOffModel -> weekOffModel.dayName }
                                as ArrayList<String> else null,
                        images = imageUrl,
                        addressLatLng = addressLatLngList.value,
                        geohash = geoHash.value
                    )
                } else {
                    //Here Code for User Update
                    userData = UserDataRequestModel(
                        userId = it.toString(),
                        isDoctor = false,
                        email = email.value.toString(),
                        name = name.value.toString(),
                        gender = selectGenderValue.value.toString(),
                        address = address.value.toString(),
                        contactNumber = contactNumber.value.toString(),
                        isEmailVerified = true,
                        isPhoneNumberVerified = true,
                        isAdmin = false,
                        dob = SimpleDateFormat(
                            DATE_MM_FORMAT,
                            Locale.getDefault()
                        ).parse(dob.value.toString()),
                        isUserVerified = true,
                        images = imageUrl

                    )

                }
                setShowProgress(true)
                when (val response = authRepository.updateUserData(userData, fireStore)) {
                    is ApiSuccessResponse -> {
                        if (response.body.userId.isNotEmpty()) {
                            name.value = ""
                            email.value = ""
                            address.value = ""
                            contactNumber.value = ""
                            dob.value = ""
                            isAvailableDate.value = ""
                            setShowProgress(false)
                            if (isDoctor.value == true) {
                                _addDoctorResponse.value =
                                    resourceProvider.getString(R.string.success)
                            } else {
                                _addDoctorResponse.value =
                                    resourceProvider.getString(R.string.success)
                            }
                        }
                    }

                    is ApiErrorResponse -> {
                        _addDoctorResponse.value = response.errorMessage
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        _addDoctorResponse.value = response.errorMessage
                        setShowProgress(false)
                    }

                    else -> {
                        setShowProgress(false)
                    }
                }
            }
        }

    }

    private suspend fun addUserData() {
        val userData = UserDataRequestModel(
            userId = firebaseAuth.currentUser?.uid.toString(),
            isDoctor = true,
            email = email.value!!,
            name = name.value!!,
            contactNumber = contactNumber.value!!,
            isNotificationEnable = notificationToggleData.value == true
        )

        when (val response = authRepository.addDoctorData(userData, fireStore)) {
            is ApiSuccessResponse -> {
                if (response.body.userId.isNotEmpty()) {
                    name.value = ""
                    email.value = ""
                    contactNumber.value = ""
                    setShowProgress(false)
                    _navigationListener.value = R.id.action_addDoctorFragment_to_LoginFragment
                    _addDoctorResponse.value = resourceProvider.getString(R.string.success)
                }
            }

            is ApiErrorResponse -> {
                _addDoctorResponse.value = response.errorMessage
                setShowProgress(false)
            }

            is ApiNoNetworkResponse -> {
                _addDoctorResponse.value = response.errorMessage
                setShowProgress(false)
            }

            else -> {
                setShowProgress(false)
            }
        }
    }


    fun hideProgress() {
        setShowProgress(false)
    }

    fun contactVerify() {
        if (context.isNetworkAvailable()) {
            if (!contactNumber.value.isNullOrEmpty()) {
                setShowProgress(true)
                _clickResponse.value = contactNumber.value.toString()
            }
        } else {
            context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    fun onEmailVerifyClick() {
        if (context.isNetworkAvailable()) {
            if (!firebaseUser.isEmailVerified) {
                emailVerification()
            }
        } else {
            context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    private fun emailVerification() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.emailVerification(firebaseUser)) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    isEmailSent.postValue(true)
                }

                is ApiErrorResponse -> {
                    setApiError(response.errorMessage)
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setNoNetworkError(response.errorMessage)
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
                }
            }
        }
    }

    fun emailVerified() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.emailVerified(firebaseUser)) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    isEmailVerified.postValue(response.body)
                    isEmailEnable.value = !response.body
                    session.putBoolean(USER_IS_EMAIL_VERIFIED, response.body)
                }

                is ApiErrorResponse -> {
                    setApiError(response.errorMessage)
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setNoNetworkError(response.errorMessage)
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
                }
            }
        }
    }

    private fun userReload() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.userReload(firebaseUser)) {
                is ApiSuccessResponse -> {
                    setShowProgress(false)
                    isUserReload.postValue(response.body)
                }

                is ApiErrorResponse -> {
                    setApiError(response.errorMessage)
                    setShowProgress(false)
                }

                is ApiNoNetworkResponse -> {
                    setNoNetworkError(response.errorMessage)
                    setShowProgress(false)
                }

                else -> {
                    setShowProgress(false)
                }
            }
        }
    }


    suspend fun checkIsEmailEveryMin() {
        session.getBoolean(USER_IS_EMAIL_VERIFIED).collectLatest {
            if (it == null || !it) {
                if (context.isNetworkAvailable()) {
                    userReload()
                } else {
                    context.toast(resourceProvider.getString(R.string.check_internet_connection))
                }
            } else {
                isEmailVerified.postValue(true)
                isEmailEnable.value = false
            }
        }
    }


    fun getDegreeItems() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = authRepository.getDegreeList(fireStore)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        degreeItems.value = response.body
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
            } else
                context.toast(resourceProvider.getString(R.string.check_internet_connection))

        }
    }

    fun getSpecializationItems() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = authRepository.getSpecializationList(fireStore)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        specializationItems.value = response.body
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
            } else
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    fun addDegreeItems(data: String) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(false)
                when (val response = authRepository.addDegree(fireStore, data)) {
                    is ApiSuccessResponse -> {
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
            } else
                context.toast(resourceProvider.getString(R.string.check_internet_connection))

        }
    }

    fun addSpecializationItems(data: String) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(false)
                when (val response = authRepository.addSpecialization(fireStore, data)) {
                    is ApiSuccessResponse -> {
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
            } else
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
        }
    }

    private fun updateDoctorData() {
        viewModelScope.launch {
            if (tempEmail.value != email.value || tempContactNumber.value != contactNumber.value) {
                setShowProgress(true)
                val userData = UserDataRequestModel(
                    userId = userId.value!!,
                    isDoctor = true,
                    email = email.value!!,
                    name = name.value!!,
                    contactNumber = contactNumber.value!!,
                    isNotificationEnable = notificationToggleData.value == true
                )

                when (val response = authRepository.updateDoctorData(userData, fireStore)) {
                    is ApiSuccessResponse -> {
                        if (response.body.userId.isNotEmpty()) {
                            name.value = ""
                            email.value = ""
                            contactNumber.value = ""
                            setShowProgress(false)
                            _navigationListener.value =
                                R.id.action_addDoctorFragment_to_LoginFragment
                            _addDoctorResponse.value = resourceProvider.getString(R.string.success)
                        }
                    }

                    is ApiErrorResponse -> {
                        _addDoctorResponse.value = response.errorMessage
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        _addDoctorResponse.value = response.errorMessage
                        setShowProgress(false)
                    }

                    else -> {
                        setShowProgress(false)
                    }
                }
            }
        }
    }


    private fun getWeekDayList() {
        weekDayList.add(
            WeekOffModel(
                dayName = resourceProvider.getString(R.string.monday),
                isWeekOff = false
            )
        )
        weekDayList.add(
            WeekOffModel(
                dayName = resourceProvider.getString(R.string.tuesday),
                isWeekOff = false
            )
        )
        weekDayList.add(
            WeekOffModel(
                dayName = resourceProvider.getString(R.string.wednesday),
                isWeekOff = false
            )
        )
        weekDayList.add(
            WeekOffModel(
                dayName = resourceProvider.getString(R.string.thursday),
                isWeekOff = false
            )
        )
        weekDayList.add(
            WeekOffModel(
                dayName = resourceProvider.getString(R.string.friday),
                isWeekOff = false
            )
        )
        weekDayList.add(
            WeekOffModel(
                dayName = resourceProvider.getString(R.string.saturday),
                isWeekOff = false
            )
        )
        weekDayList.add(
            WeekOffModel(
                dayName = resourceProvider.getString(R.string.sunday),
                isWeekOff = false
            )
        )
    }

    private fun getDBWeekDayList(weekOffList: java.util.ArrayList<String>?) {
        if (weekOffList?.isNotEmpty() == true) {
            weekDayList.forEachIndexed { index, weekOffModel ->
                weekOffList.forEachIndexed { i, s ->
                    if (weekOffModel.dayName == s)
                        weekDayList[index].isWeekOff = true

                }
            }
        }
        weekDayNameList.value = weekDayList
    }

    fun clickOnCamera() {
        isCameraClick.value = true
    }

    fun clickOnGallery() {
        isGalleryClick.value = true
    }

    fun isValidFees(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            feesError.value = resourceProvider.getString(R.string.valid_fees_desc)
        } else {
            feesError.value = null
        }
        validateAllUpdateField()
    }


    private fun uploadImage(image: Uri) {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = authRepository.uploadImage(image, storage)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        if (response.body.isNotEmpty())
                            updateUser(response.body)
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
            } else
                context.toast(resourceProvider.getString(R.string.check_internet_connection))

        }
    }
}