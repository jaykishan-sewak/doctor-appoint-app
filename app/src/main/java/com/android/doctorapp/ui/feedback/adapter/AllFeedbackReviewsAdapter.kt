package com.android.doctorapp.ui.feedback.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.AllFeedbackRowLayoutBinding
import com.android.doctorapp.repository.models.FeedbackResponseModel

class AllFeedbackReviewsAdapter(
    private var listData: List<FeedbackResponseModel>?
) : RecyclerView.Adapter<AllFeedbackReviewsAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: AllFeedbackRowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(
            item: FeedbackResponseModel?,
            position: Int,
            imageUri: Uri?
        ) {
            view.apply {
                userData = item
                index = position
                imgUri = imageUri
            }
        }
    }

    fun filterList(filterList: List<FeedbackResponseModel>) {
        listData = filterList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: AllFeedbackRowLayoutBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.all_feedback_row_layout,
                parent,
                false
            )
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (listData?.isNotEmpty() == true) listData!!.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = listData?.get(position)
        val imgUri = objects?.userDetails?.images?.toUri()
        holder.bind(objects, position, imgUri)

    }
}