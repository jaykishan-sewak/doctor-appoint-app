package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentDoctorAddressBinding
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.extension.toast
import com.android.doctorapp.util.permission.RuntimePermission.Companion.askPermission
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import java.util.Locale


class DoctorAddressFragment :
    BaseFragment<FragmentDoctorAddressBinding>(R.layout.fragment_doctor_address) {

//    @Inject
//    lateinit var viewModelFactory: ViewModelProvider.Factory
//    private val viewModel by viewModels<AdminDashboardViewModel> { viewModelFactory }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val locationUpdateInterval: Long = 0
    private val locationUpdateFastestInterval: Long = 0
    private val locationRequestCode = 100


    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.title_profile)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        binding.textUseCurrentLocation.setOnClickListener {
            askPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).onAccepted {
                val locationManager =
                    requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

                if (gps) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@onAccepted
                    }
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
//                             getting the last known or current location
                            if (location != null) {
                                latitude = location.latitude
                                longitude = location.longitude
                                val addresses: List<Address?>?
                                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                                addresses = geocoder.getFromLocation(latitude, longitude, 1)
                                val address: String = addresses!![0].getAddressLine(0)
                                context?.toast("$address")

                                findNavController().previousBackStackEntry?.savedStateHandle?.set("aaddrreess", address)

                                findNavController().popBackStack()
                            } else {
                                context?.toast(getString(R.string.something_went_wrong))
                            }
                        }
                        .addOnFailureListener {
                            context?.toast(getString(R.string.failed_to_get_current_location))
                        }
                } else {
                    val locationRequest: LocationRequest = LocationRequest.create()
                        .setInterval(locationUpdateInterval)
                        .setFastestInterval(locationUpdateFastestInterval)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    val builder = LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                    LocationServices
                        .getSettingsClient(requireActivity())
                        .checkLocationSettings(builder.build())
                        .addOnSuccessListener(requireActivity()) { response: LocationSettingsResponse? -> }
                        .addOnFailureListener(requireActivity()) { ex ->
                            if (ex is ResolvableApiException) {
                                try {
                                    val resolvable = ex
                                    resolvable.startResolutionForResult(
                                        requireActivity(),
                                        locationRequestCode
                                    )
//
//                                    val intent: Intent = resolvable.startResolutionForResult(requireActivity())
//                                    gpsEnableLauncher.launch(resolvable)

                                } catch (sendEx: SendIntentException) {
                                    // Ignore the error.
                                }
                            }
                        }
                }

            }.onDenied {
                context?.toast(getString(R.string.location_permission))
            }.ask()
        }
        return binding {

        }.root
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        if (locationRequestCode == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
//                binding.textUseCurrentLocation.performClick()

            } else {
                context?.toast(getString(R.string.location_permission))
//                Log.d("TAG", "onActivityResult: else")
            }
        }
        Log.d("TAG", "onActivityResult: ")
    }


}