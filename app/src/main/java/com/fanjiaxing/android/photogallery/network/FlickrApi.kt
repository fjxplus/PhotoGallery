package com.fanjiaxing.android.photogallery.network

import com.fanjiaxing.android.photogallery.FlickrFetcher
import com.fanjiaxing.android.photogallery.GalleryApplication
import com.fanjiaxing.android.photogallery.model.FlickrResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface FlickrApi {

    @GET("services/rest/?method=flickr.interestingness.getList&api_key=${GalleryApplication.APIKEY}&format=json&nojsoncallback=1&extras=url_s")
    fun fetchPhotos(): Call<FlickrResponse>

    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>
}