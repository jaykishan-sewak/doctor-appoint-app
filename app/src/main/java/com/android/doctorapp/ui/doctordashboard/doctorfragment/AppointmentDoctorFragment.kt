package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentAppointmentDoctorBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.RecyclerViewContainer
import com.android.doctorapp.ui.doctordashboard.adapter.PatientListAdapter
import javax.inject.Inject


class AppointmentDoctorFragment :
    BaseFragment<FragmentAppointmentDoctorBinding>(R.layout.fragment_appointment_doctor) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AppointmentDoctorViewModel by viewModels { viewModelFactory }
    private var itemList = mutableListOf<RecyclerViewContainer>()



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
        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@AppointmentDoctorFragment.viewModel
        }
        setUpWithViewModel(viewModel)
        registerObserver(layoutBinding)

        return layoutBinding.root
    }




    private fun registerObserver(layoutBinding: FragmentAppointmentDoctorBinding) {
        layoutBinding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val myAdapter = PatientListAdapter(itemList)
        layoutBinding.recyclerView.adapter = myAdapter

        viewModel.appointmentList.observe(viewLifecycleOwner) {
            myAdapter.filterList(it)
        }
//        viewModel.appointmentList3.observe(viewLifecycleOwner) {
//            Log.d(TAG, "SortedList: $it")
////            val groupData = it.groupBy {
////                it.bookingDateTime
////            }
////            val headerItems = groupData.map { (d) }
////            Log.d("grouped Data: --- ",Gson().toJson(groupData))
//            it.forEachIndexed { index, appointmentModel ->
//                if (index % 2 != 0) {
//                    mainList.add(Header(convertDate( appointmentModel.bookingDateTime.toString())))
//                }
//                else {
//                    mainList.add(AppointmentModel( appointmentModel.bookingDateTime,appointmentModel.isOnline,
//                        appointmentModel.reason, appointmentModel.status, appointmentModel.userId,
//                    appointmentModel.name, appointmentModel.age))
//                }
//            }
//        }

    }


    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.appointment)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }


}