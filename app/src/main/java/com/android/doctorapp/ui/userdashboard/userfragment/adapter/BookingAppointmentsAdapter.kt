package com.android.doctorapp.ui.userdashboard.userfragment.adapter

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.UserBookingAppointmentsRowLayoutBinding
import com.android.doctorapp.repository.models.AppointmentModel

class BookingAppointmentsAdapter(
    private var bookingAppointmentList: List<AppointmentModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<BookingAppointmentsAdapter.ItemViewHolder>() {

    private var bookAppointmentList: MutableList<AppointmentModel> = mutableListOf()

    class ItemViewHolder(val view: UserBookingAppointmentsRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(
            item: AppointmentModel,
            listener: OnItemClickListener,
            position: Int,
            imageUrl: Uri?
        ) {
            Log.d("Item", item.doctorDetails?.name ?: "")
            Log.d("Item", "${item.bookingDateTime}")
            Log.d("Item", item.status)
            Log.d("Item", "$imageUrl")
            view.apply {
                bookingData = item
                index = position
                onItemClickListener = listener
                imgUrl = imageUrl
            }
        }
    }

    private fun updatedList(updateList: List<AppointmentModel>) {
        bookAppointmentList.addAll(updateList)
        bookingAppointmentList = bookAppointmentList.sortedByDescending {
            it.bookingDateTime
        }
        Log.d(TAG, "updatedList: ${bookingAppointmentList.size}")
        notifyDataSetChanged()
    }
    fun filterList(filterList: List<AppointmentModel>) {
        Log.d(TAG, "filterList: ${filterList.size}")
        if (bookingAppointmentList.isEmpty()) {
            bookAppointmentList = filterList.sortedByDescending {
                it.bookingDateTime
            }.toMutableList()
            bookingAppointmentList = bookAppointmentList
            notifyItemRangeChanged(0, filterList.size)
        } else
            updatedList(filterList)
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
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (bookingAppointmentList.isNotEmpty()) bookingAppointmentList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = bookingAppointmentList[position]
        val imageUrl = objects.doctorDetails?.images?.toUri()
        holder.bind(objects, listener, position, imageUrl)
    }

    interface OnItemClickListener {
        fun onItemClick(item: AppointmentModel, position: Int)
        fun onClick(contact: String)
    }


//     class AppointmentDiffCallback : DiffUtil.ItemCallback<AppointmentModel>() {
//        override fun areItemsTheSame(oldItem: AppointmentModel, newItem: AppointmentModel): Boolean {
//            // Check if the unique identifier of the items is the same
//            return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: AppointmentModel, newItem: AppointmentModel): Boolean {
//            // Check if the contents of the items are the same
//            return oldItem == newItem
//        }
//    }

}