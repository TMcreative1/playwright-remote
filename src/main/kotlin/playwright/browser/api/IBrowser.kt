package playwright.browser.api

import playwright.options.NewContextOptions
import playwright.options.NewPageOptions
import playwright.page.api.IPage
import java.util.function.Consumer

interface IBrowser : AutoCloseable {

    fun onDisconnected(handler: (IBrowser) -> Unit)

    fun newContext() : IBrowserContext = newContext(null)

    fun newContext(options: NewContextOptions?): IBrowserContext

    fun newPage() : IPage = newPage(null)

    fun newPage(options: NewPageOptions?) : IPage
}