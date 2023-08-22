package com.android.doctorapp.repository

import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class AdminRepository @Inject constructor() {

    suspend fun getDoctorList(firestore: FirebaseFirestore): ApiResponse<List<UserDataResponseModel>> {
        return try {
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_NAME)
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
                firestore.collection(ConstantKey.DBKeys.TABLE_NAME).document(documentId).delete().await()
            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }
}