package com.android.doctorapp.ui.appointment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.AppointmentTimeRowLayoutBinding
import com.android.doctorapp.repository.models.AddShiftTimeModel

class AppointmentTimeAdapter(
    private val timeList: ArrayList<AddShiftTimeModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AppointmentTimeAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: AppointmentTimeRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: AddShiftTimeModel, listener: OnItemClickListener, position: Int) {
            view.apply {
                timeModel = item
                index = position
                onItemClickListener = listener
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: AppointmentTimeRowLayoutBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.appointment_time_row_layout,
                parent,
                false
            )
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (timeList.isNotEmpty()) timeList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = timeList[position]
        holder.bind(objects, listener, position)
    }

    interface OnItemClickListener {
        fun onItemClick(item: AddShiftTimeModel, position: Int)
    }

}