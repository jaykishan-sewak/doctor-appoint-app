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
import com.android.doctorapp.ui.admin.AdminDashboardActivity
import com.android.doctorapp.ui.doctordashboard.DoctorDashboardActivity
import com.android.doctorapp.ui.userdashboard.UserDashboardActivity
import com.android.doctorapp.util.constants.ConstantKey.USER
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

        viewModel.doctorChecked.observe(viewLifecycleOwner) {
            if (it) {
                startActivityFinish<DoctorDashboardActivity>()
            }
        }

        viewModel.userChecked.observe(viewLifecycleOwner) {
            if (it) {
                startActivityFinish<UserDashboardActivity>()
            }
        }

        viewModel.adminChecked.observe(viewLifecycleOwner) {
            if (it) {
                startActivityFinish<AdminDashboardActivity>()
            }
        }

        viewModel.isUserVerified.observe(viewLifecycleOwner) { it ->
            context?.alert {
                setTitle(resources.getString(R.string.complete_profile))
                setMessage(resources.getString(R.string.complete_profile_desc))
                neutralButton { dialog ->
                    dialog.dismiss()
                    if (it == USER) {
                        findNavController().navigate(R.id.action_loginFragment_to_updateUserFragment)
                    } else {
                        findNavController().navigate(R.id.action_loginFragment_to_updateDoctorFragment)
                    }

                }
                negativeButton(context.resources.getString(R.string.cancel)) {
                    requireActivity().finish()
                }
            }

        }
        viewModel.isGoogleClick.observe(viewLifecycleOwner) {
            if (it) {
                val intent: Intent = viewModel.googleSignInClient.signInIntent
                launcher.launch(intent)
            }
        }


        viewModel.signInAccountTask.observe(viewLifecycleOwner) {
            if (it?.isSuccessful == true) {
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
            viewModel.callGoogleAPI(it!!)
        }

        viewModel.navigationListener.observe(viewLifecycleOwner) {
            findNavController().navigate(it)
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