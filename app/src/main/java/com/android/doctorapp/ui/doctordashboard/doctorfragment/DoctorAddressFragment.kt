package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
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
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.extension.isGPSEnabled
import com.android.doctorapp.util.extension.toast
import com.android.doctorapp.util.permission.RuntimePermission.Companion.askPermission
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import okhttp3.internal.wait
import java.util.Locale


class DoctorAddressFragment :
    BaseFragment<FragmentDoctorAddressBinding>(R.layout.fragment_doctor_address) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val locationUpdateInterval: Long = 0
    private val locationUpdateFastestInterval: Long = 0
    private val locationRequestCode = 100

    private lateinit var locationRequest: LocationRequest

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.title_profile)
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.create()
            .setInterval(10000) // 10 seconds
            .setFastestInterval(5000) // 5 seconds
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        binding.textUseCurrentLocation.setOnClickListener {
            askPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).onAccepted {
                requestLocationUpdates()
            }.onDenied {
                context?.toast(getString(R.string.location_permission))
            }.ask()
        }

        return binding {
        }.root

    }

    fun requestLocationUpdates() {
        Log.d("TAG", "requestLocationUpdates: ")
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (context?.isGPSEnabled() == true) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } else {
            /*val locationRequest: LocationRequest = LocationRequest.create()
                .setInterval(locationUpdateInterval)
                .setFastestInterval(locationUpdateFastestInterval)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
            LocationServices
                .getSettingsClient(requireActivity())
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(requireActivity()) { response: LocationSettingsResponse? ->
                    Log.d("TAG", "requestLocationUpdates 1 : $response")
                }
                .addOnFailureListener(requireActivity()) { ex ->
                    Log.d("TAG", "requestLocationUpdates 2 : ${ex.message}")
                    if (ex is ResolvableApiException) {
                        try {
                            ex.startResolutionForResult(
                                requireActivity(),
                                locationRequestCode
                            )
                        } catch (sendEx: IntentSender.SendIntentException) {
                        }
                    }
                }
                .addOnCompleteListener {
                    Log.d("TAG", "requestLocationUpdates: ")
                }
                .continueWith { 
                    
                }
                .isComplete*/
            GpsUtils(requireContext()).turnGPSOn(object : GpsUtils.onGpsListener {
                override fun gpsStatus(isGPSEnable: Boolean) {
                    Log.d("TAG", "gpsStatus: $isGPSEnable")
                }

            })
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.lastLocation?.let { location ->
                // Handle the location update here
                latitude = location.latitude
                longitude = location.longitude

                val addresses: List<Address>?
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address: String = addresses!![0].getAddressLine(0)

                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "address",
                    address
                )
                fusedLocationClient.removeLocationUpdates(this)
                findNavController().popBackStack()
            }
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        Log.d("TAG", "onActivityResult: ")
        if (locationRequestCode == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
            } else {
                context?.toast(getString(R.string.location_permission))
            }
        }
    }
}