package com.android.doctorapp.repository

import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.FeedbackRequestModel
import com.android.doctorapp.repository.models.FeedbackResponseModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_USER_ID
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_VISITED_KEY
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.SUB_TABLE_FEEDBACK
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.TABLE_USER_DATA
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class FeedbackRepository @Inject constructor() {

    suspend fun getUserDoctorList(
        fireStore: FirebaseFirestore,
        userId: String?
    ): ApiResponse<List<UserDataResponseModel>> {
        return try {
            val response = fireStore.collection(ConstantKey.DBKeys.TABLE_APPOINTMENT)
                .whereEqualTo(FIELD_USER_ID, userId)
                .whereEqualTo(FIELD_VISITED_KEY, true)
                .get()
                .await()
            val dataList = arrayListOf<UserDataResponseModel>()
            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(AppointmentModel::class.java)

                val doctorDetails = fireStore.collection(TABLE_USER_DATA)
                    .whereEqualTo(FIELD_USER_ID, user?.doctorId)
                    .get()
                    .await()
                var dataModel = UserDataResponseModel()

                for (snapshot in doctorDetails) {
                    dataModel = snapshot.toObject()
                    dataModel.docId = snapshot.id
                    val feedbackCollection = snapshot.reference.collection(SUB_TABLE_FEEDBACK)
                        .whereEqualTo(FIELD_USER_ID, userId).get().await()

                    for (feedbackDocument in feedbackCollection.documents) {
                        if (feedbackDocument != null) {
                            val feedbackData =
                                feedbackDocument.toObject(FeedbackResponseModel::class.java)!!
                            feedbackData.feedbackDocId = feedbackDocument.id
                            dataModel.feedbackDetails = feedbackData
                        }
                    }
                }
                if (!dataList.map { it.userId }.contains(dataModel.userId))
                    dataList.add(dataModel)
            }
            ApiResponse.create(response = Response.success(dataList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun addFeedbackData(
        docId: String?,
        requestModel: FeedbackRequestModel,
        firestore: FirebaseFirestore
    ): ApiResponse<String> {
        return try {
            val data =
                firestore.collection(TABLE_USER_DATA)
                    .document(docId!!)
                    .collection(SUB_TABLE_FEEDBACK)
                    .add(requestModel)
                    .await()

            ApiResponse.create(response = Response.success(data.id))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getUserFeedbackData(
        doctorId: String?,
        fireStore: FirebaseFirestore,
        userId: String?
    ): ApiResponse<UserDataResponseModel> {
        return try {
            val userDataSnapshot =
                fireStore.collection(TABLE_USER_DATA)
                    .whereEqualTo(FIELD_USER_ID, doctorId)
                    .get()
                    .await()

            var dataModel: UserDataResponseModel? = UserDataResponseModel()
            for (userDataDocument in userDataSnapshot.documents) {
                dataModel = userDataDocument.toObject()
                dataModel?.docId = userDataDocument.id
                val feedbackCollection = userDataDocument.reference.collection(SUB_TABLE_FEEDBACK)
                    .whereEqualTo(FIELD_USER_ID, userId)
                    .get().await()

                for (feedbackDocument in feedbackCollection.documents) {
                    if (feedbackDocument != null) {
                        val feedbackData =
                            feedbackDocument.toObject(FeedbackResponseModel::class.java)!!
                        feedbackData.feedbackDocId = feedbackDocument.id
                        dataModel?.feedbackDetails = feedbackData
                    }
                }
            }
            ApiResponse.create(response = Response.success(dataModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }


    suspend fun getUpdateFeedbackData(
        doctorId: String?,
        fireStore: FirebaseFirestore,
        feedbackRequestModel: FeedbackRequestModel
    ): ApiResponse<FeedbackRequestModel> {
        return try {
            val userDataSnapshot =
                fireStore.collection(TABLE_USER_DATA)
                    .whereEqualTo(FIELD_USER_ID, doctorId)
                    .get()
                    .await()


            for (userDataDocument in userDataSnapshot.documents) {
                val feedbackCollection = userDataDocument.reference.collection(SUB_TABLE_FEEDBACK)
                    .whereEqualTo(FIELD_USER_ID, feedbackRequestModel.userId)
                    .get().await()

                for (feedbackDocument in feedbackCollection.documents) {
                    feedbackDocument.reference.set(feedbackRequestModel).await()

                }
            }
            ApiResponse.create(response = Response.success(feedbackRequestModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun deleteFeedbackData(
        userDocId: String,
        fireStore: FirebaseFirestore,
        feedbackDocId: String
    ): ApiResponse<Boolean> {
        return try {
            val userDataSnapshot =
                fireStore.collection(TABLE_USER_DATA)
                    .document(userDocId)
                    .collection(SUB_TABLE_FEEDBACK)
                    .document(feedbackDocId)
                    .delete()
                    .await()

            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getAllFeedbackList(
        doctorId: String?, firestore: FirebaseFirestore
    ): ApiResponse<List<FeedbackResponseModel>> {
        return try {
            val response =
                firestore.collection(TABLE_USER_DATA).whereEqualTo(FIELD_USER_ID, doctorId)
                    .get().await()

            val feedBackList = arrayListOf<FeedbackResponseModel>()
            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(UserDataResponseModel::class.java)
                user?.let {
                    it.docId = document.id
                    val subCollectionRef = firestore.collection(TABLE_USER_DATA).document(it.docId)
                        .collection(SUB_TABLE_FEEDBACK).get().await()

                    var dataModel = FeedbackResponseModel()
                    for (snapshot in subCollectionRef.documents) {
                        dataModel = snapshot.toObject()!!
                        dataModel.let { it1 ->
                            val userData = firestore.collection(TABLE_USER_DATA).whereEqualTo(
                                FIELD_USER_ID, it1.userId
                            ).get().await()
                            for (snapshot1 in userData) {
                                val userDetails =
                                    snapshot1.toObject(UserDataResponseModel::class.java)
                                it1.userDetails = userDetails
                            }
                        }
                        feedBackList.add(dataModel)
                    }

                }
            }
            ApiResponse.create(response = Response.success(feedBackList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }


}

