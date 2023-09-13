package com.android.doctorapp.repository

import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_USER_ID
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.TABLE_APPOINTMENT
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.TABLE_NAME
import com.google.firebase.firestore.DocumentSnapshot
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
            val bookingAppointmentResponse = fireStore.collection(TABLE_APPOINTMENT)
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
            val response = fireStore.collection(TABLE_NAME)
                .whereEqualTo(FIELD_USER_ID, userId)
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

    suspend fun getUserById(userId: String, fireStore: FirebaseFirestore): ApiResponse<UserDataResponseModel> {
        return try {
            val response = fireStore.collection(TABLE_NAME)
                .whereEqualTo(FIELD_USER_ID, userId)
                .get()
                .await()
            var dataModel = UserDataResponseModel()
            for (snapshot in response) {
                dataModel = snapshot.toObject()
            }
            ApiResponse.create(response = Response.success(dataModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getAppointmentsList(firestore: FirebaseFirestore): ApiResponse<List<AppointmentModel>> {
        return try {
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_APPOINTMENT).get().await()

            val appointmentsList = arrayListOf<AppointmentModel>()
            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(AppointmentModel::class.java)
                user?.let {
                    appointmentsList.add(it)
                }
            }
            ApiResponse.create(response = Response.success(appointmentsList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }


    suspend fun updateAppointmentData(
        requestModel: AppointmentModel,
        fireStore: FirebaseFirestore
    ): ApiResponse<AppointmentModel> {
        return try {
            val response = fireStore.collection(TABLE_APPOINTMENT)
                .whereEqualTo(FIELD_USER_ID, requestModel.userId)
                .get()
                .await()

            val updateUserResponse = fireStore.collection(TABLE_APPOINTMENT)
                .document(response.documents[0].id)
                .set(requestModel)
                .await()

            ApiResponse.create(response = Response.success(requestModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }
}