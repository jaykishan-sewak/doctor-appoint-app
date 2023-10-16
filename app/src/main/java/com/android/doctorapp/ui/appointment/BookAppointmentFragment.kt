package com.android.doctorapp.ui.appointment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentBookAppointmentBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.AddShiftTimeModel
import com.android.doctorapp.repository.models.DateSlotModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.ui.appointment.adapter.AppointmentDateAdapter
import com.android.doctorapp.ui.appointment.adapter.AppointmentTimeAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.BOOKING_DATE_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_DATE_MONTH_YEAR
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_HOUR_MINUTE_SECOND
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.negativeButton
import com.android.doctorapp.util.extension.neutralButton
import com.android.doctorapp.util.extension.openEmailSender
import com.android.doctorapp.util.extension.openPhoneDialer
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class BookAppointmentFragment :
    BaseFragment<FragmentBookAppointmentBinding>(R.layout.fragment_book_appointment) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AppointmentViewModel by viewModels { viewModelFactory }
    private lateinit var appointmentTimeAdapter: AppointmentTimeAdapter
    private lateinit var appointmentDateAdapter: AppointmentDateAdapter
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var selectedDateTime: Date

    //    private lateinit var dateStr: String
    private lateinit var timeStr: String
    private var isExpanded = false


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
        val arguments: Bundle? = arguments
        if (arguments != null) {

            val doctorDataObj =
                requireArguments().getString(ConstantKey.BundleKeys.BOOK_APPOINTMENT_DATA)
            viewModel.doctorDataObj.value =
                Gson().fromJson(doctorDataObj, UserDataResponseModel::class.java)
        }
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@BookAppointmentFragment.viewModel
        }
        setUpWithViewModel(viewModel)
        registerObserver(layoutBinding)
        return layoutBinding.root
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.title_appointment)
            .withNavigationIcon(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_back_white
                )
            )
            .withNavigationListener {
                val navController = findNavController()
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    ConstantKey.BundleKeys.USER_FRAGMENT,
                    false
                )
                findNavController().popBackStack()
            }
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }

    private fun registerObserver(layoutBinding: FragmentBookAppointmentBinding) {

        binding.rvScheduleDate.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTime.layoutManager = GridLayoutManager(requireContext(), 4)

        viewModel.getDoctorData()

        binding.textDoctorDesc.setOnClickListener {
            isExpanded = !isExpanded
            updateDescriptionText(binding.textDoctorDesc)
        }
        viewModel.daysDateList.observe(viewLifecycleOwner) {
            updateDateRecyclerview(it)
            layoutBinding.rvScheduleDate.adapter = appointmentDateAdapter
        }

        viewModel.timeSlotList.observe(viewLifecycleOwner) {
            updateTimeRecyclerview(it)
            layoutBinding.rvTime.adapter = appointmentTimeAdapter
        }

        viewModel.isBookAppointmentClick.observe(viewLifecycleOwner) { it ->
            if (it) {
                context?.alert {
                    setTitle(resources.getString(R.string.booking))
                    setMessage(resources.getString(R.string.dialog_appointment_desc))
                    neutralButton { dialog ->
                        dialog.dismiss()
                        try {
                            dateFormat = SimpleDateFormat(BOOKING_DATE_FORMAT)
                            selectedDateTime =
                                dateFormat.parse("${viewModel.dateStr.value} $timeStr")!!
                            viewModel.addBookingAppointmentData(selectedDateTime)
                        } catch (_: Exception) {
                        }
                    }
                    negativeButton(context.resources.getString(R.string.cancel)) { dialog ->
                        dialog.dismiss()
                    }

                }
            }
        }
        viewModel.navigationListener.observe(viewLifecycleOwner) { it ->
            if (it) {
                findNavController().popBackStack()
            }
        }

        viewModel.phoneClick.observe(viewLifecycleOwner) {
            requireActivity().openPhoneDialer(it)
        }
        viewModel.emailClick.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                requireActivity().openEmailSender(it)
            }
        }

    }

    private fun updateDescriptionText(descriptionTextView: TextView) {
        if (isExpanded) {
            descriptionTextView.maxLines = Int.MAX_VALUE // Expand description
        } else {
            descriptionTextView.maxLines = 2 // Collapse description to 2 lines
        }
    }


    private fun updateDateRecyclerview(dateList: ArrayList<DateSlotModel>) {
        appointmentDateAdapter = AppointmentDateAdapter(dateList,
            object : AppointmentDateAdapter.OnItemClickListener {
                override fun onItemClick(item: DateSlotModel, position: Int) {
                    dateList.forEachIndexed { index, dateSlotModel ->
                        if (dateSlotModel.date == item.date) {
                            item.date?.let { viewModel.getAppointmentData(selectedDate = it) }
                            viewModel.dateStr.value =
                                dateFormatter(item.date, FORMATTED_DATE_MONTH_YEAR)
                            dateList[index].dateSelect = true
                            viewModel.isDateSelected.value = true
                            appointmentDateAdapter.notifyItemChanged(index)
                        } else {
                            dateList[index].dateSelect = false
                            appointmentDateAdapter.notifyItemChanged(index)
                        }
                    }

                    viewModel.validateDateTime()
                }
            })
    }

    private fun updateTimeRecyclerview(timeList: ArrayList<AddShiftTimeModel>) {
        appointmentTimeAdapter = AppointmentTimeAdapter(timeList,
            object : AppointmentTimeAdapter.OnItemClickListener {
                override fun onItemClick(item: AddShiftTimeModel, position: Int) {
                    timeList.forEachIndexed { index, timeSlotModel ->
                        if (timeSlotModel.startTime == item.startTime) {
                            timeStr = dateFormatter(item.startTime, FORMATTED_HOUR_MINUTE_SECOND)
                            timeList[index].isTimeClick = true
                            appointmentTimeAdapter.notifyItemChanged(index)
                        } else {
                            timeList[index].isTimeClick = false
                            appointmentTimeAdapter.notifyItemChanged(index)
                            appointmentTimeAdapter.notifyItemChanged(index)
                        }
                    }
                    viewModel.isTimeSelected.value = true
                    viewModel.validateDateTime()
                }
            })
    }
}