package com.android.doctorapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentSymptomsBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.extension.selectDate
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


class SymptomsFragment : BaseFragment<FragmentSymptomsBinding>(R.layout.fragment_symptoms) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<SymptomsViewModel> { viewModelFactory }
    private lateinit var bindingView: FragmentSymptomsBinding
    private val myCalender: Calendar = Calendar.getInstance()


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

        viewModel.getDoctorList()
        setUpWithViewModel(viewModel)
        registerObservers()
        return bindingView.root

    }

    private fun registerObservers() {
        viewModel.isCalender.observe(viewLifecycleOwner) {
            if (binding.etLastVisitDate.id == it?.id) {
                requireContext().selectDate(
                    myCalendar = myCalender,
                    maxDate = Date().time,
                    minDate = null
                ) { dobDate ->
                    viewModel.lastVisitDate.value = dobDate
                    viewModel.isUpdateDataValid.value = true
                }
            }
        }

        viewModel.doctorList.observe(viewLifecycleOwner) { it ->
            val doctorName = arrayListOf<String>()
            it?.forEach {
                doctorName.add(it.name)
            }
            val adapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                doctorName
            )
            binding.autoCompleteTextViewDoctorName.setAdapter(adapter)
            binding.autoCompleteTextViewDoctorName.setOnItemClickListener { _, _, i, _ ->
                viewModel.doctorObj?.postValue(viewModel.doctorList.value!![i])
            }

        }

        viewModel.navigationListener.observe(viewLifecycleOwner) {
            findNavController().navigate(it)
        }
    }


    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.symptoms)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .withNavigationIcon(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_back_white
                )
            )
            .withNavigationListener {
                findNavController().popBackStack()
            }
            .build()
    }


}