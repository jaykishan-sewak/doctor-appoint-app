package com.android.doctorapp.repository

import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.DegreeResponseModel
import com.android.doctorapp.repository.models.SpecializationResponseModel
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.android.doctorapp.util.constants.ConstantKey
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject

class AddDoctorRepository @Inject constructor() {

    suspend fun addDoctorData(
        doctorRequestModel: UserDataRequestModel,
        firestore: FirebaseFirestore
    ): ApiResponse<UserDataRequestModel> {
        return try {
            val addDoctorResponse =
                firestore.collection("user_data").add(doctorRequestModel).await()
            ApiResponse.create(response = Response.success(doctorRequestModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }


    suspend fun getDegreeList(firestore: FirebaseFirestore): ApiResponse<List<DegreeResponseModel>> {
        return try {
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_DEGREE).get().await()
            val degreeList = arrayListOf<DegreeResponseModel>()
            for (document: DocumentSnapshot in response.documents) {
                val degree = document.toObject(DegreeResponseModel::class.java)
                degree?.let {
                    degreeList.add(it)
                }
            }
            ApiResponse.create(response = Response.success(degreeList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getSpecializationList(firestore: FirebaseFirestore): ApiResponse<List<SpecializationResponseModel>> {
        return try {
            val response =
                firestore.collection(ConstantKey.DBKeys.TABLE_SPECIALIZATION).get().await()
            val specializationList = arrayListOf<SpecializationResponseModel>()
            for (document: DocumentSnapshot in response.documents) {
                val specialization = document.toObject(SpecializationResponseModel::class.java)
                specialization?.let {
                    specializationList.add(it)
                }
            }
            ApiResponse.create(response = Response.success(specializationList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun addDegree(firestore: FirebaseFirestore, data: String): ApiResponse<Boolean> {
        return try {
            val docList = firestore.collection(ConstantKey.DBKeys.TABLE_DEGREE).get().await()
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_DEGREE)
                .document(docList.documents[0].id).set(data).await()
            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun addSpecialization(firestore: FirebaseFirestore, data: String): ApiResponse<Boolean> {
        return try {
            val docList = firestore.collection(ConstantKey.DBKeys.TABLE_SPECIALIZATION).get().await()
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_SPECIALIZATION)
                .document(docList.documents[0].id).set(data).await()
            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }
}