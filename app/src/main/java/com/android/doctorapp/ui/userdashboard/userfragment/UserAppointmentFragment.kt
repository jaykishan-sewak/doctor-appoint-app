package com.android.doctorapp.ui.userdashboard.userfragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.android.doctorapp.R


class UserAppointmentFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_appointment, container, false)
    }

//    fun BookAppointment() {
//        findNavController().navigate(R.id.action_user_appointment_fragment_to_book_appointment_fragment)
//    }

}