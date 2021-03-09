package com.uclan.remotecamera.androidApp.stream

import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uclan.remotecamera.androidApp.databinding.FragmentSettingsBinding

class CameraSettingsFragment : Fragment(), WifiP2pManager.ConnectionInfoListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        Log.v("CameraSettings", "this is what u wanna see ${info?.groupOwnerAddress}")
    }


}