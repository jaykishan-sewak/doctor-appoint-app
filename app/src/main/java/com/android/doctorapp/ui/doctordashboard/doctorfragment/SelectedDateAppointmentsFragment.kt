package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
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
import com.android.doctorapp.databinding.FragmentSelectedDateAppointmentsBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.ui.doctordashboard.adapter.SelectedDateAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.APPOINTMENT_DETAILS_UPDATED
import com.android.doctorapp.util.constants.ConstantKey.DATE_MM_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_DATE
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.selectDate
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class SelectedDateAppointmentsFragment :
    BaseFragment<FragmentSelectedDateAppointmentsBinding>(R.layout.fragment_selected_date_appointments) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: SelectedDateAppointmentsViewModel by viewModels { viewModelFactory }
    var date = ""
    private lateinit var adapter: SelectedDateAdapter
    private val myCalender: Calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        lifecycleScope.launch {
            viewModel.session.getBoolean(IS_ENABLED_DARK_MODE).collectLatest {
                viewModel.isDarkThemeEnable.value = it == true
            }
        }
        val arguments: Bundle? = arguments
        if (arguments != null) {
            date =
                arguments.getString(ConstantKey.BundleKeys.DATE)!!
            viewModel.selectedDate.value = Gson().fromJson(date, Date::class.java)
        }

        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@SelectedDateAppointmentsFragment.viewModel
        }
        setUpWithViewModel(viewModel)
        registerObserver(layoutBinding)
        return layoutBinding.root
    }

    private fun registerObserver(layoutBinding: FragmentSelectedDateAppointmentsBinding) {
        setAdapter(emptyList())
        layoutBinding.selectedDateRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        layoutBinding.selectedDateRecyclerView.adapter = adapter


        viewModel.selectedDate.observe(viewLifecycleOwner) {
            val navController = findNavController()
            navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
                APPOINTMENT_DETAILS_UPDATED
            )
                ?.observe(viewLifecycleOwner) {
                    if (it) {
                        viewModel.appointmentDetailsUpdated.value = it
                        viewModel.getAppointmentList()
                    }
                }
            if (viewModel.appointmentList.value == null)
                viewModel.getAppointmentList()
            else {
                if (viewModel.isCalender.value!!)
                    viewModel.getAppointmentList()
            }
            viewModel.isCalender.value = false
        }
        viewModel.appointmentList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter.filterList(it)
                viewModel.dataFound.value = true
            } else {
                adapter.filterList(it!!)
            }
        }

        viewModel.isCalender.observe(viewLifecycleOwner) {
            if (it) {
                requireContext().selectDate(
                    myCalendar = myCalender,
                    maxDate = null,
                    minDate = Date().time
                ) { dobDate ->
                    val formatter = SimpleDateFormat(FORMATTED_DATE, Locale.getDefault())
                    val date = formatter.parse(dobDate)
                    viewModel.selectedDate.value = date
                    updateToolbarTitle(dateFormatter(date!!, DATE_MM_FORMAT))
                }
            }
        }
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitleString(dateFormatter(viewModel.selectedDate.value!!, DATE_MM_FORMAT))
            .withNavigationIcon(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_back_white
                )
            )
            .withNavigationListener {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    APPOINTMENT_DETAILS_UPDATED,
                    viewModel.appointmentDetailsUpdated.value
                )
                findNavController().popBackStack()
            }
            .withMenu(R.menu.doctor_calendar_menu)
            .withMenuItems(generateMenuItems(), generateMenuClicks())
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }


    private fun generateMenuClicks(): MenuItem.OnMenuItemClickListener {
        return MenuItem.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_calendar -> {
                    viewModel.isCalender.value = true
                }
            }
            false
        }
    }

    private fun generateMenuItems(): List<Int> {
        return listOf(R.id.action_calendar)
    }

    private fun setAdapter(items: List<AppointmentModel>) {
        adapter = SelectedDateAdapter(
            items,
            object : SelectedDateAdapter.OnItemClickListener {
                override fun onItemClick(item: AppointmentModel, position: Int) {
                    val bundle = Bundle()
                    bundle.putBoolean(ConstantKey.BundleKeys.REQUEST_FRAGMENT, false)
                    bundle.putString(ConstantKey.BundleKeys.APPOINTMENT_DATA, Gson().toJson(item))
                    bundle.putBoolean(ConstantKey.BundleKeys.FROM_SELECTED_APPOINTMENTS, true)
                    findNavController().navigate(
                        R.id.action_selected_date_to_appointment_details,
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