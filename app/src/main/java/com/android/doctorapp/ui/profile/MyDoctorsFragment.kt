package com.android.doctorapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentMyDoctorsBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.ui.profile.adapter.MyDoctorsItemAdapter
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.currentDate
import com.google.gson.Gson
import javax.inject.Inject

class MyDoctorsFragment : BaseFragment<FragmentMyDoctorsBinding>(R.layout.fragment_my_doctors) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ProfileViewModel> { viewModelFactory }
    private lateinit var adapter: MyDoctorsItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (viewModel.myDoctorsList.value.isNullOrEmpty()) {
            val date = currentDate()
            viewModel.getMyDoctors(date)
        }
        val layoutBinding = binding {
            viewModel = this@MyDoctorsFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return layoutBinding.root
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.my_doctors)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .withNavigationIcon(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_back_white
                )
            )
            .withNavigationListener {
                findNavController().popBackStack()
            }
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObservers(binding)
    }

    private fun registerObservers(layoutBinding: FragmentMyDoctorsBinding) {
        setAdapter(emptyList())
        layoutBinding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        layoutBinding.recyclerView.adapter = adapter

        viewModel.myDoctorsList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter.filterList(it)
                viewModel.dataNotFound.value = false
            }
        }
    }


    private fun setAdapter(items: List<UserDataResponseModel>) {
        adapter = MyDoctorsItemAdapter(
            items,
            object : MyDoctorsItemAdapter.OnItemClickListener {
                override fun onItemClick(item: UserDataResponseModel, position: Int) {

                    val bundle = Bundle()
                    bundle.putString(
                        ConstantKey.BundleKeys.BOOK_APPOINTMENT_DATA,
                        Gson().toJson(item)
                    )
                    findNavController().navigate(
                        R.id.action_my_doctors_to_bookAppointment,
                        bundle
                    )
                }

            }
        )
    }


}