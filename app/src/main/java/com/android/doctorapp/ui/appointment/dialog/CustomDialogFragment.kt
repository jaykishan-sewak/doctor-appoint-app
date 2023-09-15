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
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.ui.appointment.AppointmentViewModel
import com.android.doctorapp.util.constants.ConstantKey
import com.google.gson.Gson
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
        val arguments: Bundle? = arguments
        if (arguments != null) {
            val appointmentObj = arguments.getString(ConstantKey.BundleKeys.APPOINTMENT_DATA)
            viewModel.appointmentObj.value =
                Gson().fromJson(appointmentObj, AppointmentModel::class.java)
        }
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@CustomDialogFragment.viewModel
        }
        registerObserver()
        return layoutBinding.root
    }

    private fun registerObserver() {
        viewModel.navigationListener.observe(viewLifecycleOwner) {
            if (it)
                dismiss()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    fun newInstance(data: AppointmentModel?): CustomDialogFragment? {
        val f = CustomDialogFragment()
        val bundle = Bundle()
        bundle.putString(ConstantKey.BundleKeys.APPOINTMENT_DATA, Gson().toJson(data))
        f.arguments = bundle
        return f
    }
}