package com.baehoons.ble_test3.utils

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import com.baehoons.ble_test3.models.BluetoothStatus
import com.google.gson.JsonObject
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.*

class DeviceManager(context: Context) {

    private val contextReference = WeakReference(context)

    private var bluetoothGatt: BluetoothGatt? = null
    private var deviceStatusListener: DeviceStatusListener? = null
    private val characteristicList = ArrayList<BluetoothGattCharacteristic>()


    companion object {
        private val TAG = DeviceManager::class.java.simpleName

    val MONITOR_SERVICE_UUID = toUUID(0x180d)
    val CHARACTERISTICS_HEART_RATE_UUID = toUUID(0x2a37)

    private fun toUUID(value: Int): UUID {
        val msb = 0x0000000000001000L
        val lsb = -0x7fffff7fa064cb05L
        val result = (value and -0x1).toLong()
        return UUID(msb or (result shl 32), lsb)
    }
}


    fun setDeviceStatusListener(listener: DeviceStatusListener?) {
        this.deviceStatusListener = listener
    }



    fun connectDevice(device: BluetoothDevice) {
        if (isDeviceConnected()) {
            Log.e(TAG, "Only 1 device can be connected at a time")
            return
        }

        bluetoothGatt = device.connectGatt(
                contextReference.get(),
                false,
                object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                        Log.d(TAG, "onConnectionStateChange Status: $status")
                        when (newState) {
                            BluetoothProfile.STATE_CONNECTED -> {
                                Log.d(TAG, "Device Connected")
                                gatt.discoverServices()
                            }
                            BluetoothProfile.STATE_DISCONNECTED -> {
                                Log.e(TAG, "Device Disconnected")

                                deviceStatusListener?.onDisconnect(gatt)
                                closeConnection()
                            }
                        }
                        when (status) {
                            BluetoothGatt.GATT_FAILURE -> {
                                Log.e(TAG, "Device connection failure")

                                deviceStatusListener?.onDisconnect(gatt)
                                closeConnection()
                            }
                        }
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {


                        gatt.services.forEach {
                            characteristicList.addAll(it.characteristics)
                        }

                        characteristicList.find {
                            it.uuid.toString() == "aaaaaaaa-bbbb-cccc-dddd-eeeeeeee0101"
                        }?.let { characteristic ->
                            gatt.writeCharacteristic(characteristic.apply {
                                val jsonData = JSONObject().apply {
                                    this.accumulate("userId", "Uv4OywyiZZhlJDvtm8JCH48WIs03")
                                    this.accumulate("loginType", 2)
                                }
                                setValue(jsonData.toString())
                                writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            })
                        }


                        for (service in gatt.services) {
                            Log.d(TAG, String.format("Service UUID: (%s)", service.uuid))

                            for (characteristic in service.characteristics) {
                                Log.d(TAG, String.format("Service CHARACT UUID: (%s)", characteristic.uuid))

                                for (descriptor in characteristic.descriptors) {
                                    Log.d(TAG, String.format("Service DESCRIPTOR UUID: (%s)", descriptor.uuid))
                                }
                            }
                        }



                        deviceStatusListener?.onConnect(gatt)
                    }

                    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                        Log.d(TAG, String.format("onCharacteristicRead %s, status=%d", characteristic.uuid, status))
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            handleCharacteristics(characteristic)
                        }
                    }

                    override fun onCharacteristicWrite(
                        gatt: BluetoothGatt?,
                        characteristic: BluetoothGattCharacteristic?,
                        status: Int
                    ) {
                        super.onCharacteristicWrite(gatt, characteristic, status)

                        Log.d("ayteneve93_test", "$status")

                    }

                    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                        Log.d(TAG, String.format("onCharacteristicChanged %s", characteristic.uuid))
                        handleCharacteristics(characteristic)
                    }

                    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
                        Log.d(TAG, "Scanner onDescriptorWrite")
                        descriptor?.characteristic?.let { bluetoothGatt?.readCharacteristic(it) }
                    }
                })
    }

    fun closeConnection() {
        bluetoothGatt?.disconnect()
        bluetoothGatt = null
    }



    private fun isDeviceConnected() = bluetoothGatt != null


    private fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enable: Boolean) {
        bluetoothGatt?.setCharacteristicNotification(characteristic, enable)

        characteristic.descriptors?.forEach { descriptor ->
            descriptor?.let {
                it.value = if (enable) {
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                } else {
                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                }
                bluetoothGatt?.writeDescriptor(it)
            }
        }
    }

    private fun handleCharacteristics(characteristic: BluetoothGattCharacteristic) {
        when (characteristic.uuid) {
            CHARACTERISTICS_HEART_RATE_UUID -> {
                deviceStatusListener?.onDataReceived(characteristic)
            }
        }
    }

    interface DeviceStatusListener {
        fun onConnect(gatt: BluetoothGatt)

        fun onDataReceived(characteristic: BluetoothGattCharacteristic)

        fun onDisconnect(gatt: BluetoothGatt)
    }
}