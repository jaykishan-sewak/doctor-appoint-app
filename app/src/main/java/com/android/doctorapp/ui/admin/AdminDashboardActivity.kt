package com.android.doctorapp.ui.admin

import android.os.Bundle
import androidx.navigation.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.ActivityAdminDashboardBinding
import com.android.doctorapp.di.base.BaseActivity

class AdminDashboardActivity :
    BaseActivity<ActivityAdminDashboardBinding>(R.layout.activity_admin_dashboard) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        navController = findNavController(R.id.nav_host_admin_fragment)

    }
}