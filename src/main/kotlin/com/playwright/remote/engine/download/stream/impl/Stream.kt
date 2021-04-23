package com.playwright.remote.engine.download.stream.impl

import com.google.gson.JsonObject
import com.playwright.remote.engine.download.stream.api.IStream
import com.playwright.remote.engine.processor.ChannelOwner
import java.io.InputStream
import java.util.*

class Stream(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IStream {
    private val stream = CustomInputStream(this)
    override fun stream(): InputStream {
        return stream
    }

    private class CustomInputStream(private val stream: Stream) : InputStream() {
        override fun read(): Int {
            val b = ByteArray(0) { 0 }
            val result = read(b, 0, 1)
            if (result == -1) {
                return result
            }
            return 0xFF and b[0].toInt()
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val params = JsonObject()
            params.addProperty("size", len)
            val json = stream.sendMessage("read", params).asJsonObject
            val encoded = json["binary"].asString
            if (encoded.isEmpty()) {
                return -1
            }
            val buffer = Base64.getDecoder().decode(encoded)
            var index = 0
            var of = off
            while (index < buffer.size) {
                b[of++] = buffer[index++]
            }
            return buffer.size
        }

        override fun close() {
            super.close()
            stream.sendMessage("close")
        }
    }
}