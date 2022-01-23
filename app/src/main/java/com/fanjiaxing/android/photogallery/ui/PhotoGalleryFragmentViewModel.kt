package com.fanjiaxing.android.photogallery.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.fanjiaxing.android.photogallery.FlickrFetcher
import com.fanjiaxing.android.photogallery.model.GalleryItem

class PhotoGalleryFragmentViewModel: ViewModel() {

    val galleryItemLiveData:LiveData<List<GalleryItem>> = FlickrFetcher.fetchPhotos()

    override fun onCleared() {
        super.onCleared()
        FlickrFetcher.cancelRequestInFlight()
    }
}