package com.playwright.remote.engine.serializer

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.domain.file.FilePayload
import com.playwright.remote.domain.serialize.SerializedArgument
import com.playwright.remote.domain.serialize.SerializedError
import com.playwright.remote.domain.serialize.SerializedError.SerializedValue
import com.playwright.remote.engine.handle.element.api.IElementHandle
import com.playwright.remote.engine.handle.element.impl.ElementHandle
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.handle.js.impl.JSHandle
import com.playwright.remote.engine.parser.IParser
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

class Serialization {

    companion object {
        @JvmStatic
        fun serializeArgument(arg: Any?): SerializedArgument {
            val result = SerializedArgument {}
            val handles = mutableListOf<IJSHandle>()
            result.value = serializeValue(arg, handles, 0)
            result.handles = emptyArray()
            var i = 0
            for (handle in handles) {
                result.handles!![i++] = SerializedArgument.Channel((handle as JSHandle).guid)
            }
            return result
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T> deserialize(value: SerializedValue?): T {
            return when {
                value?.n != null -> {
                    if (value.n!!.toDouble() == (value.n!!.toInt().toDouble())) value.n!!.toInt() as T
                    else value.n!!.toDouble() as T
                }
                value?.b != null -> value.b as T
                value?.s != null -> value.s as T
                value?.v != null -> when (value.v) {
                    "undefined" -> null
                    "null" -> null
                    "Infinity" -> Double.POSITIVE_INFINITY
                    "-Infinity" -> Double.NEGATIVE_INFINITY
                    "-0" -> -0.0
                    "NaN" -> Double.NaN
                    else -> throw PlaywrightException("Unexpected value: ${value.v}")
                } as T
                value?.a != null -> {
                    val list = mutableListOf<Any>()
                    for (v in value.a!!) {
                        list.add(v)
                    }
                    list as T
                }
                value?.o != null -> {
                    val map = linkedMapOf<String, Any>()
                    for (o in value.o!!) {
                        map[o.k as String] = deserialize(o.v)
                    }
                    map as T
                }
                else -> throw PlaywrightException("Unexpected result: ${IParser.toJson(value)}")
            }
        }

        @JvmStatic
        fun toProtocol(handles: Array<IElementHandle>): JsonArray {
            val jsonElements = JsonArray()
            for (handle in handles) {
                val jsonHandle = JsonObject()
                jsonHandle.addProperty("guid", (handle as ElementHandle).guid)
                jsonElements.add(jsonHandle)
            }
            return jsonElements
        }

        @JvmStatic
        fun parseStringList(array: JsonArray): List<String> {
            val result = arrayListOf<String>()
            for (element in array) {
                result.add(element.asString)
            }
            return result
        }

        @JvmStatic
        fun toJsonArray(files: Array<FilePayload>): JsonArray {
            val jsonFiles = JsonArray()
            for (file in files) {
                val jsonFile = JsonObject()
                jsonFile.addProperty("name", file.name)
                jsonFile.addProperty("mimeType", file.mimeType)
                jsonFile.addProperty("buffer", Base64.getEncoder().encodeToString(file.buffer))
                jsonFiles.add(jsonFile)
            }
            return jsonFiles
        }

        private fun serializeValue(value: Any?, handles: MutableList<IJSHandle>, depth: Int): SerializedValue {
            if (depth > 100) {
                throw PlaywrightException("Maximum argument depth exceeded")
            }

            val result = SerializedValue {}
            when (value) {
                is IJSHandle -> {
                    result.h = handles.size
                    handles.add(value)
                }
                value == null -> result.v = "undefined"
                is Double -> {
                    when (value) {
                        value == Double.POSITIVE_INFINITY -> result.v = "Infinity"
                        value == Double.NEGATIVE_INFINITY -> result.v = "-Infinity"
                        value == -0 -> result.v = "-0"
                        value.isNaN() -> result.v = "NaN"
                        else -> result.n = value
                    }
                }
                is Boolean -> result.b = value
                is Int -> result.n = value
                is String -> result.s = value
                is List<*> -> {
                    val list = mutableListOf<SerializedValue>()
                    for (o in value) {
                        list.add(serializeValue(o, handles, depth + 1))
                    }
                    result.a = list.toTypedArray()
                }
                is Map<*, *> -> {
                    val list = mutableListOf<SerializedValue.O>()
                    for (e in value.entries) {
                        val serializedValue = SerializedValue.O {}
                        serializedValue.k = e.key as String?
                        serializedValue.v = serializeValue(e.value, handles, depth + 1)
                        list.add(serializedValue)
                    }
                    result.o = list.toTypedArray()
                }
                is Array<*> -> {
                    val list = mutableListOf<SerializedValue>()
                    for (o in value) {
                        list.add(serializeValue(o, handles, depth + 1))
                    }
                    result.a = list.toTypedArray()
                }
                else -> throw PlaywrightException("Unsupported type of argument: $value")
            }
            return result
        }

        @JvmStatic
        fun serializeError(e: Throwable): SerializedError {
            val result = SerializedError {}
            result.error = SerializedError.Error {}
            result.error!!.message = e.message
            result.error!!.name = e.javaClass.name

            val out = ByteArrayOutputStream()
            e.printStackTrace(PrintStream(out))
            result.error!!.stack = String(out.toByteArray(), UTF_8)
            return result
        }
    }
}