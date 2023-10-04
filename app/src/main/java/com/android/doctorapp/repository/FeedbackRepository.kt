package com.android.doctorapp.repository

import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.FeedbackRequestModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.FIELD_APPROVED
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
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
                .whereEqualTo(ConstantKey.DBKeys.FIELD_APPROVED_KEY, FIELD_APPROVED)
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
                for (snapshot in doctorDetails) {
                    dataModel = snapshot.toObject()
                }
                dataList.add(dataModel)

            }
            ApiResponse.create(response = Response.success(dataList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun addFeedbackData(
        userId: String,
        requestModel: FeedbackRequestModel,
        firestore: FirebaseFirestore
    ): ApiResponse<String> {
        return try {
            val userTable =
                firestore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
            val userRef = userTable.document(userId)
            val data = userRef.collection(ConstantKey.DBKeys.SUB_TABLE_FEEDBACK).add(requestModel).await()
            ApiResponse.create(response = Response.success(data.id))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }
}
