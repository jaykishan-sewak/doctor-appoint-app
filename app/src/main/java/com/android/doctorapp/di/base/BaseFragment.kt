package com.android.doctorapp.di.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.android.doctorapp.di.base.annotation.BindingOnly
import com.android.doctorapp.di.base.toolbar.ToolbarFragment

abstract class BaseFragment<T : ViewDataBinding> constructor(
    @LayoutRes private val contentLayoutId: Int
) : ToolbarFragment() {

    protected var bindingComponent: DataBindingComponent? = DataBindingUtil.getDefaultComponent()

    private var _binding: T? = null

    @BindingOnly
    protected val binding: T
        get() = checkNotNull(_binding) {
            "Fragment $this binding cannot be accessed before onCreateView() or after onDestroyView()"
        }

    fun setUpWithViewModel(viewModel: BaseViewModel) {
        (activity as? BaseActivity<*>)?.setUpViewModel(viewModel)
    }

    var title: String? = null
        set(value) {
            field = value
            (activity as? BaseActivity<*>)?.title = value
        }

    var enableBackButton: Boolean = false
        get() = (activity as? BaseActivity<*>)?.enableBackButton ?: false
        set(value) {
            field = value
            (activity as? BaseActivity<*>)?.enableBackButton = value
        }

    fun setTitle(@StringRes resId: Int) {
        (activity as? BaseActivity<*>)?.setTitle(resId)
    }

    @BindingOnly
    protected inline fun binding(block: T.() -> Unit): T {
        return binding.apply(block)
    }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, contentLayoutId, container, false, bindingComponent)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.unbind()
        _binding = null
    }

    fun updateToolbarTitle(title: String) {
        toolbarManager?.updateTitle(title)
    }
}