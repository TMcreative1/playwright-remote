package com.playwright.remote.engine.mouse.impl

import com.google.gson.Gson
import com.playwright.remote.engine.mouse.api.IMouse
import com.playwright.remote.engine.options.*
import com.playwright.remote.engine.parser.IParser.Companion.convert
import com.playwright.remote.engine.processor.ChannelOwner

class Mouse(val page: ChannelOwner) : IMouse {
    @JvmOverloads
    override fun click(x: Double, y: Double, options: ClickOptions) {
        val params = Gson().toJsonTree(options).asJsonObject
        params.addProperty("x", x)
        params.addProperty("y", y)
        page.sendMessage("mouseClick", params)
    }

    @JvmOverloads
    override fun doubleClick(x: Double, y: Double, options: DoubleClickOptions?) {
        val clickOptions = if (options == null) ClickOptions {} else convert(options, ClickOptions::class.java)
        clickOptions.clickCount = 2
        click(x, y, clickOptions)
    }

    @JvmOverloads
    override fun down(options: DownOptions) {
        val params = Gson().toJsonTree(options).asJsonObject
        page.sendMessage("mouseDown", params)
    }

    @JvmOverloads
    override fun move(x: Double, y: Double, options: MoveOptions) {
        val params = Gson().toJsonTree(options).asJsonObject
        params.addProperty("x", x)
        params.addProperty("y", y)
        page.sendMessage("mouseMove", params)
    }

    @JvmOverloads
    override fun up(options: UpOptions) {
        val params = Gson().toJsonTree(options).asJsonObject
        page.sendMessage("mouseUp", params)
    }
}