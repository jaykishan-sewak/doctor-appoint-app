package com.android.infusiontemplate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.infusiontemplate.R
import com.android.infusiontemplate.databinding.FragmentProfileBinding
import com.android.infusiontemplate.di.AppComponentProvider
import com.android.infusiontemplate.di.base.BaseFragment
import com.android.infusiontemplate.di.base.toolbar.FragmentToolbar
import com.android.infusiontemplate.ui.authentication.AuthenticationActivity
import com.android.infusiontemplate.util.extension.fetchImageOrShowError
import com.android.infusiontemplate.util.extension.showImagePicker
import com.android.infusiontemplate.util.extension.startActivityFinish
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
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.purple_500))
            .withTitle(getString(R.string.title_profile))
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding {
            viewModel = this@ProfileFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObservers()
    }

    private fun registerObservers() {
        viewModel.apply {
            navigateToLogin.observe(viewLifecycleOwner, {
                if (it) startActivityFinish<AuthenticationActivity> { }
            })

            onProfilePictureClicked.observe(viewLifecycleOwner, {
                showImagePicker(startForProfileImageResult)
            })
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            context?.fetchImageOrShowError(result) {
                viewModel.setImage(it)
            }
        }
}