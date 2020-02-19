package com.baehoons.ble_test3

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_main.view.*

class MainAdapter :RecyclerView.Adapter<MainAdapter.ViewHolder>(){
    private val devices = ArrayList<BluetoothDevice>()

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int) = ViewHolder(
        container.inflate(R.layout.adapter_main)
    )

    override fun getItemCount() = devices.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(devices[position])
    }

    fun addDevice(device: BluetoothDevice) {
        devices.add(device)
        notifyItemInserted(itemCount)
    }

    fun clearDevices() {
        devices.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(device: BluetoothDevice) {
            view.text_view_device_name.text = device.name ?: "   -   "
            view.text_view_device_address.text = device.address
        }
    }

    fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(
        layoutRes,
        this,
        false
    )
}