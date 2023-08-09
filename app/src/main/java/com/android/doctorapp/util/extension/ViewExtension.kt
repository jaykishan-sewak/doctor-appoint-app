package com.android.doctorapp.util.extension

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.showKeyboard() {
    this.postDelayed({
        this.requestFocus()
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }, 100)
}

fun View.hideKeyboard() {
    this.isFocusableInTouchMode = true
    this.requestFocus()
    this.post {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun View.isKeyboardShown(): Boolean {
    val softKeyboardHeight = 100
    val r = Rect()
    this.getWindowVisibleDisplayFrame(r)
    val dm: DisplayMetrics = this.resources.displayMetrics
    val heightDiff: Int = this.bottom - r.bottom
    return heightDiff > softKeyboardHeight * dm.density
}