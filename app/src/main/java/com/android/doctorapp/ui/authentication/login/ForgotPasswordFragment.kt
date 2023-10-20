package com.android.doctorapp.ui.authentication.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentForgotPasswordBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.neutralButton
import javax.inject.Inject

class ForgotPasswordFragment :
    BaseFragment<FragmentForgotPasswordBinding>(R.layout.fragment_forgot_password) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: LoginViewModel by viewModels { viewModelFactory }

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
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ForgotPasswordFragment.viewModel
        }

        return layoutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver()
    }

    private fun registerObserver() {
        viewModel.navigationListener.observe(viewLifecycleOwner) {
            findNavController().navigate(it)
        }

        viewModel.forgotPassEmailSend.observe(viewLifecycleOwner) {
            if (it) {
                context?.alert {
                    setTitle(resources.getString(R.string.reset_pass_label))
                    setMessage(resources.getString(R.string.pass_reset_msg))
                        .neutralButton { dialog ->
                            dialog.dismiss()
                            findNavController().navigate(R.id.action_forgot_pass_to_login)
                        }
                }
            }
        }
    }

    override fun builder() = FragmentToolbar.Builder()
        .withId(R.id.toolbar)
        .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        .withTitle(R.string.forgot_title)
        .withNavigationIcon(
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_back_white
            )
        )
        .withNavigationListener {
            findNavController().popBackStack()
        }
        .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
        .build()


}