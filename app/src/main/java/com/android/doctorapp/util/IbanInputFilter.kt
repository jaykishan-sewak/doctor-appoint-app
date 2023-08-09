package com.android.doctorapp.util

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Matcher
import java.util.regex.Pattern


class IbanInputFilter(var pattern: String) : InputFilter {
    private lateinit var mPattern: Pattern
init {
    mPattern= Pattern.compile(pattern)
}
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {


        val matcher: Matcher = mPattern.matcher(source)
        return if (!matcher.matches()) {
            dest.toString()
        } else null

    }
}