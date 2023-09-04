package com.android.doctorapp.ui.appointment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.AppointmentDateRowLayoutBinding
import com.android.doctorapp.repository.models.AppointmentDateModel

class AppointmentAdapter(
    private var appointmentList: ArrayList<AppointmentDateModel>
): RecyclerView.Adapter<AppointmentAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: AppointmentDateRowLayoutBinding): RecyclerView.ViewHolder(view.root) {
        fun bind(item: AppointmentDateModel, position: Int) {
            view.apply {
                appointmentViewModel = item
                index = position
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: AppointmentDateRowLayoutBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.appointment_date_row_layout, parent, false)
        return AppointmentAdapter.ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (appointmentList.isNotEmpty()) appointmentList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = appointmentList[position]
        holder.bind(objects, position)
    }
}