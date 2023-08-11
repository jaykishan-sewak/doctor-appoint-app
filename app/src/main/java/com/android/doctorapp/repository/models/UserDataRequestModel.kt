package com.android.doctorapp.repository.models

import com.google.firebase.firestore.Exclude

data class UserDataRequestModel (
    var userId: String? = null,
    var isDoctor: Boolean? = null,
    var email: String? = null,
    var name: String? = null,
    var address: String? = null,
    var contactNumber :String? = null,
    var degree: String? = null,
    var specialities: String? = null,
    var availableDays: String? = null,
    var isEmailVerified: Boolean? = null,
    var isPhoneNumberVerified: Boolean? = null,
    var availableTime: String? = null,
    var imagesval: String? = null,
    var isAdmin: Boolean? = null,
    var isNotificationEnable: Boolean? = null,
    var dob: String? = null,
    var isUserVerified: Boolean? = null,
    var onlineAvailabilityDateTime: String? = null,
    var offlineAvailabilityDateTime: String? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name
        )
    }
}