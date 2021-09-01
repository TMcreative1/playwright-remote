package io.github.tmcreative1.playwright.remote.engine.download.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.download.api.IArtifact
import io.github.tmcreative1.playwright.remote.engine.download.stream.api.IStream
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.utils.Utils.Companion.writeToFile
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.deleteExisting

class Artifact(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IArtifact {
    var videoPath: Path? = null
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
        if (videoPath != null) {
            videoPath!!.deleteExisting()
        }
    }

    override fun failure(): String? {
        val result = sendMessage("failure")!!.asJsonObject
        if (result.has("error")) {
            return result["error"].asString
        }
        return null
    }

    override fun saveAs(path: Path) {
        val jsonObject = sendMessage("saveAsStream")!!.asJsonObject
        val stream = messageProcessor.getExistingObject<IStream>(jsonObject["stream"].asJsonObject["guid"].asString)
        val inputStream = stream.stream()
        videoPath = path
        writeToFile(inputStream, path)
    }
}