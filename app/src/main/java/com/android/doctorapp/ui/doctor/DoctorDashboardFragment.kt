package com.android.doctorapp.ui.doctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentDoctorDashboardBinding
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar

class DoctorDashboardFragment :
    BaseFragment<FragmentDoctorDashboardBinding>(R.layout.fragment_doctor_dashboard) {

    override fun builder() = FragmentToolbar.Builder()
        .withId(FragmentToolbar.NO_TOOLBAR)
        .build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)


        return binding {
            lifecycleOwner = viewLifecycleOwner
        }.root
    }


}