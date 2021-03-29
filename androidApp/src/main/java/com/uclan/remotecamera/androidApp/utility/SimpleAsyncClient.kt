package com.uclan.remotecamera.androidApp.utility

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.uclan.remotecamera.androidApp.stream.CameraFragment
import com.uclan.remotecamera.androidApp.stream.ConnectErrorCallback
import com.uclan.remotecamera.androidApp.stream.DisplayFragment
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@KtorExperimentalAPI
class SimpleAsyncClient(
    private var httpUrl: String,
    private var httpPort: Int,
    private var errorHandler: ConnectErrorCallback
) {

    companion object {
        const val TAG = "SimpleAsyncClient"
    }

    private val msgQueue = Channel<Frame>()

    private val client = HttpClient(CIO) {
        install(WebSockets)
        engine {
            requestTimeout = 5000
            endpoint.connectTimeout = 2000
            endpoint.connectAttempts = 3
        }
    }

    fun displayFragmentBlock(displayFragment: DisplayFragment) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Attempt receive")
            try {
                client.ws(
                    method = HttpMethod.Get,
                    host = httpUrl,
                    port = httpPort,
                    path = "/exchange"
                ) {
                    val messageRoutine = launch { nextMsg() }
                    val monitorRoutine = launch { monitorDisplay(displayFragment) }
                    monitorRoutine.join()
                    messageRoutine.cancelAndJoin()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed with exception ${e.message}")
                errorHandler.onErrorCallback()
            }
        }
    }

    fun cameraFragmentBlock(cameraFragment: CameraFragment) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Attempt advertise")
            try {
                client.ws(
                    method = HttpMethod.Get,
                    host = httpUrl,
                    port = httpPort,
                    path = "/exchange"
                ) {
                    val messageRoutine = launch { nextMsg() }
                    val monitorRoutine = launch { monitorCamera(cameraFragment) }
                    monitorRoutine.join()
                    messageRoutine.cancelAndJoin()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed with exception ${e.message}")
                errorHandler.onErrorCallback()
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.nextMsg() {
        while (true) {
            Log.d(TAG, "Holding for next message")
            val msg = msgQueue.receive()
            try {
                send(msg)
            } catch (e: Exception) {
                Log.e(TAG, "Failed with exception ${e.message}")
                errorHandler.onErrorCallback()
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.monitorDisplay(displayFragment: DisplayFragment) {
        try {
            incoming.consumeEach {
                val message = it
                Log.d(TAG, "Waiting on incoming")
                when (message) {
                    is Frame.Binary -> {
                        val content = message.readBytes()
                        Log.d(TAG, "Received img")
                        displayFragment.saveImage(
                            BitmapFactory.decodeByteArray(
                                content,
                                0,
                                content.size
                            )
                        )
                    }
                    is Frame.Text -> {
                        val content = message.readText()
                        Log.d(TAG, "Received msg $content")
                        when {
                            content.startsWith("rtsp") -> displayFragment.startStream(content)
                            content == "!return_settings" -> {
                                displayFragment.findNavController().popBackStack()
                                Toast.makeText(
                                    displayFragment.requireContext(),
                                    "Other device exited",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            content == "!unlock" -> {
                                displayFragment.requireActivity().runOnUiThread {
                                    displayFragment.init()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed with exception ${e.message}")
            errorHandler.onErrorCallback()
        }
    }

    private suspend fun DefaultClientWebSocketSession.monitorCamera(cameraFragment: CameraFragment) {
        try {
            incoming.consumeEach {
                val message = it
                Log.d(TAG, "Waiting on incoming")
                if (message is Frame.Text) {
                    val content = message.readText()
                    Log.d(TAG, "Received cmd $content")
                    when (content) {
                        "!request_url" -> {
                            queueMsg(Frame.Text(cameraFragment.rtspUrl()))
                        }
                        "!request_img" -> {
                            cameraFragment.captureImageAsByteArray()
                        }
                        "!return_settings" -> {
                            cameraFragment.requireActivity().runOnUiThread {
                                cameraFragment.lifecycleScope.launchWhenResumed {
                                    Toast.makeText(
                                        cameraFragment.requireContext(),
                                        "Other device exited",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    cameraFragment.findNavController().popBackStack()
                                }
                            }
                        }
                        "!request_unlock" -> {
                            Log.d(TAG, "Received unlock request")
                            queueMsg(Frame.Text("!unlock"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed with exception ${e.message}")
            errorHandler.onErrorCallback()
        }
    }

    fun queueMsg(msg: Frame) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Queued message")
            msgQueue.send(msg)
        }
    }
}