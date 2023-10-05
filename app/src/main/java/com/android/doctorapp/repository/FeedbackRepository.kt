package com.android.doctorapp.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.FeedbackRequestModel
import com.android.doctorapp.repository.models.FeedbackResponseModel
import com.android.doctorapp.repository.models.SymptomModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_DOCTOR_ID
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_USER_ID
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_VISITED_KEY
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.SUB_TABLE_FEEDBACK
import com.android.doctorapp.util.constants.ConstantKey.FIELD_APPROVED
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.gson.Gson
import com.google.rpc.context.AttributeContext.Api
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class FeedbackRepository @Inject constructor(
    private val session: Session
) {

    suspend fun getUserDoctorList(
        fireStore: FirebaseFirestore,
        userId: String?
    ): ApiResponse<List<UserDataResponseModel>> {
        return try {
            val response = fireStore.collection(ConstantKey.DBKeys.TABLE_APPOINTMENT)
                .whereEqualTo(ConstantKey.DBKeys.FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_VISITED_KEY, true)
                .get()
                .await()
            val dataList = arrayListOf<UserDataResponseModel>()
          

            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(AppointmentModel::class.java)
                
                val doctorDetails = fireStore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                    .whereEqualTo(ConstantKey.DBKeys.FIELD_USER_ID, user?.doctorId)
                    .get()
                    .await()
                var dataModel = UserDataResponseModel()
                var feedbackData = FeedbackResponseModel()
                for (snapshot in doctorDetails) {
                    dataModel = snapshot.toObject()
                    dataModel.docId = snapshot.id
                    val feedbackCollection = snapshot.reference.collection(ConstantKey.DBKeys.SUB_TABLE_FEEDBACK)
                    val feedbackDataSnapshot = feedbackCollection.whereEqualTo(FIELD_USER_ID, userId)
                        .get().await()
                    for (feedbackDocument in  feedbackDataSnapshot.documents) {
                        feedbackData = feedbackDocument.toObject()!!
                    }
                    dataModel.feedbackDetails = feedbackData
                }
                dataList.add(dataModel)
            }
            ApiResponse.create(response = Response.success(dataList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun addFeedbackData(
        docId: String,
        requestModel: FeedbackRequestModel,
        firestore: FirebaseFirestore
    ): ApiResponse<String> {
        return try {
            val data =
                firestore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                    .document(docId)
                    .collection(ConstantKey.DBKeys.SUB_TABLE_FEEDBACK)
                    .add(requestModel)
                    .await()

            ApiResponse.create(response = Response.success(data.id))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getUserFeedbackData(
        doctorId: String,
        fireStore: FirebaseFirestore,
        userId: String?
    ): ApiResponse<FeedbackResponseModel> {
        return try {
            val userDataSnapshot =
                fireStore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                    .whereEqualTo(FIELD_USER_ID, doctorId)
                    .get()
                    .await()

            var feedbackData = FeedbackResponseModel()
            for (userDataDocument in userDataSnapshot.documents) {
                val feedbackCollection = userDataDocument.reference.collection(ConstantKey.DBKeys.SUB_TABLE_FEEDBACK)
                val feedbackDataSnapshot = feedbackCollection.whereEqualTo(FIELD_USER_ID, userId)
                    .get().await()

                for (feedbackDocument in  feedbackDataSnapshot.documents) {
                    feedbackData = feedbackDocument.toObject()!!
                }
            }
            ApiResponse.create(response = Response.success(feedbackData))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }
}
