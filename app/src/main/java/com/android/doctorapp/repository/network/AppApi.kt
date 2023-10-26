package com.android.doctorapp.repository.network

import com.android.doctorapp.repository.models.*
import com.android.doctorapp.util.constants.ConstantKey.CONTENT_TYPE
import com.android.doctorapp.util.constants.ConstantKey.SERVER_KEY
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

private const val END_URL_LOGIN_WITH_EMAIL = "login_success"
private const val END_URL_GET_ITEMS = "items"
private const val END_URL_GET_PROFILE = "getProfile"
private const val END_URL_REGISTER = "register"
private const val SUB_URL_GET_ITEMS = "api/"
private const val END_URL_NOTIFICATION = "fcm/send"

interface AppApi {

    @POST(END_URL_LOGIN_WITH_EMAIL)
    suspend fun login(
        @Body loginRequestModel: LoginRequestModel
    ): Response<LoginResponseModel>

    @GET(END_URL_GET_ITEMS)
    suspend fun getItems(): Response<ItemsResponseModel>

    @GET(END_URL_GET_PROFILE)
    suspend fun getProfile(): Response<ProfileResponseModel>

    @POST(END_URL_REGISTER)
    suspend fun register(
        @Body registerRequestModel: RegisterRequestModel
    ): Response<LoginResponseModel>


    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:${CONTENT_TYPE}")
    @POST(END_URL_NOTIFICATION)
    suspend fun sendNotification(
        @Body notificationRequest: NotificationRequestModel
    ): Response<NotificationResponseModel>

}
