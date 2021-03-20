package com.uclan.remotecamera.androidApp.stream

import android.net.wifi.p2p.WifiP2pInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uclan.remotecamera.androidApp.R
import com.uclan.remotecamera.androidApp.databinding.FragmentSettingsBinding
import com.uclan.remotecamera.androidApp.p2p.P2PConnectionActions

class CameraSettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var info: WifiP2pInfo

    companion object {
        const val PORT = 8898
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val alertDialogBuilder: AlertDialog.Builder =
                        AlertDialog.Builder(requireContext())
                    alertDialogBuilder.setMessage("Disconnect Wifi Direct?")
                    alertDialogBuilder.setCancelable(true)

                    alertDialogBuilder.setPositiveButton(
                        getString(android.R.string.ok)
                    ) { dialog, _ ->
                        (activity as P2PConnectionActions).disconnect()
                        dialog.cancel()
                    }

                    val alertDialog: AlertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(CameraSettingsFragmentArgs.fromBundle(requireArguments()).wifiP2pInfo)
    }

    private fun init(info: WifiP2pInfo?) {
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

        binding.btnConfirm.text = if (info.isGroupOwner) "Start camera" else "Start receiver"

        binding.btnConfirm.setOnClickListener {
            val ownerAddress = info.groupOwnerAddress.hostAddress
            if (info.isGroupOwner) {
                Log.d("CameraSettingsFragment", "Identified group owner, starting camera fragment")
                findNavController().navigate(
                    CameraSettingsFragmentDirections.toCameraFragment(
                        PORT,
                        ownerAddress
                    )
                )
            } else {
                Log.d(
                    "CameraSettingsFragment",
                    "Identified group member, starting display fragment"
                )
                findNavController().navigate(
                    CameraSettingsFragmentDirections.toDisplayFragment(
                        PORT,
                        ownerAddress
                    )
                )
            }
        }
    }
}