package com.android.infusiontemplate.util.permission

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment


class PermissionFragment : Fragment() {
    private val permissionsList: MutableList<String> = ArrayList()

    @Nullable
    private var listener: PermissionListener? = null
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments: Bundle? = arguments
        if (arguments != null) {
            val permissionsArgs: List<String>? = arguments.getStringArrayList(LIST_PERMISSIONS)
            if (permissionsArgs != null) {
                permissionsList.addAll(permissionsArgs)
            }
        }
    }

    private val activityResultLauncher: ActivityResultLauncher<Array<String>> =  registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (listener!=null) {
            val listener = listener
            val acceptedPermissions: MutableList<String> = ArrayList()
            val askAgainPermissions: MutableList<String> = ArrayList()
            val refusedPermissions: MutableList<String> = ArrayList()

            for (entry in result.entries){
                val permissionName = entry.key
                if (entry.value) {
                    acceptedPermissions.add(permissionName)
                } else {
                    if (shouldShowRequestPermissionRationale(permissionName)) {
                        //listener.onDenied(permissionResult);
                        askAgainPermissions.add(permissionName)
                    } else {
                        refusedPermissions.add(permissionName)
                        //listener.onForeverDenied(permissionResult);
                    }
                }
            }
            listener?.onRequestPermissionsResult(
                acceptedPermissions,
                refusedPermissions,
                askAgainPermissions
            )
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)
                ?.commitAllowingStateLoss()
        }

    }


    override fun onResume() {
        super.onResume()
        if (permissionsList.size > 0) {
            val perms: Array<String> = permissionsList.toTypedArray()
            activityResultLauncher.launch(perms)
        } else {
            activity?.supportFragmentManager?.beginTransaction()
                ?.remove(this)
                ?.commitAllowingStateLoss()
        }
    }

    fun setListener(@Nullable listener: PermissionListener?): PermissionFragment {
        this.listener = listener
        return this
    }

    interface PermissionListener {
        fun onRequestPermissionsResult(
            acceptedPermissions: List<String>,
            refusedPermissions: List<String>,
            askAgainPermissions: List<String>,
        )
    }

    companion object {
        const val LIST_PERMISSIONS = "LIST_PERMISSIONS"
        fun newInstance(permissions: List<String?>?): PermissionFragment {
            val args = Bundle()
            args.putStringArrayList(LIST_PERMISSIONS, ArrayList(permissions.orEmpty()))
            val fragment = PermissionFragment()
            fragment.arguments = args
            return fragment
        }
    }
}