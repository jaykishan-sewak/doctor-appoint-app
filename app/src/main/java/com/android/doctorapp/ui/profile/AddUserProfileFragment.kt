package com.android.doctorapp.ui.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentAddUserProfileBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.util.extension.toast
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject


class AddUserProfileFragment :
    BaseFragment<FragmentAddUserProfileBinding>(R.layout.fragment_add_user_profile) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddUserViewModel> { viewModelFactory }
    val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            viewModel.viewModelScope.launch {
                viewModel.checkIsEmailEveryMin()
            }
            handler.postDelayed(this, 30000)
        }
    }

    private val myCalendar = Calendar.getInstance()
    private var date =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val date = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
            viewModel.dob.value = date
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            //updateLabel()
        }


    override fun builder() = FragmentToolbar.Builder()
        .withId(FragmentToolbar.NO_TOOLBAR)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
        handler.postDelayed(runnable, 1000)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return binding {
            viewModel = this@AddUserProfileFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        checkLiveData()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the callback to stop automatic calling
        handler.removeCallbacks(runnable)
    }

    private fun checkLiveData() {
        viewModel.isCalendarShow.observe(requireActivity()) {
            if (it == true) {
                context?.let { it1 ->
                    DatePickerDialog(
                        it1, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                        myCalendar[Calendar.DAY_OF_MONTH]
                    ).show()
                }
            }
        }

        viewModel.isEmailSent.observe(requireActivity()) {
            if (it == true) {
                context?.toast("Verification Email sent successfully")
            }
        }

        viewModel.isUserReload.observe(requireActivity()) {
            if (it == true) {
                viewModel.emailVerified()
            }
        }

        viewModel.isEmailVerified.observe(requireActivity()) {
            if (it == true) {
                viewModel.emailVerifyLabel.postValue("Verified")
                handler.removeCallbacks(runnable)
            }
        }

    }


}