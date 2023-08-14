package com.android.doctorapp.ui.doctor

import androidx.core.content.ContextCompat
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUpdateProfileBinding
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar

class UpdateProfileFragment: BaseFragment<FragmentUpdateProfileBinding>(R.layout.fragment_update_profile) {

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.purple_500))
            .withTitle(R.string.update_profile_title)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }
}