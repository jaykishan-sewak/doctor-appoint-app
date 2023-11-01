package com.android.doctorapp.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentFeedbackBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.ui.feedback.adapter.DoctorListAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.FEEDBACK_SUBMITTED
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.negativeButton
import com.android.doctorapp.util.extension.positiveButton
import com.google.gson.Gson
import javax.inject.Inject

class FeedbackFragment : BaseFragment<FragmentFeedbackBinding>(R.layout.fragment_feedback) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: FeedbackViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: DoctorListAdapter

    override fun builder() = FragmentToolbar.Builder()
        .withId(R.id.toolbar)
        .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        .withTitle(R.string.my_feedback)
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
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@FeedbackFragment.viewModel
        }


        setAdapter(emptyList())
        layoutBinding.doctorListRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        layoutBinding.doctorListRecyclerView.adapter = adapter

        return layoutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver()
    }

    private fun registerObserver() {

        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            FEEDBACK_SUBMITTED
        )
            ?.observe(viewLifecycleOwner) {
                if (it) {
                    viewModel.getUserDoctorList()
                }
            }

        if (viewModel.doctorList.value == null)
            viewModel.getUserDoctorList()

        viewModel.doctorList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                viewModel.dataFound.value = true
                adapter.updateListData(it)
            } else {
                adapter.updateListData(emptyList())
                viewModel.dataFound.value = false
            }
        }

    }

    private fun setAdapter(items: List<UserDataResponseModel>) {
        adapter = DoctorListAdapter(
            requireActivity(),
            items,
            object : DoctorListAdapter.OnItemClickListener {
                override fun onItemClick(item: UserDataResponseModel, position: Int) {
                    if (item.feedbackDetails == null) {
                        val bundle = Bundle()
                        bundle.putString(ConstantKey.BundleKeys.USER_DATA, Gson().toJson(item))
                        findNavController().navigate(
                            R.id.action_feedback_to_feedBack_details, bundle
                        )
                    }

                }

                override fun onEditClick(item: UserDataResponseModel, position: Int) {
                    val bundle = Bundle()
                    bundle.putString(ConstantKey.BundleKeys.USER_DATA, Gson().toJson(item))
                    bundle.putBoolean(ConstantKey.BundleKeys.IS_EDIT_CLICK, true)
                    findNavController().navigate(
                        R.id.action_feedback_to_feedBack_details, bundle
                    )
                }

                override fun onDeleteClick(item: UserDataResponseModel, position: Int) {
                    context?.alert {
                        setTitle(resources.getString(R.string.delete))
                        setMessage(resources.getString(R.string.are_you_sure_want_to_delete))

                        positiveButton(resources.getString(R.string.delete)) { dialog ->
                            viewModel.deleteFeedback(item, position)
                            viewModel.setShowProgress(true)
                            viewModel.getUserDoctorList()
                            dialog.dismiss()
                        }
                        negativeButton(resources.getString(R.string.cancel)) { dialog ->
                            dialog.dismiss()
                        }
                    }

                }


            })
    }
}