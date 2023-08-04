package com.android.infusiontemplate.util.permission.callback

import com.android.infusiontemplate.util.permission.PermissionResult

interface ForeverDeniedCallback {
    fun onForeverDenied(result: PermissionResult)
}