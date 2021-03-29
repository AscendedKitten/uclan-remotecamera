package com.uclan.remotecamera.androidApp.p2p

import android.app.ProgressDialog
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.uclan.remotecamera.androidApp.MainActivity
import com.uclan.remotecamera.androidApp.databinding.FragmentWifiBinding
import com.uclan.remotecamera.androidApp.utility.OnRecyclerViewItemClick
import java.util.*

class WiFiDirectFragment : Fragment(), WifiP2pManager.PeerListListener,
    WifiP2pManager.ConnectionInfoListener {

    private lateinit var listAdapter: WiFiDirectListAdapter
    private lateinit var progressDialog: ProgressDialog

    private var _connectingDevice: WifiP2pDevice? = null
    val connectingDevice get() = _connectingDevice
    private var _binding: FragmentWifiBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskTimer: TimerTask

    class PeerDiscoveryTask(private val activity: MainActivity?) : TimerTask() {
        override fun run() {
            if (activity == null) return
            activity.runOnUiThread {
                activity.discoverPeers()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWifiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        taskTimer.cancel()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        taskTimer.cancel()
        if (this@WiFiDirectFragment::progressDialog.isInitialized && progressDialog.isShowing)
            progressDialog.dismiss()
    }

    override fun onStart() {
        super.onStart()
        taskTimer = PeerDiscoveryTask(activity as MainActivity)
        Timer().scheduleAtFixedRate(taskTimer, 1000, 6000)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = WiFiDirectListAdapter(object : OnRecyclerViewItemClick {
            override fun onClick(device: WifiP2pDevice) {
                val config = WifiP2pConfig()
                config.deviceAddress = device.deviceAddress
                config.wps.setup = WpsInfo.PBC
                Log.d("WifiDirectFragment", "Attempt connection from ${config.deviceAddress}")

                _connectingDevice = device

                if (this@WiFiDirectFragment::progressDialog.isInitialized && progressDialog.isShowing)
                    progressDialog.dismiss()

                progressDialog = ProgressDialog.show(
                    activity,
                    "Press back to cancel",
                    "Connecting to :" + device.deviceName,
                    true,
                    true
                ) {
                    (activity as P2PConnectionActions).abort()
                }
                (activity as P2PConnectionActions).connect(config)
            }
        }, context)
        binding.recyclerView.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    fun showProgressDialogue() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressDialogue() {
        binding.progressBar.visibility = View.GONE
    }

    fun updateAll(newList: List<WifiP2pDevice>) {
        listAdapter.updateAll(newList)
    }

    fun update(device: WifiP2pDevice?) {
        if (device != null)
            listAdapter.update(device)
    }

    override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
        if (_binding != null)
            if (binding.progressBar.isShown)
                hideProgressDialogue()

        if (peers != null)
            if (!peers.deviceList.isEmpty())
                listAdapter.updateAll(peers.deviceList.toList())
            else
                Log.d("WiFiDirectActivity", "No devices scanned")
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        Log.d("WiFiDirectFragment", "Redirecting to Settings")
        findNavController().navigate(WiFiDirectFragmentDirections.toSettingsFragment(info!!))
    }
}