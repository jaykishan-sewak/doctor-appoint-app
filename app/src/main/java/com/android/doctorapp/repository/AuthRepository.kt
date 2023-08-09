package com.android.doctorapp.repository

import android.content.Context
import android.util.Log
import com.android.doctorapp.repository.local.*
import com.android.doctorapp.repository.models.ApiResponse
import com.android.doctorapp.repository.models.FirebaseRegisterResponse
import com.android.doctorapp.repository.models.LoginRequestModel
import com.android.doctorapp.repository.models.LoginResponseModel
import com.android.doctorapp.repository.models.RegisterRequestModel
import com.android.doctorapp.repository.network.AppApi
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
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
            Log.d("response---", Gson().toJson(response))
            ApiResponse.create(response = response)
        } catch (e: Exception) {
            ApiResponse.create(e.fillInStackTrace())
        }
    }

    suspend fun register(
        auth: FirebaseAuth,
        email: String,
        password: String
    ): ApiResponse<AuthResult> {
        return try {
            val result = auth?.createUserWithEmailAndPassword(
                email,
                password
            )?.await()
            Log.d("response---", result?.user?.email!!)
            ApiResponse.create(response = Response.success(result))
        } catch (e: Exception) {
            Log.d("Error ---", e.message!!)
            ApiResponse.create(e.fillInStackTrace())
        }
    }
    suspend fun login(
        auth: FirebaseAuth,
        email: String,
        password: String
    ): ApiResponse<AuthResult> {
        return try {
            val result = auth?.signInWithEmailAndPassword(
                email,
                password
            )?.await()
            Log.d("response---", result?.user?.email!!)
            ApiResponse.create(response = Response.success(result))
        } catch (e: Exception) {
            Log.d("Error ---", e.message!!)
            ApiResponse.create(e.fillInStackTrace())
        }
    }

}
