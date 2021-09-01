package io.github.tmcreative1.playwright.remote.engine.server.api

import io.github.tmcreative1.playwright.remote.core.enums.BrowserType
import io.github.tmcreative1.playwright.remote.core.enums.Platform

interface IServerProvider {
    fun launchServer(platform: Platform, browserType: BrowserType): String?
    fun stopServer()
}