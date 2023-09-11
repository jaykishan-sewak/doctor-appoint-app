package com.android.doctorapp.util.constants

object ConstantKey {

    object BundleKeys {
        const val IS_DOCTOR_OR_USER = "isDoctorOrUser"
        const val STORED_VERIFICATION_Id_KEY = "storedVerificationId"
        const val IS_DOCTOR_OR_USER_KEY = "isDoctorOrUser"
        const val USER_CONTACT_NUMBER_KEY = "userContactNumber"
        const val USER_NAME = "userName"
        const val USER_EMAIL = "userEmail"
        const val USER_ID = "userId"

    }

    const val DOCTOR = "DOCTOR"
    const val USER = "USER"
    const val NOT_FOUND = "Not Found"
    const val SUCCESS = "Success"
    const val GENDER_VALUE = "MALE"

    object DBKeys {
        const val TABLE_NAME = "user_data"
        const val FIELD_ADMIN = "admin"
        const val FIELD_DOCTOR = "doctor"
        const val TABLE_DEGREE = "degree"
        const val TABLE_SPECIALIZATION = "specialization"
        const val FIELD_USER_ID = "userId"
        const val TABLE_APPOINTMENT = "appointment"
    }

    const val FORMATTED_DATE = "dd-mm-yyyy"
    const val FULL_DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy"
    const val DATE_AND_DAY_NAME_FORMAT = "EE dd"
    const val HOUR_MIN_AM_PM_FORMAT = "h:mm a"


}