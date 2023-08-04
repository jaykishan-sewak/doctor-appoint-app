package com.android.infusiontemplate.repository.network

import com.android.infusiontemplate.repository.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

private const val END_URL_LOGIN_WITH_EMAIL = "login_success"
private const val END_URL_GET_ITEMS = "items"
private const val END_URL_GET_PROFILE = "getProfile"
private const val END_URL_REGISTER = "register"

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
}
