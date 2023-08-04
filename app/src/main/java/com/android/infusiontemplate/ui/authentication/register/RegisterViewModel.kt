package com.android.infusiontemplate.ui.authentication.register

import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.infusiontemplate.R
import com.android.infusiontemplate.di.ResourceProvider
import com.android.infusiontemplate.di.base.BaseViewModel
import com.android.infusiontemplate.repository.AuthRepository
import com.android.infusiontemplate.repository.models.*
import com.android.infusiontemplate.util.extension.asLiveData
import com.android.infusiontemplate.util.extension.isEmailAddressValid
import kotlinx.coroutines.launch
import javax.inject.Inject

class RegisterViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    val email: MutableLiveData<String> = MutableLiveData()
    val emailError: MutableLiveData<String?> = MutableLiveData()
    val password: MutableLiveData<String> = MutableLiveData()
    val passwordError: MutableLiveData<String?> = MutableLiveData()
    val firstName: MutableLiveData<String> = MutableLiveData()
    val firstNameError: MutableLiveData<String?> = MutableLiveData()
    val lastName: MutableLiveData<String> = MutableLiveData()
    val lastNameError: MutableLiveData<String?> = MutableLiveData()
    val phone: MutableLiveData<String> = MutableLiveData()
    val phoneError: MutableLiveData<String?> = MutableLiveData()
    val address: MutableLiveData<String> = MutableLiveData()
    val addressError: MutableLiveData<String?> = MutableLiveData()
    private var selectedGender = ""
    private val _registerResponse = MutableLiveData<LoginResponseModel?>()
    val registerResponse = _registerResponse.asLiveData()

    fun onCheckChanged(radioGroup: RadioGroup, id: Int) {
        selectedGender = radioGroup.findViewById<RadioButton>(id).text.toString()
    }

    fun onRegisterClick() {
        if (isValidInput()) {
            callRegisterApi()
        }
    }

    private fun callRegisterApi() {
        viewModelScope.launch {
            setShowProgress(true)
            when (val response = authRepository.register(
                RegisterRequestModel(
                    email = email.value.toString(),
                    password = password.value.toString(),
                    firstName = firstName.value.toString(),
                    lastName = lastName.value.toString(),
                    address = address.value.toString(),
                    phone = phone.value.toString(),
                    gender = selectedGender
                )
            )) {
                is ApiSuccessResponse -> {
                    _registerResponse.postValue(response.body)
                }
                is ApiErrorResponse -> {
                    setApiError(response.errorMessage)
                }
                is ApiNoNetworkResponse -> {
                    setNoNetworkError(response.errorMessage)
                }

                else -> {}
            }
            setShowProgress(false)
        }
    }

    private fun isValidInput(): Boolean {
        firstNameError.postValue(null)
        lastNameError.postValue(null)
        emailError.postValue(null)
        passwordError.postValue(null)
        phoneError.postValue(null)
        addressError.postValue(null)
        if (firstName.value.isNullOrEmpty()) {
            firstNameError.postValue(resourceProvider.getString(R.string.error_first_name))
        } else if (lastName.value.isNullOrEmpty()) {
            lastNameError.postValue(resourceProvider.getString(R.string.error_last_name))
        } else if (email.value.isNullOrEmpty()) {
            emailError.postValue(resourceProvider.getString(R.string.error_enter_email))
        } else if (email.value.toString().isEmailAddressValid().not()) {
            emailError.postValue(resourceProvider.getString(R.string.enter_valid_email))
        } else if (password.value.isNullOrEmpty()) {
            passwordError.postValue(resourceProvider.getString(R.string.error_enter_password))
        } else if (selectedGender.isEmpty()) {
            setApiError(resourceProvider.getString(R.string.error_select_gender))
        } else if (phone.value.isNullOrEmpty() || phone.value?.length != 10) {
            phoneError.postValue(resourceProvider.getString(R.string.error_valid_phone_number))
        } else if (address.value.isNullOrEmpty()) {
            addressError.postValue(resourceProvider.getString(R.string.error_enter_address))
        } else return true
        return false
    }
}