package com.uclan.remotecamera.androidApp.utility

import java.io.IOException
import java.net.Socket

class Utility {
    companion object {
        fun isPortClosed(ip: String?, port: Int): Boolean {
            try {
                Socket(ip, port).use { return true }
            } catch (ex: IOException) { }
            return false


        }
    }
}