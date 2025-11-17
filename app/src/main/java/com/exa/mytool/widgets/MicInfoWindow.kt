package com.aam.mida.mida_yk.widgets

import android.bluetooth.BluetoothGatt
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aam.loglibs.LogUtils
import com.aam.mida.mida_yk.R
import com.aam.mida.mida_yk.YKApplication
import com.aam.mida.mida_yk.adapter.MicrophoneBatteryAdapter
import com.aam.mida.mida_yk.blemic.BleMicManager
import com.aam.mida.mida_yk.blemic.BleMicManagerCallback
import com.aam.mida.mida_yk.databinding.WindowMicInfoBinding
import com.aam.mida.mida_yk.entity.MicrophoneBatteryEntity
import com.aam.mida.mida_yk.manager.BleMicDbManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 *
 * @Description 麦克风信息弹窗
 * @Author zechao.zhang
 * @CreateTime 2024/10/29
 */
class MicInfoWindow(var context: Context?): PopupWindow() {

    private val TAG = "MicInfoWindow"

    private val mDataBinding: WindowMicInfoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.window_mic_info,
            null, false)

    private val scope = MainScope()

//    private val mMicrophoneList = mutableListOf<MicrophoneBatteryEntity>()
    private var mMicrophoneBatteryAdapter = MicrophoneBatteryAdapter()

    /**
     * 麦克风电池信息
     */
    private val micBatteryInfoMap = mutableMapOf<String,Int>()

    /**
     * channelInfo
     */
    private val micChannelInfoMap = mutableMapOf<String,Int>()


    private val micCallback = object: BleMicManagerCallback {
        override fun onBlueMicStatusChangeCallback(
            module: String,
            direction: String,
            address: String?,
            state: Int,
            reason: String,
            isDelete: Boolean
        ) {
            if(state == BluetoothGatt.STATE_DISCONNECTED){
                if(!TextUtils.isEmpty(address)) {
                    synchronized(micBatteryInfoMap) {
                        if (micBatteryInfoMap.contains(address)) {
                            micBatteryInfoMap.remove(address)
                            notifyBatteryInfo()
                        }
                    }

                }
            }
        }

        override fun onBlueMicBatteryChangeCallback(
            module: String,
            direction: String,
            address: String?,
            battery: Int
        ) {
            if(!TextUtils.isEmpty(address)) {
                LogUtils.i(TAG, "battery chang in system status view")
                synchronized(micBatteryInfoMap) {
                    micBatteryInfoMap[address!!] = battery
                }
                notifyBatteryInfo()

            }
        }

        override fun onBlueMicRssiChangeCallback(
            module: String,
            direction: String,
            address: String?,
            rssi: Int
        ) {

        }

        override fun onBlueMicFreqPointChangeCallback(
            module: String,
            direction: String,
            address: String?,
            channel: Int,
            roomId: Short?
        ) {

        }

        override fun onBlueMicVersionChangeCallback(
            module: String,
            direction: String,
            address: String?,
            version: String
        ) {

        }

        override fun onInfraredMicNotifyCallback(
            module: String,
            direction: String,
            address: String?,
            battery: Int,
            channel: Int
        ) {

        }

        override fun onBlueMicKeyPressCallback(
            module: String,
            direction: String,
            address: String?,
            keyStatus: Int
        ) {
            //按键按下消息
        }

        override fun onUendModuleCallback(mModule: String, alive: Boolean) {

        }

        override fun onUendVersionCallback(module: String, version: Int) {

        }

        override fun onUendFrequencyPointCallback(module: String, direction: String, channel: Int) {
            val address = BleMicDbManager.getBleMicAddress(module, direction)
            if(!TextUtils.isEmpty(address)){
                micChannelInfoMap[address] = channel
                notifyBatteryInfo()
            }

        }

        override fun onBluetoothDeviceDiscoverCallback(name: String, address: String) {

        }

    }

    private val channel = Channel<List<MicrophoneBatteryEntity>>(Channel.CONFLATED)

    init {
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT

        mDataBinding.rvMicrophoneBattery.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.VERTICAL, false)
        mDataBinding.rvMicrophoneBattery.adapter = mMicrophoneBatteryAdapter

        val verticalSpacing = context?.resources?.getDimension(R.dimen.px8)?.toInt()?: 0
        mDataBinding.rvMicrophoneBattery.addItemDecoration(object: RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val itemPosition = parent.getChildAdapterPosition(view)
                if (itemPosition > 0) {
                    outRect.top = verticalSpacing
                }
            }
        })
        initBattery()

        contentView = mDataBinding.root

        isOutsideTouchable = true

        scope.launch(Dispatchers.Default) {
            while (isActive) {
                val newList = channel.receive()
                val oldList = mutableListOf<MicrophoneBatteryEntity>()
                // mMicrophoneBatteryAdapter.data
                oldList.addAll(mMicrophoneBatteryAdapter.data)
                withContext(Dispatchers.IO) {
                    DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                        override fun getOldListSize() = oldList.size

                        override fun getNewListSize() = newList.size

                        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                            return oldList[oldItemPosition].mac == newList[newItemPosition].mac
                        }

                        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                            return oldList[oldItemPosition].name == newList[newItemPosition].name &&
                                oldList[oldItemPosition].battery == newList[newItemPosition].battery &&
                                oldList[oldItemPosition].channel == newList[newItemPosition].channel &&
                                oldList[oldItemPosition].time == newList[newItemPosition].time
                        }

                        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any {
                            val bundle = Bundle()
                            if (oldList[oldItemPosition].name != newList[newItemPosition].name) {
                                bundle.putString("name", newList[newItemPosition].name)
                            }
                            if (oldList[oldItemPosition].battery != newList[newItemPosition].battery) {
                                bundle.putInt("battery", newList[newItemPosition].battery)
                            }
                            if (oldList[oldItemPosition].channel != newList[newItemPosition].channel) {
                                bundle.putInt("channel", newList[newItemPosition].channel)
                            }
                            if (oldList[oldItemPosition].time != newList[newItemPosition].time) {
                                bundle.putString("time", newList[newItemPosition].time)
                            }
                            return bundle
                        }
                    })
                }.let {
                    withContext(Dispatchers.Main) {
                        mMicrophoneBatteryAdapter.data.clear()
                        mMicrophoneBatteryAdapter.data.addAll(newList)
                        it.dispatchUpdatesTo(mMicrophoneBatteryAdapter)
                    }
                }

//                mMicrophoneList.clear()
//                mMicrophoneList.addAll(newList)

//                withContext(Dispatchers.Main) {
//                    result.dispatchUpdatesTo(mMicrophoneBatteryAdapter)
//                }
                delay(2000)
            }
        }
    }

    private fun initBattery() {
        BleMicManager.addCallback(micCallback)
        val onlineMicList = BleMicManager.getAllConnectedMicMac()
        for (mic in onlineMicList) {
            micBatteryInfoMap[mic] = -1
        }

        notifyBatteryInfo()
        BleMicManager.refreshAllOnlineMicInfo()
    }

    private fun notifyBatteryInfo() {
        val batteryList = mutableListOf<Pair<String,Int>>()
        synchronized(micBatteryInfoMap){
            for(kv in micBatteryInfoMap){
                batteryList.add(Pair(kv.key,kv.value))
            }
        }
        scope.launch {
            try {
                updateMicTitleHint(batteryList)
                updateMicInfoViews(batteryList)
            } catch (e: Exception) {
                LogUtils.e(TAG, e)
            }
        }
    }

    private fun updateMicInfoViews(micList: List<Pair<String, Int>>) {
        val newList = mutableListOf<MicrophoneBatteryEntity>()
        for (index in micList.indices) {
            val micAddress = BleMicDbManager.getBtMicrophoneAddress(micList[index].first)?:continue
            val moduleDirection = BleMicDbManager.getNameIndexModuleLeftRight(micAddress.nameIndex)?:continue
            val name = if(moduleDirection.second == BleMicManager.CHANNEL_LEFT){
                YKApplication.app.getString(R.string.str_mic_a)
            }else{
                YKApplication.app.getString(R.string.str_mic_b)
            }
            val channel = BleMicManager.getMicChannel(micList[index].first)
            newList.add(MicrophoneBatteryEntity(
                micList[index].first,
                name,
                micList[index].second, micList[index].second.toString(),channel))
        }

        scope.launch {
            channel.send(newList)
        }
    }

    private fun updateMicTitleHint(micList: List<Pair<String, Int>>) {
        var isMicTitleHint = false
        for ((_, battery) in micList) {
            if (battery in 0..20) {
                isMicTitleHint = true
                break
            }
        }

        scope.launch(Dispatchers.Main) {
            mDataBinding.micTitleHint.isVisible = isMicTitleHint
            ConstraintSet().apply {
                clone(mDataBinding.mainLayout)
                if (isMicTitleHint) {
                    // 提示可见
                    val margin = context?.resources?.getDimension(R.dimen.px16)?.toInt()?: 0
                    connect(mDataBinding.rvMicrophoneBattery.id, ConstraintSet.TOP, mDataBinding.micTitleHint.id,
                        ConstraintSet.BOTTOM, margin)
                } else {
                    val margin = context?.resources?.getDimension(R.dimen.px32)?.toInt()?: 0
                    connect(mDataBinding.rvMicrophoneBattery.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP, margin)
                }
            }.applyTo(mDataBinding.mainLayout)
        }
    }

    override fun showAsDropDown(anchor: View) {
        // -abs(anchor.measuredWidth - (context?.resources?.getDimension(R.dimen.px420)?.toInt() ?: 0))
        super.showAsDropDown(anchor, -abs(anchor.measuredWidth / 2 - (context?.resources?.getDimension(R.dimen.px207)?.toInt() ?: 0)),
            context?.resources?.getDimension(R.dimen.px8)?.toInt() ?: 0)
    }

    override fun dismiss() {
        super.dismiss()

        BleMicManager.removeCallback(micCallback)
        context = null
        mDataBinding.unbind()
        scope.cancel()
    }
}