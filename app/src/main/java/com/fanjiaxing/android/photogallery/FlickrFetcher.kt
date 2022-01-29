package com.fanjiaxing.android.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fanjiaxing.android.photogallery.model.FlickrResponse
import com.fanjiaxing.android.photogallery.model.GalleryItem
import com.fanjiaxing.android.photogallery.model.PhotoResponse
import com.fanjiaxing.android.photogallery.network.FlickrApi
import com.fanjiaxing.android.photogallery.network.ServiceBuilder
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "ThumbnailDownloader"

object FlickrFetcher {

    init {
        Log.i(TAG, "FlickrFetcher created")
    }
    private val flickrApi = ServiceBuilder.create<FlickrApi>()

    private lateinit var flickrRequest: Call<FlickrResponse>

    fun fetchPhotosRequest(): Call<FlickrResponse>{
        return flickrApi.fetchPhotos()
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        Log.i(TAG, "fetchPhotos()")
        return fetchPhotoMetadata(fetchPhotosRequest())
    }

    fun searchPhotosRequest(query: String): Call<FlickrResponse>{
        return flickrApi.searchPhotos(query)
    }

    fun searchPhotos(query: String): LiveData<List<GalleryItem>>{
        Log.i(TAG, "searchPhotos()")
        return fetchPhotoMetadata(searchPhotosRequest(query))
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        Log.i(TAG, "fetchPhoto()")
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use ( BitmapFactory::decodeStream )
        Log.i(TAG, "Decode bitmap = $bitmap from Response = $response")
        return bitmap
    }

    private fun fetchPhotoMetadata(flickrRequest: Call<FlickrResponse>): LiveData<List<GalleryItem>>{

        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                responseLiveData.value = galleryItems

            }

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                if (call.isCanceled) {
                    Log.i(TAG, "${call.toString()} has been canceled.")
                } else {
                    Log.i(TAG, "Fails to fetchPhotos.", t)
                }
            }

        })
        return responseLiveData
    }

    fun cancelRequestInFlight() {
        if (flickrRequest.isExecuted) {
            flickrRequest.cancel()
        }
    }

}