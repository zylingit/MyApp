package com.aam.mida.mida_yk.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.text.TextUtils
import androidx.annotation.ColorInt
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*
import com.google.zxing.MultiFormatWriter

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.lang.Exception


object QRCodeUtil {
    /**
     * 生成简单二维码
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param character_set          编码方式（一般使用UTF-8）
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @return BitMap
     */
    fun createQRCodeBitmap(
        content: String?, width: Int, height: Int, character_set: String?,
        error_correction_level: String?, margin: String?, color_black: Int,
        color_white: Int
    ): Bitmap? {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null
        }
        // 宽和高>=0
        return if (width < 0 || height < 0) {
            null
        } else try {
            /** 1.设置二维码相关配置  */
            val hints: MutableMap<EncodeHintType, String?> = mutableMapOf()
            // 字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set)
            }
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level)
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin)
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象  */
            var bitMatrix =
                QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)

            bitMatrix = deleteWhite(bitMatrix)
            val width = bitMatrix.width
            val height = bitMatrix.height

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值  */
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix[x, y]) {
                        pixels[y * width + x] = color_black //黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white // 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象  */
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    private fun deleteWhite(matrix: BitMatrix): BitMatrix? {
        val rec = matrix.enclosingRectangle
        val resWidth = rec[2] + 1
        val resHeight = rec[3] + 1
        val resMatrix = BitMatrix(resWidth, resHeight)
        resMatrix.clear()
        for (i in 0 until resWidth) {
            for (j in 0 until resHeight) {
                if ((i + rec[0]) < matrix.width && (j + rec[1]) < matrix.height
                    && matrix[i + rec[0], j + rec[1]]) {
                    resMatrix[i] = j
                }
            }
        }
        return resMatrix
    }

    /**
     * 创建二维码位图
     *
     * @param content 字符串内容
     * @param size 位图宽&高(单位:px)
     * @return
     */
    fun createQRCodeBitmap(content: String?, size: Int): Bitmap? {
        return createQRCodeBitmap(
            content,
            size,
            "UTF-8",
            "H",
            "0",
            Color.BLACK,
            Color.WHITE,
            null,
            null,
            0f
        )
    }

    /**
     * 创建二维码位图 (自定义黑、白色块颜色)
     *
     * @param content 字符串内容
     * @param size 位图宽&高(单位:px)
     * @param color_black 黑色色块的自定义颜色值
     * @param color_white 白色色块的自定义颜色值
     * @return
     */
    fun createQRCodeBitmap(
        content: String?,
        size: Int,
        @ColorInt color_black: Int,
        @ColorInt color_white: Int
    ): Bitmap? {
        return createQRCodeBitmap(
            content,
            size,
            "UTF-8",
            "H",
            "4",
            color_black,
            color_white,
            null,
            null,
            0f
        )
    }

    /**
     * 创建二维码位图 (带Logo小图片)
     *
     * @param content 字符串内容
     * @param size 位图宽&高(单位:px)
     * @param logoBitmap logo图片
     * @param logoPercent logo小图片在二维码图片中的占比大小,范围[0F,1F]。超出范围->默认使用0.2F
     * @return
     */
    fun createQRCodeBitmap(
        content: String?,
        size: Int,
        logoBitmap: Bitmap?,
        logoPercent: Float
    ): Bitmap? {
        return createQRCodeBitmap(
            content,
            size,
            "UTF-8",
            "H",
            "4",
            Color.BLACK,
            Color.WHITE,
            null,
            logoBitmap,
            logoPercent
        )
    }

    /**
     * 创建二维码位图 (Bitmap颜色代替黑色) 注意!!!注意!!!注意!!! 选用的Bitmap图片一定不能有白色色块,否则会识别不出来!!!
     *
     * @param content 字符串内容
     * @param size 位图宽&高(单位:px)
     * @param targetBitmap 目标图片 (如果targetBitmap != null, 黑色色块将会被该图片像素色值替代)
     * @return
     */
    fun createQRCodeBitmap(
        content: String?,
        size: Int,
        targetBitmap: Bitmap?
    ): Bitmap? {
        return createQRCodeBitmap(
            content,
            size,
            "UTF-8",
            "H",
            "4",
            Color.BLACK,
            Color.WHITE,
            targetBitmap,
            null,
            0f
        )
    }

    /**
     * 创建二维码位图 (支持自定义配置和自定义样式)
     *
     * @param content 字符串内容
     * @param size 位图宽&高(单位:px)
     * @param character_set 字符集/字符转码格式 (支持格式:[CharacterSetECI])。传null时,zxing源码默认使用 "ISO-8859-1"
     * @param error_correction 容错级别 (支持级别:[ErrorCorrectionLevel])。传null时,zxing源码默认使用 "L"
     * @param margin 空白边距 (可修改,要求:整型且>=0), 传null时,zxing源码默认使用"4"。
     * @param color_black 黑色色块的自定义颜色值
     * @param color_white 白色色块的自定义颜色值
     * @param targetBitmap 目标图片 (如果targetBitmap != null, 黑色色块将会被该图片像素色值替代)
     * @param logoBitmap logo小图片
     * @param logoPercent logo小图片在二维码图片中的占比大小,范围[0F,1F],超出范围->默认使用0.2F。
     * @return
     */
    fun createQRCodeBitmap(
        content: String?,
        size: Int,
        character_set: String?,
        error_correction: String?,
        margin: String?,
        @ColorInt color_black: Int,
        @ColorInt color_white: Int,
        targetBitmap: Bitmap?,
        logoBitmap: Bitmap?,
        logoPercent: Float
    ): Bitmap? {
        /** 1.参数合法性判断  */
        var targetBitmap = targetBitmap
        if (TextUtils.isEmpty(content)) { // 字符串内容判空
            return null
        }
        if (size <= 0) { // 宽&高都需要>0
            return null
        }
        try {
            /** 2.设置二维码相关配置,生成BitMatrix(位矩阵)对象  */
            val hints =
                Hashtable<EncodeHintType, String?>()
            if (!TextUtils.isEmpty(character_set)) {
                hints[EncodeHintType.CHARACTER_SET] = character_set // 字符转码格式设置
            }
            if (!TextUtils.isEmpty(error_correction)) {
                hints[EncodeHintType.ERROR_CORRECTION] = error_correction // 容错级别设置
            }
            if (!TextUtils.isEmpty(margin)) {
                hints[EncodeHintType.MARGIN] = margin // 空白边距设置
            }
            val bitMatrix: BitMatrix =
                QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            /** 3.根据BitMatrix(位矩阵)对象为数组元素赋颜色值  */
            if (targetBitmap != null) {
                targetBitmap =
                    Bitmap.createScaledBitmap(targetBitmap, size, size, false)
            }
            val pixels = IntArray(size * size)
            for (y in 0 until size) {
                for (x in 0 until size) {
                    if (bitMatrix.get(x, y)) { // 黑色色块像素设置
                        if (targetBitmap != null) {
                            pixels[y * size + x] = targetBitmap.getPixel(x, y)
                        } else {
                            pixels[y * size + x] = color_black
                        }
                    } else { // 白色色块像素设置
                        pixels[y * size + x] = color_white
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,之后返回Bitmap对象  */
            val bitmap = Bitmap.createBitmap(
                size,
                size,
                Bitmap.Config.ARGB_8888
            )
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
            /** 5.为二维码添加logo小图标  */
            return logoBitmap?.let { addLogo(bitmap, it, logoPercent) } ?: bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 向一张图片中间添加logo小图片(图片合成)
     *
     * @param srcBitmap 原图片
     * @param logoBitmap logo图片
     * @param logoPercent 百分比 (用于调整logo图片在原图片中的显示大小, 取值范围[0,1], 传值不合法时使用0.2F)
     * 原图片是二维码时,建议使用0.2F,百分比过大可能导致二维码扫描失败。
     * @return
     */
    private fun addLogo(
        srcBitmap: Bitmap?,
        logoBitmap: Bitmap?,
        logoPercent: Float
    ): Bitmap? {
        /** 1. 参数合法性判断  */
        var logoPercent = logoPercent
        if (srcBitmap == null) {
            return null
        }
        if (logoBitmap == null) {
            return srcBitmap
        }
        if (logoPercent < 0f || logoPercent > 1f) {
            logoPercent = 0.2f
        }
        /** 2. 获取原图片和Logo图片各自的宽、高值  */
        val srcWidth = srcBitmap.width
        val srcHeight = srcBitmap.height
        val logoWidth = logoBitmap.width
        val logoHeight = logoBitmap.height

        /** 3. 计算画布缩放的宽高比  */
        val scaleWidth = srcWidth * logoPercent / logoWidth
        val scaleHeight = srcHeight * logoPercent / logoHeight

        /** 4. 使用Canvas绘制,合成图片  */
        val bitmap = Bitmap.createBitmap(
            srcWidth,
            srcHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(srcBitmap, 0f, 0f, null)
        canvas.scale(scaleWidth, scaleHeight, srcWidth / 2.toFloat(), srcHeight / 2.toFloat())
        canvas.drawBitmap(
            logoBitmap,
            srcWidth / 2 - logoWidth / 2.toFloat(),
            srcHeight / 2 - logoHeight / 2.toFloat(),
            null
        )
        return bitmap
    }
}