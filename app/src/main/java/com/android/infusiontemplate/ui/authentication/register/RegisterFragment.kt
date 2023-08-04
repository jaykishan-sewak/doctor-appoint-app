package com.android.infusiontemplate.ui.authentication.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.infusiontemplate.R
import com.android.infusiontemplate.databinding.FragmentSignupBinding
import com.android.infusiontemplate.di.AppComponentProvider
import com.android.infusiontemplate.di.base.BaseFragment
import com.android.infusiontemplate.di.base.toolbar.FragmentToolbar
import com.android.infusiontemplate.ui.dashboard.DashboardActivity
import com.android.infusiontemplate.util.extension.showKeyboard
import com.android.infusiontemplate.util.extension.startActivityFinish
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
        binding.edtFirstName.showKeyboard()
        registerObserver()
    }

    private fun registerObserver() {
        viewModel.apply {
            registerResponse.observe(viewLifecycleOwner, {
                it?.let {
                    startActivityFinish<DashboardActivity> { }
                }
            })
        }
    }
}