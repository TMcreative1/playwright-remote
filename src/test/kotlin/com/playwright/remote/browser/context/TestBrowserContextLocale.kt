package com.playwright.remote.browser.context

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.enums.LoadState
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.options.NewContextOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestBrowserContextLocale : BaseTest() {

    @Test
    fun `check to affect accept language header`() {
        browser.newContext(NewContextOptions { it.locale = "fr-CH" }).use {
            val pg = it.newPage()
            val request = httpServer.futureRequest("/empty.html")
            pg.navigate(httpServer.emptyPage)
            assertEquals("fr-CH", request.get().headers["accept-language"]!![0].substring(0, 5))
        }
    }

    @Test
    fun `check to affect navigator language`() {
        browser.newContext(NewContextOptions { it.locale = "fr-CH" }).use {
            val pg = it.newPage()
            assertEquals("fr-CH", pg.evaluate("() => navigator.language"))
        }
    }

    @Test
    fun `check correct number format`() {
        browser.newContext(NewContextOptions { it.locale = "en-US" }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            assertEquals("1,000,000.5", pg.evaluate("() => (1000000.50).toLocaleString()"))
        }
        browser.newContext(NewContextOptions { it.locale = "fr-CH" }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            assertEquals("1 000 000,5", pg.evaluate("() => (1000000.50).toLocaleString().replace(/\\s/g, ' ')"))
        }
    }

    @Test
    fun `check correct date format`() {
        browser.newContext(NewContextOptions {
            it.locale = "en-US"
            it.timezoneId = "America/Los_Angeles"
        }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            val formatted = "Sat Nov 19 2016 10:12:34 GMT-0800 (Pacific Standard Time)"
            assertEquals(formatted, pg.evaluate("new Date(1479579154987).toString()"))
        }
        browser.newContext(NewContextOptions {
            it.locale = "de-DE"
            it.timezoneId = "Europe/Berlin"
        }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            val formatted = "Sat Nov 19 2016 19:12:34 GMT+0100 (Mitteleuropäische Normalzeit)"
            assertEquals(formatted, pg.evaluate("new Date(1479579154987).toString()"))
        }
    }

    @Test
    fun `check correct number format in popups`() {
        browser.newContext(NewContextOptions { it.locale = "fr-CH" }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            val popup = pg.waitForPopup {
                pg.evaluate(
                    "url => window.open(url)",
                    "${httpServer.prefixWithDomain}/formatted-number.html"
                )
            }
            assertNotNull(popup)
            popup.waitForLoadState(LoadState.DOMCONTENTLOADED)
            val result = popup.evaluate("window['result']")
            assertEquals("1 000 000,5", result)
        }
    }

    @Test
    fun `check to affect navigator language in popups`() {
        browser.newContext(NewContextOptions { it.locale = "fr-CH" }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            val popup = pg.waitForPopup {
                pg.evaluate(
                    "url => window.open(url)",
                    "${httpServer.prefixWithDomain}/formatted-number.html"
                )
            }
            assertNotNull(popup)
            popup.waitForLoadState(LoadState.DOMCONTENTLOADED)
            val result = popup.evaluate("window.initialNavigatorLanguage")
            assertEquals("fr-CH", result)
        }
    }

    @Test
    fun `check correct work of multiple pages sharing same process`() {
        browser.newContext(NewContextOptions { it.locale = "ru-Ru" }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            pg.waitForPopup { pg.evaluate("url => window.open(url)", httpServer.emptyPage) }
            pg.waitForPopup { pg.evaluate("url => window.open(url)", httpServer.emptyPage) }
        }
    }

    @Test
    fun `check to be isolated between contexts`() {
        browser.newContext(NewContextOptions { it.locale = "en-US" }).use { ctx1 ->
            for (index in 0..7) {
                ctx1.newPage()
            }
            browser.newContext(NewContextOptions { it.locale = "ru-RU" }).use { ctx2 ->
                val pg2 = ctx2.newPage()

                val localNumber = "(1000000.50).toLocaleString()"
                for (page in ctx1.pages()) {
                    assertEquals("1,000,000.5", page.evaluate(localNumber))
                }
                assertEquals("1 000 000,5", pg2.evaluate(localNumber))
            }
        }
    }

    @Test
    fun `check to not change default locale in another context`() {
        val contextLocale: (IBrowserContext) -> String = {
            val pg = it.newPage()
            pg.evaluate("(new Intl.NumberFormat()).resolvedOptions().locale") as String
        }
        val defaultLocale: String
        browser.newContext().use { defaultLocale = contextLocale(it) }
        val localeOverride = if ("ru-RU" == defaultLocale) "de-DE" else "ru_RU"

        browser.newContext(NewContextOptions { it.locale = localeOverride }).use {
            assertEquals(localeOverride, contextLocale(it))
        }

        browser.newContext().use {
            assertEquals(defaultLocale, contextLocale(it))
        }
    }
}