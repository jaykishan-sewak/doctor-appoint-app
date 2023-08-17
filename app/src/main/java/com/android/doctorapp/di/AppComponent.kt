package com.android.doctorapp.di

import com.android.doctorapp.di.core.AuthScopes
import com.android.doctorapp.di.core.CoreComponent
import com.android.doctorapp.ui.home.HomeFragment
import com.android.doctorapp.ui.authentication.login.LoginFragment
import com.android.doctorapp.ui.profile.ProfileFragment
import com.android.doctorapp.ui.authentication.register.RegisterFragment
import com.android.doctorapp.ui.doctor.AddDoctorFragment
import com.android.doctorapp.ui.profile.AddUserProfileFragment
import com.android.doctorapp.ui.doctor.UpdateDoctorProfileFragment
import dagger.Component

@AuthScopes
@Component(
    modules = [AppNetworkModule::class, AppViewModelModule::class],
    dependencies = [CoreComponent::class]

)
interface AppComponent {
    // Classes that can be injected by this Component
    fun inject(fragment: LoginFragment)
    fun inject(fragment: RegisterFragment)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: ProfileFragment)

    fun inject(fragment: AddUserProfileFragment)
    fun inject(fragment: AddDoctorFragment)
    fun inject(fragment: UpdateDoctorProfileFragment)
}