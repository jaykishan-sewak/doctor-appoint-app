package com.android.doctorapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUserDashboardBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.ui.profile.adapter.UserDoctorItemAdapter
import javax.inject.Inject


class UserDashboardFragment :
    BaseFragment<FragmentUserDashboardBinding>(R.layout.fragment_user_dashboard) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: UserDashboardViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: UserDoctorItemAdapter

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.nearest_doctor)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }


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
            viewModel = this@UserDashboardFragment.viewModel
        }
        setUpWithViewModel(viewModel)
        registerObserver(layoutBinding)
        return layoutBinding.root
    }

    private fun registerObserver(layoutBinding: FragmentUserDashboardBinding) {
        viewModel.getItems()
        setAdapter(emptyList())
        layoutBinding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        layoutBinding.recyclerView.adapter = adapter

        viewModel.doctorList.observe(viewLifecycleOwner) { it1 ->
            adapter.filterList(it1)
        }


    }


    private fun setAdapter(items: List<UserDataResponseModel>) {
        adapter = UserDoctorItemAdapter(
            items,
            object : UserDoctorItemAdapter.OnItemClickListener {
                override fun onItemClick(item: UserDataResponseModel, position: Int) {
                    findNavController().navigate(R.id.action_userDashboard_to_bookAppointment)
                }
            }
        )
    }


}