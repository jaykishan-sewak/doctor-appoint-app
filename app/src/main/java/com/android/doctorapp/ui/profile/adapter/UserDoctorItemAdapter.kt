package com.android.doctorapp.ui.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.UserRawLayoutBinding
import com.android.doctorapp.repository.models.UserDataResponseModel

class UserDoctorItemAdapter(
    private var userList: List<UserDataResponseModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<UserDoctorItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: UserRawLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(item: UserDataResponseModel, listener: OnItemClickListener, position: Int) {
            view.apply {
                userData = item
                index = position
                onItemClickListener = listener
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: UserRawLayoutBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.user_raw_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (userList.isNotEmpty()) userList.size else 0
    }

    fun filterList(filterList: List<UserDataResponseModel>) {
        userList = filterList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = userList[position]
        holder.bind(objects, listener, position)
    }

    interface OnItemClickListener {
        fun onItemClick(item: UserDataResponseModel, position: Int)

        fun onItemDelete(item: UserDataResponseModel, position: Int)

        fun onItemEdit(item: UserDataResponseModel, position: Int)

    }
}