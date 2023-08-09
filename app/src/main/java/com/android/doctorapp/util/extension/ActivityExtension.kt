package com.android.doctorapp.util.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.doctorapp.R
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUriUtils

inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    startActivity(Intent(this, T::class.java).apply(block))
}

inline fun <reified T : Activity> Fragment.startActivity(block: Intent.() -> Unit = {}) {
    startActivity(Intent(requireContext(), T::class.java).apply(block))
}

inline fun <reified T : Activity> Activity.startActivityFinish(block: Intent.() -> Unit = {}) {
    startActivity(Intent(this, T::class.java).apply(block))
    finish()
}

inline fun <reified T : Activity> Fragment.startActivityFinish(block: Intent.() -> Unit = {}) {
    startActivity(Intent(requireContext(), T::class.java).apply(block))
    requireActivity().finish()
}

fun Fragment.showImagePicker(activityResultLauncher: ActivityResultLauncher<Intent>) {
    ImagePicker.with(this)
        .crop()
        .compress(1024)
        .createIntent {
            activityResultLauncher.launch(it)
        }
}

fun Activity.showImagePicker(activityResultLauncher: ActivityResultLauncher<Intent>) {
    ImagePicker.with(this)
        .crop()
        .compress(1024)
        .createIntent {
            activityResultLauncher.launch(it)
        }
}

fun Context.fetchImageOrShowError(result: ActivityResult, imageUri: (path: String?) -> Unit) {
    val resultCode = result.resultCode
    val data = result.data
    if (resultCode == Activity.RESULT_OK) {
        val fileUri = data?.data
        fileUri?.let {
            imageUri.invoke(
                FileUriUtils.getRealPath(this, it)
            )
        } ?: run {
            alert {
                setTitle(getString(R.string.oops_text))
                setMessage(getString(R.string.something_went_wrong))
                neutralButton { }
            }
        }
    } else if (resultCode == ImagePicker.RESULT_ERROR) {
        alert {
            setTitle(getString(R.string.error))
            setMessage(ImagePicker.getError(data))
            neutralButton { }
        }
    }
}

fun <T> MutableLiveData<T>.asLiveData() = this as LiveData<T>