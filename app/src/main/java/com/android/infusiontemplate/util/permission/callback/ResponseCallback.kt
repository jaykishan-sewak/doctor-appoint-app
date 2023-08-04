package com.android.infusiontemplate.util.permission.callback

import com.android.infusiontemplate.util.permission.PermissionResult

interface ResponseCallback {
    fun onResponse(result: PermissionResult)
}