package com.uclan.remotecamera.androidApp.d2d

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.uclan.remotecamera.androidApp.MainActivity

class WiFiDirectBroadcastReceiver(
    private val activity: MainActivity,
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {

            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                activity.setIsWifiP2pEnabled(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)

                Log.d("Broadcaster", "State changed to $state")
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                val wiFiFrag: WiFiDirectFragment =
                    activity.supportFragmentManager.findFragmentByTag("WiFiDirectFragment") as WiFiDirectFragment
                if (ActivityCompat.checkSelfPermission(
                        activity.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    activity.requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1001
                    )
                }
                manager.requestPeers(channel, wiFiFrag)
                Log.d("Broadcaster", "peer discovery success 2")

            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {

            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {

            }
        }
    }
}