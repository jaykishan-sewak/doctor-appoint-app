package com.android.doctorapp.di.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.android.doctorapp.di.base.annotation.BindingOnly
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment<T : ViewDataBinding> constructor(
    @LayoutRes private val contentLayoutId: Int
) : BottomSheetDialogFragment() {

    protected var bindingComponent: DataBindingComponent? = DataBindingUtil.getDefaultComponent()

    private var _binding: T? = null

    @BindingOnly
    protected val binding: T
        get() = checkNotNull(_binding) {
            "BottomSheetDialogFragment $this binding cannot be accessed before onCreateView() or after onDestroyView()"
        }

    @BindingOnly
    protected inline fun binding(block: T.() -> Unit): T {
        return binding.apply(block)
    }

    fun setUpWithViewModel(viewModel: BaseViewModel) {
        (activity as? BaseActivity<*>)?.setUpViewModel(viewModel)
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
}