package com.android.doctorapp.ui.appointment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.doctorapp.R
import com.android.doctorapp.databinding.CustomDialogBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseDialogFragment
import com.android.doctorapp.ui.appointment.AppointmentViewModel
import javax.inject.Inject

class CustomDialogFragment : BaseDialogFragment<CustomDialogBinding>(R.layout.custom_dialog) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AppointmentViewModel by viewModels { viewModelFactory }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@CustomDialogFragment.viewModel
        }
        return layoutBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }
}