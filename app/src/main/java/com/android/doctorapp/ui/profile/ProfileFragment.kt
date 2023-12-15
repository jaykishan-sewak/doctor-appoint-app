package com.android.doctorapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentProfileBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.ui.authentication.AuthenticationActivity
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.EXTRAS_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.IS_DARK_THEME_ENABLED_KEY
import com.android.doctorapp.util.constants.ConstantKey.PROFILE_UPDATED
import com.android.doctorapp.util.extension.fetchImageOrShowError
import com.android.doctorapp.util.extension.openEmailSender
import com.android.doctorapp.util.extension.openPhoneDialer
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileFragment : BaseFragment<FragmentProfileBinding>(R.layout.fragment_profile) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ProfileViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder().withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.title_profile)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white)).build()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        viewModel.viewModelScope.launch {
            val isDarkTheme = viewModel.session.getBoolean(IS_ENABLED_DARK_MODE).firstOrNull()
            if (isDarkTheme != null) {
                if (isDarkTheme == true) {
                    viewModel.isDarkThemeClicked.value = isDarkTheme
                } else
                    viewModel.isDarkThemeClicked.value = isDarkTheme
            }
        }
        return binding {
            viewModel = this@ProfileFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObservers(binding)
    }


    private fun registerObservers(layoutBinding: FragmentProfileBinding) {

        viewModel.isDarkThemeClicked.observe(viewLifecycleOwner) {
            if (it == true) {
                layoutBinding.tvThemeEnable.text =
                    requireActivity().getString(R.string.disable_dark_theme)
            } else {
                layoutBinding.tvThemeEnable.text =
                    requireActivity().getString(R.string.enable_dark_theme)
            }
        }

        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(PROFILE_UPDATED)
            ?.observe(viewLifecycleOwner) {
                if (it) {
                    viewModel.getUserProfileData()
                }
            }

        if (viewModel.userProfileDataResponse.value == null) viewModel.getUserProfileData()


        viewModel.phoneClick.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                requireActivity().openPhoneDialer(it)
                viewModel.phoneClick.value = ""
            }
        }
        viewModel.emailClick.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                requireActivity().openEmailSender(it)
                viewModel.emailClick.value = ""
            }
        }

        viewModel.navigateToLogin.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.updateUserData("")
            }
        }

        viewModel.isTokenEmptySuccessFully.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.isTokenEmptySuccessFully.value = false
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                val extras = Bundle()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                extras.putBoolean(
                    IS_DARK_THEME_ENABLED_KEY, viewModel.isDarkThemeClicked.value == true
                )
                intent.putExtra(EXTRAS_KEY, extras)
                viewModel.clearSession()
                viewModel.isClearSessionSuccessFully.observe(viewLifecycleOwner) { it1 ->
                    if (it1) {
                        startActivity(intent)
                    }
                }
            }
        }
        viewModel.navigationListener.observe(viewLifecycleOwner) {
            findNavController().navigate(it)
        }

    }


    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            context?.fetchImageOrShowError(result) {
                viewModel.setImage(it)
            }
        }
}