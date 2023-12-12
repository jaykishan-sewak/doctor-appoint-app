package com.android.doctorapp.ui.userdashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.ActivityUserDashboardBinding
import com.android.doctorapp.di.base.BaseActivity
import com.android.doctorapp.ui.userdashboard.userfragment.UserAppointmentFragment
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.APPOINTMENT_DOCUMENT_ID
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DOCUMENT_ID
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.FRAGMENT_TYPE
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.USER_FRAGMENT
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.positiveButton
import com.android.doctorapp.util.extension.toast


class UserDashboardActivity :
    BaseActivity<ActivityUserDashboardBinding>(R.layout.activity_user_dashboard) {
//    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragmentType = intent.extras?.getString(FRAGMENT_TYPE)
        binding.lifecycleOwner = this
        navController = findNavController(R.id.userNavHostFragment)
        binding.userNavView.setupWithNavController(navController)

        if (fragmentType == USER_FRAGMENT) {
            val documentId = intent.extras?.getString(DOCUMENT_ID)
            val bundle = Bundle()
            bundle.putString(APPOINTMENT_DOCUMENT_ID, documentId)
            navController.navigate(R.id.BookingDetail, bundle)
            intent.putExtras(Bundle().apply {
                putString(DOCUMENT_ID, documentId)
                putString(FRAGMENT_TYPE, "")
            })
        }

        binding.userNavView.setOnItemSelectedListener { item ->
            val selectedTabId = item.itemId
            val currentTabId = navController.currentDestination?.id ?: 0
            if (selectedTabId == currentTabId) {
                navController.popBackStack(selectedTabId, inclusive = false)
            } else {
                navController.popBackStack(currentTabId, inclusive = true)
                navController.navigate(selectedTabId)
            }
            true
        }
        navController.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, _: Bundle? ->
            if (destination.id == R.id.navigation_search || destination.id == R.id.navigation_booking || destination.id == R.id.navigation_user_profile) binding.userNavView.visibility =
                View.VISIBLE
            else binding.userNavView.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val fragmentManager = supportFragmentManager
        val navHostFragment = fragmentManager.findFragmentById(R.id.userNavHostFragment)
        if (navHostFragment is NavHostFragment) {
            // Get the current fragment in the navigation host
            val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
            if (currentFragment is UserAppointmentFragment) {
                if (requestCode == 1 && resultCode == RESULT_OK) {
                    // Now you have a reference to the UserAppointmentFragment
                    currentFragment.requestLocationUpdates()
                    currentFragment.checkNotificationPermission()

                } else {
                    // Now you have a reference to the UserAppointmentFragment
                    currentFragment.checkNotificationPermission()
                    this.toast(getString(R.string.gps_permission_denied))
                    this.alert {
                        setMessage(R.string.dialog_msg_please_turn_on_gps)
                        positiveButton(resources.getString(R.string.ok)) {
                            // Now you have a reference to the UserAppointmentFragment
                            currentFragment.requestLocationUpdates()
                        }
                    }
                }
            }

        }

    }
}