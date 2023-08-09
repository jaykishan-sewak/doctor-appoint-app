package com.android.doctorapp.util.permission

class PermissionException(val permissionResult: PermissionResult) : Exception() {

    private val accepted: List<String?>
    private val foreverDenied: List<String>
    private val denied: List<String>
    private val runtimePermission: RuntimePermission

    init {
        accepted = permissionResult.accepted.toList()
        foreverDenied = permissionResult.foreverDenied
        denied = permissionResult.denied
        runtimePermission = permissionResult.runtimePermission
    }

    fun goToSettings(){
        permissionResult.goToSettings()
    }

    fun askAgain(){
        permissionResult.askAgain()
    }

    fun isAccepted(): Boolean {
        return permissionResult.isAccepted()
    }

    fun hasDenied(): Boolean {
        return permissionResult.hasDenied()
    }

    fun hasForeverDenied(): Boolean {
        return permissionResult.hasForeverDenied()
    }

}