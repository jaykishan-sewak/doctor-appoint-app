package com.android.doctorapp.di.base.toolbar

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

abstract class ToolbarFragment : Fragment() {
    var toolbarManager: ToolbarManager? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarManager = ToolbarManager(builder(), view)
        toolbarManager!!.prepareToolbar()
//        toolbarManager!!.updateTitle(updateToolbarTitle())
    }

    protected abstract fun builder(): FragmentToolbar
}