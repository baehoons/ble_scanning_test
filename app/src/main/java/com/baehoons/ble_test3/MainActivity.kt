package com.baehoons.ble_test3

import android.Manifest
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.baehoons.ble_test3.utils.DeviceManager
import com.baehoons.ble_test3.utils.showFragment
import com.baehoons.ble_test3.utils.showFragmentAsRoot
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(),BleActivity {
    companion object {
        const val REQUEST_ENABLE_BT = 1
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }
    private lateinit var deviceManager: DeviceManager


    var autoConnect: Boolean = false
    val bluetoothState = ObservableField(BluetoothState.STATE_DISCONNECTED)
    enum class BluetoothState(val stateCode : Int) {
        STATE_DISCONNECTED(0x00000000),
        STATE_CONNECTING(0x00000001),
        STATE_CONNECTED(0x00000002),
        STATE_DISCONNECTING(0x00000003);
        companion object {
            internal fun getStateFromCode(stateCode : Int) : BluetoothState = values().find { it.stateCode == stateCode }!!
        }
    }
    //private lateinit var deviceManager: DeviceManager
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var bluetoothGatt : BluetoothGatt?=null
    val rssi = ObservableField<Int>()

    private val mCharacteristicList = ArrayList<BluetoothGattCharacteristic>()
    private var mIsServiceDiscovered = false
    private var mIsBluetoothOnProgress = false
    private val deviceListAdapter = MainAdapter().apply {
        onDeviceClickListener = {onDeviceClicked(it)}
    }
    /* Listen for scan results */
    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {


            if(result.device.type!=0){
                deviceListAdapter.addDevice(result.device)
                Log.d("BaeHun", "${result.device.name?:"noNmae"}")
                //result.device.connectGatt(this@MainActivity,autoConnect,mBluetoothGattCallback)
            }
        }
}
    private var mBluetoothGattCallback = object : BluetoothGattCallback(){
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            this@MainActivity.rssi.set(rssi)
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            bluetoothState.set(BluetoothState.getStateFromCode(newState))
            if(newState == BluetoothProfile.STATE_CONNECTED){

            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            gatt?.let { bluetoothGatt ->
                mCharacteristicList.clear()
                bluetoothGatt.services.forEach { eachGattService ->
                    mCharacteristicList.addAll(eachGattService.characteristics)
                }
            }
            mIsServiceDiscovered = true

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        deviceManager = DeviceManager(this)
        initBLE()
        initUI()


    }


    private fun initUI() {

        title = getString(R.string.ble_scanner)

        recycler_view_devices.adapter = deviceListAdapter
        recycler_view_devices.layoutManager = LinearLayoutManager(this)
        recycler_view_devices.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        button_discover.setOnClickListener {
            with(button_discover) {
                if (text == getString(R.string.start_scanning)) {
                    deviceListAdapter.clearDevices()
                    text = getString(R.string.stop_scanning)
                    progress_bar.visible()
                    startScanning()
                } else {
                    text = getString(R.string.start_scanning)
                    progress_bar.invisible()
                    stopScanning()
                }
            }
        }

    }

    private fun initBLE() {

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_COARSE_LOCATION
            )
        }

    }

    private fun startScanning() {
        AsyncTask.execute { bluetoothLeScanner.startScan(leScanCallback) }
    }

    private fun stopScanning() {
        AsyncTask.execute { bluetoothLeScanner.stopScan(leScanCallback) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted
                } else {
                    toast("Without location access, this app cannot discover beacons.")
                }
            }
        }
    }
    fun toast(message: String){
        Toast.makeText(this, message , Toast.LENGTH_SHORT).show()
    }

    fun View.visible() { this.visibility = View.VISIBLE }

    fun View.invisible() { this.visibility = View.GONE }

    override fun onDeviceClicked(device: BluetoothDevice){
        supportFragmentManager.showFragment(R.id.container, DeviceConnectionFragment.newInstance(device))
        button_discover.text = getString(R.string.start_scanning)
        progress_bar.invisible()
        stopScanning()
        mains.visibility = View.INVISIBLE
    }

    override fun disconnectDevice() {
        deviceManager.closeConnection()
        deviceManager.setDeviceStatusListener(null)
    }


    override fun connectDevice(
        device: BluetoothDevice,
        listener: DeviceManager.DeviceStatusListener
    ) {
        deviceManager.setDeviceStatusListener(listener)
        deviceManager.connectDevice(device)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.size >= 1) {
            supportFragmentManager.popBackStack()
            mains.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }
}