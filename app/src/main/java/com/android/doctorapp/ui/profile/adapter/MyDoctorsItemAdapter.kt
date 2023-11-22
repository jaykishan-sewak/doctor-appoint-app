package com.android.doctorapp.ui.profile.adapter


import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.databinding.MyDoctorsRowLayoutBinding
import com.android.doctorapp.repository.models.UserDataResponseModel

class MyDoctorsItemAdapter(
    private var myDoctorList: List<UserDataResponseModel?>?,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MyDoctorsItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: MyDoctorsRowLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(
            item: UserDataResponseModel?,
            listener: OnItemClickListener,
            position: Int,
            imageUri: Uri?
        ) {
            view.apply {
                doctorData = item
                index = position
                onItemClickListener = listener
                doctorImageUri = imageUri
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val itemView: MyDoctorsRowLayoutBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.my_doctors_row_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (myDoctorList?.isNotEmpty() == true) myDoctorList!!.size else 0
    }

    fun filterList(filterList: List<UserDataResponseModel?>?) {
        myDoctorList = filterList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val objects = myDoctorList?.get(position)
        val imageUri = objects?.images?.toUri()
        holder.bind(objects, listener, position, imageUri)
    }

    interface OnItemClickListener {
        fun onItemClick(item: UserDataResponseModel, position: Int)
    }
}