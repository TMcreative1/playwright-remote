package playwright.browser.api

import playwright.page.api.IPage

interface IBrowserContext : AutoCloseable {
    fun newPage(): IPage
}