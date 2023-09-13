package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.content.Context
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AppointmentRepository
import javax.inject.Inject

class SelectedDateAppointmentsViewModel  @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val appointmentRepository: AppointmentRepository,
    private val context: Context,
) : BaseViewModel() {

}