package playwright.browser.api

import playwright.options.NewContextOptions
import playwright.options.NewPageOptions
import playwright.page.api.IPage

interface IBrowser : AutoCloseable {

    fun onDisconnected(handler: (IBrowser) -> Unit)

    fun offDisconnected(handler: (IBrowser) -> Unit)


    fun newContext(options: NewContextOptions? = null): IBrowserContext

    fun newPage(options: NewPageOptions? = null): IPage
}