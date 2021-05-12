package com.playwright.remote.engine.page.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.*
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.browser.impl.BrowserContext
import com.playwright.remote.engine.callback.api.IBindingCallback
import com.playwright.remote.engine.callback.api.IBindingCallback.ISource
import com.playwright.remote.engine.callback.api.IFunctionCallback
import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.dialog.api.IDialog
import com.playwright.remote.engine.download.api.IDownload
import com.playwright.remote.engine.filechooser.api.IFileChooser
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.frame.impl.Frame
import com.playwright.remote.engine.handle.element.api.IElementHandle
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.keyboard.api.IKeyboard
import com.playwright.remote.engine.keyboard.impl.Keyboard
import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.mouse.api.IMouse
import com.playwright.remote.engine.mouse.impl.Mouse
import com.playwright.remote.engine.options.*
import com.playwright.remote.engine.options.element.ClickOptions
import com.playwright.remote.engine.options.element.DoubleClickOptions
import com.playwright.remote.engine.options.element.FillOptions
import com.playwright.remote.engine.options.element.HoverOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.parser.IParser
import com.playwright.remote.engine.parser.IParser.Companion.convert
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.route.UrlMatcher
import com.playwright.remote.engine.route.request.api.IRequest
import com.playwright.remote.engine.route.response.api.IResponse
import com.playwright.remote.engine.touchscreen.api.ITouchScreen
import com.playwright.remote.engine.touchscreen.impl.TouchScreen
import com.playwright.remote.engine.waits.TimeoutSettings
import com.playwright.remote.engine.waits.api.IWait
import com.playwright.remote.engine.waits.impl.WaitPageClose
import com.playwright.remote.engine.waits.impl.WaitPageCrash
import com.playwright.remote.engine.waits.impl.WaitRace
import com.playwright.remote.engine.websocket.api.IWebSocket
import com.playwright.remote.engine.worker.api.IWorker
import com.playwright.remote.utils.Utils
import com.playwright.remote.utils.Utils.Companion.isSafeCloseError
import com.playwright.remote.utils.Utils.Companion.writeToFile
import okio.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files.readAllBytes
import java.nio.file.Path
import java.util.*
import java.util.regex.Pattern

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
    private var opener: IPage? = null
    val bindings = hashMapOf<String, IBindingCallback>()
    val workers = hashSetOf<IWorker>()

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

    @Suppress("UNCHECKED_CAST")
    override fun onWebSocket(handler: (IWebSocket) -> Unit) {
        listeners.add(WEBSOCKET, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offWebSocket(handler: (IWebSocket) -> Unit) {
        listeners.remove(WEBSOCKET, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onWorker(handler: (IWorker) -> Unit) {
        listeners.add(WORKER, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offWorker(handler: (IWorker) -> Unit) {
        listeners.remove(WORKER, handler as UniversalConsumer)
    }


    override fun addInitScript(script: String) {
        val params = JsonObject()
        params.addProperty("source", script)
        sendMessage("addInitScript", params)
    }

    override fun addInitScript(script: Path) {
        try {
            val bytes = readAllBytes(script)
            addInitScript(String(bytes, UTF_8))
        } catch (e: IOException) {
            throw PlaywrightException("Failed to read script from file", e)
        }
    }

    override fun addScriptTag(options: AddScriptTagOptions?): IElementHandle {
        return (mainFrame as Frame).addScriptTag(options)
    }

    override fun addStyleTag(options: AddStyleTagOptions?): IElementHandle {
        return (mainFrame as Frame).addStyleTag(options)
    }

    override fun bringToFront() {
        sendMessage("bringToFront")
    }

    override fun check(selector: String, options: CheckOptions?) {
        (mainFrame as Frame).check(selector, options)
    }

    override fun click(selector: String, options: ClickOptions?) {
        (mainFrame as Frame).click(selector, options)
    }

    override fun close(options: CloseOptions?) {
        if (isClosed) {
            return
        }
        val params = if (options == null) JsonObject() else Gson().toJsonTree(options).asJsonObject
        try {
            sendMessage("close", params)
        } catch (e: PlaywrightException) {
            if (!isSafeCloseError(e)) {
                throw e
            }
        }
        if (ownedContext != null) {
            (ownedContext as BrowserContext).close()
        }
    }

    override fun content(): String {
        return (mainFrame as Frame).content()
    }

    override fun context(): IBrowserContext {
        return browserContext
    }

    override fun doubleClick(selector: String, options: DoubleClickOptions?) {
        (mainFrame as Frame).doubleClick(selector, options)
    }

    override fun dispatchEvent(selector: String, type: String, eventInit: Any?, options: DispatchEventOptions?) {
        (mainFrame as Frame).dispatchEvent(selector, type, eventInit, options)
    }

    override fun emulateMedia(options: EmulateMediaOptions?) {
        val params = Gson().toJsonTree(options ?: EmulateMediaOptions {}).asJsonObject
        sendMessage("emulateMedia", params)
    }

    override fun evalOnSelector(selector: String, expression: String, arg: Any?): Any {
        return (mainFrame as Frame).evalOnSelector(selector, expression, arg)
    }

    override fun evalOnSelectorAll(selector: String, expression: String, arg: Any?): Any {
        return (mainFrame as Frame).evalOnSelectorAll(selector, expression, arg)
    }

    override fun evaluate(expression: String, arg: Any?): Any {
        return (mainFrame as Frame).evaluate(expression, arg)
    }

    override fun evaluateHandle(expression: String, arg: Any?): IJSHandle {
        return (mainFrame as Frame).evaluateHandle(expression, arg)
    }

    override fun exposeBinding(name: String, callback: IBindingCallback, options: ExposeBindingOptions?) {
        if (bindings.containsKey(name)) {
            throw PlaywrightException("Function $name has been already registered")
        }
        if ((browserContext as BrowserContext).bindings.containsKey(name)) {
            throw PlaywrightException("Function $name has been already registered in browser context")
        }
        bindings[name] = callback

        val params = JsonObject()
        params.addProperty("name", name)
        if (options?.handle != null && options.handle!!) {
            params.addProperty("needsHandle", true)
        }
        sendMessage("exposeBinding", params)
    }

    override fun exposeFunction(name: String, callback: IFunctionCallback) {
        exposeBinding(name, { _: ISource, arg: Any -> callback.call(arg) }, null)
    }

    override fun fill(selector: String, value: String, options: FillOptions?) {
        (mainFrame as Frame).fill(selector, value, options)
    }

    override fun focus(selector: String, options: FocusOptions?) {
        (mainFrame as Frame).focus(selector, options)
    }

    override fun frame(name: String): IFrame? {
        for (frame in frames) {
            if (name == frame.name()) {
                return frame
            }
        }
        return null
    }

    override fun frameByUrl(url: String): IFrame? {
        return frameFor(UrlMatcher(url))
    }

    override fun frameByUrl(url: Pattern): IFrame? {
        return frameFor(UrlMatcher(url))
    }

    override fun frameByUrl(url: (String) -> Boolean): IFrame? {
        return frameFor(UrlMatcher(url))
    }

    override fun frames(): List<IFrame> {
        return frames.toList()
    }

    override fun getAttribute(selector: String, name: String, options: GetAttributeOptions?): String? {
        return (mainFrame as Frame).getAttribute(selector, name, options)
    }

    override fun goBack(options: GoBackOptions?): IResponse? {
        val params = Gson().toJsonTree(options ?: GoForwardOptions {}).asJsonObject
        val json = sendMessage("goBack", params).asJsonObject
        if (json.has("response")) {
            return messageProcessor.getExistingObject(json["response"].asJsonObject["guid"].asString)
        }
        return null
    }

    override fun goForward(options: GoForwardOptions?): IResponse? {
        val params = Gson().toJsonTree(options ?: GoForwardOptions {}).asJsonObject
        val json = sendMessage("goForward", params).asJsonObject
        if (json.has("response")) {
            return messageProcessor.getExistingObject(json["response"].asJsonObject["guid"].asString)
        }
        return null
    }

    override fun navigate(url: String, options: NavigateOptions): IResponse? =
        mainFrame.navigate(url, options)

    override fun hover(selector: String, options: HoverOptions?) {
        mainFrame.hover(selector, options)
    }

    override fun innerHTML(selector: String, options: InnerHTMLOptions?): String {
        return mainFrame.innerHTML(selector, options)
    }

    override fun innerText(selector: String, options: InnerTextOptions?): String {
        return mainFrame.innerText(selector, options)
    }

    override fun isChecked(selector: String, options: IsCheckedOptions?): Boolean {
        return mainFrame.isChecked(selector, options)
    }

    override fun isClosed(): Boolean {
        return isClosed
    }

    override fun isDisabled(selector: String, options: IsDisabledOptions?): Boolean {
        return mainFrame.isDisabled(selector, options)
    }

    override fun isEditable(selector: String, options: IsEditableOptions?): Boolean {
        return mainFrame.isEditable(selector, options)
    }

    override fun isEnabled(selector: String, options: IsEnabledOptions?): Boolean {
        return mainFrame.isEnabled(selector, options)
    }

    override fun isHidden(selector: String, options: IsHiddenOptions?): Boolean {
        return mainFrame.isHidden(selector, options)
    }

    override fun isVisible(selector: String, options: IsVisibleOptions?): Boolean {
        return mainFrame.isVisible(selector, options)
    }

    override fun keyboard(): IKeyboard {
        return keyboard
    }

    override fun mainFrame(): IFrame {
        return mainFrame
    }

    override fun mouse(): IMouse {
        return mouse
    }

    override fun opener(): IPage? {
        if (opener == null || opener!!.isClosed()) {
            return null
        }
        return opener
    }

    override fun pause() {
        context().pause()
    }

    override fun pdf(options: PdfOptions?): ByteArray {
        if (browserContext.browser().name() == "chromium") {
            throw PlaywrightException("Page.pdf only supported in headless Chromium")
        }
        val opt = options ?: PdfOptions {}
        val params = Gson().toJsonTree(opt).asJsonObject
        params.remove("path")
        val json = sendMessage("pdf", params).asJsonObject
        val buffer = Base64.getDecoder().decode(json["pdf"].asString)
        if (opt.path != null) {
            writeToFile(buffer, opt.path!!)
        }
        return buffer
    }

    fun <T> createWaitForCloseHelper(): IWait<T> {
        return WaitRace(listOf(WaitPageClose(listeners), WaitPageCrash(listeners)))
    }

    fun <T> createWaitTimeout(timeout: Double?): IWait<T> {
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

    private fun frameFor(matcher: UrlMatcher): IFrame? {
        for (frame in frames) {
            if (matcher.test(frame.url())) {
                return frame
            }
        }
        return null
    }
}
