package com.android.infusiontemplate.ui.home

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.infusiontemplate.BR
import com.android.infusiontemplate.R
import com.android.infusiontemplate.databinding.FragmentHomeBinding
import com.android.infusiontemplate.databinding.RowItemsBinding
import com.android.infusiontemplate.di.AppComponentProvider
import com.android.infusiontemplate.di.base.BaseFragment
import com.android.infusiontemplate.di.base.toolbar.FragmentToolbar
import com.android.infusiontemplate.repository.models.Data
import com.android.infusiontemplate.ui.authentication.AuthenticationActivity
import com.android.infusiontemplate.ui.settings.SettingsActivity
import com.android.infusiontemplate.util.extension.e
import com.android.infusiontemplate.util.extension.startActivity
import com.android.infusiontemplate.util.extension.startActivityFinish
import com.android.infusiontemplate.util.extension.toast
import com.android.infusiontemplate.util.liveadapter.LiveAdapter
import javax.inject.Inject

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<HomeViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObservers()
        context?.toast("Am I displayed"){
            if (it) e("Toast Displayed")
            else e("Toast GONE")
        }

// TODO this is how to as permission
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
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.purple_500))
            .withTitle(getString(R.string.title_home))
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