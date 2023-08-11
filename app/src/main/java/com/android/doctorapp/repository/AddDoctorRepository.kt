package com.android.doctorapp.repository

import android.util.Log
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.repository.network.AppApi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AddDoctorRepository @Inject constructor() {

    private val TAG = AddDoctorRepository::class.java.simpleName

    suspend fun addDoctorData(doctorRequestModel: UserDataRequestModel, firestore: FirebaseFirestore): Any {
        try {
            val addDoctorRespone = firestore.collection("user_data").add(doctorRequestModel).await()
//            val abc = firestore.collection("user_data")
//                .document("zUVrztrKfWRZPZfZk0HXV6btkxb211").update(doctorRequestModel.toMap())
//                .await()
            Log.d(TAG, "addDoctorData: $addDoctorRespone")
            return addDoctorRespone
        } catch (e: Exception) {
            Log.d("TAGFirebase", "addDoctorData Catch : ${e.fillInStackTrace()}")
            return e.fillInStackTrace()
        }
    }



}