/*package com.uclan.remotecamera.androidApp.legacy

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import io.ktor.util.*
import java.io.IOException
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class LocalFileTransferService : JobIntentService() {

    companion object {
        const val ACTION_SEND_IMAGE = "com.uclan.remotecamera.SEND_IMAGE"
        const val ACTION_SEND_URL = "com.uclan.remotecamera.SEND_URL"
        const val EXTRAS_RTSP_LINK = "com.uclan.remotecamera.RTSP_LINK"
        const val EXTRAS_GROUP_OWNER_ADDRESS = "com.uclan.remotecamera.GROUP_OWNER_ADDRESS"
        const val EXTRAS_GROUP_OWNER_PORT = "com.uclan.remotecamera.GROUP_OWNER_PORT"

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, LocalFileTransferService::class.java, 1, intent)
        }
    }

    @KtorExperimentalAPI
    override fun onHandleWork(intent: Intent) {
        when (intent.action) {
            ACTION_SEND_IMAGE -> {
                //TODO: The very end
            }
            ACTION_SEND_URL -> {
                val rtspUrl: String = intent.extras!!.getString(EXTRAS_RTSP_LINK)!!
                val host = intent.extras!!.getString(EXTRAS_GROUP_OWNER_ADDRESS)!!
                val port = intent.extras!!.getInt(EXTRAS_GROUP_OWNER_PORT)

                val socket = Socket()

                try {
                    Log.d("FileTransferService", "Opening socket -")
                    socket.connect(InetSocketAddress(host, port), 5000)

                    Log.d("FileTransferService", "Socket connection: ${socket.isConnected}")
                    with(PrintWriter(socket.getOutputStream(), true)) {
                        println(rtspUrl)
                        close()
                    }

                    Log.d("FileTransferService", "Wrote url ($rtspUrl) to stream")

                } catch (exception: IOException) {
                    Log.e("FileTransferService", "Error opening socket: ${exception.message}")
                } finally {
                    socket.close()
                }

            }
        }
    }
}*/