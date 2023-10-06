package com.android.doctorapp.repository

import android.util.Log
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.FeedbackResponseModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_DOCTOR
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_DOCTOR_ID
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.TABLE_FEEDBACK
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.TABLE_USER_DATA
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class AdminRepository @Inject constructor(
    private val session: Session
) {

    suspend fun getDoctorList(firestore: FirebaseFirestore): ApiResponse<List<UserDataResponseModel>> {
        return try {
            val response = firestore.collection(TABLE_USER_DATA)
                .whereEqualTo(FIELD_DOCTOR, true).get().await()

            val userList = arrayListOf<UserDataResponseModel>()
            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(UserDataResponseModel::class.java)
                var total: Int
                user?.let {
                    it.docId = document.id
                    val feedbackData = firestore.collection(TABLE_FEEDBACK)
                        .whereEqualTo(FIELD_DOCTOR_ID, it.userId)
                        .get()
                        .await()
                    var feedback = FeedbackResponseModel()
                    for (snapshot in feedbackData) {
                        feedback = snapshot.toObject()
                    }
                    it.rating = feedback.rating
                    userList.add(it)

                }
            }
            ApiResponse.create(response = Response.success(userList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getLatLngDoctorList(
        firestore: FirebaseFirestore,
        latitude: Double,
        longitude: Double
    ): ApiResponse<List<UserDataResponseModel>> {
        return try {
            val center = GeoLocation(latitude, longitude)
            val radiusInM = 50.0 * 1000.0
            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
            val doctorTasks: MutableList<Task<QuerySnapshot>> = ArrayList()
            val userList = arrayListOf<UserDataResponseModel>()
            var feedback = FeedbackResponseModel()
//            for (b in bounds) {
//                val response = firestore.collection(TABLE_USER_DATA)
//                    .whereEqualTo(FIELD_DOCTOR, true)
//                    .orderBy("geohash")
//                    .startAt(b.startHash)
//                    .endAt(b.endHash)
//                doctorTasks.add(response.get())
//            }
//            Tasks.whenAllComplete(doctorTasks)
//                .addOnCompleteListener {
//                    for (task in doctorTasks) {
//                        val snap = task.result
//                        for (doc in snap!!.documents) {
//                            val user = doc.toObject(UserDataResponseModel::class.java)
//                            user?.let {
//                                it.docId = doc.id
//                                val feedbackResponse = firestore.collection(TABLE_FEEDBACK)
//                                    .whereEqualTo(FIELD_DOCTOR_ID, it.userId)
//                                    .get().await()
//                                it.rating = feedback.rating
//                                userList.add(it)
//
//                            }
//                        }
//                    }
//                }

            for (b in bounds) {
                val response = firestore.collection(TABLE_USER_DATA)
                    .whereEqualTo(FIELD_DOCTOR, true)
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)

                // Use await() to wait for the get() task to complete
                val snap = response.get().await()

                for (doc in snap.documents) {
                    val user = doc.toObject(UserDataResponseModel::class.java)
                    user?.let {
                        it.docId = doc.id

                        // Use await() to wait for the feedbackResponse task to complete
                        val feedbackResponse = firestore.collection(TABLE_FEEDBACK)
                            .whereEqualTo(FIELD_DOCTOR_ID, it.userId)
                            .get().await()

                        // Process feedback data here and update user's rating
                        it.rating = feedback.rating
                        userList.add(it)
                    }
                }
            }

            Log.d("TAG", "getLatLngDoctorList: ${userList.size}")
            ApiResponse.create(response = Response.success(userList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun deleteDoctor(
        firestore: FirebaseFirestore,
        documentId: String
    ): ApiResponse<Boolean> {
        return try {
            val response =
                firestore.collection(TABLE_USER_DATA).document(documentId)
                    .delete().await()
            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getDoctorDetails(
        userId: String,
        fireStore: FirebaseFirestore
    ): ApiResponse<UserDataResponseModel> {
        return try {
            val response = fireStore.collection(TABLE_USER_DATA)
                .whereEqualTo(ConstantKey.DBKeys.FIELD_USER_ID, userId)
                .get()
                .await()

            var dataModel = UserDataResponseModel()
            for (snapshot in response) {
                dataModel = snapshot.toObject()
                dataModel.docId = response.documents[0].id
            }
            ApiResponse.create(response = Response.success(dataModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun clearLoggedInSession() = session.clearLoggedInSession()

}