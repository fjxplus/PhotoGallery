package com.fanjiaxing.android.photogallery.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {

    private const val BASE_URL = "https://api.flickr.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(PhotoIntercetor())
        .build()

    private val retrofit =
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)
    inline fun <reified T> create() = create(T::class.java)
}