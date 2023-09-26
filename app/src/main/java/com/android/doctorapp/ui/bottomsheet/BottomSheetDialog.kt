package com.android.doctorapp.ui.bottomsheet

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentBottomSheetDialogBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseBottomSheetDialogFragment
import com.android.doctorapp.ui.doctor.AddDoctorViewModel
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.negativeButton
import com.android.doctorapp.util.extension.positiveButton
import com.android.doctorapp.util.extension.toast
import com.android.doctorapp.util.permission.RuntimePermission
import javax.inject.Inject


class BottomSheetDialog :
    BaseBottomSheetDialogFragment<FragmentBottomSheetDialogBinding>(R.layout.fragment_bottom_sheet_dialog) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddDoctorViewModel> { viewModelFactory }
    private lateinit var bindingView: FragmentBottomSheetDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        bindingView = binding {
            viewModel = this@BottomSheetDialog.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        setUpWithViewModel(viewModel)
        registerObserver(bindingView)
        return bindingView.root
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_IMAGE_PICK = 102
        private const val CAMERA_PERMISSION_CODE = 103
        private const val GALLARY_PERMISSION_CODE = 104
    }

    private fun registerObserver(bindingView: FragmentBottomSheetDialogBinding) {
        binding.tvCamera.setOnClickListener {
            RuntimePermission.askPermission(
                this,
                android.Manifest.permission.CAMERA,
            ).onAccepted {
                openCamera()
            }.onDenied {
//                activity?.let { it1 ->
//                    Navigation.findNavController(it1.findViewById(R.id.navHostFragment))
//                        .navigate(R.id.action_loginFragment_to_updateDoctorFragment)
//                }

                requireContext().toast("Denied the permissiom")
                context?.toast("Please accept permission to open camera...")
            }.onForeverDenied { result ->
                context?.alert {
                    setTitle("Permission Needed")
                    setMessage("Permission is needed for using the camera ...")
                    positiveButton("Go to settings") { result.goToSettings() }
                    negativeButton("Cancel") {
//                        activity?.let { it1 ->
//                            Navigation.findNavController(it1.findViewById(R.id.navHostFragment))
//                                .navigate(R.id.action_loginFragment_to_updateDoctorFragment)
//                        }
                        context.toast("Cancel the permission")
                    }
                }
            }.ask()

        }

        binding.tvGallery.setOnClickListener {
//            openGallery()
            RuntimePermission.askPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ).onAccepted {
                openGallery()
            }.onDenied {
//                activity?.let { it1 ->
//                    Navigation.findNavController(it1.findViewById(R.id.navHostFragment))
//                        .navigate(R.id.action_loginFragment_to_updateDoctorFragment)
//                }

                requireContext().toast("Denied the permissiom")
                context?.toast("Please accept permission to open gallery...")
            }.onForeverDenied { result ->
                context?.alert {
                    setTitle("Permission Needed")
                    setMessage("Permission is needed to take photo from gallery...")
                    positiveButton("Go to settings") { result.goToSettings() }
                    negativeButton("Cancel") {
//                        activity?.let { it1 ->
//                            Navigation.findNavController(it1.findViewById(R.id.navHostFragment))
//                                .navigate(R.id.action_loginFragment_to_updateDoctorFragment)
//                        }
                        context.toast("Cancel the permission")
                    }
                }
            }.ask()
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            launcher.launch(takePictureIntent)
        }
    }

    private fun openGallery() {
        val pickImageIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launcher1.launch(pickImageIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == AppCompatActivity.RESULT_OK) {
//                val data: Bundle? = result.data?.extras
//                Log.d(TAG, "data :${result.data!!.action}: ")
//                when (result.data?.action) {
//                    "inline-data"  -> {
//                        // This is the result of the camera capture
//                        val imageBitmap: Bitmap? = data?.get("data") as Bitmap?
//                        Log.d(TAG, "Camera capture result: $imageBitmap")
//                    }
//                    Intent.ACTION_PICK -> {
//                        // This is the result of picking an image from the gallery
//                        val selectedImageUri = result.data?.data
//                        Log.d(TAG, "Gallery pick result: $selectedImageUri")
//                    }
//                } ActivityResult{resultCode=RESULT_OK, data=Intent { act=inline-data (has extras) }}:
//2023-09-26 17:31:59.388  5312-5312  ContentValues           com.android.doctorapp                D  Intent { act=inline-data (has extras) }:
//            }

            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
//                Log.d(TAG, "${data?.action}: ")
                if (data != null && data.hasExtra("data")) {
                    // This is the result of the camera capture
                    val imageBitmap: Bitmap? = data.getParcelableExtra("data")
                    Log.d(TAG, "Camera capture result: $imageBitmap")
                } else {
                    // Handle the case where the camera result does not contain data
                    Log.d(TAG, "Camera capture result: No data received")
                }
            } else {
                // Handle the case where the user canceled the camera action
                Log.d(TAG, "Camera capture result: Canceled")
            }
        }

    private val launcher1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result1 ->
            if (result1.resultCode == AppCompatActivity.RESULT_OK) {
                if (result1.data != null) {
                    Log.d(TAG, "${result1.data!!.data}: ")
                    val selectedImageUri = result1.data?.data
                } else {
                    // Handle the case where the camera result does not contain data
                    Log.d(TAG, "Gallery result: No data received")
                }
            }

        }


}