package com.android.doctorapp.repository.models


data class LoginResponseModel(
    val firstName: String,
    val id: String,
    val lastName: String,
    val token: String
)
