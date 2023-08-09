package com.android.doctorapp.repository.models
import com.google.gson.annotations.SerializedName


data class ProfileResponseModel(
    @SerializedName("data")
    var `data`: Data? = null,
    @SerializedName("message")
    var message: String? = null,
    @SerializedName("success")
    var success: Boolean? = null
) {

    data class Data(
        @SerializedName("address")
        var address: String? = null,
        @SerializedName("email")
        var email: String? = null,
        @SerializedName("firstName")
        var firstName: String? = null,
        @SerializedName("gender")
        var gender: String? = null,
        @SerializedName("id")
        var id: String? = null,
        @SerializedName("lastName")
        var lastName: String? = null,
        @SerializedName("profile")
        var profile: String? = null,
        @SerializedName("username")
        var username: String? = null
    )
}