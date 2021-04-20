package com.playwright.remote.engine.server.api

import com.playwright.remote.core.enums.BrowserType
import com.playwright.remote.core.enums.Platform

interface IServerProvider {
    fun launchServer(platform: Platform, browserType: BrowserType): String?
    fun stopServer(): Int
}