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
import com.android.doctorapp.databinding.FragmentHistoryBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.ui.profile.adapter.HistoryAdapter
import javax.inject.Inject


class HistoryFragment : BaseFragment<FragmentHistoryBinding>(R.layout.fragment_history) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<HistoryViewModel> { viewModelFactory }
    private lateinit var adapter: HistoryAdapter


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
            viewModel = this@HistoryFragment.viewModel
        }
        setUpWithViewModel(viewModel)
        registerObservers(layoutBinding)
        return layoutBinding.root
    }


    private fun registerObservers(layoutBinding: FragmentHistoryBinding) {
        layoutBinding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HistoryAdapter(emptyList())
        layoutBinding.historyRecyclerView.adapter = adapter
        viewModel.appointmentHistoryList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter.filterList(it)
                viewModel.dataFound.value = true
            } else {
                adapter.filterList(emptyList())
                viewModel.dataFound.value = false
            }
        }
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withNavigationIcon(
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_back_white
                )
            )
            .withNavigationListener {
                findNavController().popBackStack()
            }
            .withTitle(R.string.history)
            .build()
    }

}