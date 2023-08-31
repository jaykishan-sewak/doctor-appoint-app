package com.android.doctorapp.ui.dashboard

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.ActivityDashboardBinding
import com.android.doctorapp.di.base.BaseActivity

class DashboardActivity : BaseActivity<ActivityDashboardBinding>(R.layout.activity_dashboard) {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        navController = findNavController(R.id.nav_host_fragment)
        binding.navView.setupWithNavController(navController)
        binding.navView.setOnItemReselectedListener{}
    }
}