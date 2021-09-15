package io.github.tmcreative1.playwright.remote.engine.browser.api

import io.github.tmcreative1.playwright.remote.core.enums.DeviceDescriptors
import io.github.tmcreative1.playwright.remote.engine.browser.selector.api.ISharedSelectors
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.engine.options.NewPageOptions
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage

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

    /**
     * Returns the browser version.
     */
    fun version(): String

    /**
     * Selectors can be used to install custom selector engines. See <a
     * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more information.
     */
    fun selectors(): ISharedSelectors
}