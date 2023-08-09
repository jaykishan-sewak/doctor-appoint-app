package com.android.doctorapp.util.permission.callback

import com.android.doctorapp.util.permission.PermissionResult

interface ResponseCallback {
    fun onResponse(result: PermissionResult)
}