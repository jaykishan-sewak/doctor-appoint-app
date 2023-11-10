package com.android.doctorapp.util.viewPager

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


class ImageZoomViewPager : ViewPager {
    private var mDisablePaging = false

    constructor(context: Context?) : super(context!!) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
    }

    override fun onInterceptTouchEvent(arg0: MotionEvent?): Boolean {
        return if (mDisablePaging) {
            false
        } else super.onInterceptTouchEvent(arg0)
    }

    fun setIsDragValue(b: Boolean) {
        mDisablePaging = b
    }

}