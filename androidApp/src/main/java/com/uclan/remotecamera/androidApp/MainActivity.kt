package com.uclan.remotecamera.androidApp

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.uclan.remotecamera.androidApp.d2d.WiFiDirectBroadcastReceiver
import com.uclan.remotecamera.androidApp.d2d.WiFiDirectFragment
import com.uclan.remotecamera.androidApp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val intentFilter = IntentFilter()
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager
    private lateinit var receiver: WiFiDirectBroadcastReceiver

    var isWifiP2pEnabled: Boolean = false

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
        channel.also { receiver = WiFiDirectBroadcastReceiver(this, manager, channel) }

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        _binding = ActivityMainBinding.inflate(layoutInflater)


        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, WiFiDirectFragment(), "WiFiDirectFragment")
            .commit()
    }

    private fun discoverPeers() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                if (isSupported()) {
                    if (isWifiP2pEnabled) {
                        val wiFiFrag: WiFiDirectFragment =
                            supportFragmentManager.findFragmentByTag("WiFiDirectFragment") as WiFiDirectFragment
                        wiFiFrag.showProgressDialogue()
                        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                            override fun onSuccess() {
                                Log.v("MainActivity", "peer discovery success")
                            }

                            override fun onFailure(reasonCode: Int) {
                                Log.v(
                                    "MainActivity",
                                    "peer discovery failure, error code $reasonCode"
                                )
                                //TODO: Replace with pop-up
                                Toast.makeText(
                                    applicationContext,
                                    "Peer discovery initialization went wrong!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        })
                    } else {
                        //TODO: Replace with pop-up
                        Toast.makeText(
                            applicationContext,
                            "Please enable WiFi!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    //TODO: Replace with pop-up
                    Toast.makeText(
                        applicationContext,
                        "WiFi Direct is not supported on your device!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            else -> {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        receiver.also { receiver ->
            registerReceiver(receiver, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        receiver.also { receiver ->
            unregisterReceiver(receiver)
        }
    }

    override fun onClick(v: View?) {
        discoverPeers()
    }

    fun setIsWifiP2pEnabled(isWifiP2pEnabled: Boolean) {
        this.isWifiP2pEnabled = isWifiP2pEnabled
    }

    private fun isSupported(): Boolean {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT))
            return false

        val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager? ?: return false
        if (!wifiManager.isP2pSupported)
            return false

        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager? ?: return false
        channel = manager.initialize(this, mainLooper, null) ?: return false

        return true
    }
}