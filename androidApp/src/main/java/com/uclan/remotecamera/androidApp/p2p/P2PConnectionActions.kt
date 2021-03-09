package com.uclan.remotecamera.androidApp.p2p

import android.net.wifi.p2p.WifiP2pConfig

interface P2PConnectionActions {
    fun connect(config: WifiP2pConfig?)
    fun disconnect()
    fun abort()
}