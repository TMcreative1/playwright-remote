package com.playwright.remote.engine.download.impl

import com.google.gson.JsonObject
import com.playwright.remote.engine.download.api.IArtifact
import com.playwright.remote.engine.download.stream.api.IStream
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.utils.Utils.Companion.writeToFile
import java.io.InputStream
import java.nio.file.Path

class Artifact(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IArtifact {
    override fun createReadStream(): InputStream? {
        val result = sendMessage("stream").asJsonObject
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
        val result = sendMessage("failure").asJsonObject
        if (result.has("error")) {
            return result["error"].asString
        }
        return null
    }

    override fun saveAs(path: Path) {
        val jsonObject = sendMessage("saveAsStream").asJsonObject
        val stream = messageProcessor.getExistingObject<IStream>(jsonObject["stream"].asJsonObject["guid"].asString)
        writeToFile(stream.stream(), path)
    }
}