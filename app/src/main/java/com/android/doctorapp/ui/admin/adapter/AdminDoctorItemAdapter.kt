package com.android.doctorapp.ui.admin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.DoctorRowLayoutBinding
import com.android.doctorapp.repository.models.ImageUriAndGender
import com.android.doctorapp.repository.models.UserDataResponseModel

class AdminDoctorItemAdapter(
    private val context: Context,
    private var userList: List<UserDataResponseModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AdminDoctorItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(val view: DoctorRowLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(
            item: UserDataResponseModel,
            listener: OnItemClickListener,
            position: Int,
            imageUriAndGender: ImageUriAndGender
        ) {
            view.apply {
                userData = item
                index = position
                onItemClickListener = listener
                imageGender = imageUriAndGender
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
        val imageAndGender = ImageUriAndGender(
            if (!objects.images.isNullOrEmpty()) objects.images?.toUri() else null,
            objects.gender
        )
        holder.bind(objects, listener, position, imageAndGender)
    }

    override fun getItemCount(): Int {
        return if (userList.isNotEmpty()) userList.size else 0
    }

    interface OnItemClickListener {
        fun onItemClick(item: UserDataResponseModel, position: Int)

    }
}