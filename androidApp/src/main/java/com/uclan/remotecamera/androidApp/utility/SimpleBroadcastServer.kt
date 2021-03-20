package com.uclan.remotecamera.androidApp.utility


import android.util.Log
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import java.time.Duration
import java.util.*
import kotlin.collections.LinkedHashSet

class SimpleBroadcastServer(private val port: Int) {

    companion object {
        const val TAG = "SimpleBroadcastServer"
    }

    fun start() {
        Log.d(TAG, "Attempting to start broadcast server")
        embeddedServer(Netty, port) {
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
                routing {
                    val connections =
                        Collections.synchronizedSet<DefaultWebSocketSession?>(LinkedHashSet())
                    webSocket("/exchange") {
                        connections.add(this)
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val msg = frame.readText()
                                Log.d(TAG, "Received msg '$msg'")
                                connections.forEach {
                                    it.outgoing.send(Frame.Text(msg))
                                }
                            }
                        }
                    }
                }
            }
        }.start(wait = true)
    }
}