package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentDoctorAddressBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.ui.doctor.AddDoctorViewModel
import com.android.doctorapp.util.GpsUtils
import com.android.doctorapp.util.constants.ConstantKey.DBKeys.TABLE_USER_DATA
import com.android.doctorapp.util.extension.isGPSEnabled
import com.android.doctorapp.util.extension.toast
import com.android.doctorapp.util.permission.RuntimePermission.Companion.askPermission
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import java.util.Locale
import javax.inject.Inject


class DoctorAddressFragment :
    BaseFragment<FragmentDoctorAddressBinding>(R.layout.fragment_doctor_address) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddDoctorViewModel> { viewModelFactory }


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    var fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var locationRequest: LocationRequest
    val matchingDocs: MutableList<DocumentSnapshot> = ArrayList()

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

        binding.btn.setOnClickListener {
            viewModel.test()
        }

        /*binding.btnGet.setOnClickListener {
            viewModel.getUserData().observe(viewLifecycleOwner) {
                val center = GeoLocation(23.0225, 72.5714)
                val radiusInM = 50.0 * 1000.0
//                Log.d("TAG", "onCreateView: $center   -->     $radiusInM")
                val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
                val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
                for (b in bounds) {
                    val q = fireStore.collection(TABLE_USER_DATA)
                        .orderBy("geohash")
                        .startAt(b.startHash)
                        .endAt(b.endHash)
                    tasks.add(q.get())
                }
                Tasks.whenAllComplete(tasks)
                    .addOnCompleteListener {
                        for (task in tasks) {
                            val snap = task.result
                            Log.d("TAG", "onCreateView: ${snap.documents}")
                            for (doc in snap!!.documents) {
                                val lat = doc.getDouble("latitude")!!
                                val lng = doc.getDouble("longitude")!!

                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                val docLocation = GeoLocation(lat, lng)
                                val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                                if (distanceInM <= radiusInM) {
                                    matchingDocs.add(doc)
                                }
                            }
                        }

                    // matchingDocs contains the results
                    // ...
                    }
                matchingDocs.forEachIndexed { index, documentSnapshot ->
                    Log.d("TAG", "onCreateView: ${documentSnapshot.id}")
                }
            }
        }*/


        binding.btnGet.setOnClickListener {
            val center = GeoLocation(51.5074, 0.1278)
            val radiusInM = 50.0 * 1000.0

            // Each item in 'bounds' represents a startAt/endAt pair. We have to issue
            // a separate query for each pair. There can be up to 9 pairs of bounds
            // depending on overlap, but in most cases there are 4.
            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
            val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
            for (b in bounds) {
                val q = fireStore.collection(TABLE_USER_DATA)
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
                tasks.add(q.get())
            }

            // Collect all the query results together into a single list
            Tasks.whenAllComplete(tasks)
                .addOnCompleteListener {
                    val matchingDocs: MutableList<DocumentSnapshot> = ArrayList()
                    for (task in tasks) {
                        tasks.forEachIndexed { index, task1 ->
                            Log.d("TAG", "onCreateView: ${task1.result.documents}")
                        }
                        Log.d("TAG", "onCreateView: 171")
                        val snap = task.result
                        for (doc in snap!!.documents) {
                            Log.d("TAG", "onCreateView: inside for")
                            val lat = doc.getDouble("latitude")!!
                            val lng = doc.getDouble("longitude")!!

                            // We have to filter out a few false positives due to GeoHash
                            // accuracy, but most will match
                            val docLocation = GeoLocation(lat, lng)
                            val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                            if (distanceInM <= radiusInM) {
                                matchingDocs.add(doc)
                            }
                        }
                    }

                    // matchingDocs contains the results
                    // ...
                }
        }

        return binding {
            viewModel = this@DoctorAddressFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
    }

    fun requestLocationUpdates() {
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
            GpsUtils(requireContext()).turnGPSOn(object : GpsUtils.onGpsListener {
                override fun gpsStatus(isGPSEnable: Boolean) {
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

                latitude = 11.817621
                longitude = 36.764983


                viewModel

                val hash = GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude))

                val updates: MutableMap<String, Any> = mutableMapOf(
                    "geohash" to hash,
                    "latitude" to latitude,
                    "longitude" to longitude,
                )
//                Log.d("TAG", "onLocationResult 156 : $updates")

                // Find cities within 50km of London
                val center = GeoLocation(latitude, longitude)
                val radiusInM = 50.0 * 1000.0

                // Each item in 'bounds' represents a startAt/endAt pair. We have to issue
                // a separate query for each pair. There can be up to 9 pairs of bounds
                // depending on overlap, but in most cases there are 4.
                val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
                val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
                for (b in bounds) {
//                    val q = db.collection("cities")
//                        .orderBy("geohash")
//                        .startAt(b.startHash)
//                        .endAt(b.endHash)
//                    tasks.add(q.get())
//                    Log.d("TAG", "onLocationResult 173: ${b.startHash}  -->     ${b.endHash}")
                }

                // Collect all the query results together into a single list
                Tasks.whenAllComplete(tasks)
                    .addOnCompleteListener {
                        val matchingDocs: MutableList<DocumentSnapshot> = ArrayList()
//                        Log.d("TAG", "onLocationResult: $matchingDocs")
                        for (task in tasks) {
                            val snap = task.result
                            for (doc in snap!!.documents) {
                                val lat = doc.getDouble("lat")!!
                                val lng = doc.getDouble("lng")!!
//                                Log.d("TAG", "onLocationResult 185: $lat  -->     $lng")
                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                val docLocation = GeoLocation(lat, lng)
                                val distanceInM =
                                    GeoFireUtils.getDistanceBetween(docLocation, center)
                                if (distanceInM <= radiusInM) {
                                    matchingDocs.add(doc)
                                }
                            }
                        }
                        // matchingDocs contains the results
                        // ...
                    }
                    .addOnFailureListener {
//                        Log.d("TAG", "onLocationResult: Failure")
                    }


            }
        }
    }

}