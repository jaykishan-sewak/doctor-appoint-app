package com.android.doctorapp.ui.doctor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.AddDoctorHolidayRowLayoutBinding
import com.android.doctorapp.repository.models.HolidayModel

class AddDoctorHolidayAdapter(
    private var addHolidayList: List<HolidayModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AddDoctorHolidayAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: AddDoctorHolidayRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: HolidayModel, listener: OnItemClickListener, position: Int) {
            view.apply {
                holidayModel = item
                index = position
                onItemClickListener = listener
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
        holder.bind(objects, listener, position)

    }

    fun filterList(filterList: ArrayList<HolidayModel>) {
        addHolidayList = filterList
        notifyItemRangeChanged(0, addHolidayList.size)
    }


    interface OnItemClickListener {
        fun onItemDelete(item: HolidayModel, position: Int)
    }

}