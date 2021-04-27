package com.playwright.remote.engine.page.impl

import com.google.gson.JsonObject
import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.*
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.browser.impl.BrowserContext
import com.playwright.remote.engine.callback.api.IBindingCallback
import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.dialog.api.IDialog
import com.playwright.remote.engine.download.api.IDownload
import com.playwright.remote.engine.filechooser.api.IFileChooser
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
import com.playwright.remote.engine.route.request.api.IRequest
import com.playwright.remote.engine.route.response.api.IResponse
import com.playwright.remote.engine.touchscreen.api.ITouchScreen
import com.playwright.remote.engine.touchscreen.impl.TouchScreen
import com.playwright.remote.engine.waits.TimeoutSettings
import com.playwright.remote.engine.waits.api.IWait
import com.playwright.remote.engine.waits.impl.WaitPageClose
import com.playwright.remote.engine.waits.impl.WaitPageCrash
import com.playwright.remote.engine.waits.impl.WaitRace

class Page(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IPage {
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
    val bindings = hashMapOf<String, IBindingCallback>()

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

    init {
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

    @Suppress("UNCHECKED_CAST")
    override fun onClose(handler: (IPage) -> Unit) = listeners.add(CLOSE, handler as UniversalConsumer)

    @Suppress("UNCHECKED_CAST")
    override fun offClose(handler: (IPage) -> Unit) = listeners.remove(CLOSE, handler as UniversalConsumer)

    @Suppress("UNCHECKED_CAST")
    override fun onConsoleMessage(handler: (IConsoleMessage) -> Unit) {
        listeners.add(CONSOLE, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offConsoleMessage(handler: (IConsoleMessage) -> Unit) {
        listeners.remove(CONSOLE, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCrash(handler: (IPage) -> Unit) {
        listeners.add(CRASH, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offCrash(handler: (IPage) -> Unit) {
        listeners.remove(CRASH, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onDialog(handler: (IDialog) -> Unit) {
        listeners.add(DIALOG, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offDialog(handler: (IDialog) -> Unit) {
        listeners.remove(DIALOG, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onDomContentLoaded(handler: (IPage) -> Unit) {
        listeners.add(DOMCONTENTLOADED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offDomContentLoaded(handler: (IPage) -> Unit) {
        listeners.remove(DOMCONTENTLOADED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onDownload(handler: (IDownload) -> Unit) {
        listeners.add(DOWNLOAD, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offDownload(handler: (IDownload) -> Unit) {
        listeners.remove(DOWNLOAD, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onFileChooser(handler: (IFileChooser) -> Unit) {
        listeners.add(FILECHOOSER, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offFileChooser(handler: (IFileChooser) -> Unit) {
        listeners.remove(FILECHOOSER, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onFrameAttached(handler: (IFrame) -> Unit) {
        listeners.add(FRAMEATTACHED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offFrameAttached(handler: (IFrame) -> Unit) {
        listeners.remove(FRAMEATTACHED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onFrameDetached(handler: (IFrame) -> Unit) {
        listeners.add(FRAMEDETACHED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offFrameDetached(handler: (IFrame) -> Unit) {
        listeners.remove(FRAMEDETACHED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onFrameNavigated(handler: (IFrame) -> Unit) {
        listeners.add(FRAMENAVIGATED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offFrameNavigated(handler: (IFrame) -> Unit) {
        listeners.remove(FRAMENAVIGATED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onLoad(handler: (IPage) -> Unit) {
        listeners.add(LOAD, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offLoad(handler: (IPage) -> Unit) {
        listeners.remove(LOAD, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onPageError(handler: (String) -> Unit) {
        listeners.add(PAGEERROR, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offPageError(handler: (String) -> Unit) {
        listeners.remove(PAGEERROR, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onPopup(handler: (IPage) -> Unit) {
        listeners.add(POPUP, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offPopup(handler: (IPage) -> Unit) {
        listeners.remove(POPUP, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onRequest(handler: (IRequest) -> Unit) {
        listeners.add(REQUEST, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offRequest(handler: (IRequest) -> Unit) {
        listeners.remove(REQUEST, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onRequestFailed(handler: (IRequest) -> Unit) {
        listeners.add(REQUESTFAILED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offRequestFailed(handler: (IRequest) -> Unit) {
        listeners.remove(REQUESTFAILED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onRequestFinished(handler: (IRequest) -> Unit) {
        listeners.add(REQUESTFINISHED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offRequestFinished(handler: (IRequest) -> Unit) {
        listeners.remove(REQUESTFINISHED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onResponse(handler: (IResponse) -> Unit) {
        listeners.add(RESPONSE, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offResponse(handler: (IResponse) -> Unit) {
        listeners.remove(RESPONSE, handler as UniversalConsumer)
    }

    override fun context(): IBrowserContext {
        return browserContext
    }

    override fun navigate(url: String, options: NavigateOptions): IResponse? =
        mainFrame.navigate(url, options)

    fun <T> createWaitForCloseHelper(): IWait<T> {
        return WaitRace(listOf(WaitPageClose(listeners), WaitPageCrash(listeners)))
    }

    fun <T> createWaitTimout(timeout: Double?): IWait<T> {
        return timeoutSettings.createWait(timeout)
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
}