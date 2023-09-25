package com.android.doctorapp.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentBottomSheetDialogBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseBottomSheetDialogFragment
import com.android.doctorapp.ui.doctor.AddDoctorViewModel
import javax.inject.Inject

class BottomSheetDialog :
    BaseBottomSheetDialogFragment<FragmentBottomSheetDialogBinding>(R.layout.fragment_bottom_sheet_dialog) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddDoctorViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding {
            viewModel = this@BottomSheetDialog.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
    }

}