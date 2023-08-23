package com.android.doctorapp.repository.models


data class DegreeResponseModel(
    var degreeId: String = "",
    var degreeName: List<String>? = null
) {
}