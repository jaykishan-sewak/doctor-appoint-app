package com.android.doctorapp.di.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import com.android.doctorapp.R
import com.android.doctorapp.di.base.annotation.BindingOnly
import com.android.doctorapp.util.AppProgressDialog
import com.android.doctorapp.util.constants.ConstantKey
import com.android.doctorapp.util.extension.alert
import com.android.doctorapp.util.extension.neutralButton

abstract class BaseActivity<T : ViewDataBinding> constructor(
    @LayoutRes private val contentLayoutId: Int
) : AppCompatActivity() {

    protected var bindingComponent: DataBindingComponent? = DataBindingUtil.getDefaultComponent()
    protected lateinit var navController: NavController

    @BindingOnly
    protected val binding: T by lazy(LazyThreadSafetyMode.NONE) {
        DataBindingUtil.setContentView(this, contentLayoutId, bindingComponent)
    }

    @BindingOnly
    protected inline fun binding(block: T.() -> Unit): T {
        return binding.apply(block)
    }

    init {
        addOnContextAvailableListener {
            binding.notifyChange()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }

    private lateinit var viewModel: BaseViewModel

    var enableBackButton: Boolean = false
        set(value) {
            field = value
            supportActionBar?.setDisplayHomeAsUpEnabled(value)
        }

    var title: String? = null
        set(value) {
            field = value
            supportActionBar?.title = value
        }

    fun setUpViewModel(viewModel: BaseViewModel) {
        this.viewModel = viewModel
        registerObservers()
    }


    override fun setTitle(@StringRes resId: Int) {
        super.setTitle(resId)
        supportActionBar?.setTitle(resId)
    }

    private fun registerObservers() {
        viewModel.apply {
            apiError.observe(this@BaseActivity) {
                if (it != ConstantKey.NOT_FOUND) {
                    alert {
                        setTitle(getString(R.string.wrong_crendentials))
                        setMessage(getString(R.string.you_have_entered_wrong_email_or_password))
                        neutralButton { dialog ->
                            dialog.dismiss()
                        }
                    }
                }
            }

            noNetwork.observe(this@BaseActivity) {
                alert {
                    setTitle(getString(R.string.no_network))
                    setMessage(it)
                    neutralButton { }
                }
            }

            showProgress.observe(this@BaseActivity) {
                if (it) AppProgressDialog().getInstance()?.showProgress(this@BaseActivity)
                else AppProgressDialog().getInstance()?.hideProgress()
            }
        }
    }
}
