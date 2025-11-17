package com.exa.mytool.utils

import android.util.Log
import com.aam.loglibs.LogUtils
import com.aam.mida.base.GlobalVariable
import com.aam.mida.base.base.appContext
import com.aam.mida.base.utils.TimeTampUtils
import com.aam.mida.mida_yk.room.RepositoryProvider
import com.aam.mida.mida_yk.room.entity.KtvRecordDataEntity
import com.qiniu.android.storage.Configuration
import com.qiniu.android.storage.UploadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object KTVRecordUploadManager {

    private val TAG = "KTVRecordUploadManager"

    private val uploadFinishMap = mutableMapOf<Long, Int>()
    private val uploadClientMap = mutableMapOf<Long, QiNiuUploadClient>()

    val recorderUploadListeners = mutableListOf<QiNiuUploadClient.UploadStatusListener>()

    private val scope = MainScope()

    val uploadManager by lazy {
        val configuration = Configuration.Builder()
            .connectTimeout(90)              // 链接超时。默认90秒
            .useHttps(true)                  // 是否使用https上传域名
            .useConcurrentResumeUpload(true) // 使用并发上传，使用并发上传时，除最后一块大小不定外，其余每个块大小固定为4M，
            .concurrentTaskCount(2)          // 并发上传线程数量为3
            .responseTimeout(90)             // 服务器响应超时。默认90秒
//            .recorder(recorder)              // recorder分片上传时，已上传片记录器。默认null
//            .recorder(recorder, keyGen)      // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录
            //.zone(FixedZone.zone0)           // 设置区域，不指定会自动选择。指定不同区域的上传域名、备用域名、备用IP。
            .build()
        UploadManager(configuration)
    }


    private val uploadStatusListener = object : QiNiuUploadClient.UploadStatusListener {
        override fun onProgress(id: Long, percent: Double) {
            scope.launch {
                if (percent in (0.01..0.3)) {
                    updateDB(1, id)
                    Log.d(TAG, "onProgress $id $percent")
                }
                recorderUploadListeners.forEach {
                    it.onProgress(id, percent)
                }
            }

        }

        override fun onCancel(id: Long) {
            scope.launch {
                updateDB(0, id)
                recorderUploadListeners.forEach {
                    it.onCancel(id)
                }
                uploadFinishMap.remove(id)
                uploadClientMap.remove(id)
                Log.d(TAG, "onCancel $id")
            }
        }

        override fun onFail(id: Long) {
            scope.launch {
                updateDB(0, id)
                recorderUploadListeners.forEach {
                    it.onFail(id)
                }
                uploadFinishMap.remove(id)
                uploadClientMap.remove(id)
                Log.d(TAG, "onFail $id")
            }
        }

        override fun onFinish(id: Long) {
            scope.launch {
                val finished = uploadFinishMap[id]
                if (finished != null) {
                    //第二个完成
                    uploadFinishMap.remove(id)
                    uploadClientMap.remove(id)
                    updateDB(2, id)
                    LogUtils.d(TAG, "onFinish $id count:2")
                    recorderUploadListeners.forEach {
                        it.onFinish(id)
                    }
                } else {
                    //第一个完成
                    uploadFinishMap[id] = 1
                    LogUtils.d(TAG, "onFinish $id count:1")
                }
            }

        }

    }

    private suspend fun updateDB(status: Int, id: Long) = withContext(Dispatchers.IO) {
        val recordeRoomProvider = RepositoryProvider.ktvRecorderRepository(appContext)
        val findData = recordeRoomProvider.getRecorder(id)
        findData?.let {
            it.status = status
            recordeRoomProvider.updateRecorder(it)
        }
    }


    fun scheduleUpload(recorderDataEntity: KtvRecordDataEntity) {

        if (File(recorderDataEntity.recorder_mix_path).exists()) {
            recorderDataEntity.mRelationID = createJobId()
            createUploadClient(recorderDataEntity.recorder_mix_path, 1, recorderDataEntity)
        }
        if (File(recorderDataEntity.recorder_human_path).exists()) {
            recorderDataEntity.hRelationID = createJobId()
            createUploadClient(recorderDataEntity.recorder_human_path, 2,  recorderDataEntity)
        }


    }

    private fun createUploadClient(filePath: String, songType: Int, recorderDataEntity: KtvRecordDataEntity) {

        if (uploadClientMap[recorderDataEntity.id] == null) {
            val qiNiuUploadClient = QiNiuUploadClient(uploadStatusListener)
            qiNiuUploadClient.starUploadFile(filePath,songType, recorderDataEntity)
            uploadClientMap[recorderDataEntity.id] = qiNiuUploadClient
        } else {
            uploadClientMap[recorderDataEntity.id]?.starUploadFile(filePath,songType, recorderDataEntity)
        }

    }

    private fun createJobId(): String {
        return "${GlobalVariable.mid}_HI_${System.currentTimeMillis()}_${TimeTampUtils.createUUID()}"
    }


}