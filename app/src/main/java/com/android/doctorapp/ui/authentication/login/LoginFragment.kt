package com.android.doctorapp.ui.authentication.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentLoginBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.ui.dashboard.DashboardActivity
import com.android.doctorapp.util.extension.showKeyboard
import com.android.doctorapp.util.extension.startActivityFinish
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
        binding.editText.showKeyboard()
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
    }
}