package com.android.doctorapp.repository

import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.ProfileResponseModel
import com.android.doctorapp.repository.models.SymptomModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.repository.network.AppApi
import com.android.doctorapp.util.constants.ConstantKey
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val appApi: AppApi,
    private val session: Session
) {

    suspend fun getUserProfile(): ApiResponse<ProfileResponseModel> {
        return try {
            val response = appApi.getProfile()
            ApiResponse.create(response = response)
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getProfileRecordById(
        recordId: String,
        fireStore: FirebaseFirestore
    ): ApiResponse<UserDataResponseModel> {
        return try {
            val response = fireStore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                .whereEqualTo(ConstantKey.DBKeys.FIELD_USER_ID, recordId)
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

    suspend fun clearLoggedInSession() = session.clearLoggedInSession()

    suspend fun getDoctorList(firestore: FirebaseFirestore): ApiResponse<List<UserDataResponseModel>> {
        return try {
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                .whereEqualTo(ConstantKey.DBKeys.FIELD_DOCTOR, true).get().await()

            val userList = arrayListOf<UserDataResponseModel>()
            for (document: DocumentSnapshot in response.documents) {
                val user = document.toObject(UserDataResponseModel::class.java)
                user?.let {
                    it.docId = document.id
                    userList.add(it)
                }
            }
            ApiResponse.create(response = Response.success(userList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun submitUserSymptomsData(
        symptomModel: SymptomModel,
        fireStore: FirebaseFirestore
    ): ApiResponse<SymptomModel> {
        return try {

            val response = fireStore.collection(ConstantKey.DBKeys.TABLE_SYMPTOM)
                .add(symptomModel)
                .await()

            ApiResponse.create(response = Response.success(symptomModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

}