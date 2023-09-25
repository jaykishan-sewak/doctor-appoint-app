package com.android.doctorapp.ui.doctor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.AddTimeShiftRowLayoutBinding
import com.android.doctorapp.repository.models.AddShiftTimeModel

class AddDoctorTimeAdapterNew(
    private var shiftTimeList: List<Any>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AddDoctorTimeAdapterNew.ItemViewHolder>() {

    class ItemViewHolder(val view: AddTimeShiftRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: AddShiftTimeModel, position: Int) {
            view.apply {
                addShitTimeModel = item
                index = position
//                onItemClickListener = listener
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

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
//        val objects = shiftTimeList[position]
//        holder.bind(objects, position)
    }

    override fun getItemCount(): Int {
        return if (shiftTimeList.isNotEmpty()) shiftTimeList.size else 0
    }

    fun filterList(filterList: List<Any>) {
        shiftTimeList = filterList
        notifyItemRangeChanged(0, shiftTimeList.size)
    }

    interface OnItemClickListener {
        fun startTimeClick(addShiftTimeModel: AddShiftTimeModel, position: Int)

        fun endTimeClick(addShiftTimeModel: AddShiftTimeModel, position: Int)

        fun removeShiftClick(addShiftTimeModel: AddShiftTimeModel, position: Int)
    }
}