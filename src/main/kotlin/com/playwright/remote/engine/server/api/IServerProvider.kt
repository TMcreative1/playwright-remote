package com.playwright.remote.engine.server.api

import com.playwright.remote.core.enums.BrowserType
import com.playwright.remote.core.enums.Platform

interface IServerProvider {
    fun launchServer(platform: Platform, browserType: BrowserType = BrowserType.CHROMIUM): String?
    fun stopServer() : Int
}