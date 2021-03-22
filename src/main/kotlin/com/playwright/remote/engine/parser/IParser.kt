package com.playwright.remote.engine.parser

import com.google.gson.Gson
import com.google.gson.JsonElement

interface IParser {
    companion object {
        @JvmStatic
        fun toJson(obj: Any?): String =
            Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(obj)

        @JvmStatic
        fun <T> fromJson(json: String?, classOfT: Class<T>): T = Gson().fromJson(json, classOfT)

        @JvmStatic
        fun <T> fromJson(json: JsonElement, classOfT: Class<T>): T = Gson().fromJson(json, classOfT)

        @JvmStatic
        fun <T> convert(obj: Any?, classOfT: Class<T>): T = fromJson(toJson(obj), classOfT)
    }
}