package com.android.doctorapp.ui.doctordashboard.doctorfragment

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.doctorapp.R
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.base.BaseViewModel
import com.android.doctorapp.repository.AppointmentRepository
import com.android.doctorapp.repository.models.ApiErrorResponse
import com.android.doctorapp.repository.models.ApiNoNetworkResponse
import com.android.doctorapp.repository.models.ApiSuccessResponse
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.Header
import com.android.doctorapp.util.extension.isNetworkAvailable
import com.android.doctorapp.util.extension.toast
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

class AppointmentDoctorViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val appointmentRepository: AppointmentRepository,
    private val context: Context,
) : BaseViewModel() {

    val appointmentList = MutableLiveData<List<Any>>()
    val mainList = mutableListOf<Any>()
    val appointmentList2 = MutableLiveData<List<AppointmentModel>>()
    val appointmentList3 = MutableLiveData<List<AppointmentModel>>()



    init {
//        staticData()
        getAppointmentList()
//        Log.d(TAG, "${appointmentList.value}: ")
    }

    private fun addData() {
        appointmentList3.value?.filterIndexed { index, appointmentModel ->
            if ((index + 1) % 2 != 0) {
                mainList.add(
                    Header(
                        convertDate(
                            appointmentModel.bookingDateTime.toString()
                        )
                    )
                )
                mainList.add(
                    AppointmentModel(
                        appointmentModel.bookingDateTime, appointmentModel.isOnline,
                        appointmentModel.reason, appointmentModel.status, appointmentModel.userId,
                        appointmentModel.name, appointmentModel.age
                    )
                )
            } else {
                mainList.add(
                    AppointmentModel(
                        appointmentModel.bookingDateTime, appointmentModel.isOnline,
                        appointmentModel.reason, appointmentModel.status, appointmentModel.userId,
                        appointmentModel.name, appointmentModel.age
                    )
                )
            }

        }
        appointmentList.value = mainList

    }

//    private fun staticData() {
//
//        val listUser = mutableListOf<User>()
//        listUser.add(User("Vikas", 35))
//        listUser.add(User("Vikas1", 36))
//        listUser.add(User("Vikas2", 37))
//        listUser.add(User("Vikas3", 38))
//
//        val listUser2 = mutableListOf<User>()
//        listUser2.add(User("Vika", 35))
//        listUser2.add(User("Viks1", 36))
//        listUser2.add(User("Vias2", 37))
//        listUser2.add(User("Vkas3", 38))
//
//        val listUser3 = mutableListOf<User>()
//        listUser3.add(User("Vikas3", 35))
//        listUser3.add(User("Vikas35", 36))
//        listUser3.add(User("Vikas46", 37))
//        listUser3.add(User("Vikas78", 38))
//
//        val mainList = mutableListOf<Any>()
//        mainList.add(Header(convertDate(Date("24/01/1464").toString())))
//        mainList.add(User("Vikas", 35))
//        mainList.add(User("Vikas37", 40))
//        mainList.add(Header(convertDate(Date("05/04/2023").toString())))
//        mainList.add(User("Vikas6", 36))
//        mainList.add(User("Vikas", 35))
//        mainList.add(User("Vikas37", 40))
//        mainList.add(Header(convertDate(Date("06/09/2010").toString())))
//        mainList.add(User("Vikas37", 37))
//        Log.d(TAG, "staticData: $mainList")
//        appointmentList.value = mainList
//    }

    fun convertDate(inputDateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
            val outputFormat = SimpleDateFormat("dd-MM-yyyy")

            val date = inputFormat.parse(inputDateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            e.fillInStackTrace()
            ""
        }
    }

    private fun getAppointmentList() {
        viewModelScope.launch {
            if (context.isNetworkAvailable()) {
                setShowProgress(true)
                when (val response = appointmentRepository.getAppointmentsList(fireStore)) {
                    is ApiSuccessResponse -> {
                        setShowProgress(false)
                        if (response.body.isNotEmpty()) {
                            Log.d(TAG, "getAppointmentList: ${(response.body)}")
                            appointmentList2.value = response.body!!
                            appointmentList3.value = appointmentList2.value!!.sortedBy {
                                it.bookingDateTime
                            }
                            appointmentList3.value!!.distinct()
                            Log.d("grouped Data: --- ", Gson().toJson(appointmentList3.value))
                            addData()
                        }
                    }

                    is ApiErrorResponse -> {
                        setShowProgress(false)
                    }

                    is ApiNoNetworkResponse -> {
                        setShowProgress(false)
                    }

                    else -> {
                        setShowProgress(false)
                    }
                }
            } else {
                context.toast(resourceProvider.getString(R.string.check_internet_connection))
            }
        }
    }


}