package io.github.tmcreative1.playwright.remote.engine.frame.locator.impl

import io.github.tmcreative1.playwright.remote.core.enums.WaitForSelectorState.ATTACHED
import io.github.tmcreative1.playwright.remote.domain.BoundingBox
import io.github.tmcreative1.playwright.remote.domain.file.FilePayload
import io.github.tmcreative1.playwright.remote.engine.frame.api.IFrame
import io.github.tmcreative1.playwright.remote.engine.frame.locator.api.ILocator
import io.github.tmcreative1.playwright.remote.engine.handle.element.api.IElementHandle
import io.github.tmcreative1.playwright.remote.engine.handle.js.api.IJSHandle
import io.github.tmcreative1.playwright.remote.engine.options.*
import io.github.tmcreative1.playwright.remote.engine.options.element.*
import io.github.tmcreative1.playwright.remote.engine.options.element.PressOptions
import io.github.tmcreative1.playwright.remote.engine.options.element.ScreenshotOptions
import io.github.tmcreative1.playwright.remote.engine.options.element.TypeOptions
import io.github.tmcreative1.playwright.remote.engine.parser.IParser
import java.nio.file.Path

class Locator(private val frame: IFrame, private val selector: String) : ILocator {

    @Suppress("UNCHECKED_CAST")
    override fun allInnerTexts(): List<String> {
        return frame.evalOnSelectorAll(selector, "ee => ee.map(e => e.innerText)") as List<String>
    }

    @Suppress("UNCHECKED_CAST")
    override fun allTextContents(): List<String> {
        return frame.evalOnSelectorAll(selector, "ee => ee.map(e => e.textContent || '')") as List<String>
    }

    override fun boundingBox(options: BoundingBoxOptions?): BoundingBox {
        return withElement(options) { b, _ -> b?.boundingBox()!! }
    }

    override fun check(options: CheckOptions?) {
        val opt = options ?: CheckOptions {}
        opt.strict = true
        frame.check(selector, opt)
    }

    override fun click(options: ClickOptions?) {
        val opt = options ?: ClickOptions {}
        opt.strict = true
        frame.click(selector, opt)
    }

    override fun count(): Int {
        return (evaluateAll("ee => ee.length") as Number).toInt()
    }

    override fun doubleClick(options: DoubleClickOptions?) {
        val opt = options ?: DoubleClickOptions {}
        opt.strict = true
        frame.doubleClick(selector, opt)
    }

    override fun dispatchEvent(type: String, eventInit: Any?, options: DispatchEventOptions?) {
        val opt = options ?: DispatchEventOptions {}
        opt.strict = true
        frame.dispatchEvent(selector, type, eventInit, opt)
    }

    override fun elementHandle(options: ElementHandleOptions?): IElementHandle? {
        val opt = IParser.convert(options ?: ElementHandleOptions {}, WaitForSelectorOptions::class.java)
        opt.strict = true
        opt.state = ATTACHED
        return frame.waitForSelector(selector, opt)
    }

    override fun elementHandles(): List<IElementHandle>? {
        return frame.querySelectorAll(selector)
    }


    override fun evaluate(expression: String, arg: Any?, options: EvaluateOptions?): Any? {
        return withElement(options) { h, _ -> h?.evaluate(expression, arg) }
    }

    override fun evaluateAll(expression: String, arg: Any?): Any {
        return frame.evalOnSelectorAll(selector, expression, arg)
    }

    override fun evaluateHandle(expression: String, arg: Any?, options: EvaluateHandleOptions?): IJSHandle? {
        return withElement(options) { h, _ -> h?.evaluateHandle(expression, arg) }
    }

    override fun fill(value: String, options: FillOptions?) {
        val opt = options ?: FillOptions {}
        opt.strict = true
        frame.fill(selector, value, opt)
    }

    override fun first(): ILocator {
        return Locator(frame, "$selector >> nth=0")
    }

    override fun focus(options: FocusOptions?) {
        val opt = options ?: FocusOptions {}
        opt.strict = true
        frame.focus(selector, opt)
    }

    override fun getAttribute(name: String, options: GetAttributeOptions?): String? {
        val opt = options ?: GetAttributeOptions {}
        opt.strict = true
        return frame.getAttribute(selector, name, opt)
    }

    override fun hover(options: HoverOptions?) {
        val opt = options ?: HoverOptions {}
        opt.strict = true
        frame.hover(selector, opt)
    }

    override fun innerHTML(options: InnerHTMLOptions?): String {
        val opt = options ?: InnerHTMLOptions {}
        opt.strict = true
        return frame.innerHTML(selector, opt)
    }

    override fun innerText(options: InnerTextOptions?): String {
        val opt = options ?: InnerTextOptions {}
        opt.strict = true
        return frame.innerText(selector, opt)
    }

    override fun inputValue(options: InputValueOptions?): String {
        val opt = options ?: InputValueOptions {}
        opt.strict = true
        return frame.inputValue(selector, opt)
    }

    override fun isChecked(options: IsCheckedOptions?): Boolean {
        val opt = options ?: IsCheckedOptions {}
        opt.strict = true
        return frame.isChecked(selector, opt)
    }

    override fun isDisabled(options: IsDisabledOptions?): Boolean {
        val opt = options ?: IsDisabledOptions {}
        opt.strict = true
        return frame.isDisabled(selector, opt)
    }

    override fun isEditable(options: IsEditableOptions?): Boolean {
        val opt = options ?: IsEditableOptions {}
        opt.strict = true
        return frame.isEditable(selector, opt)
    }

    override fun isEnabled(options: IsEnabledOptions?): Boolean {
        val opt = options ?: IsEnabledOptions {}
        opt.strict = true
        return frame.isEnabled(selector, opt)
    }

    override fun isHidden(options: IsHiddenOptions?): Boolean {
        val opt = options ?: IsHiddenOptions {}
        opt.strict = true
        return frame.isHidden(selector, opt)
    }

    override fun isVisible(options: IsVisibleOptions?): Boolean {
        val opt = options ?: IsVisibleOptions {}
        opt.strict = true
        return frame.isVisible(selector, opt)
    }

    override fun last(): ILocator {
        return Locator(frame, "$selector >> nth=-1")
    }

    override fun locator(selector: String): ILocator {
        return Locator(frame, "${this.selector} >> $selector")
    }

    override fun nth(index: Int): ILocator {
        return Locator(frame, "$selector >> nth=$index")
    }

    override fun press(key: String, options: PressOptions?) {
        val opt = options ?: PressOptions {}
        opt.strict = true
        frame.press(selector, key, opt)
    }

    override fun screenshot(options: ScreenshotOptions?): ByteArray? {
        return withElement(options) { h, o -> h?.screenshot(o) }
    }

    override fun scrollIntoViewIfNeeded(options: ScrollIntoViewIfNeededOptions?) {
        withElement(options) { h, o ->
            h?.scrollIntoViewIfNeeded(o)
            null
        }
    }

    override fun selectOption(value: String, options: SelectOptionOptions?): List<String> {
        val opt = options ?: SelectOptionOptions {}
        opt.strict = true
        return frame.selectOption(selector, value, opt)
    }

    override fun selectOption(value: IElementHandle, options: SelectOptionOptions?): List<String> {
        val opt = options ?: SelectOptionOptions {}
        opt.strict = true
        return frame.selectOption(selector, value, opt)
    }

    override fun selectOption(values: Array<String>, options: SelectOptionOptions?): List<String> {
        val opt = options ?: SelectOptionOptions {}
        opt.strict = true
        return frame.selectOption(selector, values, opt)
    }

    override fun selectOption(value: SelectOption, options: SelectOptionOptions?): List<String> {
        val opt = options ?: SelectOptionOptions {}
        opt.strict = true
        return frame.selectOption(selector, value, opt)
    }

    override fun selectOption(values: Array<IElementHandle>, options: SelectOptionOptions?): List<String> {
        val opt = options ?: SelectOptionOptions {}
        opt.strict = true
        return frame.selectOption(selector, values, opt)
    }

    override fun selectOption(values: Array<SelectOption>, options: SelectOptionOptions?): List<String> {
        val opt = options ?: SelectOptionOptions {}
        opt.strict = true
        return frame.selectOption(selector, values, opt)
    }

    override fun selectText(options: SelectTextOptions?) {
        withElement(options) { h, o ->
            h?.selectText(o)
            null
        }
    }

    override fun setInputFiles(files: Path, options: SetInputFilesOptions?) {
        val opt = options ?: SetInputFilesOptions {}
        opt.strict = true
        frame.setInputFiles(selector, files, opt)
    }

    override fun setInputFiles(files: Array<Path>, options: SetInputFilesOptions?) {
        val opt = options ?: SetInputFilesOptions {}
        opt.strict = true
        frame.setInputFiles(selector, files, opt)
    }

    override fun setInputFiles(file: FilePayload, options: SetInputFilesOptions?) {
        val opt = options ?: SetInputFilesOptions {}
        opt.strict = true
        frame.setInputFiles(selector, file, opt)
    }

    override fun setInputFiles(files: Array<FilePayload>, options: SetInputFilesOptions?) {
        val opt = options ?: SetInputFilesOptions {}
        opt.strict = true
        frame.setInputFiles(selector, files, opt)
    }

    override fun tap(options: TapOptions?) {
        val opt = options ?: TapOptions {}
        opt.strict = true
        frame.tap(selector, opt)
    }

    override fun textContent(options: TextContentOptions?): String {
        val opt = options ?: TextContentOptions {}
        opt.strict = true
        return frame.textContent(selector, opt)
    }

    override fun type(text: String, options: TypeOptions?) {
        val opt = options ?: TypeOptions {}
        opt.strict = true
        frame.type(selector, text, opt)
    }

    override fun uncheck(options: UncheckOptions?) {
        val opt = options ?: UncheckOptions {}
        opt.strict = true
        frame.uncheck(selector, opt)
    }

    override fun toString(): String {
        return "Locator@$selector"
    }

    private fun <R, O> withElement(options: O, callback: (IElementHandle?, O) -> R): R {
        val handle = elementHandle(IParser.convert(options, ElementHandleOptions::class.java))
        return try {
            callback(handle, options)
        } finally {
            handle?.dispose()
        }
    }
}