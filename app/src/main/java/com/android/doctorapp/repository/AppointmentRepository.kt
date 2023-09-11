package com.android.doctorapp.repository

import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class AppointmentRepository @Inject constructor() {

    suspend fun addBookingAppointment(
        appointmentModel: AppointmentModel,
        fireStore: FirebaseFirestore
    ): ApiResponse<AppointmentModel> {
        return try {
            val bookingAppointmentResponse = fireStore.collection("appointment")
                .add(appointmentModel).await()
            ApiResponse.create(response = Response.success(appointmentModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getDoctorById(
        userId: String,
        fireStore: FirebaseFirestore
    ): ApiResponse<UserDataRequestModel> {
        return try {
            val response = fireStore.collection("user_data")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            var dataModel = UserDataRequestModel()
            for (snapshot in response) {
                dataModel = snapshot.toObject()
            }
            ApiResponse.create(response = Response.success(dataModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

}