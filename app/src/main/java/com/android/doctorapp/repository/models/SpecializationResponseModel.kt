package com.android.doctorapp.repository.models

data class SpecializationResponseModel(
    var specializationId: String = "",
    var specializations: List<String>? = null
) {
}