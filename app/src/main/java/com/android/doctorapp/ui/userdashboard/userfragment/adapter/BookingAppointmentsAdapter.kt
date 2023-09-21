package com.android.doctorapp.ui.userdashboard.userfragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.UserBookingAppointmentsRowLayoutBinding
import com.android.doctorapp.repository.models.AppointmentModel

class BookingAppointmentsAdapter(
    private var bookingAppointmentList: List<AppointmentModel>,
    private val listener: BookingAppointmentsAdapter.OnItemClickListener
) : RecyclerView.Adapter<BookingAppointmentsAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: UserBookingAppointmentsRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(
            item: AppointmentModel,
            listener: BookingAppointmentsAdapter.OnItemClickListener,
            position: Int
        ) {
            view.apply {
                bookingData = item
                index = position
                onItemClickListener = listener
            }
        }
    }

    fun filterList(filterList: List<AppointmentModel>) {
        bookingAppointmentList = filterList
//        notifyItemRangeChanged(0, userList.size)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: UserBookingAppointmentsRowLayoutBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.user_booking_appointments_row_layout,
                parent,
                false
            )
        return BookingAppointmentsAdapter.ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (bookingAppointmentList.isNotEmpty()) bookingAppointmentList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = bookingAppointmentList[position]
        holder.bind(objects, listener, position)
    }

    interface OnItemClickListener {
        fun onItemClick(item: AppointmentModel, position: Int)
        fun onClick(contact: String)
    }
}