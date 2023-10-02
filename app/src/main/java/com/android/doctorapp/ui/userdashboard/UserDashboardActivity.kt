package com.android.doctorapp.ui.userdashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.ActivityUserDashboardBinding
import com.android.doctorapp.di.base.BaseActivity
import com.android.doctorapp.ui.userdashboard.userfragment.UserAppointmentFragment
import com.android.doctorapp.util.extension.toast


class UserDashboardActivity :
    BaseActivity<ActivityUserDashboardBinding>(R.layout.activity_user_dashboard) {
//    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        navController = findNavController(R.id.userNavHostFragment)
        binding.userNavView.setupWithNavController(navController)
        binding.userNavView.setOnItemSelectedListener { item ->
            val selectedTabId = item.itemId
            val currentTabId = navController.currentDestination?.id ?: 0
            if (selectedTabId == currentTabId) {
                navController.popBackStack(selectedTabId, inclusive = false)
            } else {
                navController.navigate(selectedTabId)
            }
            true
        }
        navController.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, _: Bundle? ->
            if (destination.id == R.id.navigation_search
                || destination.id == R.id.navigation_booking
                || destination.id == R.id.navigation_user_profile
            )
                binding.userNavView.visibility = View.VISIBLE
            else
                binding.userNavView.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val fragmentManager = supportFragmentManager
            val navHostFragment = fragmentManager.findFragmentById(R.id.userNavHostFragment)
            if (navHostFragment is NavHostFragment) {
                // Get the current fragment in the navigation host
                val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
                if (currentFragment is UserAppointmentFragment) {
                    // Now you have a reference to the UserAppointmentFragment
                    currentFragment.requestLocationUpdates()
                }
            }

        } else {
            this.toast(getString(R.string.location_permission))
        }
    }
}