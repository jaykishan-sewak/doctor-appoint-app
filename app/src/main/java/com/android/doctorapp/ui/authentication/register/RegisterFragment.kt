package com.android.doctorapp.ui.authentication.register

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
import com.android.doctorapp.databinding.FragmentSignupBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.neutralButton
import com.android.doctorapp.util.extension.toast
import javax.inject.Inject

class RegisterFragment : BaseFragment<FragmentSignupBinding>(R.layout.fragment_signup) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<RegisterViewModel> { viewModelFactory }

    override fun builder() = FragmentToolbar.Builder()
        .withId(FragmentToolbar.NO_TOOLBAR)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding {
            viewModel = this@RegisterFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver()
    }

    private fun registerObserver() {
        viewModel.apply {


            viewModel.isGoogleClick.observe(viewLifecycleOwner) {
                if (it) {
                    val intent: Intent = viewModel.googleSignInClient.signInIntent
                    launcher.launch(intent)
                }
            }

            viewModel.signInAccountTask.observe(viewLifecycleOwner) {
                if (it!!.isSuccessful) {
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

            viewModel.addUserResponse.observe(viewLifecycleOwner) {
                if (it.equals(resources.getString(R.string.success))) {
                    context?.toast(resources.getString(R.string.user_save_successfully))
                    viewModel.navigationListener.observe(viewLifecycleOwner) { it1 ->
                        findNavController().navigate(it1)
                    }
                } else {
                    context?.alert {
                        setTitle(resources.getString(R.string.user_not_save))
                        setMessage(it)
                        neutralButton { }
                    }
                }
            }

            viewModel.navigationListener.observe(viewLifecycleOwner) {
                findNavController().navigate(it)
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