package com.android.doctorapp.ui.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.android.doctorapp.R
import com.android.doctorapp.databinding.ActivityMainBinding
import com.android.doctorapp.di.base.BaseActivity
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.local.PreferenceDataStore
import com.android.doctorapp.ui.doctordashboard.doctorfragment.DoctorAddressFragment
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.EXTRAS_KEY
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthenticationActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    //    private lateinit var navController: NavController
    private var isDarkTheme = false

    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.UpdateUserFragment) {
            finish()
        } else if (navController.currentDestination?.id == R.id.UpdateDoctorFragment) {
            finish()
        } else if (navController.currentDestination?.id == R.id.AdminDashboardFragment) {
            finish()
        } else if (navController.currentDestination?.id == R.id.AddDoctorFragment) {
            onBackPressedDispatcher.onBackPressed()
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController(R.id.nav_host_fragment_content_main)
        val sharedPreference = PreferenceDataStore(this)

        val intent = intent
        val extras = intent.getBundleExtra(EXTRAS_KEY)
        if (extras != null) {
            isDarkTheme =
                extras.getBoolean(ConstantKey.BundleKeys.IS_DARK_THEME_ENABLED_KEY) == true
            if (isDarkTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                CoroutineScope(Dispatchers.Main).launch {
                    sharedPreference.putBoolean(IS_ENABLED_DARK_MODE, isDarkTheme)
                }
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                CoroutineScope(Dispatchers.Main).launch {
                    sharedPreference.putBoolean(IS_ENABLED_DARK_MODE, isDarkTheme)
                }
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ConstantKey.GPS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val fragmentManager = supportFragmentManager
                val navHostFragment =
                    fragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
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