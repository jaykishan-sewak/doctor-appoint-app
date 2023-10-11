package com.android.doctorapp.ui.profile.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
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
        fun bind(item: AppointmentModel, position: Int, imageUrl: Uri?) {
            view.apply {
                historyData = item
                index = position
                imgUrl = imageUrl
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
        val imageUrl = objects.doctorDetails?.images?.toUri()
        holder.bind(objects, position, imageUrl)
    }

    override fun getItemCount(): Int {
        return if (requestAppointmentList.isNotEmpty()) requestAppointmentList.size else 0
    }


}