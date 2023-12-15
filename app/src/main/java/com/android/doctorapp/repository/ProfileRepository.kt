package com.android.doctorapp.repository

import android.net.Uri
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.ProfileResponseModel
import com.android.doctorapp.repository.models.SymptomModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.repository.network.AppApi
import com.android.doctorapp.util.constants.ConstantKey
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import java.util.Date
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

    suspend fun clearLoggedInSession() = session.clearLoggedInSession().await()

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

    suspend fun updateUserData(
        notificationEnable: Boolean, userId: String?, fireStore: FirebaseFirestore
    ): ApiResponse<Boolean> {
        return try {

            val updateUserResponse =
                fireStore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                    .whereEqualTo(ConstantKey.DBKeys.FIELD_USER_ID, userId).limit(1)
                    .get().await()

            val userData: UserDataResponseModel =
                updateUserResponse.documents[0].toObject(UserDataResponseModel::class.java)!!
            userData.isNotificationEnable = notificationEnable
            val response =
                fireStore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                    .document(updateUserResponse.documents[0].id)
                    .set(userData).await()

            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun emptyUserToken(
        tokenId: String?, userId: String?, fireStore: FirebaseFirestore
    ): ApiResponse<Boolean> {
        return try {

            val updateUserResponse =
                fireStore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                    .whereEqualTo(ConstantKey.DBKeys.FIELD_USER_ID, userId).limit(1)
                    .get().await()

            val userData: UserDataResponseModel =
                updateUserResponse.documents[0].toObject(UserDataResponseModel::class.java)!!
            userData.token = tokenId
            val response =
                fireStore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                    .document(updateUserResponse.documents[0].id)
                    .set(userData).await()

            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun uploadImage(
        imageURI: Uri,
        storage: FirebaseStorage
    ): ApiResponse<String> {
        return try {
            val timestamp = System.currentTimeMillis().toString()
            val response =
                storage.reference.child("images").child("$timestamp.jpg").putFile(imageURI).await()
            val imageUrl = response.storage.downloadUrl.await()
            ApiResponse.create(response = Response.success(imageUrl.toString()))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun updateClinicImgById(
        clinicImgList: ArrayList<String>?, fireStore: FirebaseFirestore, userId: String?
    ): ApiResponse<Boolean> {
        return try {

            val updateUserResponse =
                fireStore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                    .whereEqualTo(ConstantKey.DBKeys.FIELD_USER_ID, userId).limit(1)
                    .get().await()

            val userData: UserDataResponseModel =
                updateUserResponse.documents[0].toObject(UserDataResponseModel::class.java)!!
            userData.apply {
                clinicImg = if (clinicImgList?.isNotEmpty() == true) clinicImgList.toList()
                    .map { clinicImg -> clinicImg } as ArrayList<String> else null
            }
            val response =
                fireStore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                    .document(updateUserResponse.documents[0].id)
                    .set(userData).await()


            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }


    suspend fun getMyDoctorsList(
        userId: String?, currentDate: Date, fireStore: FirebaseFirestore
    ): ApiResponse<ArrayList<UserDataResponseModel?>> {
        return try {
            val myDoctorsList = ArrayList<UserDataResponseModel?>()
            val appointmentList = ArrayList<AppointmentModel>()
            val appointmentResponse =
                fireStore.collection(ConstantKey.DBKeys.TABLE_APPOINTMENT)
                    .whereEqualTo(ConstantKey.DBKeys.FIELD_USER_ID, userId)
                    .whereLessThan(ConstantKey.DBKeys.FIELD_SELECTED_DATE, currentDate).get()
                    .await()
            for (appointmentSnapshot in appointmentResponse) {
                val appointmentModel = appointmentSnapshot.toObject((AppointmentModel::class.java))
                appointmentList.add(appointmentModel)
            }
            for (appointmentModel in appointmentList) {
                val doctorIdsInMyDoctors = myDoctorsList.map { it?.userId }
                if (appointmentModel.doctorId !in doctorIdsInMyDoctors) {
                    appointmentModel.let {
                        val doctorDetails =
                            fireStore.collection(ConstantKey.DBKeys.TABLE_USER_DATA)
                                .whereEqualTo(ConstantKey.DBKeys.FIELD_USER_ID, it.doctorId)
                                .limit(1)
                                .get()
                                .await()

                        for (document in doctorDetails.documents) {
                            val dataModel = document.toObject(UserDataResponseModel::class.java)
                            dataModel?.let { it1 ->
                                it1.docId = document.id
                            }
                            myDoctorsList.add(dataModel)
                        }
                    }
                }
            }
            ApiResponse.create(response = Response.success(myDoctorsList))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

}