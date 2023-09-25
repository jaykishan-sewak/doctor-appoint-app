package com.android.doctorapp.ui.doctor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.WeekOffDayRowLayoutBinding
import com.android.doctorapp.repository.models.WeekOffModel

class WeekOffDayAdapter(
    private var daysList: ArrayList<WeekOffModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<WeekOffDayAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: WeekOffDayRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: WeekOffModel, listener: OnItemClickListener, position: Int) {
            view.apply {
                viewModel = item
                index = position
                onItemClickListener = listener
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: WeekOffDayRowLayoutBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.week_off_day_row_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (daysList.isNotEmpty()) daysList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = daysList[position]
        holder.bind(objects, listener, position)
    }

    interface OnItemClickListener {
        fun onItemClick(item: WeekOffModel, position: Int)
    }

    fun updateWeekOffList(filterList: ArrayList<WeekOffModel>) {
        daysList = filterList
        notifyItemRangeChanged(0, daysList.size)
    }


}