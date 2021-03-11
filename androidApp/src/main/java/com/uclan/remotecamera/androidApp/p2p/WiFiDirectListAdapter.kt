package com.uclan.remotecamera.androidApp.p2p

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.uclan.remotecamera.androidApp.R
import com.uclan.remotecamera.androidApp.databinding.RecyclerViewItemBinding
import com.uclan.remotecamera.androidApp.utility.OnRecyclerViewItemClick

class WiFiDirectListAdapter(
    private val clickListener: OnRecyclerViewItemClick,
    private val context: Context?
) :
    RecyclerView.Adapter<WiFiDirectListAdapter.ViewHolder>() {

    private val list: ArrayList<WifiP2pDevice> = arrayListOf()

    class ViewHolder(
        view: View,
        private val context: Context?
    ) : RecyclerView.ViewHolder(view) {
        private val binding = RecyclerViewItemBinding.bind(view)

        fun bind(device: WifiP2pDevice, listener: OnRecyclerViewItemClick) {
            with(binding) {
                deviceInfo.text = device.deviceName
                deviceType.text =
                    HtmlCompat.fromHtml(
                        context?.getString(
                            R.string.deviceType,
                            device.primaryDeviceType
                        )!!, HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                deviceAddress.text =
                    HtmlCompat.fromHtml(
                        context.getString(
                            R.string.deviceStatus, when (device.status) {
                                0 -> "Connected"
                                1 -> "Invited"
                                2 -> "Failed"
                                3 -> "Available"
                                4 -> "Unavailable"
                                else -> "Unknown"
                            }
                        ), HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
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

    fun update(device: WifiP2pDevice) {
        val index = list.indexOf(device)
        if (index != -1)
            notifyItemChanged(index)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], clickListener)
    }

    override fun getItemCount() = list.size
}