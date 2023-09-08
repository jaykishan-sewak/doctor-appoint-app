package com.android.doctorapp.ui.doctordashboard

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.ActivityDoctorDashboardBinding
import com.android.doctorapp.di.base.BaseActivity

class DoctorDashboardActivity :
    BaseActivity<ActivityDoctorDashboardBinding>(R.layout.activity_doctor_dashboard) {


//    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        navController = findNavController(R.id.nav_host_fragment)
        binding.navView.setupWithNavController(navController)
        binding.navView.setOnItemReselectedListener {
        }
    }
}