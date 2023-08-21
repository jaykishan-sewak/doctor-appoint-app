package com.android.doctorapp.ui.admin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.DoctorRowLayoutBinding
import com.android.doctorapp.repository.models.UserDataResponseModel

class AdminDoctorItemAdapter(
    private val context: Context,
    private var userList: List<UserDataResponseModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AdminDoctorItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(val view: DoctorRowLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(item: UserDataResponseModel, listener: OnItemClickListener, position: Int) {
            view.apply {
                userData = item
                index = position
                onItemClickListener = listener
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: DoctorRowLayoutBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.doctor_row_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = userList[position]
        holder.bind(objects, listener, position)
    }

    override fun getItemCount(): Int {
        return if (userList.isNotEmpty()) userList.size else 0
    }

    interface OnItemClickListener {
        fun onItemClick(item: UserDataResponseModel, position: Int)
    }
}