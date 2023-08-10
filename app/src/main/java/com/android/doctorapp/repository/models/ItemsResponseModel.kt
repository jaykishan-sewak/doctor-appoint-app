package com.android.doctorapp.repository.models
import com.google.gson.annotations.SerializedName


data class ItemsResponseModel(
    @SerializedName("data")
    var `data`: List<Data>? = null,
    @SerializedName("message")
    var message: String? = null,
    @SerializedName("success")
    var success: Boolean? = null
)

data class Data(
    @SerializedName("Description")
    var description: String? = null,
    @SerializedName("price")
    var price: Int? = null,
    @SerializedName("product_name")
    var productName: String? = null
) {
    val currencyPrice : String
    get() = "$price INR"
}