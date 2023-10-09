package com.android.doctorapp.ui.userdashboard.userfragment

import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUserAppointmentBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.ui.userdashboard.userfragment.adapter.UserAppoitmentItemAdapter
import com.android.doctorapp.util.GpsUtils
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DOCTOR_ID
import com.android.doctorapp.util.extension.isGPSEnabled
import com.android.doctorapp.util.extension.toast
import com.android.doctorapp.util.permission.RuntimePermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.Locale
import javax.inject.Inject


class UserAppointmentFragment :
    BaseFragment<FragmentUserAppointmentBinding>(R.layout.fragment_user_appointment) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: UserAppointmentViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: UserAppoitmentItemAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitleString(viewModel.locationCity.value!!)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
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
            viewModel = this@UserAppointmentFragment.viewModel
        }

        setUpWithViewModel(viewModel)
        RuntimePermission.askPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ).onAccepted {
            requestLocationUpdates()
        }.onDenied {
            context?.toast(getString(R.string.location_permission))
        }.ask()

        registerObserver(layoutBinding)
        return layoutBinding.root
    }


    fun requestLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are not granted, request them using the launcher
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        if (requireContext().isGPSEnabled()) {
            fusedLocationClient.requestLocationUpdates(
                GpsUtils(requireContext()).locationRequest,
                locationCallback,
                null // Looper can be provided for the callback thread
            )
        } else {
            GpsUtils(requireContext()).turnGPSOn(object : GpsUtils.onGpsListener {
                override fun gpsStatus(isGPSEnable: Boolean) {
                }
            })

        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            p0.lastLocation.let { location ->
                // Handle the location update here
                latitude = location.latitude
                longitude = location.longitude
                viewModel.getItems(latitude, longitude)
                stopLocationUpdates()
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            // Permissions are granted, proceed with location updates
            requestLocationUpdates()
        } else {
            // Handle permission denial
            context?.toast(getString(R.string.location_permission))
        }
    }


    private fun registerObserver(layoutBinding: FragmentUserAppointmentBinding) {
        setAdapter(emptyList())
        layoutBinding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        layoutBinding.recyclerView.adapter = adapter

        viewModel.doctorList.observe(viewLifecycleOwner) { it1 ->
            if (!it1.isNullOrEmpty()) {
                adapter.filterList(it1)
                viewModel.dataFound.value = false
            } else {
                viewModel.dataFound.value = true
                adapter.filterList(emptyList())
            }
        }
    }


    private fun setAdapter(items: List<UserDataResponseModel>) {
        adapter = UserAppoitmentItemAdapter(
            items,
            object : UserAppoitmentItemAdapter.OnItemClickListener {
                override fun onItemClick(item: UserDataResponseModel, position: Int) {
                    val bundle = Bundle()
                    bundle.putString(ConstantKey.BundleKeys.USER_ID, item.userId)
                    bundle.putString(DOCTOR_ID, item.docId)
                    findNavController().navigate(
                        R.id.action_user_appointment_to_bookAppointment,
                        bundle
                    )
                    binding.searchEt.setText("")
                }
            }
        )
    }

}