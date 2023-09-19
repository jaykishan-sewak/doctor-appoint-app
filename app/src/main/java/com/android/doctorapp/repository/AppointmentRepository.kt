package com.android.doctorapp.repository

import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_APPROVED_KEY
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_SELECTED_DATE
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_USER_ID
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.TABLE_APPOINTMENT
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.TABLE_USER_DATA
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import java.util.Calendar
import java.util.Date
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
    ): ApiResponse<UserDataResponseModel> {
        return try {
            val response = fireStore.collection(TABLE_USER_DATA)
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

    suspend fun getUserById(
        userId: String,
        fireStore: FirebaseFirestore
    ): ApiResponse<UserDataResponseModel> {
        return try {
            val response = fireStore.collection(TABLE_USER_DATA)
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
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_APPOINTMENT)
                .whereEqualTo(FIELD_APPROVED_KEY, ConstantKey.FIELD_APPROVED)
                .get().await()


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

    suspend fun getAppointmentsSelectedDateList(
        date: Date,
        firestore: FirebaseFirestore
    ): ApiResponse<List<AppointmentModel>> {
        return try {
            val nextDate = Calendar.getInstance()
            nextDate.time = date
            nextDate.add(Calendar.DATE, 1)
            val response = firestore.collection(TABLE_APPOINTMENT)
                .whereEqualTo(
                    FIELD_APPROVED_KEY,
                    ConstantKey.FIELD_APPROVED
                )
                .whereGreaterThanOrEqualTo(FIELD_SELECTED_DATE, date)
                .whereLessThanOrEqualTo(FIELD_SELECTED_DATE, nextDate.time)
                .get().await()

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

    suspend fun getAppointmentsProgressList(
        date: Date,
        firestore: FirebaseFirestore
    ): ApiResponse<List<AppointmentModel>> {
        return try {
            val nextDate = Calendar.getInstance()
            nextDate.time = date
            nextDate.add(Calendar.DATE, 1)
            val response = firestore.collection(TABLE_APPOINTMENT)
                .whereEqualTo(FIELD_APPROVED_KEY, ConstantKey.FIELD_PENDING)
                .whereGreaterThanOrEqualTo(FIELD_SELECTED_DATE, date)
                .whereLessThanOrEqualTo(FIELD_SELECTED_DATE, nextDate.time)
                .get().await()
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

    suspend fun getAppointmentDetails(
        userId: String,
        fireStore: FirebaseFirestore
    ): ApiResponse<AppointmentModel> {
        return try {
            val response = fireStore.collection(TABLE_APPOINTMENT)
                .whereEqualTo(FIELD_USER_ID, userId)
                .get()
                .await()
            var dataModel = AppointmentModel()
            for (snapshot in response) {
                dataModel = snapshot.toObject()
            }
            ApiResponse.create(response = Response.success(dataModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getAppointmentUserDetails(
        userId: String,
        fireStore: FirebaseFirestore
    ): ApiResponse<UserDataResponseModel> {
        return try {
            val response = fireStore.collection(TABLE_USER_DATA)
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
}