package com.android.doctorapp.util

import android.graphics.Bitmap
import android.widget.ImageView
import com.android.doctorapp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

object ImageUtils {

    /**
     * set Image
     *
     * @param imageView ImageView
     * @param imageUrl  String
     */
    fun setImage(imageView: ImageView?, imageUrl: String, errorPlaceholder: Int? = null) {
        imageView?.let {
            val requestBuilder = Glide.with(it)
                .applyDefaultRequestOptions(requestOption(errorPlaceholder))
                .load(imageUrl)
            requestBuilder.into(it)
        }
    }

    /**
     * sets Circle Profile Image
     *
     * @param imageView ImageView
     * @param imageUrl  String
     */
    fun setCircleImage(
        imageView: ImageView?,
        imageUrl: String,
        errorPlaceholder: Int? = null
    ) {
        imageView?.let {
            Glide.with(it)
                .applyDefaultRequestOptions(requestOption(errorPlaceholder))
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(it)
        }
    }

    /**
     * sets Rounded radius in Image
     *
     * @param imageView ImageView
     * @param imageUrl  String
     */
    fun setRoundedRectangleImage(
        imageView: ImageView?,
        imageUrl: String,
        radius: Int,
        errorPlaceholder: Int? = null
    ) {
        imageView?.let {
            Glide.with(it)
                .applyDefaultRequestOptions(requestOption(errorPlaceholder))
                .asBitmap()
                .load(imageUrl)
                .transform(CenterInside(), RoundedCorners(radius))
                .into(it)
        }
    }

    /**
     * gets RequestOptions for setting profile image
     *
     * @return RequestOptions
     */
    private fun requestOption(errorPlaceholder: Int?): RequestOptions {
        return RequestOptions.errorOf(errorPlaceholder ?: R.drawable.ic_image_gery_24dp)
            .placeholder(errorPlaceholder ?: R.drawable.ic_image_gery_24dp)
    }

    /**
     * gets RequestOptions for setting profile image
     *
     * @return RequestOptions
     */
    private val profileImageRequestOption: RequestOptions
        get() = RequestOptions.errorOf(R.drawable.ic_person_grey_24dp)
            .placeholder(R.drawable.ic_person_grey_24dp)

    /**
     * gets RequestOptions for setting profile image
     *
     * @return RequestOptions
     */
    private val requestOption: RequestOptions
        get() = RequestOptions.errorOf(R.drawable.ic_image_gery_24dp)
            .placeholder(R.drawable.ic_image_gery_24dp)

    /**
     * sets Circle Profile Image
     *
     * @param imageView ImageView
     * @param bitmap    bitmap image to load
     */
    fun setImage(imageView: ImageView?, bitmap: Bitmap) {
        imageView?.let {
            Glide.with(it)
                .applyDefaultRequestOptions(requestOption)
                .load(bitmap)
                .into(it)
        }
    }

    /**
     * sets Circle Profile Image
     *
     * @param imageView ImageView
     * @param bitmap    Bitmap
     */
    fun setCircleProfileImage(imageView: ImageView?, bitmap: Bitmap) {
        imageView?.let {
            Glide.with(it)
                .setDefaultRequestOptions(profileImageRequestOption)
                .load(bitmap)
                .apply(RequestOptions.circleCropTransform())
                .into(it)
        }
    }
}