package com.android.doctorapp.repository

import android.util.Log
import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_IS_LOGGED_IN
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.LoginRequestModel
import com.android.doctorapp.repository.models.LoginResponseModel
import com.android.doctorapp.repository.models.RegisterRequestModel
import com.android.doctorapp.repository.models.UserDataRequestModel
import com.android.doctorapp.repository.network.AppApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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


    suspend fun getRecordById(recordId: String, fireStore: FirebaseFirestore): ApiResponse<UserDataRequestModel> {
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
}
