package com.uclan.remotecamera.androidApp.p2p

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uclan.remotecamera.androidApp.R
import com.uclan.remotecamera.androidApp.databinding.RecyclerViewItemBinding
import com.uclan.remotecamera.androidApp.utility.OnRecyclerViewItemClick

class WiFiDirectListAdapter(
    private val clickListener: OnRecyclerViewItemClick
) :
    RecyclerView.Adapter<WiFiDirectListAdapter.ViewHolder>() {

    private val list: ArrayList<WifiP2pDevice> = arrayListOf()

    class ViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        private val binding = RecyclerViewItemBinding.bind(view)


        fun bind(device: WifiP2pDevice, listener: OnRecyclerViewItemClick) {
            with(binding) {
                deviceInfo.text =
                    String.format("%s : %s", device.deviceName, device.deviceAddress)
                deviceType.text = device.primaryDeviceType
            }
            itemView.setOnClickListener {
                listener.onClick(device)
            }
        }
    }

    fun updateAll(newList: List<WifiP2pDevice>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], clickListener)
    }

    override fun getItemCount() = list.size
}