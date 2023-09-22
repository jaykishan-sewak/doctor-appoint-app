package com.android.doctorapp.ui.userdashboard

import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.ActivityUserDashboardBinding
import com.android.doctorapp.di.base.BaseActivity

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
    }
}