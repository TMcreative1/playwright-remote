package com.playwright.remote.engine.page.impl

import com.google.gson.JsonObject
import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.CLOSE
import com.playwright.remote.core.enums.EventType.FILECHOOSER
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.browser.impl.BrowserContext
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.keyboard.api.IKeyboard
import com.playwright.remote.engine.keyboard.impl.Keyboard
import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.mouse.api.IMouse
import com.playwright.remote.engine.mouse.impl.Mouse
import com.playwright.remote.engine.options.NavigateOptions
import com.playwright.remote.engine.options.ViewportSize
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.parser.IParser
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.route.response.api.IResponse
import com.playwright.remote.engine.touchscreen.api.ITouchScreen
import com.playwright.remote.engine.touchscreen.impl.TouchScreen
import com.playwright.remote.engine.waits.TimeoutSettings

class Page : ChannelOwner, IPage {
    var ownedContext: IBrowserContext? = null
    private val browserContext: IBrowserContext
    private val mainFrame: IFrame
    private var isClosed: Boolean
    private var viewPort = ViewportSize {}
    private val keyboard: IKeyboard
    private val mouse: IMouse
    private val touchScreen: ITouchScreen
    private val frames = linkedSetOf<IFrame>()
    private val timeoutSettings: TimeoutSettings

    private val listeners = object : ListenerCollection<EventType>() {
        override fun add(eventType: EventType, listener: UniversalConsumer) {
            if (eventType == FILECHOOSER) {
                updateFileChooserInterception(true)
            }
            super.add(eventType, listener)
        }

        override fun remove(eventType: EventType, listener: UniversalConsumer) {
            super.remove(eventType, listener)
            if (eventType == FILECHOOSER) {
                updateFileChooserInterception(false)
            }
        }
    }

    constructor(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : super(
        parent,
        type,
        guid,
        initializer
    ) {
        browserContext = parent as BrowserContext
        mainFrame = messageProcessor.getExistingObject(initializer["mainFrame"].asJsonObject["guid"].asString)
        isClosed = initializer["isClosed"].asBoolean
        if (initializer.has("viewportSize")) {
            viewPort = IParser.fromJson(initializer["viewportSize"], ViewportSize::class.java)
        }
        keyboard = Keyboard(this)
        mouse = Mouse(this)
        touchScreen = TouchScreen(this)
        frames.add(mainFrame)
        timeoutSettings = TimeoutSettings(browserContext.timeoutSettings)
    }

    fun didClose() {
        isClosed = true
        (browserContext as BrowserContext).pages.remove(this)
        listeners.notify(CLOSE, this)
    }

    private fun updateFileChooserInterception(enabled: Boolean) {
        if (!listeners.hasListeners(FILECHOOSER)) {
            val params = JsonObject()
            params.addProperty("intercepted", enabled)
            sendMessage("setFileChooserInterceptedNoReply", params)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onClose(handler: (IPage) -> Unit) = listeners.add(CLOSE, handler as UniversalConsumer)

    @Suppress("UNCHECKED_CAST")
    override fun offClose(handler: (IPage) -> Unit) = listeners.remove(CLOSE, handler as UniversalConsumer)

    override fun navigate(url: String, options: NavigateOptions): IResponse? =
        mainFrame.navigate(url, options)
}