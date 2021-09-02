package io.github.tmcreative1.playwright.remote.engine.download.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.download.api.IDownload
import io.github.tmcreative1.playwright.remote.engine.download.stream.api.IStream
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import java.io.InputStream
import java.nio.file.Path

class Download(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IDownload {

    override fun createReadStream(): InputStream? {
        val result = sendMessage("stream")!!.asJsonObject
        if (!result.has("stream")) {
            return null
        }
        val stream = messageProcessor.getExistingObject<IStream>(result["stream"].asJsonObject["guid"].asString)
        return stream.stream()
    }

    override fun delete() {
        sendMessage("delete")
    }

    override fun failure(): String? {
        val result = sendMessage("failure")!!.asJsonObject
        if (result.has("error")) {
            return result["error"].asString
        }
        return null
    }

    override fun saveAs(path: Path) {
        val params = JsonObject()
        params.addProperty("path", path.toString())
        sendMessage("saveAs", params)
    }

    override fun suggestedFilename(): String {
        return initializer["suggestedFilename"].asString
    }

    override fun url(): String {
        return initializer["url"].asString
    }
}