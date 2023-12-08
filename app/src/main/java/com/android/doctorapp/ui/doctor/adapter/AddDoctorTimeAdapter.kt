package com.android.doctorapp.ui.doctor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.AddTimeShiftRowLayoutBinding
import com.android.doctorapp.repository.models.AddShiftTimeModel
import com.android.doctorapp.ui.doctor.AddDoctorViewModel

class AddDoctorTimeAdapter(
    private var context: Context,
    private var viewModel: AddDoctorViewModel,
    private var shiftTimeList: ArrayList<AddShiftTimeModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AddDoctorTimeAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: AddTimeShiftRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
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
        if (viewModel.isDarkThemeEnable.value == true) {
            holder.view.btnStartTime.background =
                AppCompatResources.getDrawable(context, R.drawable.white_border_drawable)
            holder.view.btnEndTime.background =
                AppCompatResources.getDrawable(context, R.drawable.white_border_drawable)
            holder.view.addTimeDivider.setBackgroundColor(context.getColor(R.color.white))
            holder.view.imageShiftDelete.background =
                AppCompatResources.getDrawable(context, R.drawable.baseline_delete_white_outline)

        } else {
            holder.view.btnStartTime.background =
                AppCompatResources.getDrawable(context, R.drawable.blue_border_drawable)
            holder.view.btnEndTime.background =
                AppCompatResources.getDrawable(context, R.drawable.blue_border_drawable)
            holder.view.addTimeDivider.setBackgroundColor(context.getColor(R.color.white))
            holder.view.imageShiftDelete.background =
                AppCompatResources.getDrawable(context, R.drawable.baseline_delete_outline_24)
        }
        val objects = shiftTimeList[position]
        holder.bind(objects, listener, position)

    }

    interface OnItemClickListener {
        fun startTimeClick(addShiftTimeModel: AddShiftTimeModel, position: Int)

        fun endTimeClick(addShiftTimeModel: AddShiftTimeModel, position: Int)

        fun removeShiftClick(addShiftTimeModel: AddShiftTimeModel, position: Int)
    }

    fun updateShiftTimeList(filterList: ArrayList<AddShiftTimeModel>) {
        shiftTimeList = filterList
        notifyItemRangeChanged(0, shiftTimeList.size)
    }

}