package com.fanjiaxing.android.photogallery

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.fanjiaxing.android.photogallery.network.PollWorker

private const val TAG = "PollWorker"

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "received result: $resultCode")
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val requestCode = intent.getIntExtra(PollWorker.REQUEST_CODE, 0)
        val notification: Notification? = intent.getParcelableExtra(PollWorker.NOTIFICATION)
        val notificationManager = NotificationManagerCompat.from(context)
        notification?.apply {
            notificationManager.notify(requestCode, notification)
        }
    }
}