package com.uclan.remotecamera.androidApp.stream

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService

class LocalFileTransferService : JobIntentService() {

    companion object {
        const val ACTION_STREAM_PREVIEWS = "com.uclan.remotecamera.STREAM_PREVIEWS"
        const val EXTRAS_GROUP_OWNER_ADDRESS = "com.uclan.remotecamera.GROUP_OWNER_ADDRESS"
        const val EXTRAS_GROUP_OWNER_PORT = "com.uclan.remotecamera.GROUP_OWNER_PORT"

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, LocalFileTransferService::class.java, 1, intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        var context: Context = applicationContext
        if (intent.action.equals(ACTION_STREAM_PREVIEWS)) {

        }
    }
}