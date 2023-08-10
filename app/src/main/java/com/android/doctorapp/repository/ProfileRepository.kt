package com.android.doctorapp.repository

import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.ProfileResponseModel
import com.android.doctorapp.repository.network.AppApi
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val appApi: AppApi,
    private val session: Session
) {

    suspend fun getUserProfile(): ApiResponse<ProfileResponseModel> {
        return try {
            val response = appApi.getProfile()
            ApiResponse.create(response = response)
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun clearLoggedInSession() = session.clearLoggedInSession()
}