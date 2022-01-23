package com.fanjiaxing.android.photogallery.model

import com.google.gson.annotations.SerializedName

data class PhotoResponse(
    @SerializedName("photo")
    val galleryItems: List<GalleryItem>
)