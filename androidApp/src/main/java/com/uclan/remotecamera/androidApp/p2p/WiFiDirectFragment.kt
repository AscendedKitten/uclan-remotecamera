package com.uclan.remotecamera.androidApp.p2p

import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.uclan.remotecamera.androidApp.databinding.FragmentWifiBinding
import com.uclan.remotecamera.androidApp.utility.OnRecyclerViewItemClick

class WiFiDirectFragment : Fragment(), WifiP2pManager.PeerListListener {

    private lateinit var listAdapter: WiFiDirectListAdapter

    private var _binding: FragmentWifiBinding? = null
    private val binding get() = _binding!!

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
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = WiFiDirectListAdapter(object : OnRecyclerViewItemClick {
            override fun onClick(device: WifiP2pDevice) {
                val config = WifiP2pConfig()
                config.deviceAddress = device.deviceAddress
                config.wps.setup = WpsInfo.PBC
                Log.d("WifiDirectFragment", "Attempt connection from ${config.deviceAddress}")
                (activity as P2PConnectionActions).connect(config)
            }
        })
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

    override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
        if (binding.progressBar.isShown)
            hideProgressDialogue()

        if (peers != null)
            if (!peers.deviceList.isEmpty())
                listAdapter.updateAll(peers.deviceList.toList())
            else
                Log.d("WiFiDirectActivity", "No devices scanned")
    }
}