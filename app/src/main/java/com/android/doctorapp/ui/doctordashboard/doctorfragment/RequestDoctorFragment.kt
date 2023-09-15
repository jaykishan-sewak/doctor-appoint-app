package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentRequestDoctorBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.ui.doctordashboard.adapter.RequestAppointmentsAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.selectDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


class RequestDoctorFragment :
    BaseFragment<FragmentRequestDoctorBinding>(R.layout.fragment_request_doctor) {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: RequestDoctorViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: RequestAppointmentsAdapter

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

        val currentDate = Calendar.getInstance()
        currentDate.set(Calendar.HOUR_OF_DAY, 0)
        currentDate.set(Calendar.MINUTE, 0)
        currentDate.set(Calendar.SECOND, 0)
        currentDate.set(Calendar.MILLISECOND, 0)
        viewModel.requestSelectedDate.value = currentDate.time

        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@RequestDoctorFragment.viewModel
        }
        setUpWithViewModel(viewModel)
        registerObserver(layoutBinding)
        return layoutBinding.root
    }

    private fun registerObserver(layoutBinding: FragmentRequestDoctorBinding) {
        setAdapter(emptyList())
        layoutBinding.requestDoctorRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        layoutBinding.requestDoctorRecyclerView.adapter = adapter
        viewModel.requestSelectedDate.observe(viewLifecycleOwner) {
            viewModel.getRequestAppointmentList()
            viewModel.isRequestCalender.value = false
        }
        viewModel.requestAppointmentList.observe(viewLifecycleOwner) {
            adapter.filterList(it)
        }

        viewModel.isRequestCalender.observe(viewLifecycleOwner) {
            if (it) {
                requireContext().selectDate(maxDate = null, minDate = Date().time) { dobDate ->
                    val formatter = SimpleDateFormat("dd-MM-yyyy")
                    val date = formatter.parse(dobDate)
                    viewModel.requestSelectedDate.value = date
                    updateToolbarTitle(dateFormatter(date!!, ConstantKey.DATE_MM_FORMAT))
                }
            }
        }
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitleString(
                dateFormatter(
                    viewModel.requestSelectedDate.value!!,
                    ConstantKey.DATE_MM_FORMAT
                )
            )
            .withMenu(R.menu.doctor_calendar_menu)
            .withMenuItems(generateMenuItems(), generateMenuClicks())
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }

    private fun generateMenuClicks(): MenuItem.OnMenuItemClickListener {
        return MenuItem.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_calendar -> {
                    viewModel.isRequestCalender.value = true
                }
            }
            false
        }
    }

    private fun generateMenuItems(): List<Int> {
        return listOf(R.id.action_calendar)
    }

    private fun setAdapter(items: List<AppointmentModel>) {
        adapter = RequestAppointmentsAdapter(
            items,
            object : RequestAppointmentsAdapter.OnItemClickListener {
                override fun onItemClick(item: AppointmentModel, position: Int) {
                    val bundle = Bundle()
                    bundle.putBoolean(ConstantKey.BundleKeys.REQUEST_FRAGMENT, true)
                    bundle.putString(ConstantKey.BundleKeys.USER_ID, item.userId)
                    findNavController().navigate(
                        R.id.request_to_appointment_details,
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