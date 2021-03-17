package playwright.processor

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import playwright.waits.api.IWait

open class ChannelOwner(
    protected val messageProcessor: MessageProcessor,
    private val parent: ChannelOwner?,
    private val type: String,
    private val guid: String,
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

    protected fun sendMessage(method: String): JsonElement {
        return sendMessage(method, JsonObject())
    }

    protected fun sendMessage(method: String, params: JsonObject): JsonElement {
        return messageProcessor.sendMessage(guid, method, params)
    }

    fun disconnect() {
        parent?.objects?.remove(guid)
        messageProcessor.unregisterObject(guid)
        for (child in objects.values) {
            child.disconnect()
        }
        objects.clear()
    }

    fun <T> runUtil(wait: IWait<T>, code: () -> Unit): T {
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