package com.playwright.remote.engine.page.impl

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.*
import com.playwright.remote.core.enums.LoadState
import com.playwright.remote.core.enums.ScreenshotType.JPEG
import com.playwright.remote.core.enums.ScreenshotType.PNG
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.domain.file.FilePayload
import com.playwright.remote.domain.serialize.SerializedError
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.browser.impl.BrowserContext
import com.playwright.remote.engine.callback.api.IBindingCall
import com.playwright.remote.engine.callback.api.IBindingCallback
import com.playwright.remote.engine.callback.api.IBindingCallback.ISource
import com.playwright.remote.engine.callback.api.IFunctionCallback
import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.dialog.api.IDialog
import com.playwright.remote.engine.download.api.IArtifact
import com.playwright.remote.engine.download.api.IDownload
import com.playwright.remote.engine.download.impl.Artifact
import com.playwright.remote.engine.download.impl.Download
import com.playwright.remote.engine.filechooser.api.IFileChooser
import com.playwright.remote.engine.filechooser.impl.FileChooser
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
import com.playwright.remote.engine.options.ScreenshotOptions
import com.playwright.remote.engine.options.element.*
import com.playwright.remote.engine.options.element.PressOptions
import com.playwright.remote.engine.options.element.TypeOptions
import com.playwright.remote.engine.options.wait.*
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.parser.IParser.Companion.fromJson
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.route.Router
import com.playwright.remote.engine.route.UrlMatcher
import com.playwright.remote.engine.route.api.IRoute
import com.playwright.remote.engine.route.request.api.IRequest
import com.playwright.remote.engine.route.request.impl.Request
import com.playwright.remote.engine.route.response.api.IResponse
import com.playwright.remote.engine.serialize.CustomGson.Companion.gson
import com.playwright.remote.engine.touchscreen.api.ITouchScreen
import com.playwright.remote.engine.touchscreen.impl.TouchScreen
import com.playwright.remote.engine.video.api.IVideo
import com.playwright.remote.engine.video.impl.Video
import com.playwright.remote.engine.waits.TimeoutSettings
import com.playwright.remote.engine.waits.api.IWait
import com.playwright.remote.engine.waits.impl.*
import com.playwright.remote.engine.websocket.api.IWebSocket
import com.playwright.remote.engine.worker.api.IWorker
import com.playwright.remote.engine.worker.impl.Worker
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
    private val routes = Router()
    private var video: IVideo? = null
    val waitClosedOrCrashed: IWait<Any>
    val bindings = hashMapOf<String, IBindingCallback>()
    val workers = hashSetOf<IWorker>()

    val listeners = object : ListenerCollection<EventType>() {
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
        (mainFrame as Frame).page = this
        isClosed = initializer["isClosed"].asBoolean
        if (initializer.has("viewportSize")) {
            viewPort = fromJson(initializer["viewportSize"], ViewportSize::class.java)
        }
        keyboard = Keyboard(this)
        mouse = Mouse(this)
        touchScreen = TouchScreen(this)
        frames.add(mainFrame)
        timeoutSettings = TimeoutSettings(browserContext.timeoutSettings)
        waitClosedOrCrashed = createWaitForCloseHelper()
        opener =
            if (initializer.has("opener")) messageProcessor.getExistingObject(initializer["opener"].asJsonObject["guid"].asString) else null
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

    fun frameNavigated(frame: IFrame) {
        listeners.notify(FRAMENAVIGATED, frame)
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

    fun notifyPopup(popup: IPage) {
        listeners.notify(POPUP, popup)
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
        return mainFrame.addScriptTag(options)
    }

    override fun addStyleTag(options: AddStyleTagOptions?): IElementHandle {
        return mainFrame.addStyleTag(options)
    }

    override fun bringToFront() {
        sendMessage("bringToFront")
    }

    override fun check(selector: String, options: CheckOptions?) {
        mainFrame.check(selector, options)
    }

    override fun click(selector: String, options: ClickOptions?) {
        mainFrame.click(selector, options)
    }

    override fun close(options: CloseOptions?) {
        if (isClosed) {
            return
        }
        val params = if (options == null) JsonObject() else gson().toJsonTree(options).asJsonObject
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
        mainFrame.doubleClick(selector, options)
    }

    override fun dispatchEvent(selector: String, type: String, eventInit: Any?, options: DispatchEventOptions?) {
        mainFrame.dispatchEvent(selector, type, eventInit, options)
    }

    override fun emulateMedia(options: EmulateMediaOptions?) {
        val params = gson().toJsonTree(options ?: EmulateMediaOptions {}).asJsonObject
        sendMessage("emulateMedia", params)
    }

    override fun evalOnSelector(selector: String, expression: String, arg: Any?): Any {
        return mainFrame.evalOnSelector(selector, expression, arg)
    }

    override fun evalOnSelectorAll(selector: String, expression: String, arg: Any?): Any {
        return mainFrame.evalOnSelectorAll(selector, expression, arg)
    }

    override fun evaluate(expression: String, arg: Any?): Any {
        return mainFrame.evaluate(expression, arg)
    }

    override fun evaluateHandle(expression: String, arg: Any?): IJSHandle {
        return mainFrame.evaluateHandle(expression, arg)
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
        exposeBinding(name, { _: ISource, args: Array<Any> -> callback.call(args) }, null)
    }

    override fun fill(selector: String, value: String, options: FillOptions?) {
        mainFrame.fill(selector, value, options)
    }

    override fun focus(selector: String, options: FocusOptions?) {
        mainFrame.focus(selector, options)
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
        return mainFrame.getAttribute(selector, name, options)
    }

    override fun goBack(options: GoBackOptions?): IResponse? {
        val params = gson().toJsonTree(options ?: GoForwardOptions {}).asJsonObject
        val json = sendMessage("goBack", params)!!.asJsonObject
        if (json.has("response")) {
            return messageProcessor.getExistingObject(json["response"].asJsonObject["guid"].asString)
        }
        return null
    }

    override fun goForward(options: GoForwardOptions?): IResponse? {
        val params = gson().toJsonTree(options ?: GoForwardOptions {}).asJsonObject
        val json = sendMessage("goForward", params)!!.asJsonObject
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
        if (browserContext.browser().name() != "chromium") {
            throw PlaywrightException("Page.pdf only supported in headless Chromium")
        }
        val opt = options ?: PdfOptions {}
        val params = gson().toJsonTree(opt).asJsonObject
        params.remove("path")
        val json = sendMessage("pdf", params)!!.asJsonObject
        val buffer = Base64.getDecoder().decode(json["pdf"].asString)
        if (opt.path != null) {
            writeToFile(buffer, opt.path!!)
        }
        return buffer
    }

    override fun press(selector: String, key: String, options: PressOptions?) {
        mainFrame.press(selector, key, options)
    }

    override fun querySelector(selector: String): IElementHandle? {
        return mainFrame.querySelector(selector)
    }

    override fun querySelectorAll(selector: String): List<IElementHandle>? {
        return mainFrame.querySelectorAll(selector)
    }

    override fun reload(options: ReloadOptions?): IResponse? {
        val params = gson().toJsonTree(options ?: ReloadOptions {}).asJsonObject
        val json = sendMessage("reload", params)!!.asJsonObject
        if (json.has("response")) {
            return messageProcessor.getExistingObject(json["response"].asJsonObject["guid"].asString)
        }
        return null
    }

    override fun route(url: String, handler: (IRoute) -> Unit) {
        route(UrlMatcher(url), handler)
    }

    override fun route(url: Pattern, handler: (IRoute) -> Unit) {
        route(UrlMatcher(url), handler)
    }

    override fun route(url: (String) -> Boolean, handler: (IRoute) -> Unit) {
        route(UrlMatcher(url), handler)
    }

    private fun route(matcher: UrlMatcher, handler: (IRoute) -> Unit) {
        routes.add(matcher, handler)
        if (routes.size() == 1) {
            val params = JsonObject()
            params.addProperty("enabled", true)
            sendMessage("setNetworkInterceptionEnabled", params)
        }
    }

    override fun screenshot(options: ScreenshotOptions?): ByteArray {
        val opt = options ?: ScreenshotOptions {}
        if (opt.type == null) {
            opt.type = PNG
            if (opt.path != null) {
                val fileName = opt.path!!.fileName.toString()
                val extStart = fileName.lastIndexOf('.')
                if (extStart != -1) {
                    val extension = fileName.substring(extStart).lowercase()
                    if (".jpeg" == extension || ".jpg" == (extension)) {
                        opt.type = JPEG
                    }
                }
            }
        }
        val params = gson().toJsonTree(opt).asJsonObject
        params.remove("path")
        val json = sendMessage("screenshot", params)!!.asJsonObject

        val buffer = Base64.getDecoder().decode(json["binary"].asString)
        if (opt.path != null) {
            writeToFile(buffer, opt.path!!)
        }
        return buffer
    }

    override fun selectOption(selector: String, value: String?, options: SelectOptionOptions?): List<String> {
        val values = if (value == null) null else arrayOf(value)
        return selectOption(selector, values, options)
    }

    override fun selectOption(selector: String, value: IElementHandle?, options: SelectOptionOptions?): List<String> {
        val values = if (value == null) null else arrayOf(value)
        return selectOption(selector, values, options)
    }

    override fun selectOption(selector: String, values: Array<String>?, options: SelectOptionOptions?): List<String> {
        if (values == null) {
            return selectOption(selector, emptyArray<SelectOption>(), options)
        }
        return selectOption(selector, values.map { value -> SelectOption { it.value = value } }.toTypedArray(), options)
    }

    override fun selectOption(selector: String, value: SelectOption?, options: SelectOptionOptions?): List<String> {
        val values = if (value == null) null else arrayOf(value)
        return selectOption(selector, values, options)
    }

    override fun selectOption(
        selector: String,
        values: Array<IElementHandle>?,
        options: SelectOptionOptions?
    ): List<String> {
        return mainFrame.selectOption(selector, values, options)
    }

    override fun selectOption(
        selector: String,
        values: Array<SelectOption>?,
        options: SelectOptionOptions?
    ): List<String> {
        return mainFrame.selectOption(selector, values, options)
    }

    override fun setContent(html: String, options: SetContentOptions?) {
        mainFrame.setContent(html, options)
    }

    override fun setDefaultNavigationTimeout(timeout: Double) {
        timeoutSettings.defaultNavigationTimeout = timeout
        val params = JsonObject()
        params.addProperty("timeout", timeout)
        sendMessage("setDefaultNavigationTimeoutNoReply", params)
    }

    override fun setDefaultTimeout(timeout: Double) {
        timeoutSettings.defaultTimeout = timeout
        val params = JsonObject()
        params.addProperty("timeout", timeout)
        sendMessage("setDefaultTimeoutNoReply", params)
    }

    override fun setExtraHTTPHeaders(headers: Map<String, String>) {
        val params = JsonObject()
        val jsonHeaders = JsonArray()
        for (entry in headers.entries) {
            val header = JsonObject()
            header.addProperty("name", entry.key)
            header.addProperty("value", entry.value)
            jsonHeaders.add(header)
        }
        params.add("headers", jsonHeaders)
        sendMessage("setExtraHTTPHeaders", params)
    }

    override fun setInputFiles(selector: String, files: Path, options: SetInputFilesOptions?) {
        setInputFiles(selector, arrayOf(files), options)
    }

    override fun setInputFiles(selector: String, files: Array<Path>, options: SetInputFilesOptions?) {
        mainFrame.setInputFiles(selector, files, options)
    }

    override fun setInputFiles(selector: String, files: FilePayload, options: SetInputFilesOptions?) {
        setInputFiles(selector, arrayOf(files), options)
    }

    override fun setInputFiles(selector: String, files: Array<FilePayload>, options: SetInputFilesOptions?) {
        mainFrame.setInputFiles(selector, files, options)
    }

    override fun setViewportSize(width: Int, height: Int) {
        viewPort = ViewportSize {
            it.height = height
            it.width = width
        }
        val params = JsonObject()
        params.add("viewportSize", gson().toJsonTree(viewPort))
        sendMessage("setViewportSize", params)
    }

    override fun tap(selector: String, options: TapOptions?) {
        mainFrame.tap(selector, options)
    }

    override fun textContent(selector: String, options: TextContentOptions?): String {
        return mainFrame.textContent(selector, options)
    }

    override fun title(): String {
        return mainFrame.title()
    }

    override fun touchScreen(): ITouchScreen {
        return touchScreen
    }

    override fun type(selector: String, text: String, options: TypeOptions?) {
        mainFrame.type(selector, text, options)
    }

    override fun uncheck(selector: String, options: UncheckOptions?) {
        mainFrame.uncheck(selector, options)
    }

    override fun unroute(url: String, handler: ((IRoute) -> Unit)?) {
        unroute(UrlMatcher(url), handler)
    }

    override fun unroute(url: Pattern, handler: ((IRoute) -> Unit)?) {
        unroute(UrlMatcher(url), handler)
    }

    override fun unroute(url: (String) -> Boolean, handler: ((IRoute) -> Unit)?) {
        unroute(UrlMatcher(url), handler)
    }

    private fun unroute(matcher: UrlMatcher, handler: ((IRoute) -> Unit)?) {
        routes.remove(matcher, handler)
        if (routes.size() == 0) {
            val params = JsonObject()
            params.addProperty("enabled", false)
            sendMessage("setNetworkInterceptionEnabled", params)
        }
    }

    override fun url(): String {
        return mainFrame.url()
    }

    override fun video(): IVideo? {
        // Note: we are creating Video object lazily, because we do not know
        // BrowserContextOptions when constructing the page - it is assigned
        // too late during launchPersistentContext.
        if ((browserContext as BrowserContext).videosDir == null) {
            return null
        }
        return forceVideo()
    }

    private fun forceVideo(): IVideo? {
        if (video == null) {
            video = Video(this)
        }
        return video
    }

    override fun viewportSize(): ViewportSize {
        return viewPort
    }

    override fun waitForClose(options: WaitForCloseOptions?, callback: () -> Unit): IPage? {
        return waitForEventWithTimeout(CLOSE, (options ?: WaitForCloseOptions {}).timeout, callback)
    }

    override fun waitForConsoleMessage(options: WaitForConsoleMessageOptions?, callback: () -> Unit): IConsoleMessage? {
        return waitForEventWithTimeout(CONSOLE, (options ?: WaitForConsoleMessageOptions {}).timeout, callback)
    }

    override fun waitForDownload(options: WaitForDownloadOptions?, callback: () -> Unit): IDownload? {
        return waitForEventWithTimeout(DOWNLOAD, (options ?: WaitForDownloadOptions {}).timeout, callback)
    }

    override fun waitForFileChooser(options: WaitForFileChooserOptions?, callback: () -> Unit): IFileChooser? {
        return waitForEventWithTimeout(FILECHOOSER, (options ?: WaitForFileChooserOptions {}).timeout, callback)
    }

    override fun waitForFunction(expression: String, arg: Any?, options: WaitForFunctionOptions?): IJSHandle {
        return mainFrame.waitForFunction(expression, arg, options)
    }

    override fun waitForLoadState(state: LoadState?, options: WaitForLoadStateOptions?) {
        return mainFrame.waitForLoadState(state, options)
    }

    override fun waitForNavigation(options: WaitForNavigationOptions?, callback: () -> Unit): IResponse? {
        return mainFrame.waitForNavigation(options, callback)
    }

    override fun waitForPopup(options: WaitForPopupOptions?, callback: () -> Unit): IPage? {
        return waitForEventWithTimeout(POPUP, (options ?: WaitForPopupOptions {}).timeout, callback)
    }

    override fun waitForRequest(
        urlOrPredicate: String?,
        options: WaitForRequestOptions?,
        callback: () -> Unit
    ): IRequest? {
        return waitForRequest(toRequestPredicate(UrlMatcher(urlOrPredicate ?: "")), options, callback)
    }

    override fun waitForRequest(
        urlOrPredicate: Pattern,
        options: WaitForRequestOptions?,
        callback: () -> Unit
    ): IRequest? {
        return waitForRequest(toRequestPredicate(UrlMatcher(urlOrPredicate)), options, callback)
    }

    override fun waitForRequest(
        urlOrPredicate: ((IRequest) -> Boolean)?,
        options: WaitForRequestOptions?,
        callback: () -> Unit
    ): IRequest? {
        val waits = arrayListOf<IWait<IRequest>>()
        waits.add(WaitEvent(listeners, REQUEST, { request -> urlOrPredicate == null || urlOrPredicate(request) }))
        waits.add(createWaitForCloseHelper())
        waits.add(createWaitTimeout((options ?: WaitForRequestOptions {}).timeout))
        return runUtil(WaitRace(waits), callback)
    }

    override fun waitForResponse(
        urlOrPredicate: String?,
        options: WaitForResponseOptions?,
        callback: () -> Unit
    ): IResponse? {
        return waitForResponse(toResponsePredicate(UrlMatcher(urlOrPredicate ?: "")), options, callback)
    }

    override fun waitForResponse(
        urlOrPredicate: Pattern?,
        options: WaitForResponseOptions?,
        callback: () -> Unit
    ): IResponse? {
        return waitForResponse(toResponsePredicate(UrlMatcher(urlOrPredicate ?: "")), options, callback)
    }

    override fun waitForResponse(
        urlOrPredicate: ((IResponse) -> Boolean)?,
        options: WaitForResponseOptions?,
        callback: () -> Unit
    ): IResponse? {
        val waits = arrayListOf<IWait<IResponse>>()
        waits.add(WaitEvent(listeners, RESPONSE, { response -> urlOrPredicate == null || urlOrPredicate(response) }))
        waits.add(createWaitForCloseHelper())
        waits.add(createWaitTimeout((options ?: WaitForResponseOptions {}).timeout))
        return runUtil(WaitRace(waits), callback)
    }

    override fun waitForSelector(selector: String, options: WaitForSelectorOptions?): IElementHandle? {
        return mainFrame.waitForSelector(selector, options)
    }

    override fun waitForTimeout(timeout: Double) {
        mainFrame.waitForTimeout(timeout)
    }

    override fun waitForURL(url: String, options: WaitForURLOptions?) {
        waitForUrl(UrlMatcher(url), options)
    }

    override fun waitForURL(url: Pattern, options: WaitForURLOptions?) {
        waitForUrl(UrlMatcher(url), options)
    }

    override fun waitForURL(url: (String) -> Boolean, options: WaitForURLOptions?) {
        waitForUrl(UrlMatcher(url), options)
    }

    private fun waitForUrl(matcher: UrlMatcher, options: WaitForURLOptions?) {
        mainFrame.waitForURL(matcher, options)
    }

    override fun waitForWebSocket(options: WaitForWebSocketOptions?, callback: () -> Unit): IWebSocket? {
        return waitForEventWithTimeout(WEBSOCKET, (options ?: WaitForWebSocketOptions {}).timeout, callback)
    }

    override fun waitForWorker(options: WaitForWorkerOptions?, callback: () -> Unit): IWorker? {
        return waitForEventWithTimeout(WORKER, (options ?: WaitForWorkerOptions {}).timeout, callback)
    }

    override fun workers(): List<IWorker> {
        return workers.toList()
    }

    override fun onceDialog(handler: (IDialog) -> Unit) {
        val consumer: (IDialog) -> Unit = {
            handler(it)
        }
        try {
            onDialog(consumer)
        } finally {
            offDialog(consumer)
        }
    }

    override fun handleEvent(event: String, params: JsonObject) {
        when (event) {
            "dialog" -> {
                val guid = params["dialog"].asJsonObject["guid"].asString
                val dialog = messageProcessor.getExistingObject<IDialog>(guid)
                if (listeners.hasListeners(DIALOG)) {
                    listeners.notify(DIALOG, dialog)
                } else {
                    dialog.dismiss()
                }
            }
            "worker" -> {
                val guid = params["worker"].asJsonObject["guid"].asString
                val worker = messageProcessor.getExistingObject<IWorker>(guid)
                (worker as Worker).page = this
                workers.add(worker)
                listeners.notify(WORKER, worker)
            }
            "webSocket" -> {
                val guid = params["webSocket"].asJsonObject["guid"].asString
                val webSocket = messageProcessor.getExistingObject<IWebSocket>(guid)
                listeners.notify(WEBSOCKET, webSocket)
            }
            "console" -> {
                val guid = params["message"].asJsonObject["guid"].asString
                val message = messageProcessor.getExistingObject<IConsoleMessage>(guid)
                listeners.notify(CONSOLE, message)
            }
            "download" -> {
                val artifactGuid = params["artifact"].asJsonObject["guid"].asString
                val artifact = messageProcessor.getExistingObject<IArtifact>(artifactGuid)
                val download = Download(artifact, params)
                listeners.notify(DOWNLOAD, download)
            }
            "fileChooser" -> {
                val guid = params["element"].asJsonObject["guid"].asString
                val elementHandle = messageProcessor.getExistingObject<IElementHandle>(guid)
                val fileChooser = FileChooser(this, elementHandle, params["isMultiple"].asBoolean)
                listeners.notify(FILECHOOSER, fileChooser)
            }
            "bindingCall" -> {
                val guid = params["binding"].asJsonObject["guid"].asString
                val bindingCall = messageProcessor.getExistingObject<IBindingCall>(guid)
                var binding = bindings[bindingCall.name()]
                if (binding == null) {
                    binding = (browserContext as BrowserContext).bindings[bindingCall.name()]
                }
                if (binding != null) {
                    try {
                        bindingCall.call(binding)
                    } catch (e: RuntimeException) {
                        e.printStackTrace()
                    }
                }
            }
            "load" -> {
                listeners.notify(LOAD, this)
            }
            "domcontentloaded" -> {
                listeners.notify(DOMCONTENTLOADED, this)
            }
            "request" -> {
                val guid = params["request"].asJsonObject["guid"].asString
                val request = messageProcessor.getExistingObject<IRequest>(guid)
                listeners.notify(REQUEST, request)
            }
            "requestFailed" -> {
                val guid = params["request"].asJsonObject["guid"].asString
                val request = messageProcessor.getExistingObject<IRequest>(guid)
                if (params.has("failureText")) {
                    (request as Request).failure = params["failureText"].asString
                }
                listeners.notify(REQUESTFAILED, request)
            }
            "requestFinished" -> {
                val guid = params["request"].asJsonObject["guid"].asString
                val request = messageProcessor.getExistingObject<IRequest>(guid)
                listeners.notify(REQUESTFINISHED, request)
            }
            "response" -> {
                val guid = params["response"].asJsonObject["guid"].asString
                val response = messageProcessor.getExistingObject<IResponse>(guid)
                listeners.notify(RESPONSE, response)
            }
            "frameAttached" -> {
                val guid = params["frame"].asJsonObject["guid"].asString
                val frame = messageProcessor.getExistingObject<IFrame>(guid)
                frames.add(frame)
                (frame as Frame).page = this
                if (frame.parentFrame != null) {
                    (frame.parentFrame as Frame).childFrames.add(frame)
                }
                listeners.notify(FRAMEATTACHED, frame)
            }
            "frameDetached" -> {
                val guid = params["frame"].asJsonObject["guid"].asString
                val frame = messageProcessor.getExistingObject<IFrame>(guid)
                frames.remove(frame)
                (frame as Frame).isDetachedValue = true
                if (frame.parentFrame != null) {
                    (frame.parentFrame as Frame).childFrames.remove(frame)
                }
                listeners.notify(FRAMEDETACHED, frame)
            }
            "route" -> {
                val guid = params["route"].asJsonObject["guid"].asString
                val route = messageProcessor.getExistingObject<IRoute>(guid)
                var isHandle = routes.handle(route)
                if (!isHandle) {
                    isHandle = (browserContext as BrowserContext).routes.handle(route)
                }
                if (!isHandle) {
                    route.resume()
                }
            }
            "video" -> {
                val artifactGuid = params["artifact"].asJsonObject["guid"].asString
                val artifact = messageProcessor.getExistingObject<Artifact>(artifactGuid)
                forceVideo()?.setArtifact(artifact)
            }
            "pageError" -> {
                val error = fromJson(params["error"].asJsonObject, SerializedError::class.java)
                var errorStr = ""
                if (error.error != null) {
                    errorStr = "${error.error!!.name}: ${error.error!!.message}"
                    if (error.error!!.stack != null && error.error!!.stack!!.isNotEmpty()) {
                        errorStr += "\n ${error.error!!.stack}"
                    }
                }
                listeners.notify(PAGEERROR, errorStr)
            }
            "crash" -> {
                listeners.notify(CRASH, this)
            }
            "close" -> {
                didClose()
            }
        }
    }

    fun <T> createWaitForCloseHelper(): IWait<T> {
        return WaitRace(listOf(WaitPageClose(listeners), WaitPageCrash(listeners)))
    }

    fun <T> createWaitTimeout(timeout: Double?): IWait<T> {
        return timeoutSettings.createWait(timeout)
    }

    fun <T> createWaitNavigationTimeout(timeout: Double?): IWait<T> {
        return WaitTimeout(timeoutSettings.navigationTimout(timeout))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> createWaitFrameDetach(frame: IFrame): IWait<T> {
        return WaitFrameDetach(listeners, frame) as IWait<T>
    }

    private fun <T> waitForEventWithTimeout(eventType: EventType, timeout: Double?, code: () -> Unit): T? {
        val waits = arrayListOf<IWait<T>>()
        waits.add(WaitEvent(listeners, eventType))
        waits.add(createWaitForCloseHelper())
        waits.add(createWaitTimeout(timeout))
        return runUtil(WaitRace(waits), code)
    }

    private fun toRequestPredicate(matcher: UrlMatcher): (IRequest) -> Boolean {
        return { request -> matcher.test(request.url()) }
    }

    private fun toResponsePredicate(matcher: UrlMatcher): (IResponse) -> Boolean {
        return { response -> matcher.test(response.url()) }
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
