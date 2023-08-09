package com.android.doctorapp.di

import com.android.doctorapp.di.core.AuthScopes
import com.android.doctorapp.di.core.CoreComponent
import com.android.doctorapp.ui.home.HomeFragment
import com.android.doctorapp.ui.authentication.login.LoginFragment
import com.android.doctorapp.ui.profile.ProfileFragment
import com.android.doctorapp.ui.authentication.register.RegisterFragment
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
}