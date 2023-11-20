package com.android.doctorapp.ui.doctordashboard.adapter

import android.app.Service
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentZoomPreviewClinicImgBinding
import com.android.doctorapp.util.zoomImage.ZoomImageView

class PreviewImgShowAdapter(
    private val context: Context,
    private val scaleChangeListener: ZoomImageView.OnScaleChangeListener
) : PagerAdapter() {

    private var imageList: List<String> = emptyList()

    fun addAll(imageList: List<String>) {
        this.imageList = imageList
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater: LayoutInflater =
            context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding: FragmentZoomPreviewClinicImgBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_zoom_preview_clinic_img,
                null,
                false
            )
        binding.imgUri = imageList[position].toUri()

        binding.zoomImageView.getIsImageZoom(scaleChangeListener)
        container.addView(binding.root)
        return binding.root
    }

    override fun destroyItem(collection: ViewGroup, position: Int, `object`: Any) {
        collection.removeView(`object` as View)
    }
}
