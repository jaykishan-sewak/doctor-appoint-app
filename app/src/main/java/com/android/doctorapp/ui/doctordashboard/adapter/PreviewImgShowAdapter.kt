package com.android.doctorapp.ui.doctordashboard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentZoomPreviewClinicImgBinding
import com.android.doctorapp.util.zoomImage.ZoomImageView

class PreviewImgShowAdapter(
    private val context: Context,
    private val scaleChangeListener: ZoomImageView.OnScaleChangeListener
) : RecyclerView.Adapter<PreviewImgShowAdapter.ItemViewHolder>() {

    private var imageList: List<String> = emptyList()
    private var listener: OnItemClickListener? = null

    inner class ItemViewHolder(val binding: FragmentZoomPreviewClinicImgBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.apply {
                imgUri = imageList[position].toUri()
                executePendingBindings()
                zoomImageView.getIsImageZoom(scaleChangeListener)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: FragmentZoomPreviewClinicImgBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_zoom_preview_clinic_img,
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(position)
    }

    fun setOnItemClickListener(click: OnItemClickListener) {
        listener = click
    }

    fun addAll(imageList: List<String>) {
        this.imageList = imageList
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(media: String, position: Int)
        fun onItemDelete(media: String, position: Int)
    }
}
