package com.fanjiaxing.android.photogallery.model

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class GalleryItem(
    val id: String,
    val title: String,
    @SerializedName("url_s") var url: String,
    @SerializedName("owner") val owner: String,
    val height_s: Int,
    val width_s: Int
) {
    val photoPageUri: Uri
        get() {
            return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build()
        }
}
