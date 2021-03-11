package com.uclan.remotecamera.androidApp.stream

import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.uclan.remotecamera.androidApp.R
import com.uclan.remotecamera.androidApp.databinding.FragmentSettingsBinding
import com.uclan.remotecamera.androidApp.p2p.P2PConnectionActions

class CameraSettingsFragment : Fragment(), WifiP2pManager.ConnectionInfoListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var info: WifiP2pInfo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnDisconnect.setOnClickListener {
            (activity as P2PConnectionActions).disconnect()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        //(activity as P2PConnectionActions).disconnect()
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
        this.info = info!!
        Log.d("CameraSettings", "Established connection from: ${info.groupOwnerAddress}")

        with(binding) {
            groupOwner.text = HtmlCompat.fromHtml(
                getString(
                    R.string.groupOwner,
                    if (info.isGroupOwner) "Yes" else "No"
                ), HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            groupIp.text = HtmlCompat.fromHtml(
                getString(R.string.groupIp, info.groupOwnerAddress),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
    }


}