package com.android.infusiontemplate.di

import com.android.infusiontemplate.di.core.AuthScopes
import com.android.infusiontemplate.di.core.CoreComponent
import com.android.infusiontemplate.ui.home.HomeFragment
import com.android.infusiontemplate.ui.authentication.login.LoginFragment
import com.android.infusiontemplate.ui.profile.ProfileFragment
import com.android.infusiontemplate.ui.authentication.register.RegisterFragment
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