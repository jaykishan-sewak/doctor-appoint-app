package com.android.doctorapp.ui.feedback.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FeedbackDoctorRowLayoutBinding
import com.android.doctorapp.repository.models.UserDataResponseModel

class DoctorListAdapter(
    private val context: Context,
    private var listData: List<UserDataResponseModel>?,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<DoctorListAdapter.ItemViewHolder>() {
    class ItemViewHolder(val view: FeedbackDoctorRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(
            item: UserDataResponseModel?,
            listener: OnItemClickListener,
            position: Int,
            imageUri: Uri?
        ) {
            view.apply {
                userData = item
                index = position
                onItemClickListener = listener
                imgUri = imageUri
            }
        }
    }

    fun updateListData(list: List<UserDataResponseModel>?) {
        listData = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: FeedbackDoctorRowLayoutBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.feedback_doctor_row_layout,
                parent,
                false
            )
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = listData?.get(position)
        val imgUri = objects?.images?.toUri()
        holder.bind(objects, listener, position, imgUri)
    }

    override fun getItemCount(): Int {
        return if (listData?.isNotEmpty() == true) listData!!.size else 0
    }

    interface OnItemClickListener {
        fun onItemClick(item: UserDataResponseModel, position: Int)

        fun onEditClick(item: UserDataResponseModel, position: Int)

        fun onDeleteClick(item: UserDataResponseModel, position: Int)
    }
}