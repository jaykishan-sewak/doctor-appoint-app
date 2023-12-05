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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentRequestDoctorBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.ui.doctordashboard.adapter.RequestAppointmentsAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.APPOINTMENT_DETAILS_UPDATED
import com.android.doctorapp.util.constants.ConstantKey.DATE_PICKER
import com.android.doctorapp.util.constants.ConstantKey.DD_MM_FORMAT
import com.android.doctorapp.util.extension.currentDate
import com.android.doctorapp.util.extension.dateFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


class RequestDoctorFragment :
    BaseFragment<FragmentRequestDoctorBinding>(R.layout.fragment_request_doctor) {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: RequestDoctorViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: RequestAppointmentsAdapter
    private val myCalender: Calendar = Calendar.getInstance()
    private var selectedDateRange: Pair<Long, Long>? = null

    //    private lateinit var requestDatePicker: MaterialDatePicker<Pair<Long!, Long!>!>
    private lateinit var requestDatePicker: MaterialDatePicker<androidx.core.util.Pair<Long, Long>>

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
        lifecycleScope.launch {
            viewModel.session.getBoolean(IS_ENABLED_DARK_MODE).collectLatest {
                viewModel.isDarkThemeEnable.value = it == true
            }
        }
        if (viewModel.requestSelectedDate.value == null) {
            viewModel.requestSelectedDate.value = currentDate()
        }

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

        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            APPOINTMENT_DETAILS_UPDATED
        )
            ?.observe(viewLifecycleOwner) {
                if (it) {
                    viewModel.getRequestAppointmentList()
                }
            }
        viewModel.requestSelectedDate.observe(viewLifecycleOwner) {

            if (viewModel.requestAppointmentList.value == null)
                viewModel.getRequestAppointmentList()
            else {
                if (viewModel.isRequestCalender.value!!)
                    viewModel.getRequestAppointmentList()
            }
            viewModel.isRequestCalender.value = false
        }
        viewModel.requestAppointmentList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter.filterList(it)
                viewModel.dataFound.value = true
            } else {
                adapter.filterList(it!!)
                viewModel.dataFound.value = false
            }
        }

        viewModel.isRequestCalender.observe(viewLifecycleOwner) {
            if (it) {
                requestDatePicker = MaterialDatePicker.Builder.dateRangePicker().setSelection(
                    androidx.core.util.Pair(
                        selectedDateRange?.first,
                        selectedDateRange?.second
                    )
                ).build()
                requestDatePicker.show(requireActivity().supportFragmentManager, DATE_PICKER)
                requestDatePicker.addOnPositiveButtonClickListener { dateRange ->
                    selectedDateRange = Pair(dateRange.first, dateRange.second)
                    viewModel.startDate.value = changeTime(dateRange.first, true)
                    viewModel.endDate.value = changeTime(dateRange.second, false)
                    viewModel.rangeDate.value = dateFormatter(
                        viewModel.startDate.value,
                        DD_MM_FORMAT
                    ) + " To " + dateFormatter(viewModel.endDate.value, DD_MM_FORMAT)
                    updateToolbarTitle(viewModel.rangeDate.value!!)
                    requestDatePicker.dismissNow()
                    viewModel.getRequestAppointmentList()
                    viewModel.isRequestCalender.value = false

                }

                // Setting up the event for when cancelled is clicked
                requestDatePicker.addOnNegativeButtonClickListener {
                }

                // Setting up the event for when back button is pressed
                requestDatePicker.addOnCancelListener {
                }


            }
        }
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitleString(
                (if (viewModel.rangeDate.value != null) viewModel.rangeDate.value else
                    dateFormatter(
                        viewModel.requestSelectedDate.value!!,
                        ConstantKey.DATE_MM_FORMAT
                    ))!!
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
                    bundle.putString(ConstantKey.BundleKeys.APPOINTMENT_DATA, Gson().toJson(item))
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

    private fun changeTime(longDate: Long, isStart: Boolean): Date {
        val calendar = Calendar.getInstance()
        calendar.time = Date(longDate)
        if (isStart) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
        }
        return calendar.time
    }

}