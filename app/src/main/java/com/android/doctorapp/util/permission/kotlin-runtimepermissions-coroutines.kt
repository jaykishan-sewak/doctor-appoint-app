package com.android.doctorapp.util.permission

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.doctorapp.util.permission.callback.PermissionListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun FragmentActivity.askPermission(vararg permissions: String): PermissionResult =
    suspendCoroutine { continuation ->
        var resumed = false
        RuntimePermission.askPermission(this)
            .request(permissions.toList())
            .onResponse(object : PermissionListener {
                override fun onAccepted(
                    permissionResult: PermissionResult,
                    accepted: List<String?>?,
                ) {
                    if (!resumed) {
                        resumed = true
                        continuation.resume(permissionResult)
                    }
                }

                override fun onDenied(
                    permissionResult: PermissionResult,
                    denied: List<String?>?,
                    foreverDenied: List<String?>?,
                ) {
                    if (!resumed) {
                        resumed = true
                        continuation.resumeWithException(PermissionException(permissionResult))
                    }
                }

            }).ask()
    }

suspend fun Fragment.askPermission(vararg permissions: String): PermissionResult =
    suspendCoroutine { continuation ->
        var resumed = false
        when (activity) {
            null -> continuation.resumeWithException(Exception())
            else -> RuntimePermission.askPermission(this)
                .request(permissions.toList())
                .onResponse(object : PermissionListener {
                    override fun onAccepted(
                        permissionResult: PermissionResult,
                        accepted: List<String?>?,
                    ) {
                        if (!resumed) {
                            resumed = true
                            continuation.resume(permissionResult)
                        }
                    }

                    override fun onDenied(
                        permissionResult: PermissionResult,
                        denied: List<String?>?,
                        foreverDenied: List<String?>?,
                    ) {
                        if (!resumed) {
                            resumed = true
                            continuation.resumeWithException(PermissionException(permissionResult))
                        }
                    }

                }).ask()
        }
    }