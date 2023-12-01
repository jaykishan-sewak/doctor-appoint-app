package com.android.doctorapp.util.bindingAdapter

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.doctorapp.R
import com.android.doctorapp.repository.models.AddShiftTimeModel
import com.android.doctorapp.repository.models.DateSlotModel
import com.android.doctorapp.repository.models.ImageUriAndGender
import com.android.doctorapp.util.ImageUtils
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.DATE_MM_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_DATE
import com.android.doctorapp.util.constants.ConstantKey.HOUR_MIN_AM_PM_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.MALE_GENDER
import com.android.doctorapp.util.extension.convertDate
import com.android.doctorapp.util.extension.convertTime
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.hideKeyboard
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@BindingAdapter(
    "android:src",
    "path",
    "fallbackUrl",
    "bitmap",
    "circular",
    "rounderRadius",
    requireAll = false
)
fun setImageResource(
    view: View,
    resource: Int?,
    path: String?,
    fallbackUrl: Int?,
    bitmap: Bitmap?,
    circular: Boolean?,
    rounderRadius: Int?
) {
    resource?.let {
        if (view is ImageButton)
            view.setImageResource(resource)
        if (view is ImageView)
            view.setImageResource(resource)
    }
    path?.let {
        if (view is ImageView)
            if (circular == true) {
                rounderRadius?.let { radius ->
                    ImageUtils.setRoundedRectangleImage(view, it, radius, fallbackUrl)
                } ?: run {
                    ImageUtils.setCircleImage(view, it, fallbackUrl)
                }
            } else {
                ImageUtils.setImage(view, it, fallbackUrl)
            }
    }
    bitmap?.let {
        if (view is ImageView)
            if (circular == true) {
                ImageUtils.setCircleProfileImage(view, it)
            } else {
                ImageUtils.setImage(view, it)
            }
    }
}

@BindingAdapter("android:onClick")
fun setDebounceListener(view: View, onClickListener: View.OnClickListener) {
    val scope = view.findViewTreeLifecycleOwner()?.lifecycleScope
    scope?.let {
        val clickWithDebounce: (view: View) -> Unit =
            debounce(scope = scope) {
                onClickListener.onClick(it)
            }

        view.setOnClickListener(clickWithDebounce)
    } ?: run {
        view.setOnClickListener(onClickListener)
    }
}


fun <T> debounce(
    delayMillis: Long = 1000L,
    scope: CoroutineScope,
    action: (T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        if (debounceJob == null) {
            debounceJob = scope.launch {
                action(param)
                delay(delayMillis)
                debounceJob = null
            }
        }
    }
}

@BindingAdapter("onEditorActionClicked")
fun onEditorActionClicked(editText: EditText, onClickListener: View.OnClickListener) {
    editText.setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
            EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_SEND -> {
                editText.hideKeyboard()
                onClickListener.onClick(editText)
                true
            }

            else -> false
        }
    }
}

@BindingAdapter("goneWhen")
fun goneWhen(view: View, visible: Boolean) {
    view.visibility = if (visible) View.GONE else View.VISIBLE
}

@BindingAdapter("invisibleWhen")
fun invisibleWhen(view: View, visible: Boolean) {
    view.visibility = if (visible) View.INVISIBLE else View.VISIBLE
}

@BindingAdapter("strike")
fun setStrikeThrough(textView: TextView, strike: Boolean) {
    if (strike) {
        textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }
}

@BindingAdapter("underline")
fun setTextUnderline(textView: TextView, underline: Boolean) {
    if (underline)
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

@BindingAdapter("loadWebViewHtml")
fun loadWebViewHtmlData(webView: WebView, text: String?) {
    text?.let {
        webView.loadDataWithBaseURL(
            null,
            text,
            "text/html",
            "UTF-8",
            null
        )
    }
}

@BindingAdapter("mainText", "secondaryText", "secondaryColor", requireAll = false)
fun spannableText(view: TextView, mainText: String?, secondaryText: String?, secondaryColor: Int?) {
    val wordToSpan: Spannable =
        SpannableString("$mainText $secondaryText")
    secondaryColor?.let {
        mainText?.length?.let {
            wordToSpan.setSpan(
                ForegroundColorSpan(secondaryColor),
                it,
                wordToSpan.length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
    }
    view.text = wordToSpan
}

@BindingAdapter("app:genderImage")
fun setGenderImage(imageView: AppCompatImageView, gender: String?) {
    if (gender != null) {
        val drawableRes =
            if (gender.isNotEmpty() && gender == MALE_GENDER) R.drawable.ic_male_placeholder else R.drawable.ic_female_placeholder
        imageView.setImageResource(drawableRes)
    }
}

@BindingAdapter("app:appointmentDate")
fun convertDateFormat(textView: TextView, originalDateStr: String) {
    val fullDate = convertDate(originalDateStr)
    val splitDate = fullDate.replace(" ", "\n")
    textView.text = splitDate
}

@BindingAdapter("app:appointmentTime")
fun convertTimeFormat(textView: TextView, originalTimeStr: String) {
    when (originalTimeStr) {
        "Start Time" -> {
            textView.text = "Start Time"
        }

        "End Time" -> {
            textView.text = "End Time"
        }

        else -> {
            textView.text = convertTime(originalTimeStr)
        }
    }
}

@BindingAdapter("app:doctorSpecialization")
fun setSpecialization(textView: AppCompatTextView, specialities: List<String>?) {
    textView.text = if (specialities?.isNotEmpty() == true)
        android.text.TextUtils.join(",", specialities)
    else ""
}

@BindingAdapter("app:timeStyle")
fun setTimeButtonStyle(appCompatButton: AppCompatButton, timeSlotModel: AddShiftTimeModel) {
    if (timeSlotModel.isTimeClick) {
        appCompatButton.setTextAppearance(R.style.date_time_select)
    } else {
        appCompatButton.setTextAppearance(R.style.date_time_unselect)
    }
}

@BindingAdapter("app:dateStyle")
fun setDateButtonStyle(appCompatButton: AppCompatButton, dateSlotModel: DateSlotModel) {
    if (dateSlotModel.dateSelect) {
        appCompatButton.setTextAppearance(R.style.date_time_select)
    } else {
        appCompatButton.setTextAppearance(R.style.date_time_unselect)
    }
}


@BindingAdapter("app:string")
fun setString(textView: AppCompatTextView, specialities: List<String>?) {
    textView.text = if (specialities?.isNotEmpty() == true)
        android.text.TextUtils.join(",", specialities)
    else
        "-"
}

@BindingAdapter("app:dateMonthStyle")
fun setDateMonthStyle(textView: AppCompatTextView, originalTimeStr: Date) {
    textView.text = dateFormatter(originalTimeStr, FORMATTED_DATE)
}

@BindingAdapter("app:headerDate")
fun setHeaderDate(textView: AppCompatTextView, date: Date?) {
    if (date != null) {
        val date = dateFormatter(date, DATE_MM_FORMAT)
        Log.d(TAG, "setHeaderDate: $date")
        textView.text = date.ifEmpty { "" }
    } else
        textView.text = "-"
}

@BindingAdapter("app:headerTime")
fun setHeaderTime(textView: AppCompatTextView, date: Date?) {
    if (date != null) {
        val date = dateFormatter(date, HOUR_MIN_AM_PM_FORMAT)
        Log.d(TAG, "setHeaderDate: $date")
        textView.text = date.ifEmpty { "" }
    } else
        textView.text = ""
}


@BindingAdapter("app:age")
fun setAge(textView: AppCompatTextView, dateOfBirth: Date?) {
    if (dateOfBirth != null) {
        try {
            val dateFormatter = SimpleDateFormat(ConstantKey.FULL_DATE_FORMAT, Locale.getDefault())
            val dob: Date? = dateFormatter.parse(dateOfBirth.toString())
            val calendarDob = Calendar.getInstance()
            if (dob != null) {
                calendarDob.time = dob
            }
            val currentDate = Calendar.getInstance()
            val years = currentDate.get(Calendar.YEAR) - calendarDob.get(Calendar.YEAR)
            if (currentDate.get(Calendar.DAY_OF_YEAR) < calendarDob.get(Calendar.DAY_OF_YEAR)) {
                textView.text = "${years - 1}"
            }
            textView.text = years.toString()
        } catch (e: Exception) {
            textView.text = "-"
            Log.d("test---", e.message.toString())
        }
    } else
        textView.text = "-"

}

@BindingAdapter("app:status")
fun setStatus(textView: AppCompatTextView, text: String) {
    if (text.isNotEmpty()) {
        textView.text = "${text[0].uppercase()}${text.substring(1).lowercase()}"

        if (text.lowercase() == "rejected") {
            textView.setTextColor(Color.parseColor("#e60000"))
        } else if (text.lowercase() == "pending") {
            textView.setTextColor(Color.parseColor("#f1c232"))
        } else {
            textView.setTextColor(Color.parseColor("#247A28"))
        }
    } else {
        // Handle the case when the input text is empty
        textView.text = ""
        textView.setTextColor(Color.BLACK)  // Or set to a default color
    }
}


@BindingAdapter("app:imageUri")
fun loadImageFromUri(imageView: ImageView, imageUri: Uri?) {
    imageUri?.let {
        Glide.with(imageView.context)
            .load(it)
            .into(imageView)
    }
}

@BindingAdapter("app:imageUriAndGender")
fun loadImageFromUriAndGender(
    imageView: AppCompatImageView,
    imageUriAndGender: ImageUriAndGender?
) {
    val gender = imageUriAndGender?.gender
    if (imageUriAndGender?.imageUri != null) {
        Glide.with(imageView.context)
            .load(imageUriAndGender.imageUri)
            .into(imageView)

    } else {
        if (gender != null) {
            val drawableRes =
                if (gender.isNotEmpty() && gender == MALE_GENDER) R.drawable.ic_male_placeholder else R.drawable.ic_female_placeholder
            imageView.setImageResource(drawableRes)
        }
    }

}
