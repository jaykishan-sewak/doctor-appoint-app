package com.android.infusiontemplate.util.permission.callback

import com.android.infusiontemplate.util.permission.PermissionResult

interface AcceptedCallback {
    fun onAccepted(result: PermissionResult)
}