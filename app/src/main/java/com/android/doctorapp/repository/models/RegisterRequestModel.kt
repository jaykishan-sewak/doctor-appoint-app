package com.android.doctorapp.repository.models

data class RegisterRequestModel(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val phone: String,
    val gender: String
)