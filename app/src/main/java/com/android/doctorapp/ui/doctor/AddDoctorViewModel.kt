package com.android.doctorapp.ui.doctor

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.util.extension.isEmailAddressValid
import javax.inject.Inject

class AddDoctorViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider
): BaseViewModel() {


    private val TAG = "AddDoctorTag"
    val doctorName: MutableLiveData<String?> = MutableLiveData()
    val doctorNameError: MutableLiveData<String?> = MutableLiveData()

    val doctorEmail: MutableLiveData<String> = MutableLiveData()
    val doctorEmailError: MutableLiveData<String?> = MutableLiveData()

    val doctorContactNumber: MutableLiveData<String> = MutableLiveData()
    val doctorContactNumberError: MutableLiveData<String?> = MutableLiveData()

    fun isValidateName(text: CharSequence?) {
        doctorNameError.postValue(null)
        if (text?.toString().isNullOrEmpty()) {
            doctorNameError.postValue(resourceProvider.getString(R.string.valid_name_desc))
        } else if ((text?.toString()?.length!! >= 2) && ((text?.toString()?.length ?: 0) <= 3)) {
            doctorNameError.postValue(resourceProvider.getString(R.string.valid_name_desc))
        } else {
            doctorNameError.postValue(text.toString())

//            doctorName.postValue(text.toString())
        }
    }

    fun isValidEmail(text: CharSequence?) {
        doctorEmailError.postValue(null)
        if (text?.toString().isNullOrEmpty()) {
            doctorEmailError.postValue(resourceProvider.getString(R.string.enter_valid_email))
        } else if (text?.toString()?.isEmailAddressValid()?.not()!!) {
            doctorEmailError.postValue(resourceProvider.getString(R.string.enter_valid_email))
        } else {
        }
    }

    fun isValidContact(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            doctorContactNumberError.postValue(resourceProvider.getString(R.string.error_valid_phone_number))
        } else {
            if (Patterns.PHONE.matcher(text?.toString()).matches()) {
                if (text?.toString()?.length == 10) {
                     doctorContactNumberError.postValue(null)
                } else {
                     doctorContactNumberError.postValue(resourceProvider.getString(R.string.error_valid_phone_number))
                }
            } else {
                doctorContactNumberError.postValue(resourceProvider.getString(R.string.error_valid_phone_number))
            }
        }
    }
}