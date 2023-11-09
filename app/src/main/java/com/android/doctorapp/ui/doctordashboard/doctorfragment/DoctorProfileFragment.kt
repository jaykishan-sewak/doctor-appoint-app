package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentDoctorProfileBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.ui.authentication.AuthenticationActivity
import com.android.doctorapp.ui.bottomsheet.ClinicImgBottomSheetDialog
import com.android.doctorapp.ui.doctordashboard.adapter.ClinicImgAdapter
import com.android.doctorapp.ui.profile.ProfileViewModel
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DOCTOR_PROFILE_FRAGMENT
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.FROM_WHERE
import com.android.doctorapp.util.extension.fetchImageOrShowError
import com.android.doctorapp.util.extension.openEmailSender
import com.android.doctorapp.util.extension.openPhoneDialer
import javax.inject.Inject


class DoctorProfileFragment :
    BaseFragment<FragmentDoctorProfileBinding>(R.layout.fragment_doctor_profile) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ProfileViewModel> { viewModelFactory }
    lateinit var clinicBottomSheetFragment: ClinicImgBottomSheetDialog
    private lateinit var bindingView: FragmentDoctorProfileBinding
    private lateinit var clinicImgAdapter: ClinicImgAdapter


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
        bindingView = binding {
            viewModel = this@DoctorProfileFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        binding.rvClinicImages.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.addClinicImg.setOnClickListener {
            clinicBottomSheetFragment =
                ClinicImgBottomSheetDialog(object : ClinicImgBottomSheetDialog.DialogListener {
                    override fun getImageUri(uri: Uri) {
                        viewModel.uploadImage(uri)
                    }
                })
            clinicBottomSheetFragment.show(
                requireActivity().supportFragmentManager,
                "BSDialogFragment"
            )
        }
        setUpWithViewModel(viewModel)
        registerObservers()
        return bindingView.root
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.title_profile)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun registerObservers() {

        updateHolidayRecyclerview(arrayListOf())
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(ConstantKey.PROFILE_UPDATED)
            ?.observe(viewLifecycleOwner) {
                if (it) {
                    viewModel.getUserProfileData()
                }
            }
        if (viewModel.userProfileDataResponse.value == null)
            viewModel.getUserProfileData()

        viewModel.isDoctorEdit.value = false
        viewModel.isDoctorEdit.observe(viewLifecycleOwner) {
            if (it) {
                val bundle = Bundle()
                bundle.putString(FROM_WHERE, DOCTOR_PROFILE_FRAGMENT)
                findNavController().navigate(
                    R.id.action_doctor_profile_to_updateDoctorProfile,
                    bundle
                )
            }
        }
        viewModel.phoneClick.observe(viewLifecycleOwner) {
            requireActivity().openPhoneDialer(it)
        }
        viewModel.emailClick.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                requireActivity().openEmailSender(it)
            }
        }
        viewModel.navigateToLogin.observe(viewLifecycleOwner) {
            if (it) {
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
//                startActivityFinish<AuthenticationActivity>()
        }

        viewModel.clinicImgList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                Log.d(TAG, "registerObservers: $it")
                clinicImgAdapter.updateClinicImgList(it)
            }
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            context?.fetchImageOrShowError(result) {
                viewModel.setImage(it)
            }
        }

    private fun updateHolidayRecyclerview(newClinicImgList: ArrayList<String>) {
        clinicImgAdapter = ClinicImgAdapter(newClinicImgList,
            object : ClinicImgAdapter.OnItemClickListener {
                override fun onItemClick(imageUrl: Uri, position: Int) {
                }
            })
        binding.rvClinicImages.adapter = clinicImgAdapter
    }


}