package com.android.doctorapp.ui.userdashboard.userfragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.UserRawLayoutBinding
import com.android.doctorapp.repository.models.ImageUriAndGender
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
        val imageAndGender = ImageUriAndGender(
            if (!objects.images.isNullOrEmpty()) objects.images?.toUri() else null,
            objects.gender
        )

        if (objects.numberOfFeedbacks == 0)
            holder.view.tvNumberOfRatings.setTextColor(holder.itemView.context.getColor(R.color.light_grey))
        else
            holder.view.tvNumberOfRatings.setTextColor(holder.itemView.context.getColor(R.color.colorPrimary))
        holder.bind(objects, listener, position, imageAndGender)
    }

    interface OnItemClickListener {
        fun onItemClick(item: UserDataResponseModel, position: Int)
        fun onRatingClick(item: UserDataResponseModel, position: Int)
    }
}