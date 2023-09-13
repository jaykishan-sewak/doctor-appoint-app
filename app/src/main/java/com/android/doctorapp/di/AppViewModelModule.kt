package com.android.doctorapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.doctorapp.ui.admin.AdminDashboardViewModel
import com.android.doctorapp.ui.appointment.AppointmentViewModel
import com.android.doctorapp.ui.authentication.login.LoginViewModel
import com.android.doctorapp.ui.authentication.register.RegisterViewModel
import com.android.doctorapp.ui.doctor.AddDoctorViewModel
import com.android.doctorapp.ui.doctordashboard.doctorfragment.AppointmentDoctorViewModel
import com.android.doctorapp.ui.doctordashboard.doctorfragment.RequestDoctorViewModel
import com.android.doctorapp.ui.doctordashboard.doctorfragment.SelectedDateAppointmentsViewModel
import com.android.doctorapp.ui.home.HomeViewModel
import com.android.doctorapp.ui.otp.OtpVerificationViewModel
import com.android.doctorapp.ui.profile.ProfileViewModel
import com.android.doctorapp.ui.userdashboard.userfragment.UserAppointmentViewModel
import com.android.doctorapp.ui.userdashboard.userfragment.UserRequestViewModel
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

    @Binds
    @IntoMap
    @ViewModelMapKey(OtpVerificationViewModel::class)
    abstract fun bindOtpVerificationViewModel(viewModel: OtpVerificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(AdminDashboardViewModel::class)
    abstract fun bindAdminDashboardViewModel(viewModel: AdminDashboardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(AppointmentViewModel::class)
    abstract fun bindAppointmentViewModel(viewModel: AppointmentViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelMapKey(UserAppointmentViewModel::class)
    abstract fun bindUserAppointmentViewModel(viewModel: UserAppointmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(UserRequestViewModel::class)
    abstract fun bindUserRequestViewModel(viewModel: UserRequestViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(AppointmentDoctorViewModel::class)
    abstract fun bindAppointmentDoctorViewModel(viewModel: AppointmentDoctorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(RequestDoctorViewModel::class)
    abstract fun bindRequestDoctorViewModel(viewModel: RequestDoctorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(SelectedDateAppointmentsViewModel::class)
    abstract fun bindSelectedDateAppointmentsViewModel(viewModel: SelectedDateAppointmentsViewModel): ViewModel

}
