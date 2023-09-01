package com.android.doctorapp.ui.doctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentAddDoctorBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.neutralButton
import com.android.doctorapp.util.extension.toast
import com.google.gson.Gson
import javax.inject.Inject

class AddDoctorFragment : BaseFragment<FragmentAddDoctorBinding>(R.layout.fragment_add_doctor) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddDoctorViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder().withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.blue))
            .withTitle(if (viewModel.email.value.isNullOrEmpty()) R.string.add_doctor else R.string.update_doctor)
            .withNavigationIcon(requireActivity().getDrawable(R.drawable.ic_back_white))
            .withNavigationListener {
                findNavController().popBackStack()
            }.withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white)).build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val arguments: Bundle? = arguments
        if (arguments != null) {
            val doctorData = Gson().fromJson(
                arguments.getString(ConstantKey.BundleKeys.DOCTOR_DATA).toString(),
                UserDataResponseModel::class.java
            )
            viewModel.name.value = doctorData.name
            viewModel.email.value = doctorData.email
            viewModel.contactNumber.value = doctorData.contactNumber
            viewModel.userId.value = doctorData.userId
            viewModel.tempEmail.value = doctorData.email
            viewModel.tempContactNumber.value = doctorData.contactNumber

        }
        return binding {
            viewModel = this@AddDoctorFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver()
    }

    private fun registerObserver() {

        viewModel.addDoctorResponse.observe(viewLifecycleOwner) {
            if (it.equals("Success")) {
                context?.toast(resources.getString(R.string.doctor_save_successfully))
                viewModel.navigationListener.observe(viewLifecycleOwner) {
                    findNavController().popBackStack()
                }
            } else {
                context?.alert {
                    setTitle(getString(R.string.doctor_not_save))
                    setMessage(it)
                    neutralButton { }
                }
            }
        }
    }

}