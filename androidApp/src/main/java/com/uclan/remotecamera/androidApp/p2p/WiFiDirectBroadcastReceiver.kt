package com.uclan.remotecamera.androidApp.p2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Parcelable
import android.util.Log
import com.uclan.remotecamera.androidApp.MainActivity
import com.uclan.remotecamera.androidApp.R
import com.uclan.remotecamera.androidApp.stream.CameraSettingsFragment

class WiFiDirectBroadcastReceiver(
    private val activity: MainActivity,
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel
) : BroadcastReceiver() {
    @SuppressWarnings("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {

            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                activity.setIsWifiP2pEnabled(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)

                // 2 = up, 1 = offline
                Log.d("Broadcaster", "State changed to $state")
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                val wiFiFrag: WiFiDirectFragment =
                    activity.supportFragmentManager.findFragmentByTag("WiFiDirectFragment") as WiFiDirectFragment
                manager.requestPeers(channel, wiFiFrag)
                Log.d("Broadcaster", "peer discovery success 2")

            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo = intent
                    .getParcelableExtra<Parcelable>(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo?

                if (networkInfo!!.isConnected) {
                    Log.d("Broadcaster", "Attempt info request")
                    val fragment = CameraSettingsFragment()

                    activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment, "CameraSettingsFragment")
                        .addToBackStack(null)
                        .commit()
                    manager.requestConnectionInfo(channel, fragment)

                } else {
                    activity.updateAll(emptyList())
                }


            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {

            }
        }
    }
}