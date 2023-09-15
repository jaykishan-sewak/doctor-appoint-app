package com.android.doctorapp.ui.doctordashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.SelectedDateAppointmentsRowLayoutBinding
import com.android.doctorapp.repository.models.AppointmentModel

class SelectedDateAdapter(
    private var userList: List<AppointmentModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<SelectedDateAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: SelectedDateAppointmentsRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: AppointmentModel, listener: OnItemClickListener, position: Int) {
            view.apply {
                userData = item
                index = position
                onItemClickListener = listener
            }
        }
    }
    fun filterList(filterList: List<AppointmentModel>) {
        userList = filterList
//        notifyItemRangeChanged(0, userList.size)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: SelectedDateAppointmentsRowLayoutBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.selected_date_appointments_row_layout,
                parent,
                false
            )
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (userList.isNotEmpty()) userList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = userList[position]
        holder.bind(objects, listener, position)
    }

    interface OnItemClickListener {
        fun onItemClick(item: AppointmentModel, position: Int)
        fun onClick(contact: String)

    }
}