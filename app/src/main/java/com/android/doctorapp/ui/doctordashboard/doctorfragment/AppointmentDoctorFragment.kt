package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentAppointmentDoctorBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.local.IS_NEW_USER_TOKEN
import com.android.doctorapp.repository.local.USER_TOKEN
import com.android.doctorapp.repository.models.Header
import com.android.doctorapp.ui.doctordashboard.adapter.PatientListAdapter
import com.android.doctorapp.util.constants.ConstantKey.APPOINTMENT_DETAILS_UPDATED
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DATE
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.REQUEST_FRAGMENT
import com.android.doctorapp.util.extension.toast
import com.android.doctorapp.util.permission.RuntimePermission
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class AppointmentDoctorFragment :
    BaseFragment<FragmentAppointmentDoctorBinding>(R.layout.fragment_appointment_doctor) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AppointmentDoctorViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: PatientListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        lifecycleScope.launch {
            viewModel.session.getBoolean(IS_ENABLED_DARK_MODE).collectLatest {
                viewModel.isDarkThemeEnable.value = it == true
            }
        }
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@AppointmentDoctorFragment.viewModel
        }

        RuntimePermission.askPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ).onAccepted {
            requestLocationUpdates()
        }.onDenied {
            context?.toast(resources.getString(R.string.notifications_disabled))
        }.ask()

        setUpWithViewModel(viewModel)
        registerObserver(layoutBinding)

        return layoutBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are not granted, request them using the launcher
            requestNotificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            // Permissions are granted, proceed with location updates
            requireActivity().toast(resources.getString(R.string.notifications_enabled))
        } else {
            // Handle permission denial
            requireActivity().toast(resources.getString(R.string.notifications_disabled))
        }
    }

    private fun registerObserver(layoutBinding: FragmentAppointmentDoctorBinding) {
        setAdapter(emptyList())
        layoutBinding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        layoutBinding.recyclerView.adapter = adapter

        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            APPOINTMENT_DETAILS_UPDATED
        )
            ?.observe(viewLifecycleOwner) {
                if (it) {
                    viewModel.getAppointmentList()
                }
            }

        viewModel.finalAppointmentList.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                adapter.filterList(it)
                viewModel.dataFound.value = true
            } else {
                viewModel.dataFound.value = false
            }
        }
        viewModel.viewModelScope.launch {
            viewModel.session.getBoolean(IS_NEW_USER_TOKEN).collectLatest {
                if (it == true) {
                    viewModel.session.getString(USER_TOKEN).collectLatest { it1 ->
                        viewModel.updateUserData(it1)
                    }
                }
            }
        }
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.appointment)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }

    private fun setAdapter(items: List<Header>) {
        adapter = PatientListAdapter(
            items,
            object : PatientListAdapter.OnItemClickListener {
                override fun onItemClick(item: Header, position: Int) {
                    val bundle = Bundle()
                    bundle.putString(DATE, Gson().toJson(item.date))
                    bundle.putBoolean(REQUEST_FRAGMENT, false)
                    findNavController().navigate(
                        R.id.action_doctor_appointment_to_selected_date,
                        bundle
                    )
                }

                override fun onClick(contact: String) {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$contact")
                    requireActivity().startActivity(intent)
                }
            }
        )
    }


}