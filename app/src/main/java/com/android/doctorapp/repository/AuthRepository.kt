package com.android.doctorapp.repository

import com.android.doctorapp.repository.local.Session
import com.android.doctorapp.repository.local.USER_IS_LOGGED_IN
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.LoginRequestModel
import com.android.doctorapp.repository.models.LoginResponseModel
import com.android.doctorapp.repository.models.RegisterRequestModel
import com.android.doctorapp.repository.network.AppApi
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
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


}
