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


class PatientListAdapter(private var dataset: List<Any>) :
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
                holder.bind(userObject)

            }

            is HeaderViewHolder -> {
                val header = item as Header
                holder.bind(header, position)
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

}

private class UserItemViewHolder(private val binding: DoctorAppointmentsItemLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(user: AppointmentModel) {
        binding.userData = user
    }
}

private class HeaderViewHolder(private val binding: DoctorAppointmentsHeaderLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(header: Header, position: Int) {
        if (position == 0) {
            binding.headerDivider.visibility = View.GONE
        }
        binding.header = header
    }
}
