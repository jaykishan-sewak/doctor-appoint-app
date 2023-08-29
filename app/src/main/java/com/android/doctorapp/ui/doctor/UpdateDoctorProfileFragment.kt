package com.android.doctorapp.ui.doctor

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUpdateDoctorProfileBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.IS_DOCTOR_OR_USER_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.STORED_VERIFICATION_Id_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.USER_CONTACT_NUMBER_KEY
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.neutralButton
import com.android.doctorapp.util.extension.selectDate
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
    private val TAG = UpdateDoctorProfileFragment::class.java.simpleName
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var mTimePicker: TimePickerDialog
    val mcurrentTime = Calendar.getInstance()
    val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
    val minute = mcurrentTime.get(Calendar.MINUTE)
    val handler = Handler(Looper.getMainLooper())
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.purple_500))
            .withTitle(R.string.title_profile)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        handler.postDelayed(runnable, 1000)
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeAutoRetrievalTimeOut(str: String) {
                viewModel.hideProgress()
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModel.hideProgress()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                viewModel.hideProgress()
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
                viewModel.degreeLiveList.addAll(degreeList!!)

                val specialityList = binding.chipGroupSpec.children.toList()
                    .map { (it as Chip).text.toString() } as ArrayList<String>?
                viewModel.specializationLiveList.addAll(specialityList!!)

                val bundle = Bundle()
                bundle.putString(STORED_VERIFICATION_Id_KEY, storedVerificationId)
                bundle.putBoolean(IS_DOCTOR_OR_USER_KEY, true)
                bundle.putString(USER_CONTACT_NUMBER_KEY, viewModel.contactNumber.value)
                findNavController().navigate(
                    R.id.action_updateDoctorFragment_to_OtpVerificationFragment,
                    bundle
                )
            }
        }
        mTimePicker = TimePickerDialog(
            requireContext(), { view, hourOfDay, minute ->
                viewModel.availableTime.value = "$hourOfDay:$minute"
            }, hour, minute, true
        )
        bindingView = binding {
            viewModel = this@UpdateDoctorProfileFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        viewModel.setBindingData(bindingView)
        viewModel.getDegreeItems()
        viewModel.getSpecializationItems()
        return bindingView.root
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.degreeLiveList.size > 0) {
            for (i in 0 until viewModel.degreeLiveList.size) {
                addChip(viewModel.degreeLiveList.get(i))
            }
        }
        if (viewModel.specializationLiveList.size > 0) {
            for (i in 0 until viewModel.specializationLiveList.size) {
                addSpecChip(viewModel.specializationLiveList.get(i))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver()
    }

    private fun registerObserver() {
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
                binding.textContactVerify.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green
                    )
                )
            }
        }

        viewModel.isCalender.observe(viewLifecycleOwner) {
            if (binding.textDateOfBirth.id == it?.id) {
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
        viewModel.isTimeShow.observe(viewLifecycleOwner) {
            if (it) {
                mTimePicker.show()
            }
        }

        viewModel.addDoctorResponse.observe(viewLifecycleOwner) {
            if (it.equals(requireContext().resources.getString(R.string.success))) {
                context?.toast(resources.getString(R.string.doctor_update_successfully))
                viewModel.navigationListener.observe(viewLifecycleOwner) { navId ->
                    findNavController().navigate(navId)
                    findNavController().popBackStack(R.id.LoginFragment, false)
                }
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
                context?.toast(requireContext().resources.getString(R.string.verification_main_sent))
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
                viewModel.emailVerifyLabel.postValue(requireContext().resources.getString(R.string.Verified))
                viewModel.isEmailEnable.value = false
                binding.textEmailVerify.setTextColor(
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

    fun calculateAge(selectedDate: String?): Int {
        val dateFormat = SimpleDateFormat("dd-mm-yyyy", Locale.getDefault())
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


}


class CustomAutoCompleteAdapter(context: Context, suggestions: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, suggestions),
    Filterable {

    private var originalSuggestions: List<String> = suggestions.toList()

    override fun getFilter(): Filter {
        return customFilter
    }

    private val customFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            val filteredList = mutableListOf<String>()

            if (constraint.isNullOrEmpty()) {
                filteredList.addAll(originalSuggestions)
            } else {
                val filterPattern = constraint.toString().lowercase().trim()
                for (suggestion in originalSuggestions) {
                    if (suggestion.lowercase().contains(filterPattern)) {
                        filteredList.add(suggestion)
                    }
                }
            }

            if (filteredList.isEmpty()) {
                filteredList.add(ADD_SUGGESTION_ITEM)
            }

            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            clear()
            addAll(results?.values as List<String>)
            notifyDataSetChanged()
        }
    }

    companion object {
        const val ADD_SUGGESTION_ITEM = "Add"
    }

}