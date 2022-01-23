package com.fanjiaxing.android.photogallery.model

import com.google.gson.annotations.SerializedName

data class GalleryItem(
    val id: String,
    val title: String,
    @SerializedName("url_s") val url: String,
    val height_s: Int,
    val width_s: Int
)