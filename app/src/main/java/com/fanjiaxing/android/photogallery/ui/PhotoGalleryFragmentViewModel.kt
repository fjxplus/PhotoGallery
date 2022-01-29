package com.fanjiaxing.android.photogallery.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.fanjiaxing.android.photogallery.FlickrFetcher
import com.fanjiaxing.android.photogallery.GalleryApplication
import com.fanjiaxing.android.photogallery.QueryPreference
import com.fanjiaxing.android.photogallery.model.GalleryItem

class PhotoGalleryFragmentViewModel : ViewModel() {

    val galleryItemLiveData: LiveData<List<GalleryItem>>

    val searchTerm: String get() = mutableSearchTerm.value ?: ""

    private val mutableSearchTerm = MutableLiveData<String>()

    init {
        mutableSearchTerm.value = QueryPreference.getStoredQuery(GalleryApplication.context)

        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if (searchTerm.isBlank()){
                FlickrFetcher.fetchPhotos()
            }else{
                FlickrFetcher.searchPhotos(searchTerm)
            }
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreference.setStoredQuery(GalleryApplication.context, query)
        mutableSearchTerm.value = query
    }

    override fun onCleared() {
        super.onCleared()
        FlickrFetcher.cancelRequestInFlight()
    }
}