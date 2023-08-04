package com.android.infusiontemplate.repository

import com.android.infusiontemplate.repository.local.Session
import com.android.infusiontemplate.repository.models.ApiResponse
import com.android.infusiontemplate.repository.models.ProfileResponseModel
import com.android.infusiontemplate.repository.network.AppApi
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