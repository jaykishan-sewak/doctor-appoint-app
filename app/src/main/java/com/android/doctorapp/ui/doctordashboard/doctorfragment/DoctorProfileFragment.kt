package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentDoctorProfileBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.ui.profile.ProfileViewModel
import com.android.doctorapp.util.extension.fetchImageOrShowError
import com.android.doctorapp.util.extension.openEmailSender
import com.android.doctorapp.util.extension.openPhoneDialer
import javax.inject.Inject


class DoctorProfileFragment :
    BaseFragment<FragmentDoctorProfileBinding>(R.layout.fragment_doctor_profile) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ProfileViewModel> { viewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        return binding {
            viewModel = this@DoctorProfileFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.title_profile)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .withMenu(R.menu.user_edit_menu)
            .withMenuItems(generateMenuItems(), generateMenuClicks())
            .build()
    }

    private fun generateMenuClicks(): MenuItem.OnMenuItemClickListener {
        return MenuItem.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    viewModel.isDoctorEdit.value = true
                }
            }
            false
        }
    }

    private fun generateMenuItems(): List<Int> {
        return listOf(R.id.action_edit)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObservers()
    }

    private fun registerObservers() {
        viewModel.getUserProfileData()
        viewModel.isDoctorEdit.value = false
        viewModel.isDoctorEdit.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    R.id.action_doctor_profile_to_updateDoctorProfile
                )
            }
        }
        viewModel.phoneClick.observe(viewLifecycleOwner) {
            requireActivity().openPhoneDialer(it)
        }
        viewModel.emailClick.observe(viewLifecycleOwner) {
            requireActivity().openEmailSender(it)
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            context?.fetchImageOrShowError(result) {
                viewModel.setImage(it)
            }
        }


}