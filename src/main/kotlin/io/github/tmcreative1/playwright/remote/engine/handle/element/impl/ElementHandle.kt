package io.github.tmcreative1.playwright.remote.engine.handle.element.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.options.CheckOptions
import io.github.tmcreative1.playwright.remote.engine.options.SelectOption
import io.github.tmcreative1.playwright.remote.engine.parser.IParser.Companion.fromJson
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.engine.serialize.CustomGson.Companion.gson
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.deserialize
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.parseStringList
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.serializeArgument
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.toJsonArray
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.toProtocol
import io.github.tmcreative1.playwright.remote.core.enums.ElementState
import io.github.tmcreative1.playwright.remote.core.enums.ScreenshotType.JPEG
import io.github.tmcreative1.playwright.remote.core.enums.ScreenshotType.PNG
import io.github.tmcreative1.playwright.remote.domain.BoundingBox
import io.github.tmcreative1.playwright.remote.domain.file.FilePayload
import io.github.tmcreative1.playwright.remote.domain.serialize.SerializedError
import io.github.tmcreative1.playwright.remote.engine.frame.api.IFrame
import io.github.tmcreative1.playwright.remote.engine.handle.element.api.IElementHandle
import io.github.tmcreative1.playwright.remote.engine.handle.js.impl.JSHandle
import io.github.tmcreative1.playwright.remote.engine.options.element.*
import io.github.tmcreative1.playwright.remote.utils.Utils.Companion.toFilePayloads
import io.github.tmcreative1.playwright.remote.utils.Utils.Companion.writeToFile
import java.nio.file.Path
import java.util.*

class ElementHandle(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : JSHandle(
    parent,
    type,
    guid,
    initializer
), IElementHandle {
    override fun boundingBox(): BoundingBox? {
        val json = sendMessage("boundingBox")!!.asJsonObject
        if (!json.has("value")) {
            return null
        }
        return fromJson(json["value"], BoundingBox::class.java)
    }

    override fun check(options: CheckOptions?) {
        val params = gson().toJsonTree(options ?: CheckOptions {}).asJsonObject
        sendMessage("check", params)
    }

    override fun click(options: ClickOptions?) {
        val params = gson().toJsonTree(options ?: ClickOptions {}).asJsonObject
        sendMessage("click", params)
    }

    override fun contentFrame(): IFrame? {
        val json = sendMessage("contentFrame")!!.asJsonObject
        if (!json.has("frame")) {
            return null
        }
        return messageProcessor.getExistingObject(json["frame"].asJsonObject["guid"].asString)
    }

    override fun doubleClick(options: DoubleClickOptions?) {
        val params = gson().toJsonTree(options ?: DoubleClickOptions {}).asJsonObject
        sendMessage("dblclick", params)
    }

    override fun dispatchEvent(type: String, eventInit: Any?) {
        val params = JsonObject()
        params.addProperty("type", type)
        params.add("eventInit", gson().toJsonTree(serializeArgument(eventInit)))
        sendMessage("dispatchEvent", params)
    }

    override fun evalOnSelector(selector: String, expression: String, arg: Any?): Any {
        return evalOnSelector(selector, expression, arg, "evalOnSelector")
    }

    override fun evalOnSelectorAll(selector: String, expression: String, arg: Any?): Any {
        return evalOnSelector(selector, expression, arg, "evalOnSelectorAll")
    }

    private fun evalOnSelector(selector: String, expression: String, arg: Any?, method: String): Any {
        val params = JsonObject()
        params.addProperty("selector", selector)
        params.addProperty("expression", expression)
        params.add("arg", gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage(method, params)
        val value = fromJson(json!!.asJsonObject["value"], SerializedError.SerializedValue::class.java)
        return deserialize(value)
    }

    override fun fill(value: String, options: FillOptions?) {
        val params = gson().toJsonTree(options ?: FillOptions {}).asJsonObject
        params.addProperty("value", value)
        sendMessage("fill", params)
    }

    override fun focus() {
        sendMessage("focus")
    }

    override fun getAttribute(name: String): String? {
        val params = JsonObject()
        params.addProperty("name", name)
        val json = sendMessage("getAttribute", params)!!.asJsonObject
        return if (json.has("value")) json["value"].asString else null
    }

    override fun hover(options: HoverOptions?) {
        val params = gson().toJsonTree(options ?: HoverOptions {}).asJsonObject
        sendMessage("hover", params)
    }

    override fun innerHTML(): String {
        val json = sendMessage("innerHTML")!!.asJsonObject
        return json["value"].asString
    }

    override fun innerText(): String {
        val json = sendMessage("innerText")!!.asJsonObject
        return json["value"].asString
    }

    override fun isChecked(): Boolean {
        val json = sendMessage("isChecked")!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun isDisabled(): Boolean {
        val json = sendMessage("isDisabled")!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun isEditable(): Boolean {
        val json = sendMessage("isEditable")!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun isEnabled(): Boolean {
        val json = sendMessage("isEnabled")!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun isHidden(): Boolean {
        val json = sendMessage("isHidden")!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun isVisible(): Boolean {
        val json = sendMessage("isVisible")!!.asJsonObject
        return json["value"].asBoolean
    }

    override fun ownerFrame(): IFrame? {
        val json = sendMessage("ownerFrame")!!.asJsonObject
        if (!json.has("frame")) {
            return null
        }
        return messageProcessor.getExistingObject(json["frame"].asJsonObject["guid"].asString)
    }

    override fun press(key: String, options: PressOptions?) {
        val params = gson().toJsonTree(options ?: PressOptions {}).asJsonObject
        params.addProperty("key", key)
        sendMessage("press", params)
    }

    override fun querySelector(selector: String): IElementHandle? {
        val params = JsonObject()
        params.addProperty("selector", selector)
        val json = sendMessage("querySelector", params)
        val element = json!!.asJsonObject.getAsJsonObject("element") ?: return null
        return messageProcessor.getExistingObject(element["guid"].asString)
    }

    override fun querySelectorAll(selector: String): List<IElementHandle>? {
        val params = JsonObject()
        params.addProperty("selector", selector)
        val json = sendMessage("querySelectorAll", params)
        val elements = json!!.asJsonObject["elements"].asJsonArray ?: return null
        val handles = arrayListOf<IElementHandle>()
        for (item in elements) {
            handles.add(messageProcessor.getExistingObject(item.asJsonObject["guid"].asString))
        }
        return handles
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
                    if (".jpeg" == extension || ".jpg" == extension) {
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

    override fun scrollIntoViewIfNeeded(options: ScrollIntoViewIfNeededOptions?) {
        val params = gson().toJsonTree(options ?: ScrollIntoViewIfNeededOptions {}).asJsonObject
        sendMessage("scrollIntoViewIfNeeded", params)
    }

    override fun selectOption(value: String?, options: SelectOptionOptions?): List<String> {
        val values = if (value == null) null else arrayOf(value)
        return selectOption(values, options)
    }

    override fun selectOption(value: IElementHandle?, options: SelectOptionOptions?): List<String> {
        val values = if (value == null) null else arrayOf(value)
        return selectOption(values, options)
    }

    override fun selectOption(values: Array<String>?, options: SelectOptionOptions?): List<String> {
        if (values == null) {
            return selectOption(emptyArray<SelectOption>(), options)
        }
        return selectOption(values.map { value -> SelectOption { it.value = value } }.toTypedArray(), options)
    }

    override fun selectOption(value: SelectOption?, options: SelectOptionOptions?): List<String> {
        val values = if (value == null) null else arrayOf(value)
        return selectOption(values, options)
    }

    override fun selectOption(values: Array<IElementHandle>?, options: SelectOptionOptions?): List<String> {
        val params = gson().toJsonTree(options ?: SelectOptionOptions {}).asJsonObject
        if (values != null) {
            params.add("elements", toProtocol(values))
        }
        return selectOption(params)
    }

    override fun selectOption(values: Array<SelectOption>?, options: SelectOptionOptions?): List<String> {
        val params = gson().toJsonTree(options ?: SelectOptionOptions {}).asJsonObject
        if (values != null) {
            params.add("options", gson().toJsonTree(values))
        }
        return selectOption(params)
    }

    private fun selectOption(params: JsonObject): List<String> {
        val json = sendMessage("selectOption", params)!!.asJsonObject
        return parseStringList(json["values"].asJsonArray)
    }

    override fun selectText(options: SelectTextOptions?) {
        val params = gson().toJsonTree(options ?: SelectTextOptions {}).asJsonObject
        sendMessage("selectText", params)
    }

    override fun setInputFiles(files: Path, options: SetInputFilesOptions?) {
        setInputFiles(arrayOf(files), options)
    }

    override fun setInputFiles(files: Array<Path>, options: SetInputFilesOptions?) {
        setInputFiles(toFilePayloads(files), options)
    }

    override fun setInputFiles(files: FilePayload, options: SetInputFilesOptions?) {
        setInputFiles(arrayOf(files), options)
    }

    override fun setInputFiles(files: Array<FilePayload>, options: SetInputFilesOptions?) {
        val params = gson().toJsonTree(options ?: SetInputFilesOptions {}).asJsonObject
        params.add("files", toJsonArray(files))
        sendMessage("setInputFiles", params)
    }

    override fun tap(options: TapOptions?) {
        val params = gson().toJsonTree(options ?: TapOptions {}).asJsonObject
        sendMessage("tap", params)
    }

    override fun textContent(): String? {
        val json = sendMessage("textContent")!!.asJsonObject
        return if (json.has("value")) json["value"].asString else null
    }

    override fun type(text: String, options: TypeOptions?) {
        val params = gson().toJsonTree(options ?: TypeOptions {}).asJsonObject
        params.addProperty("text", text)
        sendMessage("type", params)
    }

    override fun uncheck(options: UncheckOptions?) {
        val params = gson().toJsonTree(options ?: UncheckOptions {}).asJsonObject
        sendMessage("uncheck", params)
    }

    override fun waitForElementState(state: ElementState?, options: WaitForElementStateOptions?) {
        if (state == null) {
            throw IllegalArgumentException("State cannot be null")
        }
        val params = gson().toJsonTree(options ?: WaitForElementStateOptions {}).asJsonObject
        params.addProperty("state", state.toString().lowercase())
        sendMessage("waitForElementState", params)
    }

    override fun waitForSelector(selector: String, options: WaitForElementStateOptions?): IElementHandle? {
        val params = gson().toJsonTree(options ?: WaitForElementStateOptions {}).asJsonObject
        params.addProperty("selector", selector)
        val json = sendMessage("waitForSelector", params)
        val element = json!!.asJsonObject["element"].asJsonObject ?: return null
        return messageProcessor.getExistingObject(element["guid"].asString)
    }

    override fun asElement(): IElementHandle {
        return this
    }
}