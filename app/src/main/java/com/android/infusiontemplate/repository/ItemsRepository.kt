package com.android.infusiontemplate.repository

import com.android.infusiontemplate.repository.models.ApiResponse
import com.android.infusiontemplate.repository.models.ItemsResponseModel
import com.android.infusiontemplate.repository.network.AppApi
import javax.inject.Inject

class ItemsRepository @Inject constructor(private val appApi: AppApi) {

    suspend fun getItems(): ApiResponse<ItemsResponseModel> {
        return try {
            val response = appApi.getItems()
            ApiResponse.create(response = response)
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }
}