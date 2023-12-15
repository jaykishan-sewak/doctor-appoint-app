package com.android.doctorapp.repository.local

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow

interface Session {
    suspend fun putBoolean(key: Preferences.Key<Boolean>, value: Boolean)

    suspend fun getBoolean(key: Preferences.Key<Boolean>): Flow<Boolean?>

    suspend fun putString(key: Preferences.Key<String>, value: String)

    suspend fun getString(key: Preferences.Key<String>): Flow<String?>

    suspend fun putLong(key: Preferences.Key<Long>, value: Long)

    suspend fun getLong(key: Preferences.Key<Long>): Flow<Long?>

    suspend fun putInt(key: Preferences.Key<Int>, value: Int)

    suspend fun getInt(key: Preferences.Key<Int>): Flow<Int?>

    suspend fun putDouble(key: Preferences.Key<Double>, value: Double)

    suspend fun getDouble(key: Preferences.Key<Double>): Flow<Double?>

    suspend fun putFloat(key: Preferences.Key<Float>, value: Float)

    suspend fun getFloat(key: Preferences.Key<Float>): Flow<Float?>

    suspend fun clearLoggedInSession(): CompletableDeferred<Boolean>
}