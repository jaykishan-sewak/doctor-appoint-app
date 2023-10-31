package com.android.doctorapp.ui.doctordashboard

import android.content.Intent
import android.os.Bundle
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
import com.android.doctorapp.util.constants.ConstantKey.GPS_REQUEST_CODE
import com.android.doctorapp.util.extension.toast

class DoctorDashboardActivity :
    BaseActivity<ActivityDoctorDashboardBinding>(R.layout.activity_doctor_dashboard) {


//    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        navController = findNavController(R.id.nav_host_fragment)
        binding.navView.setupWithNavController(navController)

        val fragmentType = intent.extras?.getString("fragmentType")
        val documentId = intent.extras?.getString("DocumentId")
        val isBookAppointment = intent.extras?.getBoolean("IsBookAppointment")
        if (fragmentType == "DoctorFragment") {
            val bundle = Bundle()
            bundle.putString("documentId", documentId)
            bundle.putBoolean("isBookAppointment", isBookAppointment!!)
            navController.navigate(R.id.AppointmentDetails, bundle)
        }

        binding.navView.setOnItemSelectedListener { item ->
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
        if (requestCode == GPS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val fragmentManager = supportFragmentManager
                val navHostFragment = fragmentManager.findFragmentById(R.id.nav_host_fragment)
                if (navHostFragment is NavHostFragment) {
                    // Get the current fragment in the navigation host
                    val currentFragment =
                        navHostFragment.childFragmentManager.fragments.firstOrNull()
                    if (currentFragment is DoctorAddressFragment) {
                        // Now you have a reference to the UserAppointmentFragment
                        currentFragment.requestLocationUpdates()
                    }
                }
            } else {
                baseContext.toast(getString(R.string.gps_permission_denied))
            }
        }
    }

}