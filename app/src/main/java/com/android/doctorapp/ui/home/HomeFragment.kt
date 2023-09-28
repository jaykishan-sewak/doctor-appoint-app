package com.android.doctorapp.ui.home

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.doctorapp.BR
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentHomeBinding
import com.android.doctorapp.databinding.RowItemsBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.models.Data
import com.android.doctorapp.ui.authentication.AuthenticationActivity
import com.android.doctorapp.ui.settings.SettingsActivity
import com.android.doctorapp.util.extension.e
import com.android.doctorapp.util.extension.startActivity
import com.android.doctorapp.util.extension.startActivityFinish
import com.android.doctorapp.util.extension.toast
import com.android.doctorapp.util.liveadapter.LiveAdapter
import com.android.doctorapp.util.permission.RuntimePermission.Companion.askPermission
import javax.inject.Inject

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<HomeViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObservers()
        context?.toast("Am I displayed"){
            if (it) e("Toast Displayed")
            else e("Toast GONE")
        }

//// TODO this is how to as permission
//        askPermission(
//            this,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.CAMERA,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ).onAccepted {
//            context?.toast("Accepted")
//        }.onDenied {
//            context?.toast("Rejected")
//            it.askAgain()
//        }.ask()

    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitle(R.string.title_home)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .withMenu(R.menu.menu_main)
            .withMenuItems(generateMenuItems(), generateMenuClicks())
            .build()
    }

    private fun generateMenuClicks(): MenuItem.OnMenuItemClickListener {
        return MenuItem.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> viewModel.signOut()
                R.id.action_settings -> startActivity<SettingsActivity> { }
            }
            false
        }
    }

    private fun generateMenuItems(): List<Int> {
        return listOf(R.id.action_settings, R.id.action_logout,R.id.action_menu)
    }

    private fun registerObservers() {
        viewModel.apply {
            itemResponse.observe(viewLifecycleOwner, {
                it?.data.let { list ->
                    LiveAdapter(list, BR.model)
                        .map<Data, RowItemsBinding>(R.layout.row_items) {
                            onClick {
                                context?.toast(list?.get(it.adapterPosition)?.productName.orEmpty())
                            }
                        }
                        .into(binding.rvItems)
                }
            })

            navigateToLogin.observe(viewLifecycleOwner, {
                startActivityFinish<AuthenticationActivity> { }
            })
        }
    }
}