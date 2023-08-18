package com.android.doctorapp.ui.profile

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AddDoctorRepository
import javax.inject.Inject

class AddUserViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val addRepository: AddDoctorRepository
) : BaseViewModel() {

    private val isUserDataValid: MutableLiveData<Boolean> = MutableLiveData(false)

    val name: MutableLiveData<String> = MutableLiveData()
    private val nameError: MutableLiveData<String?> = MutableLiveData()

    private val address: MutableLiveData<String> = MutableLiveData()
    private val addressError: MutableLiveData<String?> = MutableLiveData()

    private val dob: MutableLiveData<String> = MutableLiveData()
    private val dobError: MutableLiveData<String?> = MutableLiveData()

    private val contactNum: MutableLiveData<String> = MutableLiveData()
    private val contactNumError: MutableLiveData<String?> = MutableLiveData()

    private fun isAllValidate() {
        isUserDataValid.value = (!name.value.isNullOrEmpty() && !address.value.isNullOrEmpty()
                && !dob.value.isNullOrEmpty() && !contactNum.value.isNullOrEmpty()
                && nameError.value.isNullOrEmpty() && addressError.value.isNullOrEmpty()
                && dobError.value.isNullOrEmpty() && contactNumError.value.isNullOrEmpty())
    }

    fun isValidName(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            nameError.value = resourceProvider.getString(R.string.valid_name_desc)
        } else if (text?.get(0)?.isLetter() != true) {
            nameError.value = resourceProvider.getString(R.string.valid_name_start_with_char)
        } else {
            nameError.value = null
        }
        isAllValidate()
    }

    fun isValidAddress(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty() || ((text?.toString()?.length ?: 0) < 3)) {
            nameError.value = resourceProvider.getString(R.string.valid_name_desc)
        } else {
            nameError.value = null
        }
        isAllValidate()
    }

    fun isValidContact(text: CharSequence?) {
        if (text?.toString().isNullOrEmpty()) {
            contactNumError.value =
                resourceProvider.getString(R.string.error_valid_phone_number)
        } else {
            if (Patterns.PHONE.matcher(text ?: "").matches()) {
                if (text?.toString()?.length == 10) {
                    contactNumError.value = null
                } else {
                    contactNumError.value =
                        resourceProvider.getString(R.string.error_valid_phone_number)
                }
            } else {
                contactNumError.value =
                    resourceProvider.getString(R.string.error_valid_phone_number)
            }
        }
        isAllValidate()
    }
}