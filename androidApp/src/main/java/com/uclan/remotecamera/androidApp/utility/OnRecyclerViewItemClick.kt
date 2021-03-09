package com.uclan.remotecamera.androidApp.utility

import android.net.wifi.p2p.WifiP2pDevice

interface OnRecyclerViewItemClick {
    fun onClick(device: WifiP2pDevice)
}