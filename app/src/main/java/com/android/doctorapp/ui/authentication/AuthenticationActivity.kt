package com.android.doctorapp.ui.authentication

import android.os.Bundle
import android.os.PersistableBundle
import androidx.navigation.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.ActivityMainBinding
import com.android.doctorapp.di.base.BaseActivity

class AuthenticationActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

//    private lateinit var navController: NavController


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

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        navController = findNavController(R.id.nav_host_fragment_content_main)

    }

}