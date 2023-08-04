package com.android.infusiontemplate.util.permission.callback

import com.android.infusiontemplate.util.permission.PermissionResult

interface PermissionListener {
    fun onAccepted(permissionResult: PermissionResult, accepted: List<String?>?)
    fun onDenied(
        permissionResult: PermissionResult,
        denied: List<String?>?,
        foreverDenied: List<String?>?
    )
}