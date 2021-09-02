package io.github.tmcreative1.playwright.remote.engine.touchscreen.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import io.github.tmcreative1.playwright.remote.engine.page.impl.Page
import io.github.tmcreative1.playwright.remote.engine.touchscreen.api.ITouchScreen

class TouchScreen(val page: IPage) : ITouchScreen {
    override fun tap(x: Double, y: Double) {
        val params = JsonObject()
        params.addProperty("x", x)
        params.addProperty("y", y)
        (page as Page).sendMessage("touchscreenTap", params)
    }
}