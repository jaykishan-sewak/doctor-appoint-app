package com.android.doctorapp.repository

import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.SymptomModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_APPROVED_KEY
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_DOCTOR_ID
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_SELECTED_DATE
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_USER_ID
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_VISITED_KEY
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.TABLE_APPOINTMENT
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.TABLE_SYMPTOM
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.TABLE_USER_DATA
import com.android.doctorapp.util.constants.ConstantKey.FIELD_APPROVED
import com.android.doctorapp.util.constants.ConstantKey.FIELD_PENDING
import com.android.doctorapp.util.constants.ConstantKey.FIELD_REJECTED
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

    suspend fun getAppointmentsList(
        userId: String,
        firestore: FirebaseFirestore
    ): ApiResponse<List<AppointmentModel>> {
        return try {
            val response = firestore.collection(TABLE_APPOINTMENT)
                .whereEqualTo(FIELD_DOCTOR_ID, userId)
                .whereIn(FIELD_APPROVED_KEY, arrayListOf(FIELD_APPROVED, FIELD_REJECTED))
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
                .whereGreaterThanOrEqualTo(FIELD_SELECTED_DATE, date)
                .whereLessThanOrEqualTo(FIELD_SELECTED_DATE, nextDate.time)
//                .whereIn(FIELD_APPROVED_KEY, listOf(FIELD_APPROVED, FIELD_REJECTED))
                .get().await()

            val appointmentsList = arrayListOf<AppointmentModel>()
            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(AppointmentModel::class.java)
                user?.let {
                    it.id = document.id
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
        userId: String,
        firestore: FirebaseFirestore
    ): ApiResponse<List<AppointmentModel>> {
        return try {
            val nextDate = Calendar.getInstance()
            nextDate.time = date
            nextDate.add(Calendar.DATE, 1)
            val response = firestore.collection(TABLE_APPOINTMENT)
                .whereEqualTo(FIELD_DOCTOR_ID, userId)
                .whereEqualTo(FIELD_APPROVED_KEY, FIELD_PENDING)
                .whereGreaterThanOrEqualTo(FIELD_SELECTED_DATE, date)
                .whereLessThanOrEqualTo(FIELD_SELECTED_DATE, nextDate.time)
                .get().await()
            val appointmentsList = arrayListOf<AppointmentModel>()
            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(AppointmentModel::class.java)
                user?.id = document.id
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

    suspend fun updateAppointmentDataById(
        requestModel: AppointmentModel,
        fireStore: FirebaseFirestore
    ): ApiResponse<AppointmentModel> {
        return try {
            val requestData = requestModel.copy(doctorDetails = null)
            val response =
                fireStore.collection(TABLE_APPOINTMENT).document(requestModel.id)
                    .set(requestData).await()

            ApiResponse.create(response = Response.success(requestModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getAppointmentDetails(
        documentId: String,
        fireStore: FirebaseFirestore
    ): ApiResponse<AppointmentModel> {
        return try {
            val response = fireStore.collection(TABLE_APPOINTMENT)
                .document(documentId)
                .get()
                .await()
            val dataModel = response.toObject(AppointmentModel::class.java)
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

    suspend fun getBookAppointmentDetailsList(
        selectedDate: Date,
        userId: String,
        fireStore: FirebaseFirestore
    ): ApiResponse<List<AppointmentModel>> {
        return try {
            val nextDate = Calendar.getInstance()
            nextDate.time = selectedDate
            nextDate.add(Calendar.DATE, 1)
            val response = fireStore.collection(TABLE_APPOINTMENT)
                .whereEqualTo(FIELD_USER_ID, userId)
                .whereGreaterThanOrEqualTo(FIELD_SELECTED_DATE, selectedDate)
                .whereLessThanOrEqualTo(FIELD_SELECTED_DATE, nextDate.time)
                .get()
                .await()
            val bookedAppointmentList = arrayListOf<AppointmentModel>()

            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(AppointmentModel::class.java)
                user?.let {
                    it.id = document.id
                    val doctorDetails = fireStore.collection(TABLE_USER_DATA)
                        .whereEqualTo(FIELD_USER_ID, it.doctorId)
                        .get()
                        .await()
                    var dataModel = UserDataResponseModel()
                    for (snapshot in doctorDetails) {
                        dataModel = snapshot.toObject()
                    }
                    it.doctorDetails = dataModel
                    bookedAppointmentList.add(it)
                }
            }
            ApiResponse.create(response = Response.success(bookedAppointmentList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getUserSymptomDetails(
        userId: String,
        fireStore: FirebaseFirestore
    ): ApiResponse<SymptomModel> {
        return try {
            val response = fireStore.collection(TABLE_SYMPTOM)
                .whereEqualTo(FIELD_USER_ID, userId)
                .get()
                .await()
            var dataModel = SymptomModel()
            for (snapshot in response) {
                dataModel = snapshot.toObject()
            }
            ApiResponse.create(response = Response.success(dataModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getAppointmentsHistoryList(
        userId: String,
        firestore: FirebaseFirestore
    ): ApiResponse<List<AppointmentModel>> {
        return try {
            val response = firestore.collection(TABLE_APPOINTMENT)
                .whereEqualTo(FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_VISITED_KEY, true)
                .get().await()


            val appointmentsList = arrayListOf<AppointmentModel>()
            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(AppointmentModel::class.java)
                user?.let {
                    it.id = document.id
                    val doctorDetails = firestore.collection(TABLE_USER_DATA)
                        .whereEqualTo(FIELD_USER_ID, it.doctorId)
                        .get()
                        .await()
                    var dataModel = UserDataResponseModel()
                    for (snapshot in doctorDetails) {
                        dataModel = snapshot.toObject()
                    }
                    it.doctorDetails = dataModel
                    appointmentsList.add(it)
                }
            }
            ApiResponse.create(response = Response.success(appointmentsList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getDoctorAppointmentByDate(
        doctorId: String,
        date: Date,
        fireStore: FirebaseFirestore
    ): ApiResponse<ArrayList<AppointmentModel>> {
        return try {
            val nextDate = Calendar.getInstance()
            nextDate.time = date
            nextDate.add(Calendar.DATE, 1)
            val appointmentList = ArrayList<AppointmentModel>()
            val appointmentResponse = fireStore.collection(TABLE_APPOINTMENT)
                .whereEqualTo(FIELD_DOCTOR_ID, doctorId)
                .whereGreaterThanOrEqualTo(FIELD_SELECTED_DATE, date)
                .whereLessThanOrEqualTo(FIELD_SELECTED_DATE, nextDate.time)
                .whereIn(FIELD_APPROVED_KEY, listOf(FIELD_APPROVED, FIELD_PENDING))
                .get()
                .await()
            for (appointmentSnapshot in appointmentResponse) {
                appointmentList.add(appointmentSnapshot.toObject())
            }
            ApiResponse.create(response = Response.success(appointmentList))

        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getDoctorById(
        doctorDocumentId: String,
        fireStore: FirebaseFirestore
    ): ApiResponse<UserDataResponseModel> {
        return try {
            val doctorResponse = fireStore.collection(TABLE_USER_DATA)
                .document(doctorDocumentId)
                .get()
                .await()
            val doctorDataModel = doctorResponse.toObject(UserDataResponseModel::class.java)
            ApiResponse.create(response = Response.success(doctorDataModel))

        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

}