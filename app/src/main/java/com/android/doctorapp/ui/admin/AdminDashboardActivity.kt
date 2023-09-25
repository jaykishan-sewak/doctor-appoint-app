package com.android.doctorapp.ui.admin

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.ActivityAdminDashboardBinding
import com.android.doctorapp.di.base.BaseActivity

class AdminDashboardActivity :
    BaseActivity<ActivityAdminDashboardBinding>(R.layout.activity_admin_dashboard) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        navController = findNavController(R.id.nav_host_admin_fragment)
        binding.adminNavView.setupWithNavController(navController)
        binding.adminNavView.setOnItemSelectedListener { item ->
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
            if (destination.id == R.id.admin_dashboard
                || destination.id == R.id.admin_dashboard
            )
                binding.adminNavView.visibility = View.VISIBLE
            else
                binding.adminNavView.visibility = View.GONE
        }
    }
}