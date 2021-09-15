package io.github.tmcreative1.playwright.remote.engine.worker.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.core.enums.EventType
import io.github.tmcreative1.playwright.remote.core.enums.EventType.CLOSE
import io.github.tmcreative1.playwright.remote.domain.serialize.SerializedError.SerializedValue
import io.github.tmcreative1.playwright.remote.engine.handle.js.api.IJSHandle
import io.github.tmcreative1.playwright.remote.engine.listener.ListenerCollection
import io.github.tmcreative1.playwright.remote.engine.listener.UniversalConsumer
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForCloseOptions
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import io.github.tmcreative1.playwright.remote.engine.page.impl.Page
import io.github.tmcreative1.playwright.remote.engine.parser.IParser
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.engine.serialize.CustomGson.Companion.gson
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.deserialize
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.serializeArgument
import io.github.tmcreative1.playwright.remote.engine.waits.api.IWait
import io.github.tmcreative1.playwright.remote.engine.waits.impl.WaitEvent
import io.github.tmcreative1.playwright.remote.engine.waits.impl.WaitRace
import io.github.tmcreative1.playwright.remote.engine.worker.api.IWorker

class Worker(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IWorker {
    private val listeners = ListenerCollection<EventType>()
    var page: IPage? = null

    @Suppress("UNCHECKED_CAST")
    override fun onClose(handler: (IWorker) -> Unit) {
        listeners.add(CLOSE, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offClose(handler: (IWorker) -> Unit) {
        listeners.remove(CLOSE, handler as UniversalConsumer)
    }

    override fun evaluate(expression: String, arg: Any?): Any {
        val params = JsonObject()
        params.addProperty("expression", expression)
        params.add("arg", gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("evaluateExpression", params)
        val value = IParser.fromJson(json!!.asJsonObject["value"], SerializedValue::class.java)
        return deserialize(value)
    }

    override fun evaluateHandle(expression: String, arg: Any?): IJSHandle {
        val params = JsonObject()
        params.addProperty("expression", expression)
        params.add("arg", gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("evaluateExpressionHandle", params)
        return messageProcessor.getExistingObject(json!!.asJsonObject["handle"].asJsonObject["guid"].asString)
    }

    override fun url(): String {
        return initializer["url"].asString
    }

    override fun waitForClose(options: WaitForCloseOptions?, callback: () -> Unit): IWorker? {
        return waitForEventWithTimeout(
            CLOSE,
            if (options == null) WaitForCloseOptions {}.timeout else options.timeout,
            callback
        )
    }

    private fun <T> waitForEventWithTimeout(eventType: EventType, timeout: Double?, code: () -> Unit): T? {
        val waitList = arrayListOf<IWait<T>>()
        waitList.add(WaitEvent(listeners, eventType))
        waitList.add((page as Page).createWaitForCloseHelper())
        waitList.add((page as Page).createWaitTimeout(timeout))
        return runUtil(WaitRace(waitList), code)
    }

    override fun handleEvent(event: String, params: JsonObject) {
        if ("close" == event) {
            if (page != null) {
                (page as Page).workers.remove(this)
            }
            listeners.notify(CLOSE, this)
        }
    }
}