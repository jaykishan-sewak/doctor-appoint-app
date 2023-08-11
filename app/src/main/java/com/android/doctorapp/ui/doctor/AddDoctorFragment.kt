package com.android.doctorapp.ui.doctor

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentAddDoctorBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.ui.profile.ProfileViewModel
import javax.inject.Inject

class AddDoctorFragment: BaseFragment<FragmentAddDoctorBinding>(R.layout.fragment_add_doctor) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddDoctorViewModel> { viewModelFactory }
//    private var notificationEnable = false
    private val TAG = "AddDoctorTag"

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
        return binding {
            viewModel = this@AddDoctorFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver()
//        binding.setOnGenderChange { buttonView, isChecked ->
//            Log.d(TAG, "onViewCreated: -->         $isChecked")
//            notificationEnable = isChecked
//        }

//        binding.toggleDoctorNotification.setOnClickListener {
//            viewModel.setToggleState(binding.toggleDoctorNotification.isChecked)
//        }

    }

    private fun registerObserver() {
        viewModel.apply {
            viewModel.doctorNameErrorTrue.observe(viewLifecycleOwner) { nameIt ->
                if (nameIt) {
                    viewModel.doctorEmailErrorTrue.observe(viewLifecycleOwner) {emailIt ->
                        if (emailIt) {
                            viewModel.doctorContactNumberErrorTrue.observe(viewLifecycleOwner) { contactIt ->
                                binding.btn.isEnabled = contactIt
                            }
                        } else {
                            binding.btn.isEnabled = false
                        }
                    }
                } else {
                    binding.btn.isEnabled = false
                }
            }
            viewModel.toggleLiveData.observe(viewLifecycleOwner) {
                Log.d(TAG, "registerObserver: $it")
//                Log.d(TAG, "registerObserver: ${viewModel.toggleChecked.get()}")
            }
        }
         viewModel.navigationListener.observe(viewLifecycleOwner) {
             findNavController().navigate(it)
         }
    }

}