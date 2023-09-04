package com.android.doctorapp.ui.appointment

import android.os.Bundle
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
import com.android.doctorapp.ui.appointment.adapter.AppointmentAdapter
import com.android.doctorapp.ui.appointment.adapter.AppointmentTimeAdapter
import javax.inject.Inject


class BookAppointmentFragment: BaseFragment<FragmentBookAppointmentBinding>(R.layout.fragment_book_appointment) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AppointmentViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: AppointmentAdapter
    private lateinit var timeAdapter: AppointmentTimeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.purple_500))
            .withTitle(R.string.title_appointment)
            .withNavigationIcon(requireActivity().getDrawable(R.drawable.ic_back_white))
            .withNavigationListener {
                findNavController().popBackStack()
            }
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver()
    }

    private fun registerObserver() {
//        viewModel.get15DaysList()
        viewModel.scheduleDateList.observe(viewLifecycleOwner) {
            adapter = AppointmentAdapter(it)
            binding.rvScheduleDate.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvScheduleDate.adapter = adapter
        }

        viewModel.scheduleTimeList.observe(viewLifecycleOwner) {
            timeAdapter = AppointmentTimeAdapter(it)
            binding.rvTime.layoutManager = GridLayoutManager(requireContext(), 4)
            binding.rvTime.adapter = timeAdapter
        }

    }

}