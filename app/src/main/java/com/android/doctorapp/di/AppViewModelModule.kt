package com.android.doctorapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.doctorapp.ui.home.HomeViewModel
import com.android.doctorapp.ui.authentication.login.LoginViewModel
import com.android.doctorapp.ui.profile.ProfileViewModel
import com.android.doctorapp.ui.authentication.register.RegisterViewModel
import com.android.doctorapp.ui.doctor.AddDoctorViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AppViewModelModule {
    @Binds
    @IntoMap
    @ViewModelMapKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelMapKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel(viewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(RegisterViewModel::class)
    abstract fun bindRegisterViewModel(viewModel: RegisterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(AddDoctorViewModel::class)
    abstract fun bindAddDoctorViewModel(viewModel: AddDoctorViewModel): ViewModel

}
