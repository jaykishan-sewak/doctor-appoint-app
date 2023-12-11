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
        const val USER_DATA = "userData"
        const val OTP_FRAGMENT = "otpFragment"
        const val ADDRESS_FRAGMENT = "addressFragment"
        const val DOCTOR_PROFILE_FRAGMENT = "doctorProfileFragment"
        const val FROM_WHERE = "fromWhere"
        const val SELECTED_TAB = "selectedTab"
        const val BOOK_APPOINTMENT_DATA = "bookAppointmentData"
        const val IS_EDIT_CLICK = "isEditClick"
        const val FROM_SELECTED_APPOINTMENTS = "fromSelectedAppointments"
        const val FRAGMENT_TYPE = "fragmentType"
        const val DOCTOR_FRAGMENT = "DoctorFragment"
        const val DOCUMENT_ID = "DocumentId"
        const val IS_BOOK_APPOINTMENT = "isBookAppointment"
        const val USER_FRAGMENT = "UserFragment"
        const val APPOINTMENT_DOCUMENT_ID = "documentId"
        const val VIEW_CLINIC_IMAGE_URL = "viewClinicImageUrl"
        const val GET_CLINIC_IMAGE_LIST_KEY = "getClinicImageList"
        const val OPEN_IMAGE_POSITION_KEY = "openImagePosition"
        const val IS_DARK_THEME_ENABLED_KEY = "isDarkThemeEnabled"
        const val EXTRAS_KEY = "extrasKey"

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
    const val APPOINTMENT_DETAILS_UPDATED = "appointmentDetailsUpdated"
    const val FEEDBACK_SUBMITTED = "feedbackSubmitted"
    const val PROFILE_UPDATED = "profileUpdated"
    const val PAGINATION_LIMIT_KEY = 10.toLong()


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
        const val FIELD_VISITED_KEY = "visited"
        const val SUB_TABLE_FEEDBACK = "sub_feedback"
        const val FIELD_DOC_ID_KEY = "id"


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
    const val KEY_GEO_HASH = "geohash"
    const val KEY_LATITUDE = "latitude"
    const val KEY_LONGITUDE = "longitude"

    const val FORMATTED_TIME = "HH:mm"

    const val DATE_PICKER = "DatePicker"
    const val DD_MM_FORMAT = "dd-MM-yyyy"
    const val PAST_LABEL = "Past"
    const val UPCOMING_LABEL = "Upcoming"

    const val SERVER_KEY =
        "AAAAjGR1U8c:APA91bHBkrHTp6UKnC8KWd4OjTtkUnxK0r6UL_r9rrr58MfxiampTgfX7jE79fcqcS5DWHicuK2k4GJByNuFf0qfmaG3oBkHLD8hgTDNxE8tK6xI_hTbBJnMa4HvV35Tv5O2aMf6ui_0"
    const val CONTENT_TYPE = "application/json"
    const val APPOINTMENT_REJECTED_BY = "Booked appointment rejected by"
    const val APPOINTMENT_APPROVED_BY = "Appointment approved by"
    const val APPOINTMENT_BOOKED_BY = "Appointment booked by"

    const val CHANNEL_ID = "channelId"
    const val MY_CHANNEL = "myChannel"
}