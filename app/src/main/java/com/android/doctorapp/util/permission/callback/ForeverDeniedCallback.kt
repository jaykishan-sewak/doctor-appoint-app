package com.android.doctorapp.util.permission.callback

import com.android.doctorapp.util.permission.PermissionResult

interface ForeverDeniedCallback {
    fun onForeverDenied(result: PermissionResult)
}