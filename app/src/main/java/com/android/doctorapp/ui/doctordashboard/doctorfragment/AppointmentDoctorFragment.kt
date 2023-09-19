package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentAppointmentDoctorBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.Header
import com.android.doctorapp.ui.doctordashboard.adapter.PatientListAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.google.gson.Gson
import javax.inject.Inject


class AppointmentDoctorFragment :
    BaseFragment<FragmentAppointmentDoctorBinding>(R.layout.fragment_appointment_doctor) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AppointmentDoctorViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: PatientListAdapter


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
            viewModel = this@AppointmentDoctorFragment.viewModel
        }
        setUpWithViewModel(viewModel)
        registerObserver(layoutBinding)

        return layoutBinding.root
    }


    private fun registerObserver(layoutBinding: FragmentAppointmentDoctorBinding) {
        setAdapter(emptyList())
        layoutBinding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        layoutBinding.recyclerView.adapter = adapter

        viewModel.finalAppointmentList.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                adapter.filterList(it)
                viewModel.dataFound.postValue(true)
            }
        }

    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.appointment)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }

    private fun setAdapter(items: List<Header>) {
        adapter = PatientListAdapter(
            items,
            object : PatientListAdapter.OnItemClickListener {
                override fun onItemClick(item: Header, position: Int) {
                    val bundle = Bundle()
                    bundle.putString(ConstantKey.BundleKeys.DATE, Gson().toJson(item.date))
                    bundle.putBoolean(ConstantKey.BundleKeys.REQUEST_FRAGMENT, false)
                    findNavController().navigate(
                        R.id.action_doctor_appointment_to_selected_date,
                        bundle
                    )
                }

                override fun onClick(contact: String) {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$contact")
                    requireActivity().startActivity(intent)
                }
            }
        )
    }


}