package com.android.doctorapp.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentDoctorDetailsBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.ADMIN_FRAGMENT
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.negativeButton
import com.android.doctorapp.util.extension.positiveButton
import javax.inject.Inject

class DoctorDetailsFragment :
    BaseFragment<FragmentDoctorDetailsBinding>(R.layout.fragment_doctor_details) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AdminDashboardViewModel> { viewModelFactory }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.doctor_details)
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
        val arguments: Bundle? = arguments
        if (arguments != null) {
            viewModel.userId.value =
                arguments.getString(ConstantKey.BundleKeys.USER_ID).toString()
            viewModel.itemPosition.value = arguments.getInt(ConstantKey.BundleKeys.ITEM_POSITION)
        }
        viewModel.getDoctorDetail()
        return binding {
            viewModel = this@DoctorDetailsFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver()
    }

    private fun registerObserver() {
        val navController = findNavController()
        navController.previousBackStackEntry?.savedStateHandle?.set(
            ConstantKey.BundleKeys.ADMIN_FRAGMENT,
            false
        )
        viewModel.navigationListener.observe(viewLifecycleOwner) {
            val bundle = Bundle()
            bundle.putBoolean(
                ADMIN_FRAGMENT,
                true
            )
            findNavController().navigate(
                it, bundle
            )
        }
        viewModel.deleteId.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                context?.alert {
                    setTitle(resources.getString(R.string.delete))
                    setMessage(resources.getString(R.string.are_you_sure_want_to_delete))

                    positiveButton { dialog ->
                        viewModel.deleteDoctor(it)
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            ConstantKey.BundleKeys.ADMIN_FRAGMENT,
                            true
                        )
                        navController.popBackStack()
                        dialog.dismiss()
                    }
                    negativeButton(resources.getString(R.string.cancel)) { dialog ->
                        dialog.dismiss()
                    }
                }

            }
        }
    }

}
