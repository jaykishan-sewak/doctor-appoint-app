package com.android.doctorapp.ui.appointment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.android.doctorapp.repository.models.TimeSlotModel
import com.android.doctorapp.ui.appointment.adapter.AppointmentDateAdapter
import com.android.doctorapp.ui.appointment.adapter.AppointmentTimeAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.negativeButton
import com.android.doctorapp.util.extension.neutralButton
import java.util.Date
import javax.inject.Inject

class BookAppointmentFragment :
    BaseFragment<FragmentBookAppointmentBinding>(R.layout.fragment_book_appointment) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AppointmentViewModel by viewModels { viewModelFactory }
    private lateinit var appointmentTimeAdapter: AppointmentTimeAdapter
    private lateinit var appointmentDateAdapter: AppointmentDateAdapter
    private lateinit var selectedTime: Date

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
            viewModel.doctorId.value = arguments.getString(ConstantKey.BundleKeys.USER_ID).toString()
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
            .withNavigationIcon(requireActivity().getDrawable(R.drawable.ic_back_white))
            .withNavigationListener {
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
                        viewModel.addBookingAppointmentData(selectedTime)
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
    }

    private fun updateDateRecyclerview(dateList: ArrayList<DateSlotModel>) {
        appointmentDateAdapter = AppointmentDateAdapter(dateList,
            object : AppointmentDateAdapter.OnItemClickListener {
                override fun onItemClick(item: DateSlotModel, position: Int) {
                    dateList.forEachIndexed { index, dateSlotModel ->
                        if (dateSlotModel.date == item.date) {
                            dateList[index].dateSelect = true
                            appointmentDateAdapter.notifyItemChanged(index)
                        } else {
                            dateList[index].dateSelect = false
                            appointmentDateAdapter.notifyItemChanged(index)
                        }
                    }

                    viewModel.isDateSelected.value = true
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
                            timeList[index].isTimeClick = true
                            selectedTime = item.startTime!!
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