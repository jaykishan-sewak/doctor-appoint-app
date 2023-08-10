package com.android.doctorapp.util.permission.callback

import com.android.doctorapp.util.permission.PermissionResult

interface PermissionListener {
    fun onAccepted(permissionResult: PermissionResult, accepted: List<String?>?)
    fun onDenied(
        permissionResult: PermissionResult,
        denied: List<String?>?,
        foreverDenied: List<String?>?
    )
}