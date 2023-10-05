package com.android.doctorapp.ui.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.UserHistoryRowLayoutBinding
import com.android.doctorapp.repository.models.AppointmentModel

class HistoryAdapter(
    private var requestAppointmentList: List<AppointmentModel>,
) : RecyclerView.Adapter<HistoryAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: UserHistoryRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: AppointmentModel, position: Int) {
            view.apply {
                historyData = item
                index = position
            }
        }
    }

    fun filterList(filterList: List<AppointmentModel>) {
        requestAppointmentList = filterList
//        notifyItemRangeChanged(0, userList.size)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: UserHistoryRowLayoutBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.user_history_row_layout,
                parent,
                false
            )
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = requestAppointmentList[position]
        holder.bind(objects, position)
    }

    override fun getItemCount(): Int {
        return if (requestAppointmentList.isNotEmpty()) requestAppointmentList.size else 0
    }


}