package com.android.doctorapp.util.permission.callback

import com.android.doctorapp.util.permission.PermissionResult

interface AcceptedCallback {
    fun onAccepted(result: PermissionResult)
}