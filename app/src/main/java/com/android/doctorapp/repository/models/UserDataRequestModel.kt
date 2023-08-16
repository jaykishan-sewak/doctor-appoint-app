package com.android.doctorapp.repository.models

data class UserDataRequestModel(
    var userId: String = "",
    var isDoctor: Boolean = false,
    var email: String = "",
    var name: String = "",
    var address: String = "",
    var contactNumber:String = "",
    var degree: String = "",
    var specialities: String = "",
    var availableDays: String = "",
    var isEmailVerified: Boolean = false,
    var isPhoneNumberVerified: Boolean = false,
    var availableTime: String = "",
    var images: String = "",
    var isAdmin: Boolean = false,
    var isNotificationEnable: Boolean = false,
    var dob: String = "",
    var isUserVerified: Boolean = false,
    var onlineAvailabilityDateTime: String = "",
    var offlineAvailabilityDateTime: String = ""
)