package com.baehoons.ble_test3

import android.bluetooth.BluetoothDevice
import com.baehoons.ble_test3.utils.DeviceManager

interface BleActivity {
    fun onDeviceClicked(device: BluetoothDevice)

    fun disconnectDevice()

    fun connectDevice(device: BluetoothDevice, listener: DeviceManager.DeviceStatusListener)
}
