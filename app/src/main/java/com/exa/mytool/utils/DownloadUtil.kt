package com.aam.mida.mida_yk.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.aam.loglibs.LogUtils
import com.aam.mida.mida_yk.net.ApiFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.isActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


sealed class DowloadStatus {
    class DowloadProcess(val currentLength: Long, val length: Long, val process: Float) :
        DowloadStatus()

    class DowloadErron(val t: Throwable) : DowloadStatus()
    class DowloadSuccess(val uri: Uri) : DowloadStatus()
}

abstract class IDowloadBuild {
    open fun getFileName(): String? = null
    open fun getUri(contentType: String): Uri? = null
    open fun getDownloadFile(): File? = null
    abstract fun getContext(): Context //贪方便的话，返回Application就行
}

class DownloadBuild(val cxt: Context) : IDowloadBuild() {
    override fun getContext(): Context = cxt
}

fun download(scope: CoroutineScope, url: String, build: IDowloadBuild) = flow {
    val request = Request.Builder().url(url).build()
    val response = try {
        ApiFactory.mClient.newCall(request).execute()
    } catch (e: Exception) {
        emit(DowloadStatus.DowloadErron(e))
        return@flow
    }

    response.body?.let { body ->
        val length = body.contentLength()
        val contentType = body.contentType().toString()
        val ios = body.byteStream()
        val info = try {
            Log.d("download", "star dowloadBuildToOutputStream")
            dowloadBuildToOutputStream(build, contentType)
        } catch (e: Exception) {
            emit(DowloadStatus.DowloadErron(e))
            DowloadInfo(null)
            return@flow
        }
        val ops = info.ops
        if (ops == null) {
            emit(DowloadStatus.DowloadErron(RuntimeException("下载出错")))
            return@flow
        }

        //下载的长度
        var currentLength: Long = 0
        //写入文件
        val bufferSize = 1024/* * 8*/
        val buffer = ByteArray(bufferSize)
        val bufferedInputStream = BufferedInputStream(ios, bufferSize)
        try {
            var readLength: Int = 0
            Log.d("download", "star read stream")
            while (scope.isActive && bufferedInputStream.read(buffer, 0, bufferSize)
                    .also { readLength = it } != -1
            ) {
                ops.write(buffer, 0, readLength)
                currentLength += readLength
                emit(
                    DowloadStatus.DowloadProcess(
                        currentLength.toLong(),
                        length,
                        currentLength.toFloat() / length.toFloat()
                    )
                )
            }
            if (info.uri != null) {
                emit(DowloadStatus.DowloadSuccess(info.uri))
            } else {
                emit(DowloadStatus.DowloadSuccess(Uri.fromFile(info.file)))
            }
        } catch (e: Exception) {
            emit(DowloadStatus.DowloadErron(e))
            DowloadInfo(null)
            return@flow
        } finally {
            bufferedInputStream.close()
            ops.flush()
            (ops as? FileOutputStream)?.fd?.sync()
            ops.close()
            ios.close()
        }

    } ?: kotlin.run {
        emit(DowloadStatus.DowloadErron(RuntimeException("下载出错")))
    }
}.flowOn(Dispatchers.IO)



suspend fun downloadSyn(url: String, build: IDowloadBuild): Boolean = withContext(Dispatchers.IO) {
    val request = Request.Builder().url(url).build()
    val response = try {
        ApiFactory.mClient.newCall(request).execute()
    }catch (e:Exception){
        LogUtils.e(e.message?:"")
        return@withContext false
    }

    if (response.code != 200) {
        return@withContext false
    }

    response.body?.let { body ->
        val length = body.contentLength()
        val contentType = body.contentType().toString()
        val ios = body.byteStream()
        val info = try {
            Log.d("download", "star dowloadBuildToOutputStream")
            dowloadBuildToOutputStream(build, contentType)
        } catch (e: Exception) {
            DowloadInfo(null)
            return@withContext false
        }
        val ops = info.ops ?: return@withContext false

        //下载的长度
        var currentLength: Long = 0
        //写入文件
        val bufferSize = 1024/* * 8*/
        val buffer = ByteArray(bufferSize)
        val bufferedInputStream = BufferedInputStream(ios, bufferSize)

        try {

            var readLength: Int = 0
            Log.d("download", "star read stream")
            while (bufferedInputStream.read(buffer, 0, bufferSize)
                    .also { readLength = it } != -1
            ) {
                ops.write(buffer, 0, readLength)
                currentLength += readLength
            }

            return@withContext true
        } catch (e: Exception) {
            DowloadInfo(null)
            return@withContext false
        } finally {
            bufferedInputStream.close()
            ops.flush()
            (ops as? FileOutputStream)?.fd?.sync()
            ops.close()
            ios.close()
        }

    } ?:return@withContext false
}

private fun dowloadBuildToOutputStream(build: IDowloadBuild, contentType: String): DowloadInfo {
    val context = build.getContext()
    val uri = build.getUri(contentType)
    return if (build.getDownloadFile() != null) {
        val file = build.getDownloadFile()!!
        DowloadInfo(FileOutputStream(file), file)
    } else if (uri != null) {
        DowloadInfo(context.contentResolver.openOutputStream(uri), uri = uri)
    } else {
        val name = build.getFileName()
        val fileName = if (!name.isNullOrBlank()) name else "${System.currentTimeMillis()}.${
            MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(contentType)
        }"
        val file = File("${context.filesDir}", fileName)
        DowloadInfo(FileOutputStream(file), file)
    }
}

private class DowloadInfo(val ops: OutputStream?, val file: File? = null, val uri: Uri? = null)