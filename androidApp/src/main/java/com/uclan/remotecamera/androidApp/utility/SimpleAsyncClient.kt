package com.uclan.remotecamera.androidApp.utility

import android.util.Log
import com.uclan.remotecamera.androidApp.stream.CameraFragment
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*

@KtorExperimentalAPI
class SimpleAsyncClient(
    private var httpUrl: String,
    private var httpPort: Int
) {

    companion object {
        const val TAG = "SimpleAsyncClient"
    }

    private val client = HttpClient(CIO) {
        install(WebSockets)

    }

    // send_image <-> req_image
    // advertise_url

    suspend fun send(msg: String) {
        client.ws(
            method = HttpMethod.Get,
            host = httpUrl,
            port = httpPort,
            path = "/exchange"
        ) {
            Log.d(TAG, "Attempting to send $msg")
            send(msg)
        }
    }

    suspend fun advertise(cameraFragment: CameraFragment) {
        client.ws(
            method = HttpMethod.Get,
            host = httpUrl,
            port = httpPort,
            path = "/exchange"
        ) {
            try {
                for (message in incoming) {
                    message as Frame.Text? ?: continue
                    Log.d(TAG, "Received cmd: ${message.readText()}")
                    when (message.readText()) {
                        "!request_url" -> {
                            send(cameraFragment.rtspUrl())
                        }
                        "!request_img" -> {
                            val byteArray: ByteArray? = cameraFragment.captureImageAsByteArray()
                            if (byteArray != null)
                                send(Frame.Binary(true, byteArray))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error receiving request: ${e.message}")
            }
        }
    }
}