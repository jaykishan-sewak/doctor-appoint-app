package com.android.infusiontemplate.util.permission.callback

import com.android.infusiontemplate.util.permission.PermissionResult

interface DeniedCallback {
    fun onDenied(result: PermissionResult)
}