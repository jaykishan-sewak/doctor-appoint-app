package com.android.doctorapp.ui.appointment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentAppointmentDetailBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.ui.appointment.dialog.CustomDialogFragment
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.FIELD_APPROVED
import com.android.doctorapp.util.constants.ConstantKey.FIELD_REJECTED
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.negativeButton
import com.android.doctorapp.util.extension.neutralButton
import com.google.gson.Gson
import javax.inject.Inject

class AppointmentDetailFragment :
    BaseFragment<FragmentAppointmentDetailBinding>(R.layout.fragment_appointment_detail) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val viewModel: AppointmentViewModel by viewModels { viewModelFactory }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.appointment_detail)
            .withNavigationIcon(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_back_white
                )
            )
            .withNavigationListener {
                findNavController().popBackStack()
            }
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val arguments: Bundle? = arguments
        if (arguments != null) {
            viewModel.isShowBothButton.value =
                arguments.getBoolean(ConstantKey.BundleKeys.REQUEST_FRAGMENT)
            val appointmentObj = arguments.getString(ConstantKey.BundleKeys.APPOINTMENT_DATA)
            viewModel.appointmentObj.value =
                Gson().fromJson(appointmentObj, AppointmentModel::class.java)
        }
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@AppointmentDetailFragment.viewModel
        }
        setUpWithViewModel(viewModel)
        registerObserver()
        return layoutBinding.root
    }

    private fun registerObserver() {
        viewModel.getAppointmentDetails()
        viewModel.confirmClick.observe(viewLifecycleOwner) { it ->
            if (it) {
                context?.alert {
                    setTitle(resources.getString(R.string.confirm))
                    setMessage(resources.getString(R.string.approve_appointment_desc))
                    neutralButton(text = getString(R.string.approve)) { dialog ->
                        viewModel.updateAppointmentStatus(FIELD_APPROVED)
                        dialog.dismiss()
                    }
                    negativeButton(context.resources.getString(R.string.cancel)) { dialog ->
                        dialog.dismiss()
                    }
                }
            }
        }
        viewModel.rejectClick.observe(viewLifecycleOwner) { it ->
            if (it) {
                context?.alert {
                    setTitle(resources.getString(R.string.confirm))
                    setMessage(resources.getString(R.string.reject_appointment_desc))
                    neutralButton(text = getString(R.string.reject)) { dialog ->
                        viewModel.updateAppointmentStatus(FIELD_REJECTED)
                        dialog.dismiss()
                    }
                    negativeButton(context.resources.getString(R.string.cancel)) { dialog ->
                        dialog.dismiss()

                    }
                }
            }
        }
        viewModel.cancelClick.observe(viewLifecycleOwner) {
            if (it) {
                CustomDialogFragment(requireContext(),
                    object : CustomDialogFragment.OnButtonClickListener {
                        override fun oClick(text: String) {
                            viewModel.appointmentRejectApiCall(text)
                        }
                    }).show()
            }
        }
        viewModel.navigationListener.observe(viewLifecycleOwner) {
            if (it)
                findNavController().popBackStack()
        }

        viewModel.updateClick.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.appointmentUpdateApiCall()
            }
        }

    }
}