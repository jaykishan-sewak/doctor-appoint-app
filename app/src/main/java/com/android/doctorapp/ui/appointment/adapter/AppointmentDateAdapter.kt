package com.android.doctorapp.ui.appointment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.DateRowLayoutBinding
import com.android.doctorapp.repository.models.DateSlotModel

class AppointmentDateAdapter(
    private val daysList: ArrayList<DateSlotModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AppointmentDateAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: DateRowLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(item: DateSlotModel, listener: OnItemClickListener, position: Int) {
            view.apply {
                dateSlotModel = item
                index = position
                onItemClickListener = listener
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: DateRowLayoutBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.date_row_layout, parent, false)
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
        fun onItemClick(item: DateSlotModel, position: Int)
    }

}