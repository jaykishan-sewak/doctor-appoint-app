package com.android.doctorapp.ui.doctor

import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUpdateDoctorProfileBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.AddShiftTimeModel
import com.android.doctorapp.repository.models.HolidayModel
import com.android.doctorapp.repository.models.TimeSlotModel
import com.android.doctorapp.repository.models.WeekOffModel
import com.android.doctorapp.ui.doctor.adapter.AddDoctorHolidayAdapter
import com.android.doctorapp.ui.doctor.adapter.AddDoctorTimeAdapter
import com.android.doctorapp.ui.doctor.adapter.AddDoctorTimingAdapter
import com.android.doctorapp.ui.doctor.adapter.CustomAutoCompleteAdapter
import com.android.doctorapp.ui.doctor.adapter.WeekOffDayAdapter
import com.android.doctorapp.ui.doctordashboard.DoctorDashboardActivity
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.IS_DOCTOR_OR_USER_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.STORED_VERIFICATION_Id_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.USER_CONTACT_NUMBER_KEY
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_DATE
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.convertDateToFull
import com.android.doctorapp.util.extension.convertDateToMonth
import com.android.doctorapp.util.extension.convertTime
import com.android.doctorapp.util.extension.dateFormatter
import com.android.doctorapp.util.extension.neutralButton
import com.android.doctorapp.util.extension.selectDate
import com.android.doctorapp.util.extension.startActivityFinish
import com.android.doctorapp.util.extension.toast
import com.google.android.material.chip.Chip
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class UpdateDoctorProfileFragment :
    BaseFragment<FragmentUpdateDoctorProfileBinding>(R.layout.fragment_update_doctor_profile) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddDoctorViewModel> { viewModelFactory }
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var mTimePicker: TimePickerDialog
    val handler = Handler(Looper.getMainLooper())
    var isFromAdmin: Boolean = false
    var isNotFromAdmin: Boolean = false
    private val runnable = object : Runnable {
        override fun run() {
            viewModel.viewModelScope.launch {
                viewModel.checkIsEmailEveryMin()
            }
            handler.postDelayed(this, 10000)
        }
    }
    lateinit var bindingView: FragmentUpdateDoctorProfileBinding
    var enteredDegreeText: String = ""
    var enteredSpecializationText: String = ""
    private val holidayList = ArrayList<HolidayModel>()
    private lateinit var weekOffDayAdapter: WeekOffDayAdapter
    private val tempStrWeekOffList = ArrayList<String>()
    private lateinit var addDoctorTimingAdapter: AddDoctorTimingAdapter
    private lateinit var addDoctorHolidayAdapter: AddDoctorHolidayAdapter
    private lateinit var addDoctorTimeAdapter: AddDoctorTimeAdapter
    private var tempAddShitList = ArrayList<AddShiftTimeModel>()

    private lateinit var startTimeCalendar: Calendar
    private lateinit var endTimeCalendar: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(if (isFromAdmin) R.string.update_doctor else R.string.title_profile)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .withNavigationIcon(if (isFromAdmin || !isNotFromAdmin) requireActivity().getDrawable(R.drawable.ic_back_white) else null)
            .withNavigationListener {
                findNavController().popBackStack()
            }
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        handler.postDelayed(runnable, 1000)
        val arguments: Bundle? = arguments
        if (arguments != null)
            isFromAdmin = arguments.getBoolean(ConstantKey.BundleKeys.ADMIN_FRAGMENT)
        isNotFromAdmin = isFromAdmin

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeAutoRetrievalTimeOut(str: String) {
                viewModel.hideProgress()
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModel.hideProgress()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                viewModel.hideProgress()
                context?.alert {
                    setMessage(e.message)
                    neutralButton { }
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                viewModel.hideProgress()
                storedVerificationId = verificationId
                resendToken = token
                val degreeList = binding.chipGroup.children.toList()
                    .map { (it as Chip).text.toString() } as ArrayList<String>?
                viewModel.degreeLiveList.value = degreeList!!

                val specialityList = binding.chipGroupSpec.children.toList()
                    .map { (it as Chip).text.toString() } as ArrayList<String>?
                viewModel.specializationLiveList.value = specialityList!!

                val bundle = Bundle()
                bundle.putString(STORED_VERIFICATION_Id_KEY, storedVerificationId)
                bundle.putBoolean(IS_DOCTOR_OR_USER_KEY, true)
                bundle.putString(USER_CONTACT_NUMBER_KEY, viewModel.contactNumber.value)
                viewModel.isEmailSent.value = false
                findNavController().navigate(
                    R.id.action_updateDoctorFragment_to_OtpVerificationFragment,
                    bundle
                )
            }
        }

        startTimeCalendar = Calendar.getInstance()
        endTimeCalendar = Calendar.getInstance()


        bindingView = binding {
            viewModel = this@UpdateDoctorProfileFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        binding.rvWeekOff.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvAddTiming.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvHoliday.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        viewModel.setBindingData(bindingView)
        viewModel.getDegreeItems()
        viewModel.getSpecializationItems()
        setUpWithViewModel(viewModel)
        registerObserver(bindingView)
        return bindingView.root
    }

    private fun registerObserver(layoutBinding: FragmentUpdateDoctorProfileBinding) {

        viewModel.getUserData().observe(viewLifecycleOwner) {
            viewModel.name.value = it.name
            viewModel.email.value = it.email
            viewModel.contactNumber.value = it.contactNumber
        }

        viewModel.userResponse.observe(viewLifecycleOwner) {
            if (it?.address?.isNotEmpty()!!) {
                viewModel.name.value = it.name
                viewModel.email.value = it.email
                viewModel.contactNumber.value = it.contactNumber
                viewModel.address.value = it.address
                viewModel.selectGenderValue.value = it.gender
                viewModel.dob.value = dateFormatter(it.dob, ConstantKey.DATE_MM_FORMAT)
            } else {
                viewModel.name.value = it.name
                viewModel.email.value = it.email
                viewModel.contactNumber.value = it.contactNumber
            }
        }
        viewModel.clickResponse.observe(viewLifecycleOwner) {
            sendVerificationCode("+91$it")
        }

        viewModel.isPhoneVerify.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.validateAllUpdateField()
                layoutBinding.textContactVerify.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green
                    )
                )
            }
        }

        viewModel.isCalender.observe(viewLifecycleOwner) {
            if (layoutBinding.textDateOfBirth.id == it?.id) {
                requireContext().selectDate(maxDate = Date().time, minDate = null) { dobDate ->
                    if (calculateAge(dobDate) > 22) {
                        viewModel.dob.value = dobDate
                        viewModel.dobError.value = null
                    } else {
                        viewModel.isDobGreater22()
                    }
                }
            } else if (layoutBinding.btnAddHoliday.id == it?.id) {
                requireContext().selectDate(
                    maxDate = null,
                    minDate = null
                ) { holidayDate ->
                    val monthDate = convertDateToMonth(holidayDate)
                    Log.d("TAG", "registerObserver 1 : $holidayDate")
                    Log.d("TAG", "registerObserver 2 : ${convertDateToMonth(holidayDate)}")
                    holidayList.add(HolidayModel(holidayDate = convertDateToFull(monthDate)))
                    updateHolidayRecyclerview(holidayList)
                }
            } else if (layoutBinding.btnAddTiming.id == it?.id) {
                tempAddShitList.add(
                    AddShiftTimeModel(
                        isTimeSlotBook = false,
                    )
                )
                viewModel.addShitTimeSlotList.value = tempAddShitList
                viewModel.validateAllUpdateField()
            } else {
                requireContext().selectDate(
                    maxDate = null,
                    minDate = Date().time
                ) { availableDate ->
                    viewModel.isAvailableDate.value = availableDate
                }
            }
        }

        viewModel.addDoctorResponse.observe(viewLifecycleOwner) {
            if (it.equals(requireContext().resources.getString(R.string.success))) {
                context?.toast(resources.getString(R.string.doctor_update_successfully))
                if (isFromAdmin) {
                    findNavController().popBackStack()
                } else
                    startActivityFinish<DoctorDashboardActivity> { }
            } else {
                context?.alert {
                    setTitle(getString(R.string.doctor_not_save))
                    setMessage(it)
                    neutralButton { }
                }
            }
        }

        viewModel.isEmailSent.observe(viewLifecycleOwner) {
            if (it == true) {
                context?.toast(requireContext().resources.getString(R.string.verification_email_sent))
            }
        }
        viewModel.isUserReload.observe(viewLifecycleOwner) {
            if (it == true) {
                viewModel.emailVerified()
            }
        }
        viewModel.isEmailVerified.observe(viewLifecycleOwner) {
            if (it == true) {
                viewModel.validateAllUpdateField()
                viewModel.emailVerifyLabel.postValue(requireContext().resources.getString(R.string.verified))
                viewModel.isEmailEnable.value = false
                layoutBinding.textEmailVerify.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green
                    )
                )
                handler.removeCallbacks(runnable)
            }
        }


        viewModel.degreeList.observe(viewLifecycleOwner) {
            val adapter =
                CustomAutoCompleteAdapter(
                    requireContext(),
                    it?.degreeName!!
                )
            bindingView.autoCompleteTextView.setAdapter(adapter)

            bindingView.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                val selectedItem = adapter.getItem(position)
                if (selectedItem == CustomAutoCompleteAdapter.ADD_SUGGESTION_ITEM) {
                    addChip(enteredDegreeText.uppercase())
                    bindingView.autoCompleteTextView.setText(enteredDegreeText.uppercase())
                    addItem(enteredDegreeText.uppercase())
                    bindingView.autoCompleteTextView.setText("")
                } else {
                    addChip(selectedItem!!)
                    bindingView.autoCompleteTextView.setText("")
                }
            }
            bindingView.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() != CustomAutoCompleteAdapter.ADD_SUGGESTION_ITEM)
                        enteredDegreeText = s.toString()
                }
            })
        }
        viewModel.specializationList.observe(viewLifecycleOwner) {
            val adapter =
                CustomAutoCompleteAdapter(
                    requireContext(),
                    it?.specializations!!
                )
            bindingView.autoCompleteTextViewSpec.setAdapter(adapter)

            bindingView.autoCompleteTextViewSpec.setOnItemClickListener { _, _, position, _ ->
                val selectedItem = adapter.getItem(position)
                if (selectedItem == CustomAutoCompleteAdapter.ADD_SUGGESTION_ITEM) {
                    addSpecChip(enteredSpecializationText.uppercase())
                    bindingView.autoCompleteTextViewSpec.setText(enteredSpecializationText.uppercase())
                    addSpecializationItem(enteredSpecializationText.uppercase())
                    bindingView.autoCompleteTextViewSpec.setText("")
                } else {
                    addSpecChip(selectedItem!!)
                    bindingView.autoCompleteTextViewSpec.setText("")
                }
            }
            bindingView.autoCompleteTextViewSpec.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() != CustomAutoCompleteAdapter.ADD_SUGGESTION_ITEM)
                        enteredSpecializationText = s.toString()
                }
            })
        }
        viewModel.degreeLiveList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                for (element in it) {
                    addChip(element)
                }
            }
        }

        viewModel.specializationLiveList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                for (element in it) {
                    addSpecChip(element)
                }
            }
        }

        viewModel.weekDayNameList.observe(viewLifecycleOwner) {
            updateWeekOffRecyclerview(it)
            layoutBinding.rvWeekOff.adapter = weekOffDayAdapter
        }

        viewModel.addShitTimeSlotList.observe(viewLifecycleOwner) {
            updateAddShiftTimeAdapter(it)
        }

    }

    private fun addSpecializationItem(uppercase: String) {
        viewModel.addSpecializationItems(uppercase)

    }

    private fun addItem(data: String) {
        viewModel.addDegreeItems(data)
    }

    private fun addChip(text: String) {
        val chip = Chip(requireContext())
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            bindingView.chipGroup.removeView(chip)
            viewModel.validateAllUpdateField()
        }
        bindingView.chipGroup.addView(chip)
        viewModel.validateAllUpdateField()
    }

    private fun addSpecChip(text: String) {
        val chip = Chip(requireContext())
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            bindingView.chipGroupSpec.removeView(chip)
            viewModel.validateAllUpdateField()
        }
        bindingView.chipGroupSpec.addView(chip)
        viewModel.validateAllUpdateField()
    }

    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(viewModel.firebaseAuth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun calculateAge(selectedDate: String?): Int {
        val dateFormat = SimpleDateFormat(FORMATTED_DATE, Locale.getDefault())
        val today = Calendar.getInstance()
        val birthDate = Calendar.getInstance()
        return try {
            val date: Date = dateFormat.parse(selectedDate)
            birthDate.time = date
            var years =
                today[Calendar.YEAR] - birthDate[Calendar.YEAR]
            if (today[Calendar.MONTH] < birthDate[Calendar.MONTH] || today[Calendar.MONTH] == birthDate[Calendar.MONTH] && today[Calendar.DAY_OF_MONTH] < birthDate[Calendar.DAY_OF_MONTH]) {
                years--
            }
            years
        } catch (e: ParseException) {
            e.printStackTrace()
            -1
        }
    }

    private fun updateWeekOffRecyclerview(weekOffDayList: ArrayList<WeekOffModel>) {
        weekOffDayAdapter = WeekOffDayAdapter(weekOffDayList,
            object : WeekOffDayAdapter.OnItemClickListener {
                override fun onItemClick(item: WeekOffModel, position: Int) {
                    weekOffDayList.forEachIndexed { index, weekOffModel ->
                        if (weekOffDayList[index].dayName == item.dayName) {
                            if (weekOffDayList[index].isWeekOff) {
                                weekOffDayList[index].isWeekOff = false
                                tempStrWeekOffList.remove(weekOffDayList[index].dayName)
                            } else {
                                weekOffDayList[index].isWeekOff = true
                                tempStrWeekOffList.add(weekOffDayList[index].dayName)
                            }
                        } else {

                        }

                    }
                    weekOffDayAdapter.notifyItemChanged(position)
                    viewModel.strWeekOffList.value = tempStrWeekOffList
                }

            })
    }

    private fun showTimePickerDialog(isStartTime: Boolean, position: Int) {
        val currentTime = Calendar.getInstance()
        val hour1 = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute1 = currentTime.get(Calendar.MINUTE)
        val calendar = if (isStartTime) startTimeCalendar else endTimeCalendar

        // Create a TimePickerDialog with the current time
        mTimePicker = TimePickerDialog(
            requireContext(), { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                val selectedTime = calendar.time
                if (isStartTime) {
                    val timeContainsOrNot = tempAddShitList.any {
                        convertTime(it.startTime.toString()) == convertTime(selectedTime.toString())
                    }
                    if (timeContainsOrNot) {
                        context?.toast(getString(R.string.already_selected_time))
                    } else {
                        tempAddShitList[position].startTime = selectedTime
                    }
                    viewModel.validateAllUpdateField()
                } else {
                    if (calendar.after(startTimeCalendar)) {
                        tempAddShitList[position].endTime = selectedTime
                        viewModel.validateAllUpdateField()
                    } else {
                        endTimeCalendar = startTimeCalendar.clone() as Calendar
                        context?.toast(getString(R.string.end_time_grater))
                    }
                }
                addDoctorTimeAdapter.notifyDataSetChanged()
            },
            hour1,
            minute1,
            true
        )

        // Show the TimePickerDialog
        mTimePicker.show()
    }

    private fun updateAddTimeRecyclerview(newAddTimeList: ArrayList<TimeSlotModel>) {
        addDoctorTimingAdapter = AddDoctorTimingAdapter(newAddTimeList)
        viewModel.availableTimeList.value = newAddTimeList
        binding.rvAddTiming.adapter = addDoctorTimingAdapter
        viewModel.validateAllUpdateField()
    }

    private fun updateHolidayRecyclerview(newHolidayList: ArrayList<HolidayModel>) {
        addDoctorHolidayAdapter = AddDoctorHolidayAdapter(newHolidayList,
            object : AddDoctorHolidayAdapter.OnItemClickListener {
                override fun onItemDelete(item: HolidayModel, position: Int) {
                    newHolidayList.remove(item)
                    addDoctorHolidayAdapter.notifyDataSetChanged()

                }

            })
        viewModel.holidayList.value = newHolidayList
        binding.rvHoliday.adapter = addDoctorHolidayAdapter
    }

    private fun updateAddShiftTimeAdapter(addShitTimeList: ArrayList<AddShiftTimeModel>) {
        addDoctorTimeAdapter = AddDoctorTimeAdapter(addShitTimeList,
            object : AddDoctorTimeAdapter.OnItemClickListener {
                override fun startTimeClick(addShiftTimeModel: AddShiftTimeModel, position: Int) {
                    showTimePickerDialog(true, position)
                }

                override fun endTimeClick(addShiftTimeModel: AddShiftTimeModel, position: Int) {
                    showTimePickerDialog(false, position)
                }

                override fun removeShiftClick(addShiftTimeModel: AddShiftTimeModel, position: Int) {
                    tempAddShitList.removeAt(position)
                    addDoctorTimeAdapter.notifyDataSetChanged()
                    viewModel.validateAllUpdateField()
                }


            }
        )
        binding.rvAddTiming.adapter = addDoctorTimeAdapter
    }

}