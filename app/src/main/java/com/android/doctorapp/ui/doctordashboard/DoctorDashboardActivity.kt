package com.android.doctorapp.ui.doctordashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.ActivityDoctorDashboardBinding
import com.android.doctorapp.di.base.BaseActivity
import com.android.doctorapp.ui.doctordashboard.doctorfragment.DoctorAddressFragment

class DoctorDashboardActivity :
    BaseActivity<ActivityDoctorDashboardBinding>(R.layout.activity_doctor_dashboard) {


//    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        navController = findNavController(R.id.nav_host_fragment)
        binding.navView.setupWithNavController(navController)
        binding.navView.setOnItemSelectedListener { item ->
            val selectedTabId = item.itemId
            val currentTabId = navController.currentDestination?.id ?: 0
            if (selectedTabId == currentTabId) {
                navController.popBackStack(selectedTabId, inclusive = false)
            } else {
                navController.navigate(selectedTabId)
            }
            true
        }
        navController.addOnDestinationChangedListener { controller: NavController?, destination: NavDestination, arguments: Bundle? ->
            if (destination.id == R.id.navigation_appointment
                || destination.id == R.id.navigation_request
                || destination.id == R.id.navigation_profile
            )
                binding.navView.visibility = View.VISIBLE
            else
                binding.navView.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10000) {
            /*val fragment = navController.currentDestination as? DoctorAddressFragment
            fragment?.requestLocationUpdates()*/
            /*val yourFragmentInBackStack = navController.previousBackStackEntry?.destination
            if (yourFragmentInBackStack?.id == R.id.navigation_doctor_address) {
                yourFragment.yourMethod()
            }*/

            /*val currentDestinationId = navController.currentDestination?.id ?: 0
            if (currentDestinationId == R.id.navigation_doctor_address) {
                val fragmentManager = supportFragmentManager

                val fragment = fragmentManager.findFragmentById(currentDestinationId)
                if (fragment is DoctorAddressFragment) {
                    Log.d("TAG", "onActivityResult: if")
                    fragment.requestLocationUpdates()
                } else {
                    Log.d("TAG", "onActivityResult: else")
                }
            }*/

            val fragmentManager = supportFragmentManager
            val navHostFragment = fragmentManager.findFragmentById(R.id.nav_host_fragment)
            if (navHostFragment is NavHostFragment) {
                // Get the current fragment in the navigation host
                val currentFragment = navHostFragment.childFragmentManager.fragments
                Log.d("TAG", "onActivityResult: $currentFragment")
                currentFragment.forEachIndexed { index, fragment ->
                    Log.d("TAG", "onActivityResult: $fragment")
                    if (fragment is DoctorAddressFragment) {
                        fragment.requestLocationUpdates()
                    }
                }
// if (currentFragment is DoctorAddressFragment) {
// // Now you have a reference to the UserAppointmentFragment
// currentFragment.requestLocationUpdates()
// }
            }

        }

    }

}