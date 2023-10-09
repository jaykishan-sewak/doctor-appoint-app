package com.android.doctorapp.ui.doctordashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.DoctorAppointmentsHeaderLayoutBinding
import com.android.doctorapp.databinding.DoctorAppointmentsItemLayoutBinding
import com.android.doctorapp.repository.models.AppointmentModel
import com.android.doctorapp.repository.models.Header


class PatientListAdapter(
    private var dataset: List<Any>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_HEADER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataset[position]) {
            is Header -> VIEW_TYPE_HEADER
            is AppointmentModel -> VIEW_TYPE_USER
            else -> -1 // Invalid view type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_USER -> {
                val itemView: DoctorAppointmentsItemLayoutBinding =
                    DataBindingUtil.inflate(
                        inflater,
                        R.layout.doctor_appointments_item_layout,
                        parent,
                        false
                    )
                UserItemViewHolder(itemView)
            }

            VIEW_TYPE_HEADER -> {
                val itemView: DoctorAppointmentsHeaderLayoutBinding =
                    DataBindingUtil.inflate(
                        inflater,
                        R.layout.doctor_appointments_header_layout,
                        parent,
                        false
                    )
                HeaderViewHolder(itemView)
            }

            else -> {
                val itemView: DoctorAppointmentsHeaderLayoutBinding =
                    DataBindingUtil.inflate(
                        inflater,
                        R.layout.doctor_appointments_header_layout,
                        parent,
                        false
                    )
                HeaderViewHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dataset[position]
        when (holder) {
            is UserItemViewHolder -> {
                val userObject = item as AppointmentModel
                holder.bind(userObject, listener)

            }

            is HeaderViewHolder -> {
                val header = item as Header
                holder.bind(header, listener, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (dataset.isNotEmpty()) dataset.size else 0
    }

    fun filterList(filterList: List<Any>) {
        dataset = filterList
        notifyItemRangeChanged(0, dataset.size)
    }

    interface OnItemClickListener {
        fun onItemClick(item: Header, position: Int)
        fun onClick(contact: String)

    }

}

private class UserItemViewHolder(private val binding: DoctorAppointmentsItemLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(user: AppointmentModel, listener: PatientListAdapter.OnItemClickListener) {
        binding.userData = user
        binding.onClickListener = listener
    }
}

private class HeaderViewHolder(private val binding: DoctorAppointmentsHeaderLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(header: Header, listener: PatientListAdapter.OnItemClickListener, position: Int) {
        binding.apply {
            header1 = header
            index = position
            onItemClickListener = listener
        }

    }

}

