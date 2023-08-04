package com.android.infusiontemplate.di

import androidx.annotation.StringRes

interface ResourceProvider {

    fun getString(@StringRes resId: Int, vararg params: Any = emptyArray()): String

}
