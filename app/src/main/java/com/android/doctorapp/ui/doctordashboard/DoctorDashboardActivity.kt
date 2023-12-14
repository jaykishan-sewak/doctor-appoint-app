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
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.APPOINTMENT_DOCUMENT_ID
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DOCTOR_FRAGMENT
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DOCUMENT_ID
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.FRAGMENT_TYPE
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.IS_BOOK_APPOINTMENT
import com.android.doctorapp.util.constants.ConstantKey.GPS_REQUEST_CODE
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.positiveButton
import com.android.doctorapp.util.extension.toast

class DoctorDashboardActivity :
    BaseActivity<ActivityDoctorDashboardBinding>(R.layout.activity_doctor_dashboard) {


//    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        navController = findNavController(R.id.nav_host_fragment)
        binding.navView.setupWithNavController(navController)

        val fragmentType = intent.extras?.getString(FRAGMENT_TYPE)
        val documentId = intent.extras?.getString(DOCUMENT_ID)
        val isBookAppointment = intent.extras?.getBoolean(IS_BOOK_APPOINTMENT)
        if (fragmentType == DOCTOR_FRAGMENT) {
            val bundle = Bundle()
            bundle.putString(APPOINTMENT_DOCUMENT_ID, documentId)
            bundle.putBoolean(IS_BOOK_APPOINTMENT, isBookAppointment!!)
            navController.navigate(R.id.AppointmentDetails, bundle)
            intent.putExtras(Bundle().apply {
                putString(DOCUMENT_ID, documentId)
                putBoolean(IS_BOOK_APPOINTMENT, isBookAppointment)
                putString(FRAGMENT_TYPE, "")
            })
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
            val fragmentManager = supportFragmentManager
            val navHostFragment = fragmentManager.findFragmentById(R.id.nav_host_fragment)
            if (navHostFragment is NavHostFragment) {
                // Get the current fragment in the navigation host
                val currentFragment =
                    navHostFragment.childFragmentManager.fragments.firstOrNull()

                if (resultCode == RESULT_OK) {
                    if (currentFragment is DoctorAddressFragment) {
                        // Now you have a reference to the DoctorAddressFragment
                        currentFragment.requestLocationUpdates()
                    }
                } else {
                    baseContext.toast(getString(R.string.gps_permission_denied))
                    this.alert {
                        setMessage(R.string.dialog_msg_please_turn_on_gps)
                        positiveButton(resources.getString(R.string.ok)) {
                            // Now you have a reference to the DoctorAddressFragment
                            if (currentFragment is DoctorAddressFragment) {
                                // Now you have a reference to the DoctorAddressFragment
                                currentFragment.requestLocationUpdates()
                            }
                        }
                    }
                }
            }
        }
    }

}