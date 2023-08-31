package com.android.doctorapp.ui.doctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentDoctorDashboardBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar

class DoctorDashboardFragment: BaseFragment<FragmentDoctorDashboardBinding>(R.layout.fragment_doctor_dashboard) {

    override fun builder() = FragmentToolbar.Builder()
        .withId(FragmentToolbar.NO_TOOLBAR)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding.txtClick.setOnClickListener {
            findNavController().navigate(R.id.UpdateDoctorFragment)
        }

        return binding {
            lifecycleOwner = viewLifecycleOwner
        }.root
    }


}