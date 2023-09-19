package com.android.doctorapp.ui.profile

import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUpdateDoctorProfileBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.ui.doctor.AddDoctorViewModel
import com.android.doctorapp.ui.doctor.UpdateDoctorProfileFragment
import com.android.doctorapp.ui.userdashboard.UserDashboardActivity
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.neutralButton
import com.android.doctorapp.util.extension.selectDate
import com.android.doctorapp.util.extension.startActivityFinish
import com.android.doctorapp.util.extension.toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AddUserProfileFragment :
    BaseFragment<FragmentUpdateDoctorProfileBinding>(R.layout.fragment_update_doctor_profile) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddDoctorViewModel> { viewModelFactory }
    val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            viewModel.viewModelScope.launch {
                viewModel.checkIsEmailEveryMin()
            }
            handler.postDelayed(this, 10000)
        }
    }

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private val TAG = UpdateDoctorProfileFragment::class.java.simpleName
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken


    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.title_profile)
            .withNavigationIcon(requireActivity().getDrawable(R.drawable.ic_back_white))
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
        handler.postDelayed(runnable, 10000)
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModel.hideProgress()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                viewModel.hideProgress()
                context?.alert {
                    setMessage(e.message)
                    neutralButton { }
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                viewModel.hideProgress()
                storedVerificationId = verificationId
                resendToken = token
                val bundle = Bundle()
                bundle.putString(
                    ConstantKey.BundleKeys.STORED_VERIFICATION_Id_KEY,
                    storedVerificationId
                )
                bundle.putBoolean(ConstantKey.BundleKeys.IS_DOCTOR_OR_USER, false)
                bundle.putString(
                    ConstantKey.BundleKeys.USER_CONTACT_NUMBER_KEY,
                    viewModel.contactNumber.value
                )
                viewModel.isEmailSent.value = false
                findNavController().navigate(
                    R.id.action_updateUserFragment_to_OtpVerificationFragment,
                    bundle
                )

            }
        }


        // Inflate the layout for this fragment
        return binding {
            viewModel = this@AddUserProfileFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        checkLiveData()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the callback to stop automatic calling
        handler.removeCallbacks(runnable)
    }

    private fun checkLiveData() {

        viewModel.addDoctorResponse.observe(viewLifecycleOwner) {
            if (it.equals(ConstantKey.SUCCESS)) {
                context?.toast(resources.getString(R.string.user_save_successfully))
                startActivityFinish<UserDashboardActivity>()

            } else {
                context?.alert {
                    setTitle(getString(R.string.user_not_save))
                    setMessage(it)
                    neutralButton { dialog ->
                        dialog.dismiss()

                    }
                }
            }
        }

        viewModel.isCalender.observe(viewLifecycleOwner) {

            if (binding.textDateOfBirth.id == it?.id) {
                requireContext().selectDate(maxDate = Date().time, minDate = null) { dobDate ->
                    viewModel.dob.value = dobDate
                }
            } else {
                requireContext().selectDate(maxDate = null, minDate = Date().time)
                { availableDate ->
                    viewModel.isAvailableDate.value = availableDate
                }
            }
        }

        viewModel.isEmailSent.observe(viewLifecycleOwner) {
            if (it == true) {
                context?.toast(resources.getString(R.string.verification_email_sent))
            }
        }

        viewModel.isUserReload.observe(viewLifecycleOwner) {
            if (it == true) {
                viewModel.emailVerified()
            }
        }

        viewModel.isEmailVerified.observe(viewLifecycleOwner) {
            if (it == true) {
                viewModel.validateAllUpdateField()
                viewModel.emailVerifyLabel.postValue(resources.getString(R.string.verified))
                binding.textEmailVerify.isClickable = false
                binding.textEmailVerify.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green
                    )
                )
                handler.removeCallbacks(runnable)
            }
        }
        viewModel.getModelUserData().observe(viewLifecycleOwner) {
            viewModel.email.value = it[0].email
            Log.d(ContentValues.TAG, "registerObserver: ${it[0]}")
        }

        viewModel.userResponse.observe(viewLifecycleOwner) {
            viewModel.name.value = it.name
            viewModel.email.value = it.email
            viewModel.contactNumber.value = it.contactNumber
            viewModel.address.value = it.address
            viewModel.selectGenderValue.value = it.gender
            viewModel.dob.value = dateFormatter(it.dob, ConstantKey.DATE_MM_FORMAT)
        }

        viewModel.clickResponse.observe(viewLifecycleOwner) {
            sendVerificationCode("+91$it")
        }

        viewModel.isPhoneVerify.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.validateAllUpdateField()
                binding.textUserContactVerify.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green
                    )
                )
                binding.etContact.isEnabled = false
            }
        }

    }

    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(viewModel.firebaseAuth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


}