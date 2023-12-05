package com.android.doctorapp.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentFeedbackDetailsBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.FEEDBACK_SUBMITTED
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class FeedbackDetailFragment :
    BaseFragment<FragmentFeedbackDetailsBinding>(R.layout.fragment_feedback_details) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: FeedbackViewModel by viewModels { viewModelFactory }

    override fun builder() = FragmentToolbar.Builder()
        .withId(R.id.toolbar)
        .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        .withTitle(R.string.feedback)
        .withNavigationIcon(
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_back_white
            )
        )
        .withNavigationListener {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                FEEDBACK_SUBMITTED,
                false
            )
            findNavController().popBackStack()
        }
        .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        lifecycleScope.launch {
            viewModel.session.getBoolean(IS_ENABLED_DARK_MODE).collectLatest {
                if (it == true)
                    binding.etFeedBackMsg.background = AppCompatResources.getDrawable(
                        requireActivity(),
                        R.drawable.custom_edittext_bg_dark
                    )
                else
                    binding.etFeedBackMsg.background = AppCompatResources.getDrawable(
                        requireActivity(),
                        R.drawable.custom_edittext_bg
                    )
            }
        }
        val arguments: Bundle? = arguments
        if (arguments != null) {
            val userObj = arguments.getString(ConstantKey.BundleKeys.USER_DATA)
            viewModel.isEditClick.value = arguments.getBoolean(ConstantKey.BundleKeys.IS_EDIT_CLICK)
            viewModel.userDataObj.value =
                Gson().fromJson(userObj, UserDataResponseModel::class.java)
//            viewModel.doctorId.value = appointmentObj
        }

        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModal = this@FeedbackDetailFragment.viewModel
        }

        return layoutBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver()
    }

    private fun registerObserver() {
        viewModel.navigationListener.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    FEEDBACK_SUBMITTED,
                    true
                )
                findNavController().popBackStack()
            }
        }

        viewModel.isEditClick.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.getUserFeedbackData()
            }
        }


    }


}