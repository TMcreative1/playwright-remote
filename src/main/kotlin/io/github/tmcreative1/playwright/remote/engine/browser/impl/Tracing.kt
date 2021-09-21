package io.github.tmcreative1.playwright.remote.engine.browser.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.browser.api.IBrowserContext
import io.github.tmcreative1.playwright.remote.engine.browser.api.ITracing
import io.github.tmcreative1.playwright.remote.engine.download.api.IArtifact
import io.github.tmcreative1.playwright.remote.engine.options.tracing.StartTracingOptions
import io.github.tmcreative1.playwright.remote.engine.options.tracing.StopChunkOptions
import io.github.tmcreative1.playwright.remote.engine.options.tracing.StopTracingOptions
import io.github.tmcreative1.playwright.remote.engine.processor.MessageProcessor
import io.github.tmcreative1.playwright.remote.engine.serialize.CustomGson.Companion.gson
import java.nio.file.Path

class Tracing(private val context: IBrowserContext, private val messageProcessor: MessageProcessor) : ITracing {

    override fun start(options: StartTracingOptions?) {
        val params = gson().toJsonTree(options ?: StartTracingOptions {}).asJsonObject
        context as BrowserContext
        context.sendMessage("tracingStart", params)
        context.sendMessage("tracingStartChunk")
    }

    override fun stop(options: StopTracingOptions?) {
        stopChunk(options?.path)
        (context as BrowserContext).sendMessage("tracingStop")
    }

    override fun startChunk() {
        (context as BrowserContext).sendMessage("tracingStartChunk")
    }

    override fun stopChunk(options: StopChunkOptions?) {
        stopChunk(options?.path)
    }

    private fun export(path: Path) {
        context as BrowserContext
        val json = context.sendMessage("tracingExport")!!.asJsonObject
        val artifact =
            messageProcessor.getExistingObject<IArtifact>(json["artifact"].asJsonObject["guid"].asString)
        artifact.saveAs(path)
    }

    private fun stopChunk(path: Path?) {
        val params = JsonObject()
        params.addProperty("save", path != null)
        val json = (context as BrowserContext).sendMessage("tracingStopChunk", params)!!.asJsonObject
        if (!json.has("artifact")) {
            return
        }
        val artifact = messageProcessor.getExistingObject<IArtifact>(json["artifact"].asJsonObject["guid"].asString)
        artifact.saveAs(path!!)
    }
}