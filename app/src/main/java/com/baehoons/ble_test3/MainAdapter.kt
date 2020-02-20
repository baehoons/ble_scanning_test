package com.baehoons.ble_test3

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_main.view.*
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList

class MainAdapter :RecyclerView.Adapter<RecyclerView.ViewHolder>(){



    private val devices = ArrayList<BluetoothDevice>()
    class DeviceHolder(parent:ViewGroup):RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_main,parent,false))
    {
        fun onBind(device: BluetoothDevice){
            itemView.run {
                var type_text:String
                when(device.type){
                    0 -> type_text = "Unknown_Type_Device"
                    1 -> type_text = "클래식(BR/EDR)"
                    2->  type_text = "듀얼(BR/EDR/LE)"
                    3 -> type_text = "저전력(LE-only)"
                    else -> type_text = "Unknown_Type_Device"
                }
                text_view_device_name.text = device.name ?: "   -   "
                text_view_device_address.text = device.address
//            var uuid_text = (device.uuids).joinToString(separator = "-")
//            view.uuid.text = uuid_text.substring(0,8)+" ..."
                type.text = type_text
            }
        }
    }


    override fun onCreateViewHolder(container: ViewGroup, viewType: Int) = DeviceHolder(
        container
    )

    override fun getItemCount() = devices.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as? DeviceHolder)?.onBind(devices[position])
    }

    fun addDevice(device: BluetoothDevice) {
        if(!devices.contains(device)){
            devices.add(device)
            notifyDataSetChanged()
        }

    }

    fun clearDevices() {
        devices.clear()
        notifyDataSetChanged()
    }

//    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
//
//        fun bind(device: BluetoothDevice) {
//            var type_text:String
//            when(device.type){
//                0 -> type_text = "Unknown_Type_Device"
//                1 -> type_text = "클래식(BR/EDR)"
//                2->  type_text = "듀얼(BR/EDR/LE)"
//                3 -> type_text = "저전력(LE-only)"
//                else -> type_text = "Unknown_Type_Device"
//            }
//            view.text_view_device_name.text = device.name ?: "   -   "
//            view.text_view_device_address.text = device.address
////            var uuid_text = (device.uuids).joinToString(separator = "-")
////            view.uuid.text = uuid_text.substring(0,8)+" ..."
//            view.type.text = type_text
//        }
//    }

//    fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(
//        layoutRes,
//        this,
//        false
//    )
}