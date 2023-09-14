package com.android.doctorapp.repository

import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class AdminRepository @Inject constructor() {

    suspend fun getDoctorList(firestore: FirebaseFirestore): ApiResponse<List<UserDataResponseModel>> {
        return try {
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                .whereEqualTo(ConstantKey.DBKeys.FIELD_DOCTOR, true).get().await()

            val userList = arrayListOf<UserDataResponseModel>()
            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(UserDataResponseModel::class.java)
                user?.let {
                    it.id = document.id
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
                dataModel.id = response.documents[0].id
            }
            ApiResponse.create(response = Response.success(dataModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }
}