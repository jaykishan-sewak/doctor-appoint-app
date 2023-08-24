package com.android.doctorapp.repository.models

import java.util.Date

data class UserDataRequestModel(
    var userId: String = "",
    var isDoctor: Boolean = false,
    var email: String = "",
    var name: String = "",
    var gender: String = "MALE",
    var address: String = "",
    var contactNumber:String = "",
    var degree: ArrayList<String>? = null,
    var specialities: ArrayList<String>? = null,
    var availableDays: String = "",
    var isEmailVerified: Boolean = false,
    var isPhoneNumberVerified: Boolean = false,
    var availableTime: String = "",
    var images: String = "",
    var isAdmin: Boolean = false,
    var isNotificationEnable: Boolean = false,
    var dob: Date? = null,
    var isUserVerified: Boolean = false,
    var onlineAvailabilityDateTime: String = "",
    var offlineAvailabilityDateTime: String = ""
)
