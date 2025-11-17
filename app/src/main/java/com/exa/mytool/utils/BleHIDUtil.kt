package com.aam.mida.mida_yk.utils

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.aam.loglibs.LogUtils
import com.aam.mida.mida_yk.entity.BleBean
import kotlinx.coroutines.*

class BleHIDUtil {

    private val TAG = "yk-kBleHIDUtil"

    /**
     * 蓝牙遥控的名称
     */
    private val TARGET_BLE_NAME = "Amel RC3"

    private var mContext: Context? = null

    private var mBluetoothAdapter: BluetoothAdapter? = null

    private var mBluetoothManager: BluetoothManager? = null

    private val bleBroadcastReceiver = BleBroadcastReceiver()

    private var mBluetoothDevice: BluetoothHidDevice? = null

    private var isReceiverRegister = false

    private val scope = CoroutineScope(Dispatchers.IO)

    var onBondDeviceListChange: ((List<BleBean>) -> Unit)? = null

    private val mListener = object: BluetoothProfile.ServiceListener {
        @SuppressLint("MissingPermission")
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            LogUtils.d(TAG, "onServiceConnected")
            if (profile == BluetoothProfile.HID_DEVICE) {
                mBluetoothDevice = proxy as? BluetoothHidDevice
            }

            getBondDeviceList()
        }

        override fun onServiceDisconnected(profile: Int) {
            LogUtils.d(TAG, "onServiceDisconnected")
        }

    }

    fun init(context: Context) {
        LogUtils.d(TAG, "init")
        this.mContext = context
        initBle()
    }

    @SuppressLint("MissingPermission")
    private fun initBle() {
        mContext?.let { context ->
            mBluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            mBluetoothAdapter = mBluetoothManager?.adapter
            if (mBluetoothAdapter?.isEnabled == false) {
                mBluetoothAdapter?.enable()
            }

            val intentFilter = IntentFilter()
//            intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
//            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
//            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            intentFilter.addAction("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED")
            context.registerReceiver(bleBroadcastReceiver, intentFilter)
            isReceiverRegister = true
        }

        try {
            mBluetoothAdapter?.getProfileProxy(mContext, mListener, BluetoothProfile.HID_DEVICE)
        } catch (e: Exception) {
            LogUtils.e(TAG, "init ble error", e)
        }
    }

    private fun isConnected(device: BluetoothDevice): Boolean {
        try {
            val method = BluetoothDevice::class.java.getMethod("isConnected")
            return method.invoke(device) as Boolean
        } catch (e: Exception) {
            Log.w(TAG, "反射失败", e)
            return false
        }
    }

    fun destroy() {
        LogUtils.d(TAG, "destroy")
        if (isReceiverRegister) {
            mContext?.unregisterReceiver(bleBroadcastReceiver)
            isReceiverRegister = false
        }

        this.mContext = null
        mBluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, mBluetoothDevice)
    }

    ///////////////////////////////////////////////////////////////////////////
    // 接收蓝牙连接信息
    ///////////////////////////////////////////////////////////////////////////

    inner class BleBroadcastReceiver: BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val bleName = device?.name
                    val address = device?.address

                    LogUtils.d(TAG, "bond state change: $bleName, $address, ${device?.bondState}")
                    if (device?.bondState == BluetoothDevice.BOND_BONDED
                        || device?.bondState == BluetoothDevice.BOND_NONE) {
                        getBondDeviceList()
                    }
                }
                "android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED" -> {
                    // 连接状态变化
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val bleName = device?.name
                    val address = device?.address
                    val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0)
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        // 连接成功
                        LogUtils.d(TAG, " 收到广播 connect to $bleName, $address")
                        getBondDeviceList()
                    } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        // 断开连接
                        LogUtils.d(TAG, "收到广播 disconnect to $bleName, $address")
                        getBondDeviceList()
                    }
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    fun getBondDeviceList() {
        scope.launch(Dispatchers.IO) {
            val state = intArrayOf(
                BluetoothProfile.STATE_DISCONNECTING,
                BluetoothProfile.STATE_DISCONNECTED,
                BluetoothProfile.STATE_CONNECTED,
                BluetoothProfile.STATE_CONNECTING
            )
//            mBluetoothManager?.getConnectedDevices()
            val connectedDevices = mBluetoothDevice?.getDevicesMatchingConnectionStates(state)?: emptyList()
            val bondedDevice = mBluetoothAdapter?.bondedDevices?: mutableListOf()
            val result = mutableListOf<BleBean>()
//            result.addAll(bondedDevice.map {
//                BleBean(it.name, it.address, 2)
//            })

            bondedDevice.filter { it.name == TARGET_BLE_NAME }.forEach {
                if (isConnected(it)) {
                    result.add(BleBean(it.name, it.address, 1))
                } else {
                    result.add(BleBean(it.name, it.address, 2))
                }
            }

            withContext(Dispatchers.Main) {
                onBondDeviceListChange?.invoke(result)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentBondDeviceList(): MutableList<BleBean> {
        val state = intArrayOf(
            BluetoothProfile.STATE_DISCONNECTING,
            BluetoothProfile.STATE_DISCONNECTED,
            BluetoothProfile.STATE_CONNECTED,
            BluetoothProfile.STATE_CONNECTING
        )
//            mBluetoothManager?.getConnectedDevices()
        val connectedDevices =
            mBluetoothDevice?.getDevicesMatchingConnectionStates(state) ?: emptyList()
        val bondedDevice = mBluetoothAdapter?.bondedDevices ?: mutableListOf()
        val result = mutableListOf<BleBean>()
//            result.addAll(bondedDevice.map {
//                BleBean(it.name, it.address, 2)
//            })

        bondedDevice.filter { it.name == TARGET_BLE_NAME }.forEach {
            if (isConnected(it)) {
                result.add(BleBean(it.name, it.address, 1))
            } else {
                result.add(BleBean(it.name, it.address, 2))
            }
        }
        return result
    }
}