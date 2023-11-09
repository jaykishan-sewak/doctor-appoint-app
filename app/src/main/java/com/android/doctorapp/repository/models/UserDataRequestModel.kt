package com.android.doctorapp.repository.models

import java.util.Date

data class UserDataRequestModel(
    var userId: String = "",
    var isDoctor: Boolean = false,
    var email: String = "",
    var name: String = "",
    var gender: String = "MALE",
    var address: String = "",
    var contactNumber: String = "",
    var doctorFees: Int? = null,
    var doctorDescription: String = "",
    var degree: ArrayList<String>? = null,
    var specialities: ArrayList<String>? = null,
    var isEmailVerified: Boolean = false,
    var isPhoneNumberVerified: Boolean = false,
    var availableTime: ArrayList<AddShiftRequestModel>? = null,
    var images: String? = "",
    var isAdmin: Boolean = false,
    var isNotificationEnable: Boolean = false,
    var dob: Date? = null,
    var isUserVerified: Boolean = false,
    var holidayList: ArrayList<Date>? = null,
    var weekOffList: ArrayList<String>? = null,
    var addressLatLng: Map<String, Any>? = null,
    var geohash: String? = null,
    var token: String? = null,
    var clinicImg: ArrayList<String>? = null
)

