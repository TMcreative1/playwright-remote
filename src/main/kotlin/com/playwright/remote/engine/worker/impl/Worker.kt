package com.playwright.remote.engine.worker.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.CLOSE
import com.playwright.remote.domain.serialize.SerializedError.SerializedValue
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.options.wait.WaitForCloseOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.page.impl.Page
import com.playwright.remote.engine.parser.IParser
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.serializer.Serialization.Companion.deserialize
import com.playwright.remote.engine.serializer.Serialization.Companion.serializeArgument
import com.playwright.remote.engine.waits.api.IWait
import com.playwright.remote.engine.waits.impl.WaitEvent
import com.playwright.remote.engine.waits.impl.WaitRace
import com.playwright.remote.engine.worker.api.IWorker

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
        params.add("arg", Gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("evaluateExpression", params)
        val value = IParser.fromJson(json.asJsonObject["value"], SerializedValue::class.java)
        return deserialize(value)
    }

    override fun evaluateHandle(expression: String, arg: Any?): IJSHandle {
        val params = JsonObject()
        params.addProperty("expression", expression)
        params.add("arg", Gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("evaluateExpressionHandle", params)
        return messageProcessor.getExistingObject(json.asJsonObject["handle"].asJsonObject["guid"].asString)
    }

    override fun url(): String {
        return initializer["url"].asString
    }

    override fun waitForClose(options: WaitForCloseOptions?, callback: () -> Unit): IWorker {
        return waitForEventWithTimeout(
            CLOSE,
            if (options == null) WaitForCloseOptions {}.timeout else options.timeout,
            callback
        )
    }

    private fun <T> waitForEventWithTimeout(eventType: EventType, timeout: Double?, code: () -> Unit): T {
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