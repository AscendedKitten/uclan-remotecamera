/*
package com.uclan.remotecamera.androidApp.utility

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket

class SimpleBroadcastServer(private val port: Int) : Thread() {

    private lateinit var serverSocket: ServerSocket
    private var clients = mutableMapOf<String, PrintWriter>()

    companion object {
        private const val TAG = "SimpleHttpServer"
    }

    override fun run() {
        try {
            serverSocket = ServerSocket(port)
            while (true) {
                val client = serverSocket.accept()

                clients[client.inetAddress.hostAddress] =
                    PrintWriter(client.getOutputStream(), true)
                Log.d(
                    TAG,
                    "Accepted connection from ${client.inetAddress.hostAddress}"
                )

                Thread {
                    val broadcast =
                        BufferedReader(InputStreamReader(client.getInputStream())).readLine()
                    Log.d(
                        TAG,
                        "Received message from ${client.inetAddress.hostAddress}: $broadcast"
                    )

                    for (otherClient in clients) {
                        if (otherClient.key != client.inetAddress.hostAddress) {
                            otherClient.value.println(broadcast)
                            Log.d(TAG, "Broadcasting to ${otherClient.key}")
                        }
                    }
                }.start()
            }
        } catch (exception: IOException) {
            Log.e(TAG, "Error opening server socket: ${exception.message}")
        }
    }

    fun shutdown() {
        if (this::serverSocket.isInitialized && !serverSocket.isClosed) {
            serverSocket.close()
            Log.d(TAG, "Closing server socket")
        } else Log.d(TAG, "Cannot close server socket; no socket open")
    }

}
 */