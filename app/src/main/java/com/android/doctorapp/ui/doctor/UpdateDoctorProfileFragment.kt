package com.android.doctorapp.ui.doctor

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUpdateDoctorProfileBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DOCTOR_CONTACT_NUMBER_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DOCTOR_EMAIL_ID_KEY
import com.android.doctorapp.util.constants.ConstantKey.BundleKeys.DOCTOR_NAME_KEY
import com.google.android.material.chip.Chip
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UpdateDoctorProfileFragment :
    BaseFragment<FragmentUpdateDoctorProfileBinding>(R.layout.fragment_update_doctor_profile) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddDoctorViewModel> { viewModelFactory }
    private lateinit var doctorName: String
    private lateinit var doctorEmail: String
    private lateinit var doctorContactNumber: String
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private val TAG = UpdateDoctorProfileFragment::class.java.simpleName
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
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

        val arguments: Bundle? = arguments
        if (arguments != null) {
            doctorName = arguments.getString(DOCTOR_NAME_KEY).toString()
            doctorEmail = arguments.getString(DOCTOR_EMAIL_ID_KEY).toString()
            doctorContactNumber = arguments.getString(DOCTOR_CONTACT_NUMBER_KEY).toString()
        } else {
            doctorName = ""
            doctorEmail = ""
            doctorContactNumber = ""
        }

        viewModel.doctorName.value = doctorName
        viewModel.doctorEmail.value = doctorEmail
        viewModel.doctorContactNumber.value = doctorContactNumber

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // This method is called when the verification is completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                startActivity(Intent(applicationContext, MainActivity::class.java))
//                finish()
                Log.d(TAG, "onVerificationCompleted Success")
            }

            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                Log.d(TAG, "onVerificationFailed  $e")
            }

            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token
                // Start a new activity using intent
                // also send the storedVerificationId using intent
                // we will use this id to send the otp back to firebase
//                val intent = Intent(applicationContext,OtpActivity::class.java)
//                intent.putExtra("storedVerificationId",storedVerificationId)
//                startActivity(intent)
//                finish()
            }
        }
        bindingView = binding {
            viewModel = this@UpdateDoctorProfileFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        viewModel.getDegreeItems()
        viewModel.getSpecializationItems()
        return bindingView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver()
    }

    private fun registerObserver() {
        viewModel.clickResponse.observe(viewLifecycleOwner) {
            sendVerificationCode("+91$it")
        }
        viewModel.degreeList.observe(viewLifecycleOwner) {
            Log.d("degreeList---", Gson().toJson(it))
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

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() != CustomAutoCompleteAdapter.ADD_SUGGESTION_ITEM)
                        enteredDegreeText = s.toString()
                }
            })
        }
        viewModel.specializationList.observe(viewLifecycleOwner) {
            Log.d("specList---", Gson().toJson(it))
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
        }
        bindingView.chipGroup.addView(chip)
    }

    private fun addSpecChip(text: String) {
        val chip = Chip(requireContext())
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            bindingView.chipGroup.removeView(chip)
        }
        bindingView.chipGroupSpec.addView(chip)
    }

    private fun sendVerificationCode(number: String) {
        Log.d(TAG, "sendVerificationCode: $number")
        val options = PhoneAuthOptions.newBuilder(viewModel.firebaseAuth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d(TAG, "Auth started")
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