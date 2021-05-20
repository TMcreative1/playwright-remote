package com.playwright.remote.engine.frame.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.core.enums.InternalEventType
import com.playwright.remote.core.enums.InternalEventType.LOADSTATE
import com.playwright.remote.core.enums.InternalEventType.NAVIGATED
import com.playwright.remote.core.enums.LoadState
import com.playwright.remote.core.enums.WaitUntilState
import com.playwright.remote.core.enums.WaitUntilState.LOAD
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.domain.file.FilePayload
import com.playwright.remote.domain.serialize.SerializedError.SerializedValue
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.handle.element.api.IElementHandle
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.options.*
import com.playwright.remote.engine.options.element.*
import com.playwright.remote.engine.options.element.PressOptions
import com.playwright.remote.engine.options.element.TypeOptions
import com.playwright.remote.engine.options.wait.WaitForFunctionOptions
import com.playwright.remote.engine.options.wait.WaitForLoadStateOptions
import com.playwright.remote.engine.options.wait.WaitForNavigationOptions
import com.playwright.remote.engine.options.wait.WaitForURLOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.page.impl.Page
import com.playwright.remote.engine.parser.IParser.Companion.convert
import com.playwright.remote.engine.parser.IParser.Companion.fromJson
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.route.UrlMatcher
import com.playwright.remote.engine.route.response.api.IResponse
import com.playwright.remote.engine.serializer.Serialization.Companion.deserialize
import com.playwright.remote.engine.serializer.Serialization.Companion.parseStringList
import com.playwright.remote.engine.serializer.Serialization.Companion.serializeArgument
import com.playwright.remote.engine.serializer.Serialization.Companion.toJsonArray
import com.playwright.remote.engine.serializer.Serialization.Companion.toProtocol
import com.playwright.remote.engine.waits.api.IWait
import com.playwright.remote.engine.waits.impl.WaitLoadState
import com.playwright.remote.engine.waits.impl.WaitNavigation
import com.playwright.remote.engine.waits.impl.WaitRace
import com.playwright.remote.engine.waits.impl.WaitTimeout
import com.playwright.remote.utils.Utils.Companion.toFilePayloads
import okio.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files.readAllBytes
import java.nio.file.Path
import java.util.regex.Pattern

class Frame(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IFrame {
    private var name: String = initializer["name"].asString
    private var url: String = initializer["url"].asString
    var parentFrame: IFrame? = null
    val childFrames = linkedSetOf<IFrame>()
    private val loadStates = hashSetOf<LoadState>()
    private val internalListeners = ListenerCollection<InternalEventType>()
    var page: IPage? = null
    var isDetachedValue = false

    init {
        if (initializer.has("parentFrame")) {
            parentFrame = messageProcessor.getExistingObject(initializer["parentFrame"].asJsonObject["guid"].asString)
            (parentFrame as Frame).childFrames.add(this)
        }
        for (item in initializer["loadStates"].asJsonArray) {
            loadStates.add(LoadState.valueOf(item.asString.toUpperCase()))
        }
    }

    override fun name(): String {
        return name
    }

    override fun url(): String {
        return url
    }

    override fun page(): IPage? {
        return page
    }

    fun addScriptTag(options: AddScriptTagOptions?): IElementHandle {
        val opt = options ?: AddScriptTagOptions {}
        val jsonOptions = Gson().toJsonTree(opt).asJsonObject
        if (opt.path != null) {
            jsonOptions.remove("path")
            val encoded: ByteArray
            try {
                encoded = readAllBytes(opt.path!!)
            } catch (e: IOException) {
                throw PlaywrightException("Failed to read from file", e)
            }
            var content = String(encoded, UTF_8)
            content += "//$ sourceURL= ${opt.path.toString().replace("\n", "")}"
            jsonOptions.addProperty("content", content)
        }
        val json = sendMessage("addScriptTag", jsonOptions)
        return messageProcessor.getExistingObject(json!!.asJsonObject["element"].asJsonObject["guid"].asString)
    }

    fun addStyleTag(options: AddStyleTagOptions?): IElementHandle {
        val opt = options ?: AddStyleTagOptions {}
        val jsonOptions = Gson().toJsonTree(opt).asJsonObject
        if (opt.path != null) {
            jsonOptions.remove("path")
            val encoded: ByteArray
            try {
                encoded = readAllBytes(opt.path)
            } catch (e: IOException) {
                throw PlaywrightException("Failed to read from", e)
            }
            var content = String(encoded, UTF_8)
            content += "/*# sourceURL=${opt.path.toString().replace("\n", "")}*/"
            jsonOptions.addProperty("content", content)
        }
        val json = sendMessage("addStyleTag", jsonOptions)
        return messageProcessor.getExistingObject(json!!.asJsonObject["element"].asJsonObject["guid"].asString)
    }

    fun check(selector: String, options: CheckOptions?) {
        val opt = options ?: CheckOptions {}
        val params = Gson().toJsonTree(opt).asJsonObject
        params.addProperty("selector", selector)
        sendMessage("check", params)
    }

    fun click(selector: String, options: ClickOptions?) {
        val opt = options ?: ClickOptions {}
        val params = Gson().toJsonTree(opt).asJsonObject
        params.addProperty("selector", selector)
        sendMessage("click", params)
    }

    fun content(): String {
        return sendMessage("content")!!.asJsonObject["value"].asString
    }

    fun doubleClick(selector: String, options: DoubleClickOptions?) {
        val params = Gson().toJsonTree(options ?: DoubleClickOptions {}).asJsonObject
        params.addProperty("selector", selector)
        sendMessage("dblclick", params)
    }

    fun dispatchEvent(selector: String, type: String, eventInit: Any?, options: DispatchEventOptions?) {
        val params = Gson().toJsonTree(options ?: DispatchEventOptions {}).asJsonObject
        params.addProperty("selector", selector)
        params.addProperty("type", type)
        params.add("eventInit", Gson().toJsonTree(serializeArgument(eventInit)))
        sendMessage("dispatchEvent", params)
    }

    fun evalOnSelector(selector: String, expression: String, arg: Any?): Any =
        evalOnSelector(selector, expression, arg, "evalOnSelector")

    fun evalOnSelectorAll(selector: String, expression: String, arg: Any?): Any =
        evalOnSelector(selector, expression, arg, "evalOnSelectorAll")

    private fun evalOnSelector(selector: String, expression: String, arg: Any?, method: String): Any {
        val params = JsonObject()
        params.addProperty("selector", selector)
        params.addProperty("expression", expression)
        params.add("arg", Gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage(method, params)
        val value = fromJson(json!!.asJsonObject["value"], SerializedValue::class.java)
        return deserialize(value)
    }

    fun evaluate(expression: String, arg: Any?): Any {
        val params = JsonObject()
        params.addProperty("expression", expression)
        params.addProperty("world", "main")
        params.add("arg", Gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("evaluateExpression", params)
        val value = fromJson(json!!.asJsonObject["value"], SerializedValue::class.java)
        return deserialize(value)
    }

    fun evaluateHandle(expression: String, arg: Any?): IJSHandle {
        val params = JsonObject()
        params.addProperty("expression", expression)
        params.addProperty("world", "main")
        params.add("arg", Gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("evaluateExpressionHandle", params)
        return messageProcessor.getExistingObject(json!!.asJsonObject["handle"].asJsonObject["guid"].asString)
    }

    fun fill(selector: String, value: String, options: FillOptions?) {
        val params = Gson().toJsonTree(options ?: FillOptions {}).asJsonObject
        params.addProperty("selector", selector)
        params.addProperty("value", value)
        sendMessage("fill", params)
    }

    fun focus(selector: String, options: FocusOptions?) {
        val params = Gson().toJsonTree(options ?: FocusOptions {}).asJsonObject
        params.addProperty("selector", selector)
        sendMessage("focus", params)
    }

    fun getAttribute(selector: String, name: String, options: GetAttributeOptions?): String? {
        val params = Gson().toJsonTree(options ?: GetAttributeOptions {}).asJsonObject
        params.addProperty("selector", selector)
        params.addProperty("name", name)
        val json = sendMessage("getAttribute", params)!!.asJsonObject
        if (json.has("value")) {
            return json["value"].asString
        }
        return null
    }

    override fun navigate(url: String, options: NavigateOptions): IResponse? {
        val params = Gson().toJsonTree(options).asJsonObject
        params.addProperty("url", url)
        val result = sendMessage("goto", params)
        val jsonResponse = result!!.asJsonObject["response"].asJsonObject ?: return null
        return messageProcessor.getExistingObject(jsonResponse["guid"].asString)
    }

    override fun hover(selector: String, options: HoverOptions?) {
        val params = Gson().toJsonTree(options ?: HoverOptions {}).asJsonObject
        params.addProperty("selector", selector)
        sendMessage("hover", params)
    }

    override fun innerHTML(selector: String, options: InnerHTMLOptions?): String {
        val params = Gson().toJsonTree(options ?: InnerHTMLOptions {}).asJsonObject
        params.addProperty("selector", selector)
        val json = sendMessage("innerHTML", params)!!.asJsonObject
        return json["value"].asString
    }

    override fun innerText(selector: String, options: InnerTextOptions?): String {
        val params = Gson().toJsonTree(options ?: InnerTextOptions {}).asJsonObject
        params.addProperty("selector", selector)
        val json = sendMessage("innerText", params)!!.asJsonObject
        return json["value"].asString
    }

    override fun isChecked(selector: String, options: IsCheckedOptions?): Boolean {
        val params = Gson().toJsonTree(options ?: IsCheckedOptions {}).asJsonObject
        params.addProperty("selector", selector)
        val json = sendMessage("isChecked", params)!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun isDetached(): Boolean {
        return isDetachedValue
    }

    override fun isDisabled(selector: String, options: IsDisabledOptions?): Boolean {
        val params = Gson().toJsonTree(options ?: IsDisabledOptions {}).asJsonObject
        params.addProperty("selector", selector)
        val json = sendMessage("isDisabled", params)!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun isEditable(selector: String, options: IsEditableOptions?): Boolean {
        val params = Gson().toJsonTree(options ?: IsEditableOptions {}).asJsonObject
        params.addProperty("selector", selector)
        val json = sendMessage("isEditable", params)!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun isEnabled(selector: String, options: IsEnabledOptions?): Boolean {
        val params = Gson().toJsonTree(options ?: IsEnabledOptions {}).asJsonObject
        params.addProperty("selector", selector)
        val json = sendMessage("isEnabled", params)!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun isHidden(selector: String, options: IsHiddenOptions?): Boolean {
        val params = Gson().toJsonTree(options ?: IsHiddenOptions {}).asJsonObject
        params.addProperty("selector", selector)
        val json = sendMessage("isHidden", params)!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun isVisible(selector: String, options: IsVisibleOptions?): Boolean {
        val params = Gson().toJsonTree(options ?: IsVisibleOptions {}).asJsonObject
        params.addProperty("selector", selector)
        val json = sendMessage("isVisible", params)!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun press(selector: String, key: String, options: PressOptions?) {
        val params = Gson().toJsonTree(options ?: PressOptions {}).asJsonObject
        params.addProperty("selector", selector)
        params.addProperty("key", key)
        sendMessage("press", params)
    }

    override fun querySelector(selector: String): IElementHandle? {
        val params = JsonObject()
        params.addProperty("selector", selector)
        val json = sendMessage("querySelector", params)
        val element = json!!.asJsonObject["element"].asJsonObject ?: return null
        return messageProcessor.getExistingObject(element["guid"].asString)
    }

    override fun querySelectorAll(selector: String): List<IElementHandle>? {
        val params = JsonObject()
        params.addProperty("selector", selector)
        val json = sendMessage("querySelectorAll", params)
        val elements = json!!.asJsonObject["elements"].asJsonArray ?: return null
        val handles = arrayListOf<IElementHandle>()
        for (element in elements) {
            handles.add(messageProcessor.getExistingObject(element.asJsonObject["guid"].asString))
        }
        return handles
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
        val params = Gson().toJsonTree(options ?: SelectOptionOptions {}).asJsonObject
        params.addProperty("selector", selector)
        if (values != null) {
            params.add("elements", toProtocol(values))
        }
        return selectOption(params)
    }

    override fun selectOption(
        selector: String,
        values: Array<SelectOption>?,
        options: SelectOptionOptions?
    ): List<String> {
        val params = Gson().toJsonTree(options ?: SelectOptionOptions {}).asJsonObject
        params.addProperty("selector", selector)
        if (values != null) {
            params.add("options", Gson().toJsonTree(values))
        }
        return selectOption(params)
    }

    private fun selectOption(params: JsonObject): List<String> {
        val json = sendMessage("selectOption", params)!!.asJsonObject
        return parseStringList(json["values"].asJsonArray)
    }

    override fun setContent(html: String, options: SetContentOptions?) {
        val params = Gson().toJsonTree(options ?: SetContentOptions {}).asJsonObject
        params.addProperty("html", html)
        sendMessage("setContent", params)
    }

    override fun setInputFiles(selector: String, files: Path, options: SetInputFilesOptions?) {
        setInputFiles(selector, arrayOf(files), options)
    }

    override fun setInputFiles(selector: String, files: Array<Path>, options: SetInputFilesOptions?) {
        setInputFiles(selector, toFilePayloads(files), options)
    }

    override fun setInputFiles(selector: String, files: FilePayload, options: SetInputFilesOptions?) {
        setInputFiles(selector, arrayOf(files), options)
    }

    override fun setInputFiles(selector: String, files: Array<FilePayload>, options: SetInputFilesOptions?) {
        val params = Gson().toJsonTree(options ?: SetInputFilesOptions {}).asJsonObject
        params.addProperty("selector", selector)
        params.add("files", toJsonArray(files))
        sendMessage("setInputFiles", params)
    }

    override fun tap(selector: String, options: TapOptions?) {
        val params = Gson().toJsonTree(options ?: TapOptions {}).asJsonObject
        params.addProperty("selector", selector)
        sendMessage("tap", params)
    }

    override fun textContent(selector: String, options: TextContentOptions?): String {
        val params = Gson().toJsonTree(options ?: TextContentOptions {}).asJsonObject
        params.addProperty("selector", selector)
        return sendMessage("textContent", params)!!.asJsonObject["value"].asString
    }

    override fun title(): String {
        return sendMessage("title")!!.asJsonObject["value"].asString
    }

    override fun type(selector: String, text: String, options: TypeOptions?) {
        val params = Gson().toJsonTree(options ?: TypeOptions {}).asJsonObject
        params.addProperty("selector", selector)
        params.addProperty("text", text)
        sendMessage("type", params)
    }

    override fun uncheck(selector: String, options: UncheckOptions?) {
        val params = Gson().toJsonTree(options ?: UncheckOptions {}).asJsonObject
        params.addProperty("selector", selector)
        sendMessage("uncheck", params)
    }

    override fun waitForFunction(expression: String, arg: Any?, options: WaitForFunctionOptions?): IJSHandle {
        val params = Gson().toJsonTree(options ?: WaitForFunctionOptions {}).asJsonObject
        params.addProperty("expression", expression)
        params.add("arg", Gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("waitForFunction", params)
        val element = json!!.asJsonObject["handle"].asJsonObject
        return messageProcessor.getExistingObject(element["guid"].asString)
    }

    override fun waitForLoadState(state: LoadState?, options: WaitForLoadStateOptions?) {
        val waits = arrayListOf<IWait<Void?>>()
        waits.add(WaitLoadState(state, internalListeners, loadStates))
        waits.add((page as Page).createWaitForCloseHelper())
        waits.add((page as Page).createWaitTimeout((options ?: WaitForLoadStateOptions {}).timeout))
        runUtil(WaitRace(waits)) {}
    }

    override fun waitForNavigation(options: WaitForNavigationOptions?, callback: () -> Unit): IResponse? {
        return waitForNavigation(options, null, callback)
    }

    private fun waitForNavigation(
        options: WaitForNavigationOptions?,
        matcher: UrlMatcher?,
        callback: () -> Unit
    ): IResponse? {
        val opt = options ?: WaitForNavigationOptions {}
        if (opt.waitUntil == null) {
            opt.waitUntil = LOAD
        }
        val waits = arrayListOf<IWait<IResponse?>>()
        waits.add(
            WaitNavigation(
                matcher ?: UrlMatcher.forOneOf(opt.url),
                convert(opt.waitUntil, LoadState::class.java),
                internalListeners,
                messageProcessor,
                loadStates
            )
        )
        waits.add((page as Page).createWaitForCloseHelper())
        waits.add((page as Page).createWaitFrameDetach(this))
        waits.add((page as Page).createWaitNavigationTimeout(opt.timeout))
        return runUtil(WaitRace(waits), callback)
    }

    override fun waitForSelector(selector: String, options: WaitForSelectorOptions?): IElementHandle? {
        val params = Gson().toJsonTree(options ?: WaitForSelectorOptions {}).asJsonObject
        params.addProperty("selector", selector)
        val json = sendMessage("waitForSelector", params)
        val element = json!!.asJsonObject["element"].asJsonObject ?: return null
        return messageProcessor.getExistingObject(element["guid"].asString)
    }

    override fun waitForTimeout(timeout: Double) {
        runUtil(object : WaitTimeout<Void?>(timeout) {
            override fun get(): Void? {
                return null
            }
        }) {}
    }

    override fun waitForURL(url: String, options: WaitForURLOptions?) {
        waitForURL(UrlMatcher(url), options)
    }

    override fun waitForURL(url: Pattern, options: WaitForURLOptions?) {
        waitForURL(UrlMatcher(url), options)
    }

    override fun waitForURL(url: (String) -> Boolean, options: WaitForURLOptions?) {
        waitForURL(UrlMatcher(url), options)
    }

    fun waitForURL(matcher: UrlMatcher, options: WaitForURLOptions?) {
        val opt = options ?: WaitForURLOptions {}
        if (matcher.test(url())) {
            waitForLoadState(
                convert(opt.waitUntil, LoadState::class.java),
                convert(opt, WaitForLoadStateOptions::class.java)
            )
            return
        }
        waitForNavigation(convert(opt, WaitForNavigationOptions::class.java), matcher) {}
    }

    override fun handleEvent(event: String, params: JsonObject) {
        when (event) {
            "loadstate" -> {
                val add = params["add"]
                if (add != null) {
                    val state = LoadState.valueOf(add.asString.toUpperCase())
                    loadStates.add(state)
                    internalListeners.notify(LOADSTATE, state)
                }
                val remove = params["remove"]
                if (remove != null) {
                    loadStates.remove(LoadState.valueOf(remove.asString.toUpperCase()))
                }
            }
            "navigated" -> {
                url = params["url"].asString
                name = params["name"].asString
                if (!params.has("error") && page != null) {
                    (page as Page).frameNavigated(this)
                }
                internalListeners.notify(NAVIGATED, params)
            }
        }
    }
}