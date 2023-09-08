package com.android.doctorapp.repository

import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class AppointmentRepository @Inject constructor() {

    suspend fun addBookingAppointment(appointmentModel: AppointmentModel,
    firestore: FirebaseFirestore): ApiResponse<AppointmentModel> {
        return try {
            val bookingAppointmentResponse = firestore.collection("appointment")
                .add(appointmentModel).await()
            ApiResponse.create(response = Response.success(appointmentModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

}