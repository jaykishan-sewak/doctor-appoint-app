package com.android.doctorapp.util.permission

import androidx.annotation.Nullable

class PermissionResult(
    val runtimePermission: RuntimePermission,
    @Nullable accepted: List<String?>?,
    @Nullable foreverDenied: List<String>?,
    @Nullable denied: List<String>?
) {
    val accepted: MutableList<String?> = ArrayList()
    val foreverDenied: MutableList<String> = ArrayList()
    val denied: MutableList<String> = ArrayList()
    fun askAgain() {
        runtimePermission.ask()
    }

    fun isAccepted(): Boolean {
        return foreverDenied.isEmpty() && denied.isEmpty()
    }

    fun goToSettings() {
        runtimePermission.goToSettings()
    }

    fun hasDenied(): Boolean {
        return denied.isNotEmpty()
    }

    fun hasForeverDenied(): Boolean {
        return foreverDenied.isNotEmpty()
    }

    init {
        if (accepted != null) {
            this.accepted.addAll(accepted)
        }
        if (foreverDenied != null) {
            this.foreverDenied.addAll(foreverDenied)
        }
        if (denied != null) {
            this.denied.addAll(denied)
        }
    }
}