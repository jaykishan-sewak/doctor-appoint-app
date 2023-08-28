package com.android.doctorapp.util.constants

object ConstantKey {

    object BundleKeys {
        const val IS_DOCTOR_OR_USER = "isDoctorOrUser"
        const val STORED_VERIFICATION_Id_KEY = "storedVerificationId"
        const val IS_DOCTOR_OR_USER_KEY = "isDoctorOrUser"
        const val USER_CONTACT_NUMBER_KEY = "userContactNumber"
    }

    const val DOCTOR = "DOCTOR"
    const val USER = "USER"

    object DBKeys {
        const val TABLE_NAME = "user_data"
        const val FIELD_ADMIN = "admin"
        const val FIELD_DOCTOR = "doctor"
        const val TABLE_DEGREE= "degree"
        const val TABLE_SPECIALIZATION= "specialization"

    }
}