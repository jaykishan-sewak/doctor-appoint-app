package com.android.doctorapp.ui.doctor.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.AddTimeShiftRowLayoutBinding
import com.android.doctorapp.repository.models.AddShiftTimeModel

class AddDoctorTimeAdapter(
    private val shiftTimeList: ArrayList<AddShiftTimeModel>,
//    private val shiftTimeSize: Int,
    private val listener: OnItemClickListener
): RecyclerView.Adapter<AddDoctorTimeAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: AddTimeShiftRowLayoutBinding): RecyclerView.ViewHolder(view.root) {
        fun bind(item: AddShiftTimeModel, listener: OnItemClickListener, position: Int) {
            view.apply {
                addShitTimeModel = item
                index = position
                onItemClickListener = listener
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: AddTimeShiftRowLayoutBinding = DataBindingUtil.inflate(
            layoutInflater, R.layout.add_time_shift_row_layout,
            parent,
            false
        )
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (shiftTimeList.isNotEmpty()) shiftTimeList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = shiftTimeList[position]
        holder.bind(objects, listener, position)

    }

    interface OnItemClickListener {
        fun startTimeClick(addShiftTimeModel: AddShiftTimeModel, position: Int)

        fun endTimeClick(addShiftTimeModel: AddShiftTimeModel, position: Int)

        fun removeShiftClick(addShiftTimeModel: AddShiftTimeModel, position: Int)
    }

}