package com.android.doctorapp.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
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
import com.android.doctorapp.ui.authentication.AuthenticationActivity
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.ADMIN_FRAGMENT
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.ITEM_POSITION
import com.android.doctorapp.util.extension.startActivityFinish
import javax.inject.Inject


class AdminDashboardFragment :
    BaseFragment<FragmentAdminDashboardBinding>(R.layout.fragment_admin_dashboard) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AdminDashboardViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: AdminDoctorItemAdapter
    override fun builder() = FragmentToolbar.Builder()
        .withId(R.id.toolbar)
        .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        .withTitle(R.string.doctor_list)
        .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
        .withMenu(R.menu.admin_logout_menu)
        .withMenuItems(generateMenuItems(), generateMenuClicks())
        .build()


    private fun generateMenuClicks(): MenuItem.OnMenuItemClickListener {
        return MenuItem.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_admin_logout -> {
                    viewModel.isLogoutClick.value = true
                }
            }
            false
        }
    }

    private fun generateMenuItems(): List<Int> {
        return listOf(R.id.action_admin_logout)
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
            viewModel = this@AdminDashboardFragment.viewModel
        }
        return layoutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver(binding)
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(ADMIN_FRAGMENT)
            ?.observe(viewLifecycleOwner) {
                if (it)
                    viewModel.getItems()

            }
    }

    private fun registerObserver(layoutBinding: FragmentAdminDashboardBinding) {
        viewModel.doctorList.observe(viewLifecycleOwner) {
            updateRecyclerView(it!!)
            layoutBinding.recyclerView.layoutManager = GridLayoutManager(requireActivity(), 2)
            layoutBinding.recyclerView.adapter = adapter

        }

        viewModel.navigationListener.observe(viewLifecycleOwner) {
            findNavController().navigate(it)
        }

        viewModel.isLogoutClick.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.signOut()
                startActivityFinish<AuthenticationActivity>()
            }
        }

    }

    private fun updateRecyclerView(items: List<UserDataResponseModel>) {
        adapter = AdminDoctorItemAdapter(
            requireActivity(),
            items,
            object : AdminDoctorItemAdapter.OnItemClickListener {
                override fun onItemClick(item: UserDataResponseModel, position: Int) {
                    val bundle = Bundle()
                    bundle.putString(ConstantKey.BundleKeys.USER_ID, item.userId)
                    bundle.putInt(ITEM_POSITION, position)
                    findNavController().navigate(
                        R.id.admin_to_doctor_details, bundle
                    )
                    binding.searchEt.text?.clear()
                }
            })
    }

}