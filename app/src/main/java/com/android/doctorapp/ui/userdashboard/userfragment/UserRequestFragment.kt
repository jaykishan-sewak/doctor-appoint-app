package com.android.doctorapp.ui.userdashboard.userfragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


class UserRequestFragment :
    BaseFragment<FragmentUserRequestBinding>(R.layout.fragment_user_request) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: UserRequestViewModel by viewModels { viewModelFactory }
    lateinit var adapter: BookingAppointmentsAdapter
    private val myCalender: Calendar = Calendar.getInstance()


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

        binding.nestedSV.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                // in this method we are incrementing page number,
                // making progress bar visible and calling get data method.
                if (viewModel.dataLoaded.value == true) {
                    viewModel.dataLoaded.value = false
                    viewModel.loadingPB.value = true
                    if (binding.tabLayout.getTabAt(0)?.isSelected == true)
                        viewModel.getUpcomingAppointmentList()
                    else
                        viewModel.getPastAppointmentList()
                }
            }
        })

        binding.tabLayout.getTabAt(viewModel.selectedTabPosition.value!!)?.select()
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(ConstantKey.APPOINTMENT_DETAILS_UPDATED)
            ?.observe(viewLifecycleOwner) {
                if (it) {
                    if (binding.tabLayout.getTabAt(0)?.isSelected!!) {
                        callApiForTab2()
                    } else {
                        callApiForTab1()
                    }
                }
            }
        if (viewModel.pastAppointments.value == null) {
            if (binding.tabLayout.getTabAt(0)?.isSelected == true) {
                callApiForTab2()
            } else {
                callApiForTab1()
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel._pastAppointments.value = emptyList()
                viewModel.lastDocument = null
                viewModel.selectedTabPosition.value = tab?.position ?: return
                when (viewModel.selectedTabPosition.value) {
                    0 -> callApiForTab2()
                    1 -> callApiForTab1()
                    else -> {}
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        viewModel.requestSelectedDate.observe(viewLifecycleOwner) {
            viewModel.isDoctorRequestCalendar.value = false
        }

        viewModel.pastAppointments.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                viewModel.loadingPB.value = false
                adapter.filterList(it)
                viewModel.dataFound.value = true
            } else {
                adapter.filterList(emptyList())
                viewModel.dataFound.value = false
            }
        }
        viewModel.isDoctorRequestCalendar.observe(viewLifecycleOwner) {
            if (it) {
                requireContext().selectDate(
                    myCalendar = myCalender,
                    maxDate = null,
                    minDate = null
                ) { dobDate ->
                    val formatter =
                        SimpleDateFormat(ConstantKey.FORMATTED_DATE, Locale.getDefault())
                    val date = formatter.parse(dobDate)
                    viewModel.requestSelectedDate.value = date
                    updateToolbarTitle(dateFormatter(date!!, ConstantKey.DATE_MM_FORMAT))
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("tabValue")
            ?.observe(viewLifecycleOwner) { result ->
                if (result.equals(ConstantKey.PAST_LABEL)) {
                    viewModel.selectedTabPosition.postValue(1)
                    viewModel.dataFound.postValue(true)
                }
            }
    }

    fun callApiForTab1() {
        viewModel.setShowProgress(true)
        viewModel.upcomingOrPast.value = ConstantKey.PAST_LABEL
        viewModel.getPastAppointmentList()
    }

    fun callApiForTab2() {
        viewModel.setShowProgress(true)
        viewModel.upcomingOrPast.value = ConstantKey.UPCOMING_LABEL
        viewModel.getUpcomingAppointmentList()
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.my_booking)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
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
                    bundle.putString(
                        ConstantKey.BundleKeys.SELECTED_TAB,
                        viewModel.upcomingOrPast.value
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