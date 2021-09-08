package com.playwright.remote.browser.context.cookies

import com.playwright.remote.base.BaseTest
import com.playwright.remote.engine.options.Cookie
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestBrowserContextClearCookies : BaseTest() {

    @Test
    fun `check to clear cookies`() {
        page.navigate(httpServer.emptyPage)
        browserContext.addCookies(listOf(Cookie {
            it.name = "cookie1"
            it.value = "1"
            it.url = httpServer.emptyPage
        }))
        assertEquals("cookie1=1", page.evaluate("document.cookie"))
        browserContext.clearCookies()
        assertEquals(emptyList(), browserContext.cookies())
        page.reload()
        assertEquals("", page.evaluate("document.cookie"))
    }

    @Test
    fun `check to isolate cookies when clearing`() {
        browser.newContext().use {
            browserContext.addCookies(listOf(Cookie { cookie ->
                cookie.name = "page1cookie"
                cookie.value = "page1value"
                cookie.url = httpServer.emptyPage
            }))
            it.addCookies(listOf(Cookie { cookie ->
                cookie.name = "page2cookie"
                cookie.value = "page2value"
                cookie.url = httpServer.emptyPage
            }))

            assertEquals(1, browserContext.cookies().size)
            assertEquals(1, it.cookies().size)

            browserContext.clearCookies()
            assertEquals(0, browserContext.cookies().size)
            assertEquals(1, it.cookies().size)

            it.clearCookies()
            assertEquals(0, browserContext.cookies().size)
            assertEquals(0, it.cookies().size)
        }
    }
}