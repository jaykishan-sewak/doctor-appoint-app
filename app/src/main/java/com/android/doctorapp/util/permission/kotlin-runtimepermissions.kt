package com.android.doctorapp.util.permission

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.doctorapp.util.permission.callback.ResponseCallback

fun Fragment.askPermission(vararg permissions: String, acceptedblock: (PermissionResult) -> Unit): KotlinRuntimePermission {
    return KotlinRuntimePermission(RuntimePermission.askPermission(activity)
        .request(permissions.toList())
        .onAccepted(acceptedblock))
}

fun FragmentActivity.askPermission(vararg permissions: String, acceptedblock: (PermissionResult) -> Unit): KotlinRuntimePermission {
    return KotlinRuntimePermission(RuntimePermission.askPermission(this)
        .request(permissions.toList())
        .onAccepted(acceptedblock))
}

class KotlinRuntimePermission(var runtimePermission: RuntimePermission) {

    init {
        runtimePermission.ask()
    }

    fun onDeclined(block: ((PermissionResult) -> Unit)) : KotlinRuntimePermission {
        runtimePermission.onResponse(object : ResponseCallback{
            override fun onResponse(result: PermissionResult) {
                if (result.hasDenied() || result.hasForeverDenied()){
                    block(result)
                }
            }

        })
        return this
    }
}