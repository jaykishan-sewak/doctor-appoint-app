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
        const val DATE = "headerDate"
        const val DOCTOR_DATA = "doctorData"
        const val ITEM_POSITION = "itemPosition"
        const val ADMIN_FRAGMENT = "adminFragment"
        const val REQUEST_FRAGMENT = "REQUEST_FRAGMENT"
        const val APPOINTMENT_DATA = "appointmentData"
        const val BOOKING_FRAGMENT = "BOOKING_FRAGMENT"
        const val BOOKING_APPOINTMENT_DATA = "bookingAppointmentData"
        const val DOCTOR_ID = "doctorId"

    }

    const val DOCTOR = "DOCTOR"
    const val USER = "USER"
    const val NOT_FOUND = "Not Found"
    const val SUCCESS = "Success"
    const val MALE_GENDER = "MALE"
    const val FEMALE_GENDER = "FEMALE"
    const val FIELD_APPROVED = "APPROVED"
    const val FIELD_PENDING = "PENDING"
    const val FIELD_REJECTED = "REJECTED"

    object DBKeys {
        const val TABLE_USER_DATA = "user_data"
        const val FIELD_ADMIN = "admin"
        const val FIELD_DOCTOR = "doctor"
        const val TABLE_DEGREE = "degree"
        const val TABLE_SPECIALIZATION = "specialization"
        const val FIELD_USER_ID = "userId"
        const val TABLE_APPOINTMENT = "appointment"
        const val FIELD_SELECTED_DATE = "bookingDateTime"
        const val FIELD_APPROVED_KEY = "status"
        const val FIELD_DOCTOR_ID = "doctorId"
        const val TABLE_SYMPTOM = "symptom"
        const val TABLE_FEEDBACK = "feedback"


    }

    const val FORMATTED_DATE = "dd-MM-yyyy"
    const val DATE_MONTH_FORMAT = "dd-MMM-yyyy"
    const val FULL_DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy"
    const val DATE_AND_DAY_NAME_FORMAT = "EE dd"
    const val HOUR_MIN_AM_PM_FORMAT = "h:mm a"
    const val DATE_MM_FORMAT = "dd-MM-yyyy"
    const val DAY_NAME_FORMAT = "EEE"
    const val FULL_DAY_NAME_FORMAT = "EEEE"
    const val USER_TYPE_ADMIN = "ADMIN"
    const val USER_TYPE_USER = "USER"
    const val USER_TYPE_DOCTOR = "DOCTOR"
    const val BOOKING_DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss"
    const val FORMATTED_DATE_MONTH_YEAR = "EEE MMM dd yyyy"
    const val FORMATTED_HOUR_MINUTE_SECOND = "HH:mm:ss"

    const val GPS_REQUEST_CODE = 1

}