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
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentAllFeedbackBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.ui.feedback.adapter.AllFeedbackReviewsAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class AllFeedbackFragment :
    BaseFragment<FragmentAllFeedbackBinding>(R.layout.fragment_all_feedback) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AllFeedbackViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: AllFeedbackReviewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        lifecycleScope.launch {
            viewModel.session.getBoolean(IS_ENABLED_DARK_MODE).collectLatest {
                if (it == true)
                    binding.feedbackLayout.setBackgroundColor(requireActivity().getColor(R.color.aap_bg_dark_grey))
                else
                    binding.feedbackLayout.setBackgroundColor(requireActivity().getColor(R.color.app_bg))
            }
        }
        val arguments: Bundle? = arguments
        if (arguments != null) {
            val doctorDataObj =
                requireArguments().getString(ConstantKey.BundleKeys.BOOK_APPOINTMENT_DATA)
            viewModel.doctorDataObj.value =
                Gson().fromJson(doctorDataObj, UserDataResponseModel::class.java)
        }
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@AllFeedbackFragment.viewModel
        }
        return layoutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver(binding)
    }


    private fun registerObserver(layoutBinding: FragmentAllFeedbackBinding) {
        layoutBinding.allFeedbackRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AllFeedbackReviewsAdapter(emptyList())
        viewModel.getAllFeedbackList()
        layoutBinding.allFeedbackRecyclerView.adapter = adapter
        viewModel.feedbackList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter.filterList(it)
                viewModel.dataFound.value = true
            } else {
                adapter.filterList(emptyList())
                viewModel.dataFound.value = false
            }
        }
    }

    override fun builder() = FragmentToolbar.Builder()
        .withId(R.id.toolbar)
        .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        .withTitle(R.string.patients_feedback_label)
        .withNavigationIcon(
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_back_white
            )
        )
        .withNavigationListener {
            findNavController().popBackStack()
        }
        .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
        .build()

}