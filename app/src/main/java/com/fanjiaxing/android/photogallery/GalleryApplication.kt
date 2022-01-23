package com.fanjiaxing.android.photogallery

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class GalleryApplication: Application() {

    @SuppressLint("StaticFieldLeak")
    companion object{
        lateinit var context:Context
        const val APIKEY = "f911320adca6bf7fbd8989c7f8f406f3"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}