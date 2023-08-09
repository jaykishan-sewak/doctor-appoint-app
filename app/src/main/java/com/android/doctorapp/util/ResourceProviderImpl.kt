package com.android.doctorapp.util

import android.content.Context
import com.android.doctorapp.di.ResourceProvider

class ResourceProviderImpl(private val context: Context) : ResourceProvider {

    override fun getString(resId: Int, vararg params: Any): String {
        return if (params.isEmpty()) {
            context.getString(resId)
        } else {
            context.getString(resId, *params)
        }
    }

}
