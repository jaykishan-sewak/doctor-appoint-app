package com.android.doctorapp.ui.userdashboard.userfragment

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
import com.android.doctorapp.databinding.FragmentUserRequestBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.ui.userdashboard.userfragment.adapter.BookingAppointmentsAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.currentDate
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.openPhoneDialer
import com.android.doctorapp.util.extension.selectDate
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


class UserRequestFragment :
    BaseFragment<FragmentUserRequestBinding>(R.layout.fragment_user_request) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: UserRequestViewModel by viewModels { viewModelFactory }
    lateinit var adapter: BookingAppointmentsAdapter

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
        viewModel.requestSelectedDate.value = currentDate()
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@UserRequestFragment.viewModel
        }
        setUpWithViewModel(viewModel)
        registerObserver(layoutBinding)
        return layoutBinding.root
    }

    private fun registerObserver(layoutBinding: FragmentUserRequestBinding) {
        setAdapter(emptyList())
        binding.requestUserRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.requestUserRecyclerView.adapter = adapter

        viewModel.requestSelectedDate.observe(viewLifecycleOwner) {
            viewModel.getRequestAppointmentList()
            viewModel.isDoctorRequestCalendar.value = false
        }
        viewModel.userAppointmentData.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                adapter.filterList(it)
                viewModel.dataFound.value = true
            } else {
                adapter.filterList(emptyList())
                viewModel.dataFound.value = false
            }
        }

        viewModel.isDoctorRequestCalendar.observe(viewLifecycleOwner) {
            if (it) {
                requireContext().selectDate(maxDate = null, minDate = null) { dobDate ->
                    val formatter =
                        SimpleDateFormat(ConstantKey.FORMATTED_DATE, Locale.getDefault())
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
                    viewModel.isDoctorRequestCalendar.value = true
                }
            }
            false
        }
    }

    private fun generateMenuItems(): List<Int> {
        return listOf(R.id.action_calendar)
    }

    private fun setAdapter(items: List<AppointmentModel>) {
        adapter = BookingAppointmentsAdapter(
            items,
            object : BookingAppointmentsAdapter.OnItemClickListener {
                override fun onItemClick(item: AppointmentModel, position: Int) {
                    val bundle = Bundle()
                    bundle.putBoolean(ConstantKey.BundleKeys.BOOKING_FRAGMENT, true)
                    bundle.putString(
                        ConstantKey.BundleKeys.BOOKING_APPOINTMENT_DATA,
                        Gson().toJson(item)
                    )
                    findNavController().navigate(
                        R.id.action_user_booking_to_bookingDetail,
                        bundle
                    )
                }

                override fun onClick(contact: String) {
                    requireActivity().openPhoneDialer(contact)
                }
            }
        )
    }

}