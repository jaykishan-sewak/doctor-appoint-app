package com.android.doctorapp.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentAdminDashboardBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.ui.admin.adapter.AdminDoctorItemAdapter
import com.google.gson.Gson
import javax.inject.Inject


class AdminDashboardFragment :
    BaseFragment<FragmentAdminDashboardBinding>(R.layout.fragment_admin_dashboard) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AdminDashboardViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: AdminDoctorItemAdapter
    override fun builder() = FragmentToolbar.Builder()
        .withId(R.id.toolbar)
        .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.blue))
        .withTitle(R.string.doctor_list)
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
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@AdminDashboardFragment.viewModel
        }
        setUpWithViewModel(viewModel)
        registerObserver(layoutBinding)
        return layoutBinding.root
    }

    private fun registerObserver(layoutBinding: FragmentAdminDashboardBinding) {
        viewModel.getItems()
        viewModel.doctorList.observe(viewLifecycleOwner) {
            updateRecyclerView(it)
            layoutBinding.recyclerView.layoutManager = GridLayoutManager(requireActivity(), 2)
            layoutBinding.recyclerView.adapter = adapter

        }
    }

    private fun updateRecyclerView(items: List<UserDataResponseModel>) {
        adapter = AdminDoctorItemAdapter(
            requireActivity(),
            items,
            object : AdminDoctorItemAdapter.OnItemClickListener {
                override fun onItemClick(item: UserDataResponseModel, position: Int) {
                    Log.d("item Cllick ---", Gson().toJson(item))
                }

                override fun onItemDelete(item: UserDataResponseModel, position: Int) {
                    viewModel.deleteDoctor(item.id, position)
                }

            })
    }

}