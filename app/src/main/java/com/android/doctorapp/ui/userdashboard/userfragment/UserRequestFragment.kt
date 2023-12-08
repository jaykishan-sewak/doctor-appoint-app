package com.android.doctorapp.ui.userdashboard.userfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUserRequestBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.ui.userdashboard.userfragment.adapter.BookingAppointmentsAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.currentDate
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.openPhoneDialer
import com.android.doctorapp.util.extension.selectDate
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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


    private var currentPage = PaginationScrollListener.PAGE_START
    private var isLastPage = false
    private var totalPage = 10
    private var isLoading = false
    private var itemCount = 0
    private var bookAppointmentList: MutableList<AppointmentModel> = mutableListOf()


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
                if (it == true)
                    binding.tabLayout.background =
                        AppCompatResources.getDrawable(requireActivity(), R.drawable.tab_shape_dark)
                else
                    binding.tabLayout.background =
                        AppCompatResources.getDrawable(requireActivity(), R.drawable.tab_shape)

            }
        }
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@UserRequestFragment.viewModel
        }

        setUpWithViewModel(viewModel)
        registerObserver(layoutBinding)
        return layoutBinding.root
    }

    private fun registerObserver(layoutBinding: FragmentUserRequestBinding) {

        val layoutManager = LinearLayoutManager(requireContext())
        setAdapter(mutableListOf())
        binding.requestUserRecyclerView.layoutManager = layoutManager
        binding.requestUserRecyclerView.adapter = adapter


        viewModel.requestSelectedDate.value = currentDate()
        binding.tabLayout.getTabAt(viewModel.selectedTabPosition.value!!)?.select()
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(ConstantKey.APPOINTMENT_DETAILS_UPDATED)
            ?.observe(viewLifecycleOwner) {
                if (it) {
                    if (binding.tabLayout.getTabAt(0)?.isSelected!!) {
                        viewModel.lastDocument = null
                        callApiForTab2()
                    } else {
                        viewModel.lastDocument = null
                        callApiForTab1()
                    }
                }
            }

        binding.requestUserRecyclerView.addOnScrollListener(object :
            PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                if (binding.tabLayout.getTabAt(0)?.isSelected == true)
                    viewModel.getUpcomingAppointmentList()
                else
                    viewModel.getPastAppointmentList()
                isLoading = true
                currentPage++
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })


        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("tabValue")
            ?.observe(viewLifecycleOwner) { result ->
                if (result.equals(ConstantKey.PAST_LABEL)) {
                    viewModel.selectedTabPosition.postValue(1)
                    viewModel.dataFound.postValue(true)
                    isLastPage = false
                    viewModel._pastAppointments.value = viewModel.userAppointmentData
                } else {
                    isLastPage = false
                    viewModel.dataFound.postValue(true)
                    viewModel._pastAppointments.value = viewModel.userAppointmentData
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
                adapter.clear()
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
                bookAppointmentList = it as MutableList<AppointmentModel>
                itemCount += bookAppointmentList.size
                doApiCall(bookAppointmentList)
                viewModel.dataFound.value = true
            } else {
                if (bookAppointmentList.isNotEmpty()) {
                    isLastPage = true
                    adapter.removeLoading()
                }
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

    }

    fun callApiForTab1() {
        reAssignValues()
        viewModel.upcomingOrPast.value = ConstantKey.PAST_LABEL
        currentPage = PaginationScrollListener.PAGE_START
        viewModel.getPastAppointmentList()
    }

    fun callApiForTab2() {
        reAssignValues()
        viewModel.upcomingOrPast.value = ConstantKey.UPCOMING_LABEL
        currentPage = PaginationScrollListener.PAGE_START
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

    private fun setAdapter(items: MutableList<AppointmentModel>) {
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
                    viewModel._pastAppointments.value = emptyList()
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

    private fun doApiCall(list: MutableList<AppointmentModel>) {
        val items = ArrayList<AppointmentModel>()
        items.addAll(list)
        if (currentPage != PaginationScrollListener.PAGE_START) adapter.removeLoading()

        viewModel.setShowProgress(false)
        adapter.addItems(items)

        if (list.size >= totalPage) {
            adapter.addLoading()
        } else {
            isLastPage = true
        }
        isLoading = false
    }


    private fun reAssignValues() {
        viewModel.userAppointmentData.clear()
        isLastPage = false
        viewModel.dataFound.value = true
        viewModel.setShowProgress(true)
        bookAppointmentList.clear()
        itemCount = 0
    }
}