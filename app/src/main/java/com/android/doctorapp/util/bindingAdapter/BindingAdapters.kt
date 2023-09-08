package com.android.doctorapp.util.bindingAdapter

import android.graphics.Bitmap
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.doctorapp.R
import com.android.doctorapp.util.ImageUtils
import com.android.doctorapp.util.extension.hideKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
fun setGenderImage(imageView: AppCompatImageView, gender: String) {
    val drawableRes =
        if (gender == "MALE") R.drawable.ic_male_placeholder else R.drawable.ic_female_placeholder
    imageView.setImageResource(drawableRes)
}

@BindingAdapter("app:doctorSpecialization")
fun setSpecialization(textView: AppCompatTextView, specialities: List<String>?) {
    textView.text = if (specialities?.isNotEmpty() == true)
        android.text.TextUtils.join(",", specialities)
    else ""
}