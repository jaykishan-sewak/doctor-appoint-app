package com.android.doctorapp.ui.appointment

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentBookAppointmentBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.models.AddShiftTimeModel
import com.android.doctorapp.repository.models.DateSlotModel
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.ui.appointment.adapter.AppointmentDateAdapter
import com.android.doctorapp.ui.appointment.adapter.AppointmentTimeAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.BOOKING_DATE_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_DATE_MONTH_YEAR
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_HOUR_MINUTE_SECOND
import com.android.doctorapp.util.constants.ConstantKey.KEY_LATITUDE
import com.android.doctorapp.util.constants.ConstantKey.KEY_LONGITUDE
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.hideKeyboard
import com.android.doctorapp.util.extension.negativeButton
import com.android.doctorapp.util.extension.neutralButton
import com.android.doctorapp.util.extension.openDirectionMap
import com.android.doctorapp.util.extension.openEmailSender
import com.android.doctorapp.util.extension.openPhoneDialer
import com.android.doctorapp.util.setResizableText
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
                viewModel.isDarkThemeEnable.value = it
            }
        }
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

        viewModel.doctorDetails.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.textDoctorDesc.setResizableText(it.doctorDescription, 2, true)
            }
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
                binding.buttonBookAppointment.hideKeyboard()
                context?.alert {
                    setTitle(resources.getString(R.string.booking))
                    setMessage(resources.getString(R.string.dialog_appointment_desc))
                    neutralButton(resources.getString(R.string.book_label)) { dialog ->
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
            if (!it.isNullOrEmpty()) {
                requireActivity().openPhoneDialer(it)
                viewModel.phoneClick.value = ""
            }
        }
        viewModel.emailClick.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                requireActivity().openEmailSender(it)
                viewModel.emailClick.value = ""
            }
        }

        viewModel.isDirectionClick.observe(viewLifecycleOwner) {
            if (it) {
                val smallValue = 0.0001
                val originalLatitude =
                    viewModel.doctorDataObj.value?.addressLatLng?.get(KEY_LATITUDE).toString()
                        .toDoubleOrNull() ?: 0.0
                val originalLongitude =
                    viewModel.doctorDataObj.value?.addressLatLng?.get(KEY_LONGITUDE).toString()
                        .toDoubleOrNull() ?: 0.0

                val formattedAdjustedLatitude = "%f".format(originalLatitude + smallValue)
                val formattedAdjustedLongitude = "%f".format(originalLongitude + smallValue)

                requireActivity().openDirectionMap(
                    "$formattedAdjustedLatitude,$formattedAdjustedLongitude"
                )
                viewModel.isDirectionClick.value = false
            }
        }

        viewModel.viewClinicClicked.observe(viewLifecycleOwner) {
            if (it) {
                val bundle = Bundle()
                val imageList: MutableList<String>? = viewModel.clinicImageList.value
                if (!imageList.isNullOrEmpty()) {
                    bundle.putStringArrayList(
                        ConstantKey.BundleKeys.GET_CLINIC_IMAGE_LIST_KEY, ArrayList(
                            imageList
                        )
                    )
                }
                findNavController().navigate(R.id.action_book_appointment_to_viewClinic, bundle)
                viewModel.viewClinicClicked.value = false
            }
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