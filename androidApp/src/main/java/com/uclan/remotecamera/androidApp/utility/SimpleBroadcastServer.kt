package com.uclan.remotecamera.androidApp.utility


import android.util.Log
import com.google.common.collect.EvictingQueue
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*

class SimpleBroadcastServer(private val port: Int) {

    companion object {
        const val TAG = "SimpleBroadcastServer"
    }

    fun start() {
        embeddedServer(Netty, port) {
            install(WebSockets) {
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }
            routing {
                val connections = EvictingQueue.create<DefaultWebSocketSession>(2)
                webSocket("/exchange") {
                    Log.d(TAG, "Registered connection")
                    connections.add(this)
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val msg = frame.readText()
                            Log.d(TAG, "Received msg '$msg'")
                            connections.forEach {
                                if (!it.equals(this))
                                    it.outgoing.send(Frame.Text(msg))
                            }
                        } else {
                            val msg = frame.readBytes()
                            Log.d(TAG, "Received img object")
                            connections.forEach {
                                if (!it.equals(this))
                                    it.outgoing.send(Frame.Binary(true, msg))
                            }
                        }
                    }
                }
            }
        }.start()
    }
}