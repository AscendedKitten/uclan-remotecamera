package com.uclan.remotecamera.androidApp

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import androidx.databinding.DataBindingUtil
import com.uclan.remotecamera.androidApp.databinding.ActivityMainBinding
import com.uclan.remotecamera.androidApp.p2p.P2PConnectionActions
import com.uclan.remotecamera.androidApp.p2p.WiFiDirectBroadcastReceiver
import com.uclan.remotecamera.androidApp.p2p.WiFiDirectFragment
import com.uclan.remotecamera.androidApp.utility.GenericAlert
import java.util.*

class MainActivity : AppCompatActivity(), P2PConnectionActions {

    companion object {
        const val PERMISSIONS_LOCATION_REQUEST_CODE = 1001
    }

    private val intentFilter = IntentFilter()
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager
    private lateinit var receiver: WiFiDirectBroadcastReceiver

    private lateinit var taskTimer: TimerTask

    var isWifiP2pEnabled: Boolean = false

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    class PeerDiscoveryTask(private val activity: MainActivity?) : TimerTask() {
        override fun run() {
            if (activity == null) return
            if (!activity.supportFragmentManager.findFragmentByTag("WiFiDirectFragment")?.isVisible!!) return
            activity.runOnUiThread {
                Log.d("MainActivity", "still going")
                activity.discoverPeers()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_LOCATION_REQUEST_CODE)
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.e("MainActivity", "Permission was denied")
                finish()
            }
    }

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

        if (!isSupported() && isWifiP2pEnabled)
            GenericAlert().create(this@MainActivity, "Error", "Device does not support WiFi Direct")
                .show()
        else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_LOCATION_REQUEST_CODE
            )
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, WiFiDirectFragment(), "WiFiDirectFragment")
            .commit()
    }

    @SuppressWarnings("MissingPermission")
    private fun discoverPeers() {
        val wifiFrag: WiFiDirectFragment =
            supportFragmentManager.findFragmentByTag("WiFiDirectFragment") as WiFiDirectFragment
        if (isWifiP2pEnabled) {
            if (isLocationEnabled()) {
                wifiFrag.showProgressDialogue()
                manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.v("MainActivity", "peer discovery success")
                    }

                    override fun onFailure(reasonCode: Int) {
                        Log.v(
                            "MainActivity",
                            "peer discovery failure, error code $reasonCode"
                        )
                        wifiFrag.hideProgressDialogue()
                    }
                })
            } else {
                GenericAlert().create(
                    this@MainActivity,
                    "Error",
                    "Please enable Location Services!"
                )
                    .show()
                wifiFrag.hideProgressDialogue()
            }
        } else {
            GenericAlert().create(
                this@MainActivity,
                "Error",
                "Please enable WiFi!"
            )
                .show()
            wifiFrag.hideProgressDialogue()
        }

    }

    fun updateAll(newList: List<WifiP2pDevice>) {
        (supportFragmentManager.findFragmentByTag("WiFiDirectFragment") as WiFiDirectFragment).updateAll(
            newList
        )
    }

    @SuppressWarnings("MissingPermission")
    override fun connect(config: WifiP2pConfig?) {
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.v("MainActivity", "connection success")
            }

            override fun onFailure(reason: Int) {
                GenericAlert().create(this@MainActivity, "Error", "Connection failed!").show()
            }
        })

    }

    override fun disconnect() {
    }

    override fun abort() {
        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        taskTimer = PeerDiscoveryTask(this)
        Timer().scheduleAtFixedRate(taskTimer, 1000, 6000)
        receiver.also { receiver ->
            registerReceiver(receiver, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        taskTimer.cancel()
        receiver.also { receiver ->
            unregisterReceiver(receiver)
        }
    }

    fun setIsWifiP2pEnabled(isWifiP2pEnabled: Boolean) {
        this.isWifiP2pEnabled = isWifiP2pEnabled
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            this@MainActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun isSupported(): Boolean {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.e("MainActivity", "No WifiDirect support")
            return false
        }

        val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager? ?: return false
        if (!wifiManager.isP2pSupported) {
            Log.e("MainActivity", "WifiDirect not supported by hardware or device is offline")
            return false
        }

        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager? ?: run {
            Log.e("MainActivity", "WifiDirect system service not available")
            return false
        }
        channel = manager.initialize(this, mainLooper, null) ?: run {
            Log.e("MainActivity", "WifiDirect initialization failure")
            return false
        }

        return true
    }
}