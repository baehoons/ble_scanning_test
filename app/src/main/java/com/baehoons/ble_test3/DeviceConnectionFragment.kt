package com.baehoons.ble_test3

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.baehoons.ble_test3.utils.DeviceManager
import kotlinx.android.synthetic.main.fragment_device_connection.*

class DeviceConnectionFragment : Fragment() {

    private lateinit var bleActivity: BleActivity

    private var device: BluetoothDevice? = null

    private var initialActionBarColor = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device_connection, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let { attachedActivity ->
            if (attachedActivity is BleActivity) {
                bleActivity = attachedActivity
            } else {
                Log.e(TAG, "Dialog have to be attached to the BleActivity.")
                attachedActivity.onBackPressed()
            }
        }

        parseArguments(savedInstanceState)
        initViews()
    }

    override fun onStart() {
        super.onStart()
        bleActivity.connectDevice(device!!, object : DeviceManager.DeviceStatusListener {
            override fun onConnect(gatt: BluetoothGatt) {
                Log.d(TAG, "On device connected")
                activity?.runOnUiThread {
                    updateConnectionState(DeviceConnectionState.CONNECTED)
                }
                val servicesBuilder = StringBuilder()
                gatt.services
                    .map { it.uuid }

                activity?.runOnUiThread {
                    showDeviceAvailableServices(servicesBuilder.toString())
                }
            }

            override fun onDataReceived(characteristic: BluetoothGattCharacteristic) {
                Log.d(TAG, "On data received")
                activity?.runOnUiThread {
                    showReceivedData(characteristic.value)
                }
            }

            override fun onDisconnect(gatt: BluetoothGatt) {
                activity?.runOnUiThread {
                    gatt.close()
                    updateConnectionState(DeviceConnectionState.DISCONNECTED)
                }
            }
        })

    }

    override fun onDestroyView() {
        activity?.window?.statusBarColor = initialActionBarColor
        super.onDestroyView()
    }


    private fun showDeviceAvailableServices(services: String) {
        tv_gatt_services?.text = services
    }

    private fun showReceivedData(data: ByteArray) {
        if (data.isNotEmpty()) {
            tv_gatt_state?.text = data[1].toString()
            tv_gatt_state?.visibility = View.VISIBLE
        }
    }

    private fun updateConnectionState(connectionState: DeviceConnectionState) {
        when (connectionState) {
            DeviceConnectionState.CONNECTING -> {
                tv_connection_state?.setText(R.string.connect_device_screen_connecting)
            }
            DeviceConnectionState.CONNECTED -> {
                tv_connection_state?.setText(R.string.connect_device_screen_connected)
            }
            DeviceConnectionState.DISCONNECTED -> {
                tv_connection_state?.setText(R.string.connect_device_screen_disconnected)
            }
        }
    }

    override fun onStop() {
        bleActivity.disconnectDevice()
        super.onStop()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply {
            initialActionBarColor = window?.statusBarColor ?: 0
            window?.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        }
    }


    private fun parseArguments(savedInstanceState: Bundle?) {
        val params = savedInstanceState ?: arguments
        params?.let {
            device = it.getParcelable(ARG_BLUETOOTH_DEVICE) as? BluetoothDevice
        }
    }
    private fun initViews() {
        val deviceAddress = device?.address ?: ""
        val deviceName = device?.name ?: ""
        val title = if (deviceName.isBlank()) {
            deviceAddress
        } else {
            "$deviceName\n$deviceAddress"
        }


        tv_title?.text = title
        btn_close?.setOnClickListener {
            updateConnectionState(DeviceConnectionState.DISCONNECTED)
            activity?.onBackPressed()
        }
        updateConnectionState(DeviceConnectionState.CONNECTING)

    }




    companion object {
        private val TAG = DeviceConnectionFragment::class.java.simpleName

        private const val ARG_DEVICE_TYPE = "ARG_DEVICE_TYPE"
        private const val ARG_BLUETOOTH_DEVICE = "ARG_BLUETOOTH_DEVICE"

        fun newInstance( device: BluetoothDevice): Fragment {
            return DeviceConnectionFragment().apply {
                arguments = Bundle().also {
                    it.putParcelable(ARG_BLUETOOTH_DEVICE, device)
                }
            }
        }
    }
}
enum class DeviceConnectionState {
    CONNECTING, CONNECTED, DISCONNECTED
}
