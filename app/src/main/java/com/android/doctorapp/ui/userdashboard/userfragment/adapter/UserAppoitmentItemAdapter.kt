package com.android.doctorapp.ui.userdashboard.userfragment.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.UserRawLayoutBinding
import com.android.doctorapp.repository.models.UserDataResponseModel

class UserAppoitmentItemAdapter(
    private var userList: List<UserDataResponseModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<UserAppoitmentItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: UserRawLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(
            item: UserDataResponseModel,
            listener: OnItemClickListener,
            position: Int,
            imageUri: Uri
        ) {
            view.apply {
                userData = item
                index = position
                onItemClickListener = listener
                userImageUri = imageUri
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserAppoitmentItemAdapter.ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: UserRawLayoutBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.user_raw_layout, parent, false)
        return UserAppoitmentItemAdapter.ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (userList.isNotEmpty()) userList.size else 0
    }

    fun filterList(filterList: List<UserDataResponseModel>) {
        userList = filterList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: UserAppoitmentItemAdapter.ItemViewHolder, position: Int) {
        val objects = userList[position]
        val imageUri = objects.images?.toUri()
        holder.bind(objects, listener, position, imageUri!!)
    }

    interface OnItemClickListener {
        fun onItemClick(item: UserDataResponseModel, position: Int)

    }
}