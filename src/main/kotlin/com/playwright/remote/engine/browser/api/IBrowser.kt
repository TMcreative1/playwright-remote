package com.playwright.remote.engine.browser.api

import com.playwright.remote.engine.options.NewContextOptions
import com.playwright.remote.engine.options.NewPageOptions
import com.playwright.remote.engine.page.api.IPage

interface IBrowser : AutoCloseable {

    fun onDisconnected(handler: (IBrowser) -> Unit)

    fun offDisconnected(handler: (IBrowser) -> Unit)

    fun newContext(options: NewContextOptions? = null): IBrowserContext

    fun newPage(options: NewPageOptions? = null): IPage
}