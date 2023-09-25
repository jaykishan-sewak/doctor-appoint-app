package com.android.doctorapp.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentFeedbackDetailsBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.constants.ConstantKey
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
        .withNavigationIcon(requireActivity().getDrawable(R.drawable.ic_back_white))
        .withNavigationListener {
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
        val arguments: Bundle? = arguments
        if (arguments != null) {
            viewModel.doctorId.value =
                arguments.getString(ConstantKey.BundleKeys.DOCTOR_ID)
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
            if (it)
                findNavController().popBackStack()
        }

    }


}