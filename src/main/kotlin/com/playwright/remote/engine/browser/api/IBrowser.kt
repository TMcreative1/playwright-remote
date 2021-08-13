package com.playwright.remote.engine.browser.api

import com.playwright.remote.core.enums.DeviceDescriptors
import com.playwright.remote.engine.options.NewContextOptions
import com.playwright.remote.engine.options.NewPageOptions
import com.playwright.remote.engine.page.api.IPage

interface IBrowser : AutoCloseable {

    fun onDisconnected(handler: (IBrowser) -> Unit)

    fun offDisconnected(handler: (IBrowser) -> Unit)

    fun newContext(): IBrowserContext = newContext(null)

    fun newContext(options: NewContextOptions?): IBrowserContext

    fun newPage(): IPage = newPage(null)

    fun newPage(options: NewPageOptions?): IPage = newPage(options, null)

    fun newPage(options: NewPageOptions?, device: DeviceDescriptors?): IPage

    fun name(): String

    fun contexts(): List<IBrowserContext>
}