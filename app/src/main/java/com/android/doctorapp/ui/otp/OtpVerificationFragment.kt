package com.android.doctorapp.ui.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentOtpVerificationBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.IS_DOCTOR_OR_USER_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.STORED_VERIFICATION_Id_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.USER_CONTACT_NUMBER_KEY
import javax.inject.Inject

class OtpVerificationFragment :
    BaseFragment<FragmentOtpVerificationBinding>(R.layout.fragment_otp_verification) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: OtpVerificationViewModel by viewModels { viewModelFactory }
    private lateinit var verificationId: String
    lateinit var contactNumber: String

    override fun builder() = FragmentToolbar.Builder()
        .withId(FragmentToolbar.NO_TOOLBAR)
        .build()


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
            verificationId = arguments.getString(STORED_VERIFICATION_Id_KEY).toString()
            viewModel.isDoctorOrUser.value = arguments.getBoolean(IS_DOCTOR_OR_USER_KEY)
            contactNumber = arguments.getString(USER_CONTACT_NUMBER_KEY).toString()
        } else {
            verificationId = ""
            viewModel.isDoctorOrUser.value = false
        }

        viewModel.userContactNumber.value =
            resources.getString(R.string.otp_desc, contactNumber.takeLast(3))

        viewModel.otpVerificationId.value = verificationId

        return binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@OtpVerificationFragment.viewModel
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)

        binding.otpPinView.setPinViewEventListener { pinview, fromUser ->
            viewModel._otpDigit.value = pinview.value
        }
        registerObserver()
    }

    private fun registerObserver() {
        viewModel.otpDigit1.observe(viewLifecycleOwner) {
            if (viewModel.isOtpFilled()) {
                // Perform OTP verification
                viewModel.isAllValidate()
            } else {
            }
        }
        viewModel.navigationListener.observe(viewLifecycleOwner) {
            findNavController().popBackStack()

        }
    }

}