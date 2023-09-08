package com.android.doctorapp.di

import com.android.doctorapp.di.core.AuthScopes
import com.android.doctorapp.di.core.CoreComponent
import com.android.doctorapp.ui.admin.AdminDashboardFragment
import com.android.doctorapp.ui.authentication.login.LoginFragment
import com.android.doctorapp.ui.authentication.register.RegisterFragment
import com.android.doctorapp.ui.doctor.AddDoctorFragment
import com.android.doctorapp.ui.admin.DoctorDetailsFragment
import com.android.doctorapp.ui.profile.AddUserProfileFragment
import com.android.doctorapp.ui.doctor.UpdateDoctorProfileFragment
import com.android.doctorapp.ui.doctordashboard.doctorfragment.AppointmentDoctorFragment
import com.android.doctorapp.ui.doctordashboard.doctorfragment.RequestDoctorFragment
import com.android.doctorapp.ui.home.HomeFragment
import com.android.doctorapp.ui.otp.OtpVerificationFragment
import com.android.doctorapp.ui.profile.ProfileFragment
import com.android.doctorapp.ui.userdashboard.userfragment.UserAppointmentFragment
import com.android.doctorapp.ui.userdashboard.userfragment.UserRequestFragment
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
    fun inject(fragment: AdminDashboardFragment)
    fun inject(fragment: OtpVerificationFragment)
    fun inject(fragment: DoctorDetailsFragment)
    fun inject(fragment: UserAppointmentFragment)
    fun inject(fragment: UserRequestFragment)
    fun inject(fragment: AppointmentDoctorFragment)
    fun inject(fragment: RequestDoctorFragment)
}