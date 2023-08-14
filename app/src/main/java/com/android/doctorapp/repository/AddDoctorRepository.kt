package com.android.doctorapp.repository

import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.DoctorResponse
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class AddDoctorRepository @Inject constructor() {

    suspend fun addDoctorData(doctorRequestModel: UserDataRequestModel, firestore: FirebaseFirestore): ApiResponse<DoctorResponse> {
        return try {
            val addDoctorResponse = firestore.collection("user_data").add(doctorRequestModel).await()
            ApiResponse.create(response = Response.success(DoctorResponse("Success")))
        } catch (e: Exception) {
//            ApiResponse.create(response = Response.success(DoctorResponse(e.message.toString())))
            ApiResponse.create(e.fillInStackTrace())
        }
    }
}