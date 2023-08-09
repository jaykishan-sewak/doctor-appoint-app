package com.android.doctorapp.util.permission.callback

import com.android.doctorapp.util.permission.PermissionResult

interface DeniedCallback {
    fun onDenied(result: PermissionResult)
}