package com.android.doctorapp.ui.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.Calendar
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
            handler.postDelayed(this, 30000)
        }
    }

    private val myCalendar = Calendar.getInstance()
    private var date =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val date = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
            viewModel.dob.value = date
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            //updateLabel()
        }

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private val TAG = UpdateDoctorProfileFragment::class.java.simpleName
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken


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
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            }

            override fun onVerificationFailed(e: FirebaseException) {
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
                findNavController().navigate(
                    R.id.action_updateUserFragment_to_OtpVerificationFragment,
                    bundle
                )

            }
        }
        handler.postDelayed(runnable, 1000)

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

        viewModel.isCalendarShow.observe(viewLifecycleOwner) {
            if (it == true) {
                context?.let { it1 ->
                    DatePickerDialog(
                        it1, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                        myCalendar[Calendar.DAY_OF_MONTH]
                    ).show()
                }
            }
        }

        viewModel.isEmailSent.observe(viewLifecycleOwner) {
            if (it == true) {
                context?.toast("Verification Email sent successfully")
            }
        }

        viewModel.isUserReload.observe(viewLifecycleOwner) {
            if (it == true) {
                viewModel.emailVerified()
            }
        }

        viewModel.isEmailVerified.observe(viewLifecycleOwner) {
            if (it == true) {
                viewModel.emailVerifyLabel.postValue("Verified")
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
            //viewModel.name.value = it[0].name
            viewModel.email.value = it[0].email
            //viewModel.contactNum.value = it[0].contactNumber
        }

        viewModel.clickResponse.observe(viewLifecycleOwner) {
            sendVerificationCode("+91$it")
        }

        viewModel.isPhoneVerify.observe(viewLifecycleOwner) {
            if (it == true) {
                viewModel.isPhoneVerifyValue.value = "Verified"
                binding.textContactVerify.isClickable = false
                binding.textContactVerify.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green
                    )
                )

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