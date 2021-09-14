package io.github.tmcreative1.playwright.remote.engine.processor

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.waits.api.IWait

open class ChannelOwner(
    protected val messageProcessor: MessageProcessor,
    private val parent: ChannelOwner?,
    val type: String,
    val guid: String,
    protected val initializer: JsonObject
) {

    constructor(
        parent: ChannelOwner,
        type: String,
        guid: String,
        initializer: JsonObject
    ) : this(parent.messageProcessor, parent, type, guid, initializer)

    constructor(messageProcessor: MessageProcessor, type: String, guid: String) : this(
        messageProcessor,
        null,
        type,
        guid,
        JsonObject()
    )

    private val objects = hashMapOf<String, ChannelOwner>()

    init {
        messageProcessor.registerObject(guid, this)
        if (parent != null) {
            parent.objects[guid] = this
        }
    }

    @JvmOverloads
    fun sendMessage(method: String, params: JsonObject = JsonObject()): JsonElement? {
        return messageProcessor.sendMessage(guid, method, params)
    }

    fun disconnect() {
        parent?.objects?.remove(guid)
        messageProcessor.unregisterObject(guid)
        for (child in objects.values.toList()) {
            child.disconnect()
        }
        objects.clear()
    }

    internal fun <T> runUtil(wait: IWait<T>, code: () -> Unit): T? {
        try {
            code()
            while (!wait.isFinished()) {
                messageProcessor.processMessage()
            }
            return wait.get()
        } finally {
            wait.dispose()
        }
    }

    open fun handleEvent(event: String, params: JsonObject) {}
}