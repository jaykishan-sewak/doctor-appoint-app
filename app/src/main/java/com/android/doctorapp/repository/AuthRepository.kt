package com.android.doctorapp.repository

import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_IS_LOGGED_IN
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.DegreeResponseModel
import com.android.doctorapp.repository.models.LoginRequestModel
import com.android.doctorapp.repository.models.LoginResponseModel
import com.android.doctorapp.repository.models.RegisterRequestModel
import com.android.doctorapp.repository.models.SpecializationResponseModel
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.repository.network.AppApi
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.FIELD_USER_ID
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject


class AuthRepository @Inject constructor(
    private val authApi: AppApi,
    private val session: Session
) {
    /**
     *
     * @param loginRequestModel request param model for login with email api.
     */
    suspend fun login(loginRequestModel: LoginRequestModel): ApiResponse<LoginResponseModel> {
        return try {
            val response = authApi.login(loginRequestModel)
            if (response.isSuccessful) {
                session.putBoolean(USER_IS_LOGGED_IN, true)
            }
            ApiResponse.create(response = response)
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    /**
     *
     * @param registerRequestModel request param model for login with email api.
     */
    suspend fun register(registerRequestModel: RegisterRequestModel): ApiResponse<LoginResponseModel> {
        return try {
            val response = authApi.register(registerRequestModel)
            if (response.isSuccessful) {
                session.putBoolean(USER_IS_LOGGED_IN, true)
            }
            ApiResponse.create(response = response)
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    /**
     * This method is used to create user email and password for
     * firebase authentication. It returns the result of <AuthResult> type.
     */
    suspend fun register(
        auth: FirebaseAuth,
        email: String,
        password: String
    ): ApiResponse<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(
                email,
                password
            ).await()
            ApiResponse.create(response = Response.success(result))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    /**
     * This method is used for email and password
     * firebase authentication. It returns the result of <AuthResult> type.
     */
    suspend fun login(
        auth: FirebaseAuth,
        email: String,
        password: String
    ): ApiResponse<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(
                email,
                password
            ).await()
            ApiResponse.create(response = Response.success(result))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    /**
     * This method is used for google sign in
     * firebase authentication. It returns the result of <AuthResult> type.
     */
    suspend fun googleLogin(
        auth: FirebaseAuth,
        authCredential: AuthCredential
    ): ApiResponse<AuthResult> {
        return try {
            val result = auth.signInWithCredential(
                authCredential
            ).await()
            ApiResponse.create(response = Response.success(result))

        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    fun signInAccountTask(
        result: androidx.activity.result.ActivityResult
    ): ApiResponse<Task<GoogleSignInAccount>> {
        return try {
            val signInAccountTask: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(
                    result.data
                )

            ApiResponse.create(response = Response.success(signInAccountTask))

        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    fun googleSignInAccount(
        signInAccountTask: Task<GoogleSignInAccount>
    ): ApiResponse<GoogleSignInAccount> {
        return try {
            val googleSignInAccount: GoogleSignInAccount =
                signInAccountTask.getResult(ApiException::class.java)
            ApiResponse.create(response = Response.success(googleSignInAccount))
        } catch (e: java.lang.Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    fun authCredentials(
        idToken: String
    ): ApiResponse<AuthCredential> {
        return try {
            val authCredential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
            ApiResponse.create(response = Response.success(authCredential))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun addUserData(
        userRequestModel: UserDataRequestModel,
        firestore: FirebaseFirestore
    ): ApiResponse<UserDataRequestModel> {
        return try {
            firestore.collection("user_data").add(userRequestModel).await()
            ApiResponse.create(response = Response.success(userRequestModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }


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

    suspend fun updateUserData(
        doctorRequestModel: UserDataRequestModel,
        fireStore: FirebaseFirestore
    ): ApiResponse<UserDataRequestModel> {
        return try {

            val response = fireStore.collection("user_data")
                .whereEqualTo("userId", doctorRequestModel.userId)
                .get()
                .await()

            val updateUserResponse = fireStore.collection("user_data")
                .document(response.documents[0].id)
                .set(doctorRequestModel)
                .await()

            ApiResponse.create(response = Response.success(doctorRequestModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getRecordById(
        recordId: String,
        fireStore: FirebaseFirestore
    ): ApiResponse<UserDataRequestModel> {
        return try {
            val response = fireStore.collection("user_data")
                .whereEqualTo("userId", recordId)
                .get()
                .await()

            var dataModel = UserDataRequestModel()
            for (snapshot in response) {
                dataModel = snapshot.toObject()
            }
            ApiResponse.create(response = Response.success(dataModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun emailVerification(firebaseUser: FirebaseUser): ApiResponse<Boolean> {
        return try {
            firebaseUser.sendEmailVerification().await()
            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    fun emailVerified(firebaseUser: FirebaseUser): ApiResponse<Boolean> {
        return try {
            val result = firebaseUser.isEmailVerified
            ApiResponse.create(response = Response.success(result))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    fun userReload(firebaseUser: FirebaseUser): ApiResponse<Boolean> {
        return try {
            val result = firebaseUser.reload()
            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }


    suspend fun getDegreeList(firestore: FirebaseFirestore): ApiResponse<DegreeResponseModel> {
        return try {
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_DEGREE).get().await()
            var degreeObj: DegreeResponseModel? = null
            for (document: DocumentSnapshot in response.documents) {
                val degree = document.toObject(DegreeResponseModel::class.java)
                degree?.degreeId = document.id
                degree?.let {
                    degreeObj = it
                }
            }
            ApiResponse.create(response = Response.success(degreeObj))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun getSpecializationList(firestore: FirebaseFirestore): ApiResponse<SpecializationResponseModel> {
        return try {
            val response =
                firestore.collection(ConstantKey.DBKeys.TABLE_SPECIALIZATION).get().await()
            var specializationObj: SpecializationResponseModel? = null
            for (document: DocumentSnapshot in response.documents) {
                val specialization = document.toObject(SpecializationResponseModel::class.java)
                specialization?.let {
                    specializationObj = it
                }
            }
            ApiResponse.create(response = Response.success(specializationObj))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun addDegree(firestore: FirebaseFirestore, data: String): ApiResponse<Boolean> {
        return try {
            val docList = firestore.collection(ConstantKey.DBKeys.TABLE_DEGREE).get().await()
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_DEGREE)
                .document(docList.documents[0].id).update("degreeName", FieldValue.arrayUnion(data))
                .await()
            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun addSpecialization(
        firestore: FirebaseFirestore,
        data: String
    ): ApiResponse<Boolean> {
        return try {
            val docList =
                firestore.collection(ConstantKey.DBKeys.TABLE_SPECIALIZATION).get().await()
            val response = firestore.collection(ConstantKey.DBKeys.TABLE_SPECIALIZATION)
                .document(docList.documents[0].id)
                .update("specializations", FieldValue.arrayUnion(data)).await()
            ApiResponse.create(response = Response.success(true))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun updateDoctorData(
        doctorRequestModel: UserDataRequestModel,
        fireStore: FirebaseFirestore
    ): ApiResponse<UserDataRequestModel> {
        return try {
            val response = fireStore.collection(ConstantKey.DBKeys.TABLE_NAME)
                .whereEqualTo(FIELD_USER_ID, doctorRequestModel.userId)
                .get()
                .await()

            val updateUserResponse = fireStore.collection(ConstantKey.DBKeys.TABLE_NAME)
                .document(response.documents[0].id)
                .set(doctorRequestModel)
                .await()

            ApiResponse.create(response = Response.success(doctorRequestModel))
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }
}
