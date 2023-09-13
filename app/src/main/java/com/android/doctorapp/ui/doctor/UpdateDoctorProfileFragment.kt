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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUpdateDoctorProfileBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.TimeSlotModel
import com.android.doctorapp.repository.models.WeekOffModel
import com.android.doctorapp.ui.doctor.adapter.AddDoctorTimingAdapter
import com.android.doctorapp.ui.doctor.adapter.CustomAutoCompleteAdapter
import com.android.doctorapp.ui.doctor.adapter.WeekOffDayAdapter
import com.android.doctorapp.ui.doctordashboard.DoctorDashboardActivity
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.IS_DOCTOR_OR_USER_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.STORED_VERIFICATION_Id_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.USER_CONTACT_NUMBER_KEY
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_DATE
import com.android.doctorapp.util.constants.ConstantKey.FULL_DATE_FORMAT
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.convertDateToFull
import com.android.doctorapp.util.extension.convertDateToMonth
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

    //    private val TAG = UpdateDoctorProfileFragment::class.java.simpleName
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var mTimePicker: TimePickerDialog
    private val mCurrentTime: Calendar = Calendar.getInstance()
//    private val hour = mCurrentTime.get(Calendar.HOUR_OF_DAY)
//    private val minute = mCurrentTime.get(Calendar.MINUTE)
    val handler = Handler(Looper.getMainLooper())
    var isFromAdmin: Boolean = false
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
    private val holidayList = ArrayList<Date>()
    private lateinit var weekOffDayAdapter: WeekOffDayAdapter
    private val tempStrWeekOffList = ArrayList<String>()

    val calendar = Calendar.getInstance()
//    val hourOfDay1 = calendar.get(Calendar.HOUR_OF_DAY)
//    val minute1 = calendar.get(Calendar.MINUTE)

    private var addTimeList = ArrayList<TimeSlotModel>()
    private lateinit var addDoctorTimeAdapter: AddDoctorTimingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.purple_500))
            .withTitle(if (isFromAdmin) R.string.update_doctor else R.string.title_profile)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .withNavigationIcon(if (isFromAdmin) requireActivity().getDrawable(R.drawable.ic_back_white) else null)
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
        /*mTimePicker = TimePickerDialog(
            requireContext(), { view, hourOfDay, minute ->
//                viewModel.availableTime.value = "$hourOfDay:$minute"
            }, hour, minute, true
        )*/

        /*mTimePicker = TimePickerDialog(
            requireContext(), { view, hourOfDay, minute ->
//                viewModel.availableTime.value = "$hourOfDay:$minute"
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val selectedTime = calendar.time
                val sdf = java.text.SimpleDateFormat(FULL_DATE_FORMAT)
                val formattedTime = sdf.format(selectedTime)
                Log.d("TAG", "onCreateView: $formattedTime")

            }, hourOfDay1, minute1, true
        )*/

        /*mTimePicker = TimePickerDialog(
            requireContext(), { view, hourOfDay, minute ->
                // Get the selected time from the TimePicker
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // Combine the selected time with the current date
                val selectedTime = calendar.time

                // Now you have a Date object with both date and time
                // You can use selectedTime as needed
                // For example, you can format it to a string or do other operations
                // For formatting to a string, you can use SimpleDateFormat
                val sdf = java.text.SimpleDateFormat(FULL_DATE_FORMAT)
                val formattedTime = sdf.format(selectedTime)

                // Print or use the formattedTime
                println("Selected Time: $formattedTime")
                Log.d("TAG", "showTimePickerDialog: $formattedTime")
            },
            hourOfDay,
            minute,
            true
        )*/

        bindingView = binding {
            viewModel = this@UpdateDoctorProfileFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        binding.rvWeekOff.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        bindingView.rvAddTiming.layoutManager =
            GridLayoutManager(requireContext(), 3)
        viewModel.setBindingData(bindingView)
        viewModel.getDegreeItems()
        viewModel.getSpecializationItems()
        setUpWithViewModel(viewModel)
        registerObserver(bindingView)
        return bindingView.root
    }

    private fun registerObserver(layoutBinding: FragmentUpdateDoctorProfileBinding) {
        viewModel.getModelUserData().observe(viewLifecycleOwner) {
            viewModel.name.value = it[0].name
            viewModel.email.value = it[0].email
            viewModel.contactNumber.value = it[0].contactNumber
        }
        viewModel.clickResponse.observe(viewLifecycleOwner) {
            sendVerificationCode("+91$it")
        }

        viewModel.isPhoneVerify.observe(viewLifecycleOwner) {
            if (!it) {
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
            } else {
                requireContext().selectDate(
                    maxDate = null,
                    minDate = Date().time
                ) { availableDate ->
                    viewModel.isAvailableDate.value = availableDate
                }
            }
        }

        viewModel.addTime.observe(viewLifecycleOwner) {
            if (layoutBinding.btnAddTiming.id == it.id) {
//                mTimePicker.show()
                 showTimePickerDialog()
            } else {
            }
        }

//        viewModel.isTimeShow.observe(viewLifecycleOwner) {
//            if (it) {
//                mTimePicker.show()
//            }
//        }

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

        viewModel.holidayClickResponse.observe(viewLifecycleOwner) {
            if (it) {
                requireContext().selectDate(
                    maxDate = null,
                    minDate = null
                ) { holidayDate ->
                    val monthDate = convertDateToMonth(holidayDate)
                    holidayList.add(convertDateToFull(monthDate))
                }
            }
        }

        viewModel.weekDayNameList.observe(viewLifecycleOwner) {
            updateWeekOffRecyclerview(it)
            layoutBinding.rvWeekOff.adapter = weekOffDayAdapter
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
                    /*weekOffDayList.forEachIndexed { index, weekOffModel ->
                        if (weekOffDayList[index].dayName == item.dayName) {
                            weekOffDayList[index].isWeekOff = true
                        } else {
                            if (weekOffDayList[index].isWeekOff) {
                                weekOffDayList[index].isWeekOff = true
                            } else {
                                weekOffDayList[index].isWeekOff = false
                            }
                        }
                    }
                    weekOffDayAdapter.notifyDataSetChanged()*/
                    weekOffDayList.forEachIndexed { index, weekOffModel ->
                        if (weekOffDayList[index].dayName == item.dayName) {
//                            weekOffDayList[index].isWeekOff = !weekOffDayList[index].isWeekOff
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
//                    weekOffList.add(WeekOffModel(dayName = weekOffDayList[index].dayName, isWeekOff = weekOffDayList[index].isWeekOff))
                }

            })
    }

    private fun showTimePickerDialog() {
        val currentTime = Calendar.getInstance()
        val hour1 = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute1 = currentTime.get(Calendar.MINUTE)

        // Create a TimePickerDialog with the current time
        mTimePicker = TimePickerDialog(
            requireContext(), { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                val selectedTime = calendar.time
                addTimeList.add(TimeSlotModel(timeSlot = selectedTime, isTimeSlotBook = false, isTimeClick = false))
                updateAddTimeRecyclerview(addTimeList)
            },
            hour1,
            minute1,
            true
        )

        // Show the TimePickerDialog
        mTimePicker.show()
    }


    private fun updateAddTimeRecyclerview(newAddTimeList: ArrayList<TimeSlotModel>) {
        addDoctorTimeAdapter = AddDoctorTimingAdapter(newAddTimeList)
        binding.rvAddTiming.adapter = addDoctorTimeAdapter
    }


}