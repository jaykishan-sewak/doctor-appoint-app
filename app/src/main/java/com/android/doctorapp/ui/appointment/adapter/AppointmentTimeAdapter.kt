package com.android.doctorapp.ui.appointment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.AppointmentTimeRowLayoutBinding
import com.android.doctorapp.repository.models.AppointmentTimeModel

class AppointmentTimeAdapter(
        private var appointmentTimeList: ArrayList<AppointmentTimeModel>
): RecyclerView.Adapter<AppointmentTimeAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: AppointmentTimeRowLayoutBinding): RecyclerView.ViewHolder(view.root) {
        fun bind(item: AppointmentTimeModel, position: Int) {
            view.apply {
                appointmentViewModel = item
                index = position
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: AppointmentTimeRowLayoutBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.appointment_time_row_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
         return if (appointmentTimeList.isNotEmpty()) appointmentTimeList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = appointmentTimeList[position]
        holder.bind(objects, position)
    }
}