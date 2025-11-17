package com.aam.mida.mida_yk.utils

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.storage.StorageManager
import android.text.TextUtils
import com.exa.mediacenter.ScanDiskUtils
import java.io.File
import java.io.IOException


object VersionUtil {
    private const val TAG = "VersionUtil"

    /**
     * 获取应用是否已安装
     */
    fun checkApkInstalled(context: Context, pkgName: String): Boolean {
        if (TextUtils.isEmpty(pkgName)) {
            return false
        }
        try {
            context.packageManager.getPackageInfo(pkgName, 0)
        } catch (x: Exception) {
            return false
        }
        return true
    }

    fun getVersionValidByteArray(arrayByte: ByteArray): ByteArray{
        var retByteArray = byteArrayOf()
        for(item in arrayByte){
            if(item in 48 .. 57 || item == 46.toByte()){
                retByteArray+= item
            }
        }
        return retByteArray
    }

    fun getVersionValidByteArray(str:String): String{
        var byteArray = byteArrayOf()
        for(item in str.toByteArray()){
            if(item in 48 .. 57 || item == 46.toByte()){
                byteArray+= item
            }
        }
        return String(byteArray)
    }


    fun compareVersion(ver1 :String, ver2 :String): Int {
        val version1 = ver1.trim().removePrefix("V").removePrefix("v").trim()
        val version2 = ver2.trim().removePrefix("V").removePrefix("v").trim()
        val ver1Str = version1.split(".")

        val ver2Str = version2.split(".")
        //取最小的长度
        val minLen = Math.min(ver2Str.size, ver1Str.size)
        for(i in 0 until minLen){

            var ver1Num = 0
            try {
                ver1Num = ver1Str[i].toInt()
            }catch (e:Exception){
                e.printStackTrace()
            }
            var ver2Num = 0
            try {
                ver2Num = ver2Str[i].toInt()
            }catch (e:Exception){
                e.printStackTrace()
            }
            if(ver1Num == ver2Num){
                continue
            }else if (ver1Num > ver2Num){
                return 1
            }else{
                return -1
            }
        }
        // 前面的数字都相同是，看版本号长度，长的大，短的小,相同长度则版本号相同
        if(ver1Str.size == ver2Str.size){
            return 0
        } else if(ver1Str.size > ver2Str.size ){
            return 1
        } else {
            return -1
        }

    }

    /**
     * 获取应用的logo,版本号，大小
     */
    fun getVersionInfo(context: Context, pkgName: String): Triple<Drawable, String, String>? {
        var result: Triple<Drawable, String, String>? = null
        if (checkApkInstalled(context, pkgName).not()) {
            return null
        }
        val manager: PackageManager = context.packageManager
        try {
            val info: PackageInfo = manager.getPackageInfo(pkgName, 0)
            val appInfo: ApplicationInfo = manager.getApplicationInfo(pkgName, 0)
            val loadIcon = appInfo.loadIcon(manager)
            val apkFile = File(appInfo.publicSourceDir)
            result =
                Triple(loadIcon, info.versionName, ScanDiskUtils.byteToMB(apkFile.length()) ?: "0")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return result
    }


    /**
     * 系统存储空间
     *
     * @return [String]
     */
    fun totalStorage(context: Context): MutableList<Long> {
        val size = mutableListOf<Long>()
        try {
            val storageStatsManager =
                context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            val availBytes = storageStatsManager.getFreeBytes(StorageManager.UUID_DEFAULT) //可用空间大小
            val totalBytes = storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT) //总空间大小
            size.add((availBytes))
            size.add((totalBytes))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 将字节数转化为 KB MB GB
     */
    fun byteToMB(size: Long): String {
        val kb: Long = 1000
        val mb = kb * 1000
        val gb = mb * 1000
        return if (size >= gb) {
            String.format("%.1f GB", size.toFloat() / gb)
        } else if (size >= mb) {
            val f = size.toFloat() / mb
            String.format(if (f > 100) "%.0f MB" else "%.1f MB", f)
        } else if (size > kb) {
            val f = size.toFloat() / kb
            String.format(if (f > 100) "%.0f KB" else "%.1f KB", f)
        } else {
            String.format("%d B", size)
        }
    }


    fun longToInt(size: Long): Int {
        val kb: Long = 1000
        val mb = kb * 1000
        val gb = mb * 1000
        return (size / gb).toInt()
    }
}