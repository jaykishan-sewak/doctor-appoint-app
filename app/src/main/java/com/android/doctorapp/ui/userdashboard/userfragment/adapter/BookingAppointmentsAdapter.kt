package com.android.doctorapp.ui.userdashboard.userfragment.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.AppointmentsRowLoadingBinding
import com.android.doctorapp.databinding.UserBookingAppointmentsRowLayoutBinding
import com.android.doctorapp.repository.models.AppointmentModel

class BookingAppointmentsAdapter(
    private var bookingAppointmentList: MutableList<AppointmentModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_LOADING = 0
    }

    private var isLoaderVisible = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_ITEM) {
            val itemView: UserBookingAppointmentsRowLayoutBinding =
                DataBindingUtil.inflate(
                    inflater,
                    R.layout.user_booking_appointments_row_layout,
                    parent,
                    false
                )
            BookAppointemntsItemViewHolder(itemView)
        } else {
            val itemView: AppointmentsRowLoadingBinding =
                DataBindingUtil.inflate(
                    inflater,
                    R.layout.appointments_row_loading,
                    parent,
                    false
                )
            BookingLoadingItemViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = bookingAppointmentList[position]
        when (holder) {
            is BookAppointemntsItemViewHolder -> {
                val userObject = item
                val imageUrl = userObject.doctorDetails?.images?.toUri()
                holder.bind(userObject, listener, position, imageUrl)

            }

            is BookingLoadingItemViewHolder -> {
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoaderVisible) {
            if (position == bookingAppointmentList.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return if (bookingAppointmentList.isNotEmpty()) bookingAppointmentList.size else 0
    }

    fun addItems(newPostItems: List<AppointmentModel>) {
        bookingAppointmentList.addAll(newPostItems)
        notifyDataSetChanged()
    }

    fun addLoading() {
        isLoaderVisible = true
        bookingAppointmentList.add(AppointmentModel())
        notifyItemInserted(bookingAppointmentList.size - 1)
    }

    fun removeLoading() {
        isLoaderVisible = false
        val position = bookingAppointmentList.size - 1
        val item = getItem(position)
        if (item != null) {
            bookingAppointmentList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        bookingAppointmentList.clear()
        notifyDataSetChanged()
    }

    fun getItem(position: Int): AppointmentModel? {
        return if (position in 0 until bookingAppointmentList.size) {
            bookingAppointmentList[position]
        } else {
            null
        }
    }

    inner class BookAppointemntsItemViewHolder(val binding: UserBookingAppointmentsRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: AppointmentModel,
            listener: OnItemClickListener,
            position: Int,
            imageUrl: Uri?
        ) {
            binding.apply {
                bookingData = item
                index = position
                onItemClickListener = listener
                imgUrl = imageUrl
            }
        }
    }

    inner class BookingLoadingItemViewHolder(val binding: AppointmentsRowLoadingBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    interface OnItemClickListener {
        fun onItemClick(item: AppointmentModel, position: Int)
        fun onClick(contact: String)
    }


}