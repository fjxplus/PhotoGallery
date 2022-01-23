package com.fanjiaxing.android.photogallery.network

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.fanjiaxing.android.photogallery.FlickrFetcher
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit
) : HandlerThread(TAG) {

    private var hasQuit = false
    private lateinit var requestHandler: Handler

    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetcher = FlickrFetcher
    val fragmentLifecycleObserver: LifecycleObserver = object: LifecycleObserver{
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setup() {
            Log.i(TAG, "Starting background thread")
            start()
            looper
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun tearDown() {
            Log.i(TAG, "Destroying background thread")
            quit()
        }
    }
    val viewLifecycleObserver: LifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun clearQueue(){
            Log.i(TAG, "Clearing all requests from queue")
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        Log.i(TAG, "onLooperPrepared()")
        super.onLooperPrepared()
        requestHandler = object : Handler(looper) {
            override fun handleMessage(msg: Message) {
                Log.i(TAG, "handleMessage()")
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got a url: $url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return
        Log.i(TAG, "handleRequest url is:$url")
        val bitmap = flickrFetcher.fetchPhoto(url) ?: return


        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuit){
                return@Runnable
            }
            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap)
        })
    }

}