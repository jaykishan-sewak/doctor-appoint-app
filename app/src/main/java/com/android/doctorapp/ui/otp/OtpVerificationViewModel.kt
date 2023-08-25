package com.android.doctorapp.ui.otp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.doctorapp.R
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.util.SingleLiveEvent
import com.android.doctorapp.util.extension.asLiveData
import com.android.doctorapp.util.extension.toast
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import javax.inject.Inject


class OtpVerificationViewModel @Inject constructor(
    private val context: Context
) : BaseViewModel() {

    val _otpDigit = MutableLiveData<String>()
    val otpDigit1: LiveData<String> get() = _otpDigit
    val isDataValid: MutableLiveData<Boolean> = MutableLiveData(false)
    val otpVerificationId: MutableLiveData<String?> = MutableLiveData()

    private val _navigationListener = SingleLiveEvent<Int>()
    val navigationListener = _navigationListener.asLiveData()

    val isDoctorOrUser: MutableLiveData<Boolean> = MutableLiveData(true)

    init {
        firebaseUser = firebaseAuth.currentUser!!
    }

    fun isOtpFilled(): Boolean {
        return _otpDigit.value?.isNotEmpty() == true
    }

    fun isAllValidate() {
        isDataValid.value = !otpDigit1.value.isNullOrEmpty()
    }

    fun otpVerification() {
        setShowProgress(true)
        if (otpDigit1.value?.isNotEmpty() == true) {
            val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                otpVerificationId.value.toString(), otpDigit1.value.toString()
            )
            signInWithPhoneAuthCredential(credential)
        } else {
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    setShowProgress(false)
                    if (isDoctorOrUser.value == true) {
                        _navigationListener.value = R.id.action_otpFragment_to_updateDoctorFragment
                    } else {
                        _navigationListener.value = R.id.action_otpFragment_to_updateUserFragment
                    }
                } else {
                    setShowProgress(false)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        context.toast("Invalid OTP")
                    }
                }
            }
    }
}