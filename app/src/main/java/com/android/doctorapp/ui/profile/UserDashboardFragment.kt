package com.android.doctorapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUserDashboardBinding
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar


class UserDashboardFragment :
    BaseFragment<FragmentUserDashboardBinding>(R.layout.fragment_user_dashboard) {


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

        binding.tvUserDashboardClick.setOnClickListener {
            findNavController().navigate(R.id.UpdateDoctorFragment)
        }

        return binding {
            lifecycleOwner = viewLifecycleOwner
        }.root
    }


}