package com.android.doctorapp.ui.doctordashboard.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.ClinicImagesrowLayoutBinding

class ClinicImgAdapter(
    private var clinicImagesList: List<String>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ClinicImgAdapter.ItemViewHolder>() {

    var isUserViewClinicImg = false

    class ItemViewHolder(val view: ClinicImagesrowLayoutBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(listener: OnItemClickListener, position: Int, imageUrl: Uri?) {
            view.apply {
                index = position
                onItemClickListener = listener
                clinicImgUri = imageUrl
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: ClinicImagesrowLayoutBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.clinic_imagesrow_layout,
            parent,
            false
        )
        itemView.ivDeleteClinicImg.visibility = if (isUserViewClinicImg) View.GONE else View.VISIBLE
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (clinicImagesList.isNotEmpty()) clinicImagesList.size else 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val imageUrl = clinicImagesList[position].toUri()
        holder.bind(listener, position, imageUrl)

    }

    fun updateClinicImgList(filterList: ArrayList<String>, isUserViewImages: Boolean) {
        isUserViewClinicImg = isUserViewImages
        clinicImagesList = filterList
        notifyDataSetChanged()
    }


    interface OnItemClickListener {
        fun onItemClick(imageUrl: Uri, position: Int)
        fun onItemDelete(imageUrl: Uri, position: Int)
    }

}
