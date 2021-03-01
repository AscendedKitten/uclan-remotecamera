package com.uclan.remotecamera.androidApp.d2d

import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.uclan.remotecamera.androidApp.databinding.FragmentWifiBinding
import com.uclan.remotecamera.androidApp.ui.WiFiDirectListAdapter

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
        listAdapter = WiFiDirectListAdapter()
        binding.recyclerView.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    fun showProgressDialogue() {
        val progressBar: ProgressBar = binding.progressBar
        progressBar.visibility = View.VISIBLE
        progressBar.tooltipText = "Discovering..."
    }

    override fun onPeersAvailable(peers: WifiP2pDeviceList?) {
        if (binding.progressBar.isShown)
            binding.progressBar.visibility = View.GONE

        if (peers != null)
            if (!peers.deviceList.isEmpty())
                listAdapter.updateAll(peers.deviceList.toList())
            else
                Log.d("WiFiDirectActivity", "No devices scanned")
    }
}