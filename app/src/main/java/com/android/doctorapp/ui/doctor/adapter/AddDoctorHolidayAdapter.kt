package com.android.doctorapp.ui.doctor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.AddDoctorHolidayRowLayoutBinding
import com.android.doctorapp.repository.models.HolidayModel

class AddDoctorHolidayAdapter(
    private val addHolidayList: ArrayList<HolidayModel>
) : RecyclerView.Adapter<AddDoctorHolidayAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: AddDoctorHolidayRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: HolidayModel, position: Int) {
            view.apply {
                holidayModel = item
                index = position
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: AddDoctorHolidayRowLayoutBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.add_doctor_holiday_row_layout,
            parent,
            false
        )
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (addHolidayList.isNotEmpty()) addHolidayList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = addHolidayList[position]
        holder.bind(objects, position)

    }

}