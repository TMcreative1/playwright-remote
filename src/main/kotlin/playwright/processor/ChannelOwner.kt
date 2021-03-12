package playwright.processor

import com.google.gson.JsonObject

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

    protected fun sendMessage(method: String, params: JsonObject) {
        //TODO implement send message
    }
}