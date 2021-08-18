package com.playwright.remote.engine.parser

import com.google.gson.JsonElement
import com.playwright.remote.engine.serialize.CustomGson.Companion.gson
import java.io.Reader

interface IParser {
    companion object {
        @JvmStatic
        fun toJson(obj: Any?): String =
            gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(obj)

        @JvmStatic
        fun <T> fromJson(json: String?, classOfT: Class<T>): T = gson().fromJson(json, classOfT)

        @JvmStatic
        fun <T> fromJson(json: JsonElement, classOfT: Class<T>): T = gson().fromJson(json, classOfT)

        @JvmStatic
        fun <T> fromJson(json: Reader, classOfT: Class<T>): T = gson().fromJson(json, classOfT)

        @JvmStatic
        fun <T> convert(obj: Any?, classOfT: Class<T>): T = fromJson(toJson(obj), classOfT)

    }
}