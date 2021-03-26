package com.playwright.remote.engine.touchscreen.impl

import com.google.gson.JsonObject
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.page.impl.Page
import com.playwright.remote.engine.touchscreen.api.ITouchScreen

class TouchScreen(val page: IPage) : ITouchScreen {
    override fun tap(x: Double, y: Double) {
        val params = JsonObject()
        params.addProperty("x", x)
        params.addProperty("y", y)
        (page as Page).sendMessage("touchscreenTap", params)
    }
}