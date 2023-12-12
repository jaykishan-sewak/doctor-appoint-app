package com.android.doctorapp.ui.userdashboard.userfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentBookingDetailBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.ui.appointment.dialog.CustomDialogFragment
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.APPOINTMENT_DETAILS_UPDATED
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.APPOINTMENT_DOCUMENT_ID
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class BookingDetailFragment :
    BaseFragment<FragmentBookingDetailBinding>(R.layout.fragment_booking_detail) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val viewModel: BookingDetailViewModel by viewModels { viewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        lifecycleScope.launch {
            viewModel.session.getBoolean(IS_ENABLED_DARK_MODE).collectLatest {
                viewModel.isDarkThemeEnable.value = it
            }
        }

        if (arguments != null) {
            val appointmentObj =
                requireArguments().getString(ConstantKey.BundleKeys.BOOKING_APPOINTMENT_DATA)
            if (appointmentObj != null) {
                viewModel.appointmentObj.value =
                    Gson().fromJson(appointmentObj, AppointmentModel::class.java)
                viewModel.selectedTab.value =
                    requireArguments().getString(ConstantKey.BundleKeys.SELECTED_TAB)
                viewModel.isCancelEnabled.value = viewModel.checkAppointmentDate()
            } else {
                viewModel.documentId.value = requireArguments().getString(APPOINTMENT_DOCUMENT_ID)
                viewModel.setShowProgress(false)
                viewModel.getAppointmentDetails()
            }

        }
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@BookingDetailFragment.viewModel
        }
        setUpWithViewModel(viewModel)
        registerObserver()
        return layoutBinding.root
    }

    private fun registerObserver() {

        viewModel.cancelClick.observe(viewLifecycleOwner) {
            if (it) {
                CustomDialogFragment(viewModel.isDarkThemeEnable.value, requireContext(),
                    object : CustomDialogFragment.OnButtonClickListener {
                        override fun oClick(text: String) {
                            viewModel.appointmentRejectApiCall(text)
                        }
                    }).show()
            }
        }

        viewModel.navigationListener.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    APPOINTMENT_DETAILS_UPDATED,
                    true
                )
                findNavController().popBackStack()
            }
        }
        viewModel.appointmentObj.observe(viewLifecycleOwner) {
            if (it?.doctorDetails?.images != null)
                viewModel.imageUri.value = it.doctorDetails?.images?.toUri()
        }
    }

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
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "tabValue",
                    viewModel.selectedTab.value
                )

                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    APPOINTMENT_DETAILS_UPDATED,
                    false
                )
                findNavController().popBackStack()
            }
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }


}