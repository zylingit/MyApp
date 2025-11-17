package com.exa.mytool.utils

import android.util.Log
import com.aam.loglibs.LogUtils
import com.aam.mida.base.GlobalVariable
import com.aam.mida.mida_yk.entity.QToken
import com.aam.mida.mida_yk.net.RetrofitManager
import com.aam.mida.mida_yk.room.entity.KtvRecordDataEntity
import com.qiniu.android.http.ResponseInfo
import com.qiniu.android.storage.UpCancellationSignal
import com.qiniu.android.storage.UpCompletionHandler
import com.qiniu.android.storage.UploadOptions
import com.qiniu.android.utils.Etag
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.FileInputStream

class QiNiuUploadClient(private val uploadStatusListener: UploadStatusListener) {

    private val TAG = "QiNiuUploadClient"

    private val scope = MainScope()


    var isCancel = false


    private val cancellationSignal = UpCancellationSignal {
        isCancel
    }

    fun starUploadFile(filePath: String,soundType: Int, recordDataEntity: KtvRecordDataEntity)  {
        scope.launch {
            val data = mutableMapOf(
                "file_key" to  if (soundType == 2){
                     "${recordDataEntity.hRelationID}.aac"
                } else {
                    "${recordDataEntity.mRelationID}.aac"
                },
            )
            val params = mutableMapOf<String, Any>()
            params["data"] = data
            try {
                val api = RetrofitManager.api
                val qToken = if (soundType == 2){
                    api.getRecordUploadDryToken(params)
                } else {
                    api.getRecordUploadToken(params)
                }
                qToken.data?.let {
                    upload2Qiniu(filePath, soundType, recordDataEntity, it)
                }
            } catch (e: Exception) {
                uploadStatusListener.onFail(recordDataEntity.id)
                LogUtils.e(
                    TAG, "get token fail:}"
                )
            }
        }
    }

    private fun upload2Qiniu(
        filePath: String,
        soundType: Int,
        recordDataEntity: KtvRecordDataEntity,
        qToken: QToken
    ) {
        val data = CustomVar(
            uid = recordDataEntity.uid.toString(),
            mid = GlobalVariable.mid.toString(),
            rid = if (soundType == 2){recordDataEntity.hRelationID} else {recordDataEntity.mRelationID},//uploadEntity.fileName,
            path = filePath,
            name = if (filePath.isNotEmpty()) {
                filePath.substring(filePath.lastIndexOf("/")+1, filePath.length)
            } else {
                ""
            },
            songId = recordDataEntity.songId,
            songName = recordDataEntity.songName,
            singer = recordDataEntity.artist,
            startTime = "${recordDataEntity.time / 1000}",
            endTime = "",
            duration = "${recordDataEntity.duration}",
            coverUrl = recordDataEntity.coverUrl,
            finish_time = "${recordDataEntity.time}",
            relationID = if (soundType == 2){recordDataEntity.mRelationID}else{""}
        )

        /**
         * params	Map<String, String>	自定义变量，key 必须以 x: 开始
         * mimeType	String	指定文件的 mimeType
         * progressHandler	UpProgressHandler	上传进度回调
         * cancellationSignal	UpCancellationSignal	取消上传，当 isCancelled() 返回 true 时，不再执行更多上传
         */
        val opt = UploadOptions(
            data.getParams(), null, true, { key, percent ->
                Log.d(TAG, "$key --progress--:$percent")
                uploadStatusListener.onProgress(recordDataEntity.id, percent)
            },
            cancellationSignal
        )

        /**
         * data:	byte[]/String/File	数据，可以是 byte 数组、文件路径、文件、数据流和 Uri 资源
         * key:	String	保存在服务器上的资源唯一标识
         * token:	String	服务器分配的 token
         * completionHandler:	UpCompletionHandler	上传回调函数，必填
         * options:	UploadOptions	如果需要进度通知、中途取消、指定 mimeType，则需要填写相应字段，详见下面的 UploadOptions 参数说明
         */
        KTVRecordUploadManager.uploadManager.put(
            data.path, qToken.file_key, qToken.token,
            object : UpCompletionHandler {
                override fun complete(key: String?, info: ResponseInfo?, response: JSONObject?) {
                    LogUtils.d(TAG, "complete--0:$key")
                    if (info != null) {
                        if (info.isOK) {
                            LogUtils.d(TAG, "complete finish--1")
                            uploadStatusListener.onFinish(recordDataEntity.id)
                        } else {
                            LogUtils.e(TAG, "complete fail isCancel--2：$isCancel")
                            if (isCancel) {
                                LogUtils.e(TAG, "isCancel--3")
                                uploadStatusListener.onCancel(recordDataEntity.id)
                            }else{
                                uploadStatusListener.onFail(recordDataEntity.id)
                            }
                        }
                    }
                }
            },
            opt
        )
    }

    data class CustomVar(
        var path: String = "", //录音文件
        var name: String = "", //录音文件名 <uid_time.wav>
        var appId: String = GlobalVariable.productKey,
        //用户id
        var uid: String = "",
        //机器id
        var mid: String = "",
        //录音id
        var rid: String = "",
        //对应的歌曲id
        var songId: String = "",
        //歌曲名称
        var songName: String = "",
        var singer: String = "",
        //录音时长
        var duration: String = "",
        //录音开始时间
        var startTime: String = "",
        //录音结束时间
        var endTime: String = "",
        //封面
        var coverUrl: String = "",
        var finish_time: String = "",
        var relationID: String = "",
        var url: String = ""
    ) {
        //文件eTag值
        var fileHash: String = ""

        //文件大小
        var fileSize: String = ""

        init {
            fileHash = Etag.file(path)
            val fis = FileInputStream(path)
            fileSize = fis.available().toString()
            fis.close()
            url = "$rid.aac"
        }

        fun getParams(): Map<String, String> {
            val params = HashMap<String, String>()
            params["x:app_id"] = appId
            params["x:Uid"] = uid
            params["x:Mid"] = mid
            params["x:Songid"] = songId
            params["x:Fsize"] = fileSize
            params["x:Fhash"] = fileHash
            params["x:PlayTime"] = duration
            params["x:play_time"] = duration
            params["x:SongName"] = songName
            params["x:Jobid"] = rid
            params["x:Stime"] = startTime
            params["x:Ftime"] = endTime
            params["x:cover_url"] = coverUrl
            params["x:finish_time"] = startTime
            params["x:RelationID"] = relationID
            params["x:url"] = url
            return params
        }
    }


    interface UploadStatusListener{
        fun onProgress(id: Long, percent: Double)
        fun onCancel(id: Long)
        fun onFail(id: Long)
        fun onFinish(id: Long)
    }

}