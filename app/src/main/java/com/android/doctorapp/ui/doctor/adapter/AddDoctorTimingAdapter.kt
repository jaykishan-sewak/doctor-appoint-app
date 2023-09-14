package com.android.doctorapp.ui.doctor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.AddDoctorTimingRowLayoutBinding
import com.android.doctorapp.repository.models.TimeSlotModel

class AddDoctorTimingAdapter(
    private val addTimeSlotList: ArrayList<TimeSlotModel>
) : RecyclerView.Adapter<AddDoctorTimingAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: AddDoctorTimingRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: TimeSlotModel, position: Int) {
            view.apply {
                timeModel = item
                index = position
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: AddDoctorTimingRowLayoutBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.add_doctor_timing_row_layout,
            parent,
            false
        )
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (addTimeSlotList.isNotEmpty()) addTimeSlotList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = addTimeSlotList[position]
        holder.bind(objects, position)

    }

}