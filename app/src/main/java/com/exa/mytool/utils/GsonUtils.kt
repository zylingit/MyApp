package com.exa.mytool.utils

import android.text.TextUtils
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

/**
 * @Author: chenjuncong_mac
 * @Date: 2023/6/6 09:57
 * @Description:
 */
object GsonUtils {
    private val gson = GsonBuilder().disableHtmlEscaping().create()

    /**
     * <pre>
     * JSON字符串转换为List数组, 提供两种方式(主要解决调用的容易程度)
     * 1. TypeToken<List></List><T>> token 参数转换
     * 2. Class<T> cls 方式转换
     *
     * @param json
     * @return List<T>
     *
     * <pre>
    </pre></T></T></T></pre> */
    fun <T> convertList(json: String?, token: TypeToken<List<T>?>): List<T> {
        return if (TextUtils.isEmpty(json)) {
            ArrayList()
        } else gson.fromJson(
            json,
            token.type
        )
    }

    /**
     * <pre>
     * Json格式转换, 由JSON字符串转化到制定类型T
     *
     * @param json
     * @param cls
     * @return T
     *
     * <pre>
    </pre></pre> */
    fun <T> fromJson(json: String, cls: Class<T>): T? {
        return if (TextUtils.isEmpty(json)) {
            null
        } else try {
            gson.fromJson(json, cls)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * <pre>
     * java对象转化JSON
     *
     * @return String
     *
     * <pre>
    </pre></pre> */
    fun toJson(obj: Any?): String {
        return if (obj == null) {
            ""
        } else gson.toJson(obj)
    }

    fun getJsonObjectAsString(jsonObject: JsonObject?, name: String?): String? {
        if (jsonObject == null || TextUtils.isEmpty(name)) {
            return null
        }
        val jsonElement = jsonObject[name]
        return jsonElement?.asString
    }

    fun getJsonObjectChild(jsonObject: JsonObject?, name: String?): JsonObject? {
        if (jsonObject == null || TextUtils.isEmpty(name)) {
            return null
        }
        val jsonElement = jsonObject[name]
        return jsonElement?.asJsonObject
    }

    fun getJsonObjectAsBoolean(jsonObject: JsonObject?, name: String?): Boolean {
        if (jsonObject == null || TextUtils.isEmpty(name)) {
            return false
        }
        val jsonElement = jsonObject[name]
        return jsonElement?.asBoolean ?: false
    }
}