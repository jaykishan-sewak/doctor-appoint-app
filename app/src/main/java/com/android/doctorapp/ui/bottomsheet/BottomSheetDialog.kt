package com.android.doctorapp.ui.bottomsheet

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


class BottomSheetDialog(listener: DialogListener) :
    BaseBottomSheetDialogFragment<FragmentBottomSheetDialogBinding>(R.layout.fragment_bottom_sheet_dialog) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddDoctorViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val bindingView = binding {
            viewModel = this@BottomSheetDialog.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        setUpWithViewModel(viewModel)
        registerObserver()
        return bindingView.root
    }

    private fun registerObserver() {
        viewModel.isCameraClick.observe(viewLifecycleOwner) {
            if (it) {
                RuntimePermission.askPermission(
                    this,
                    android.Manifest.permission.CAMERA,
                ).onAccepted {
                    openCamera()
                }.onDenied {
                    requireContext().toast(getString(R.string.denied_the_permission))
                }.onForeverDenied { result ->
                    context?.alert {
                        setTitle(getString(R.string.permission_needed))
                        setMessage(getString(R.string.permission_msg))
                        positiveButton(getString(R.string.go_to_settings)) { result.goToSettings() }
                        negativeButton(getString(R.string.cancel)) {
                            context.toast(getString(R.string.cancel_the_permission))
                        }
                    }
                }.ask()
            }
        }
        viewModel.isGalleryClick.observe(viewLifecycleOwner) {
            if (it) {
                openGallery()
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            cameraLauncher.launch(takePictureIntent)
        }
    }


    private fun openGallery() {
        val pickImageIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(pickImageIntent)
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                val uri: Uri = bitmapToUri(requireContext(), imageBitmap)

//                val imageUri = saveImageToFile(imageBitmap)
                listener.getImageUri(uri)
                dismiss()
            } else {
                Log.d("camera---", "cancel")
            }
        }

    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                Log.d("URI---", result.data?.data.toString())
                viewModel.imageUri.value = result.data?.data
                listener.getImageUri(result.data?.data!!)
                dismiss()
            } else
                Log.d("camera---", "cancel")
        }

    private fun saveImageToFile(imageBitmap: Bitmap): Uri {
        val imagesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val imageFile = File(imagesDir, "captured_image.jpg")

        val imageOutputStream = FileOutputStream(imageFile)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutputStream)
        imageOutputStream.flush()
        imageOutputStream.close()

        return Uri.fromFile(imageFile)
    }

    interface DialogListener {
        fun getImageUri(uri: Uri)
    }
}