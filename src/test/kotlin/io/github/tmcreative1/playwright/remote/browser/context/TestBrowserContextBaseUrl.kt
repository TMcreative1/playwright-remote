package io.github.tmcreative1.playwright.remote.browser.context

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.FulfillOptions
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.engine.options.NewPageOptions
import io.github.tmcreative1.playwright.remote.engine.route.request.api.IRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestBrowserContextBaseUrl : BaseTest() {

    @Test
    fun `check to construct a new url when a base url in browser new context is passed to page goto`() {
        browser.newContext(NewContextOptions { it.baseURL = httpServer.prefixWithDomain }).use {
            val pg = it.newPage()
            val response = pg.navigate("/empty.html")
            assertNotNull(response)
            assertEquals(httpServer.emptyPage, response.url())
        }
    }

    @Test
    fun `check to construct a new url when a base url in browser new page is passed to page goto`() {
        browser.newPage(NewPageOptions { it.baseURL = httpServer.prefixWithDomain }).use {
            val response = it.navigate("/empty.html")
            assertNotNull(response)
            assertEquals(httpServer.emptyPage, response.url())
        }
    }

    @Test
    fun `check to construct the url correctly when a base url without a trailing slash in browser new page is passed to page goto`() {
        browser.newPage(NewPageOptions { it.baseURL = "${httpServer.prefixWithDomain}/url-construction" }).use {
            assertEquals("${httpServer.prefixWithDomain}/mypage.html", it.navigate("mypage.html")!!.url())
            assertEquals("${httpServer.prefixWithDomain}/mypage.html", it.navigate("./mypage.html")!!.url())
            assertEquals("${httpServer.prefixWithDomain}/mypage.html", it.navigate("/mypage.html")!!.url())
        }
    }

    @Test
    fun `check to not construct a new url when valid url are passed`() {
        browser.newPage(NewPageOptions { it.baseURL = "http://microsoft.com" }).use {
            val response = it.navigate(httpServer.emptyPage)
            assertNotNull(response)
            assertEquals(httpServer.emptyPage, response.url())

            it.navigate("data:text/html,Hello world")
            assertEquals("data:text/html,Hello world", it.evaluate("window.location.href"))

            it.navigate("about:blank")
            assertEquals("about:blank", it.evaluate("window.location.href"))
        }
    }

    @Test
    fun `check to be able to match a url relative to it's given url without url matcher`() {
        browser.newPage(NewPageOptions { it.baseURL = "${httpServer.prefixWithDomain}/foobar/" }).use { pg ->
            pg.navigate("/incorrect/index.html")
            pg.waitForURL("/incorrect/index.html")
            assertEquals("${httpServer.prefixWithDomain}/incorrect/index.html", pg.url())

            pg.route("./incorrect/index.html") { route ->
                route.fulfill(FulfillOptions { opt -> opt.body = "base-url-matched-route" })
            }
            val request = arrayListOf<IRequest?>(null)
            val response = pg.waitForResponse("./incorrect/index.html") {
                request[0] = pg.waitForRequest("./incorrect/index.html") {
                    pg.navigate("./incorrect/index.html")
                }
            }
            assertNotNull(request)
            assertNotNull(response)
            assertEquals("${httpServer.prefixWithDomain}/foobar/incorrect/index.html", request[0]!!.url())
            assertEquals("${httpServer.prefixWithDomain}/foobar/incorrect/index.html", response.url())
            assertEquals("base-url-matched-route", response.text())
        }
    }
}