package com.android.doctorapp.repository.models

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import retrofit2.Response
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.regex.Pattern

/**
 * Common class used by API responses.
 * @param <T> the type of the response object
</T> */
@Suppress("unused") // T is used in extending classes
sealed class ApiResponse<T> {
    companion object {

        fun <T> create(error: Throwable): ApiResponse<T> {
            if (error is ConnectException || error is UnknownHostException) {
                return ApiNoNetworkResponse(
                    "Please connect to the internet and try again."
                )
            }
            return ApiErrorResponse(
                error.message ?: "unknown error"
            )
        }

        fun <T> create(
            response: Response<T>
        ): ApiResponse<T> {
            return if (response.isSuccessful) {
                val body = response.body()
                if (body == null || response.code() == 204) {
                    ApiEmptyResponse()
                } else {
                    ApiSuccessResponse(
                        body = body
                    )
                }
            } else {
                val msg = response.errorBody()?.string()
                val errorMsg = if (msg.isNullOrEmpty()) {
                    response.message()
                } else {
                    try {
                        var message = ""
                        val listType =
                            object : TypeToken<List<String>>() {}.type
                        val jsonObject = JSONObject(msg)
                        var errorMessage = jsonObject.optString("message")
                        errorMessage = errorMessage.replace("%1", "%s")
                        val parameters = Gson().fromJson<List<String>>(
                            jsonObject.optJSONArray("parameters")?.toString(),
                            listType
                        )
                        message = if (parameters != null) {
                            message.plus(String.format(errorMessage, parameters.joinToString()))
                        } else {
                            message.plus(errorMessage)
                        }
                        message
                    } catch (e: Exception) {
                        e.printStackTrace()
                        msg
                    }
                }
                ApiErrorResponse(
                    errorMsg ?: "unknown error"
                )
            }
        }
    }
}

/**
 * separate class for HTTP 204 responses so that we can make ApiSuccessResponse's body non-null.
 */
class ApiEmptyResponse<T> : ApiResponse<T>()

/**
 * separate class for Network Error
 */
class ApiNoNetworkResponse<T>(val errorMessage: String) : ApiResponse<T>()

data class ApiSuccessResponse<T>(
    val body: T

) : ApiResponse<T>() {

    companion object {
        private val PAGE_PATTERN = Pattern.compile("\\bpage=(\\d+)")
        private const val NEXT_LINK = "next"

    }
}

data class ApiErrorResponse<T>(val errorMessage: String) : ApiResponse<T>()

