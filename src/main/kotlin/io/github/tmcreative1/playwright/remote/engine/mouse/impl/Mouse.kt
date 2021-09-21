package io.github.tmcreative1.playwright.remote.engine.mouse.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.mouse.api.IMouse
import io.github.tmcreative1.playwright.remote.engine.options.DownOptions
import io.github.tmcreative1.playwright.remote.engine.options.MoveOptions
import io.github.tmcreative1.playwright.remote.engine.options.UpOptions
import io.github.tmcreative1.playwright.remote.engine.options.mouse.ClickOptions
import io.github.tmcreative1.playwright.remote.engine.options.mouse.DoubleClickOptions
import io.github.tmcreative1.playwright.remote.engine.parser.IParser.Companion.convert
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.engine.serialize.CustomGson.Companion.gson

class Mouse(val page: ChannelOwner) : IMouse {
    override fun click(x: Double, y: Double, options: ClickOptions) {
        val params = gson().toJsonTree(options).asJsonObject
        params.addProperty("x", x)
        params.addProperty("y", y)
        page.sendMessage("mouseClick", params)
    }

    override fun doubleClick(x: Double, y: Double, options: DoubleClickOptions?) {
        val clickOptions = if (options == null) ClickOptions {} else convert(options, ClickOptions::class.java)
        clickOptions.clickCount = 2
        click(x, y, clickOptions)
    }

    override fun down(options: DownOptions) {
        val params = gson().toJsonTree(options).asJsonObject
        page.sendMessage("mouseDown", params)
    }

    override fun move(x: Double, y: Double, options: MoveOptions) {
        val params = gson().toJsonTree(options).asJsonObject
        params.addProperty("x", x)
        params.addProperty("y", y)
        page.sendMessage("mouseMove", params)
    }

    override fun up(options: UpOptions) {
        val params = gson().toJsonTree(options).asJsonObject
        page.sendMessage("mouseUp", params)
    }

    override fun wheel(deltaX: Double, deltaY: Double) {
        val params = JsonObject()
        params.addProperty("deltaX", deltaX)
        params.addProperty("deltaY", deltaY)
        page.sendMessage("mouseWheel", params)
    }
}