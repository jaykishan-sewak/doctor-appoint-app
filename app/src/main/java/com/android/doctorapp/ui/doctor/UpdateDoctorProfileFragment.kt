package com.android.doctorapp.ui.doctor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUpdateDoctorProfileBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DOCTOR_CONTACT_NUMBER_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DOCTOR_EMAIL_ID_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DOCTOR_NAME_KEY
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UpdateDoctorProfileFragment: BaseFragment<FragmentUpdateDoctorProfileBinding>(R.layout.fragment_update_doctor_profile) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddDoctorViewModel> { viewModelFactory }
    private lateinit var doctorName: String
    private lateinit var doctorEmail: String
    private lateinit var doctorContactNumber: String
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private val TAG = UpdateDoctorProfileFragment::class.java.simpleName
    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }


    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.purple_500))
            .withTitle(R.string.title_profile)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }

     override fun onCreateView(
         inflater: LayoutInflater,
         container: ViewGroup?,
         savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

         val arguments: Bundle? = arguments
         if (arguments != null) {
             doctorName = arguments.getString(DOCTOR_NAME_KEY).toString()
             doctorEmail = arguments.getString(DOCTOR_EMAIL_ID_KEY).toString()
             doctorContactNumber = arguments.getString(DOCTOR_CONTACT_NUMBER_KEY).toString()
         } else {
             doctorName = ""
             doctorEmail = ""
             doctorContactNumber = ""
         }

         viewModel.doctorName.value = doctorName
         viewModel.doctorEmail.value = doctorEmail
         viewModel.doctorContactNumber.value = doctorContactNumber

         callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // This method is called when the verification is completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                startActivity(Intent(applicationContext, MainActivity::class.java))
//                finish()
                Log.d(TAG , "onVerificationCompleted Success")
            }

            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                Log.d(TAG , "onVerificationFailed  $e")
            }

            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG,"onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token
                // Start a new activity using intent
                // also send the storedVerificationId using intent
                // we will use this id to send the otp back to firebase
//                val intent = Intent(applicationContext,OtpActivity::class.java)
//                intent.putExtra("storedVerificationId",storedVerificationId)
//                startActivity(intent)
//                finish()
            }
        }

        return binding {
            viewModel = this@UpdateDoctorProfileFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver()
    }

    private fun registerObserver() {
        viewModel.clickResponse.observe(viewLifecycleOwner) {
            sendVerificationCode("+91$it")
        }
    }

    private fun sendVerificationCode(number: String) {
        Log.d(TAG, "sendVerificationCode: $number")
        val options = PhoneAuthOptions.newBuilder(viewModel.firebaseAuth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d(TAG , "Auth started")
    }

}