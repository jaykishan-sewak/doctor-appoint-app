package com.android.doctorapp.ui.doctordashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.RequestAppointmentsRowLayoutBinding
import com.android.doctorapp.repository.models.AppointmentModel

class RequestAppointmentsAdapter(
    private var requestAppointmentList: List<AppointmentModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RequestAppointmentsAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: RequestAppointmentsRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: AppointmentModel, listener: OnItemClickListener, position: Int) {
            view.apply {
                appointmentsData = item
                index = position
                onItemClickListener = listener
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
        val itemView: RequestAppointmentsRowLayoutBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.request_appointments_row_layout,
                parent,
                false
            )
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (requestAppointmentList.isNotEmpty()) requestAppointmentList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = requestAppointmentList[position]
        holder.bind(objects, listener, position)
    }

    interface OnItemClickListener {
        fun onItemClick(item: AppointmentModel, position: Int)
        fun onClick(contact: String)
    }
}