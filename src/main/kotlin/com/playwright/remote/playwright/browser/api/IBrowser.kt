package com.playwright.remote.playwright.browser.api

import com.playwright.remote.playwright.options.NewContextOptions
import com.playwright.remote.playwright.options.NewPageOptions
import com.playwright.remote.playwright.page.api.IPage

interface IBrowser : AutoCloseable {

    fun onDisconnected(handler: (IBrowser) -> Unit)

    fun offDisconnected(handler: (IBrowser) -> Unit)

    fun newContext(options: NewContextOptions? = null): IBrowserContext

    fun newPage(options: NewPageOptions? = null): IPage
}