package com.android.doctorapp.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentSymptomsBinding
import com.android.doctorapp.databinding.FragmentUpdateDoctorProfileBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.ui.userdashboard.UserDashboardActivity
import com.android.doctorapp.util.extension.selectDate
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Date
import javax.inject.Inject


class SymptomsFragment : BaseFragment<FragmentSymptomsBinding> (R.layout.fragment_symptoms) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<SymptomsViewModel> { viewModelFactory }

    lateinit var bindingView: FragmentSymptomsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        bindingView = binding {
            viewModel = this@SymptomsFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        setUpWithViewModel(viewModel)
        registerObserver(bindingView)
        return bindingView.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun registerObservers(layoutBinding: FragmentSymptomsBinding) {
        viewModel.isCalender.observe(viewLifecycleOwner) {
            if (layoutBinding.textDateOfBirth.id == it?.id) {
                requireContext().selectDate(maxDate = Date().time, minDate = null) { dobDate ->
                    if (calculateAge(dobDate) > 22) {
                        viewModel.dob.value = dobDate
                        viewModel.dobError.value = null
                    } else {
                        viewModel.isDobGreater22()
                    }
                }
            }
        }


    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.symptoms)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .withNavigationIcon(requireActivity().getDrawable(R.drawable.ic_back_white))
            .withNavigationListener{
                findNavController().popBackStack()
            }
            .build()
    }


}