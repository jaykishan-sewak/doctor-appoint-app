package com.android.doctorapp.ui.doctordashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import com.android.doctorapp.R
import com.android.doctorapp.repository.models.MyObject
import com.android.doctorapp.repository.models.RecyclerViewContainer
import com.android.doctorapp.util.constants.ConstantRowTypeEnum

class PatientListAdapter(private val dataset: MutableList<RecyclerViewContainer>) :
    RecyclerView.Adapter<PatientListAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val viewMap: MutableMap<Int, View> = HashMap()

        init {
            findViewItems(itemView)
        }

        private fun findViewItems(itemView: View) {
            addTOMap(itemView)
            if (itemView is ViewGroup) {
                val childCount = itemView.childCount
                (0 until childCount)
                    .map { itemView.getChildAt(it) }
                    .forEach { findViewItems(it)}
            }
        }

        private fun addTOMap(itemView: View) {
            viewMap[itemView.id] = itemView
        }

        fun setHeader(@IdRes id: Int, text: String) {
            val view = (viewMap[id] ?: throw IllegalArgumentException("View for $id not found")) as? TextView
                ?: throw IllegalArgumentException("View for $id is not a TextView")
            view.text = text
        }

        fun setItems(item: MyObject, @IdRes textViewId: Int) {
            val view = (viewMap[textViewId] ?: throw IllegalArgumentException("View for $textViewId not found")) as? TextView ?: throw IllegalArgumentException("View for $textViewId is not a TextView")
            view.text = item.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val inflatedView : View = when(viewType) {
            ConstantRowTypeEnum.ROW.ordinal -> layoutInflater.inflate(R.layout.doctor_appointments_item_layout, parent, false)
            else -> layoutInflater.inflate(R.layout.doctor_appointments_header_layout, parent, false)
        }
        return MyViewHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataset[position]

        if (item.isHeader) {
            item.headerDate.let {
                holder.setHeader(R.id.appointmentsDate, item.headerDate!!)
            }
            item.headerTitle.let {
                holder.setHeader(R.id.btnViewAll, item.headerTitle!!)
            }
        } else {
            holder.setItems(item.myObject!!, R.id.tvPatientname   )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataset[position].isHeader) {
            0
        } else {
            1
        }
     }
}