package com.android.doctorapp.ui.authentication.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentLoginBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.ui.dashboard.DashboardActivity
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.negativeButton
import com.android.doctorapp.util.extension.neutralButton
import com.android.doctorapp.util.extension.startActivityFinish
import com.android.doctorapp.util.extension.toast
import javax.inject.Inject


class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: LoginViewModel by viewModels { viewModelFactory }


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


        return binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@LoginFragment.viewModel
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
//        binding.editText.showKeyboard()
        registerObserver()
        // TODO demo how to add alert
//        context?.alert {
//            setTitle("Add title")
//            setMessage("Add message")
//            neutralButton {  }
//            negativeButton {  }
//            positiveButton("Yes")
//        }
    }

    private fun registerObserver() {
        viewModel.loginResponse.observe(viewLifecycleOwner) {
            it?.let {
                startActivityFinish<DashboardActivity> { }
            }
        }

        viewModel.isUserVerified.observe(viewLifecycleOwner) { it ->
            if (it == false) {
                context?.alert {
                    setTitle("Complete Profile")
                    setMessage("Please complete your profile to continue")
                    neutralButton{
                        viewModel._navigationListener.postValue(R.id.action_loginFragment_to_updateUserFragment)
                    }
                    negativeButton("Cancel") {
                        requireActivity().finish()
                    }
                }
            }
        }
        viewModel.isGoogleClick.observe(viewLifecycleOwner) {
            if (it) {
                val intent: Intent = viewModel.googleSignInClient.signInIntent
                launcher.launch(intent)
            }
        }

        viewModel.navigationListener.observe(viewLifecycleOwner) {
            findNavController().navigate(it)
        }

        viewModel.signInAccountTask.observe(viewLifecycleOwner) {
            if (it.isSuccessful) {
                val msg = getString(R.string.sign_with_google_successful)
                context?.toast(msg)
                viewModel.callGoogleSignInAccountAPI(it)
            }
        }

        viewModel.googleSignInAccount.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.callAuthCredentialsAPI(it.idToken!!)
            }
        }
        viewModel.authCredential.observe(viewLifecycleOwner) {
            viewModel.callGoogleAPI(it)
        }

        viewModel.isUserVerified.observe(viewLifecycleOwner) {
            if (!it) {
                context?.alert {
                    setTitle(context.resources.getString(R.string.complete_profile))
                    setMessage(context.resources.getString(R.string.complete_profile_desc))
                    neutralButton { dialog ->
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_loginFragment_to_updateDoctorFragment)
                    }
                    negativeButton(context.resources.getString(R.string.cancel)) {
//                        exitProcess(0)
                        requireActivity().finishAffinity()

                    }
                }
            }
        }

    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            //Check condition
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                // When request code is equal to RESULT_OK initialize task
                viewModel.callSignInAccountTaskAPI(result)
            }
        }


}