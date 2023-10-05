package com.android.doctorapp.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.FeedbackRequestModel
import com.android.doctorapp.repository.models.FeedbackResponseModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class AdminRepository @Inject constructor(
    private val session: Session
) {

    suspend fun getDoctorList(firestore: FirebaseFirestore): ApiResponse<List<UserDataResponseModel>> {
        return try {
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                .whereEqualTo(ConstantKey.DBKeys.FIELD_DOCTOR, true).get().await()

            val userList = arrayListOf<UserDataResponseModel>()
            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(UserDataResponseModel::class.java)
                var total: Int
                user?.let {
                    it.docId = document.id
                    val feedbackData = firestore.collection(ConstantKey.DBKeys.TABLE_FEEDBACK)
                        .whereEqualTo(ConstantKey.DBKeys.FIELD_DOCTOR_ID, it.userId)
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

    suspend fun deleteDoctor(
        firestore: FirebaseFirestore,
        documentId: String
    ): ApiResponse<Boolean> {
        return try {
            val response =
                firestore.collection(ConstantKey.DBKeys.TABLE_USER_DATA).document(documentId)
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
            val response = fireStore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
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

    suspend fun getFeedbackDoctorList(firestore: FirebaseFirestore): ApiResponse<List<UserDataResponseModel>> {
        return try {
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                .whereEqualTo(ConstantKey.DBKeys.FIELD_DOCTOR, true).get().await()

            val userList = arrayListOf<UserDataResponseModel>()
            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(UserDataResponseModel::class.java)

                user?.let {
                    it.docId = document.id
                    val subCollectionRef = firestore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                        .document(it.docId)
                        .collection(ConstantKey.DBKeys.SUB_TABLE_FEEDBACK)

                    val querySnapshot = subCollectionRef.get().await()
                    var total = 0F
                    var numberOfFeedbacks = 0
                    val feedbackList = mutableListOf<FeedbackRequestModel>()
                    for (document1 in querySnapshot.documents) {
                        val feedback = document1.toObject(FeedbackResponseModel::class.java)
                        Log.d(TAG, "getFeedbackDoctorList: ${feedback?.rating}")
                        feedback.let {
                            total += it!!.rating!!
                            numberOfFeedbacks++
                        }
                    }

                    if (numberOfFeedbacks > 0) {
                        it.rating = total / numberOfFeedbacks
                    } else {
                        it.rating = 0F // Set a default rating if there are no feedbacks
                    }

                    userList.add(it)

                }
            }
            ApiResponse.create(response = Response.success(userList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }
}