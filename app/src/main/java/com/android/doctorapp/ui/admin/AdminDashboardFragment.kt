package com.android.doctorapp.ui.admin

import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentAdminDashboardBinding
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar

class AdminDashboardFragment: BaseFragment<FragmentAdminDashboardBinding>(R.layout.fragment_admin_dashboard) {

     override fun builder() = FragmentToolbar.Builder()
        .withId(FragmentToolbar.NO_TOOLBAR)
        .build()

}