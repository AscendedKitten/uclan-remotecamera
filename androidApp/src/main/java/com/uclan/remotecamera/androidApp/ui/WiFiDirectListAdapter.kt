package com.uclan.remotecamera.androidApp.ui

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uclan.remotecamera.androidApp.R
import com.uclan.remotecamera.androidApp.databinding.RecyclerViewItemBinding

class WiFiDirectListAdapter(private val list: ArrayList<WifiP2pDevice> = arrayListOf()) :
    RecyclerView.Adapter<WiFiDirectListAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val binding = RecyclerViewItemBinding.bind(view)
        override fun onClick(v: View?) {
            //TODO
        }

        fun bind(device: WifiP2pDevice) {
            with(binding) {
                binding.deviceInfo.text =
                    String.format("%s : %s", device.deviceName, device.deviceAddress)
                binding.deviceType.text = device.primaryDeviceType
            }
        }
    }

    fun updateAll(newList: List<WifiP2pDevice>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun clearAll() {
        list.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device: WifiP2pDevice = list[position]
        holder.bind(device)
    }

    override fun getItemCount() = list.size
}