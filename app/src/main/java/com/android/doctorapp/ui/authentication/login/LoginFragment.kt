package com.android.doctorapp.ui.authentication.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.android.doctorapp.util.extension.startActivityFinish
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject

class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: LoginViewModel by viewModels { viewModelFactory }
    private lateinit var googleSignInClient: GoogleSignInClient

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

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("602980832199-da60b9fdshqg3gdnsqdkv2ltbu1qeg2p.apps.googleusercontent.com")
            .requestEmail()
            .build()

        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

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
        viewModel.loginResponse.observe(viewLifecycleOwner, {
            it?.let {
                startActivityFinish<DashboardActivity> { }
            }
        })

        viewModel.navigationListener.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.isGoogleClick.observe(viewLifecycleOwner) {
            if (it) {
                val intent: Intent = googleSignInClient.signInIntent
                launcher.launch(intent)
            }
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            //Check condition
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                // When request code is equal to 100 initialize task
                val signInAccountTask: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)

                if (signInAccountTask.isSuccessful) {
                    // When google sign in successful initialize string
                    val msg = "Google sign in successful"
                    displayToast(msg)

                    // Initialize sign in account
                    try {
                        val googleSignInAccount =
                            signInAccountTask.getResult(ApiException::class.java)

                        //Check condition
                        if (googleSignInAccount != null) {
                            // When sign in account is not equal to null initialize auth credential
                            val authCredential: AuthCredential = GoogleAuthProvider.getCredential(
                                googleSignInAccount.idToken, null
                            )
                            // Check credentials
                            viewModel.auth?.signInWithCredential(authCredential)
                                ?.addOnCompleteListener { task ->

                                    //Check condition
                                    if (task.isSuccessful) {
//                                    startActivity(Intent(this@MainActivity, ProfileActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                        displayToast("Firebase authentication successful")
                                    } else
                                        displayToast("Authentication failed" + task.exception!!.message)
                                }
                        }
                    } catch (e: ApiException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    private fun displayToast(msg: String) {
        Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
    }

}