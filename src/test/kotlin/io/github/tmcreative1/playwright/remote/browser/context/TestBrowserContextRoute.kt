package com.playwright.remote.browser.context

import com.playwright.remote.base.BaseTest
import com.playwright.remote.engine.options.FulfillOptions
import com.playwright.remote.engine.route.api.IRoute
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestBrowserContextRoute : BaseTest() {

    @Test
    fun `check to intercept`() {
        browser.newContext().use {
            val intercepted = arrayListOf(false)
            val pg = it.newPage()
            it.route("**/empty.html") { route ->
                intercepted[0] = true
                val request = route.request()
                assertTrue(request.url().contains("empty.html"))
                assertNotNull(request.headers()["user-agent"])
                assertEquals("GET", request.method())
                assertNull(request.postData())
                assertTrue(request.isNavigationRequest())
                assertEquals("document", request.resourceType())
                assertEquals(pg.mainFrame(), request.frame())
                assertEquals("about:blank", request.frame().url())
                route.resume()
            }
            val response = pg.navigate(httpServer.emptyPage)
            assertNotNull(response)
            assertTrue(response.ok())
            assertTrue(intercepted[0])
        }
    }

    @Test
    fun `check to unroute`() {
        browser.newContext().use {
            val pg = it.newPage()
            val intercepted = arrayListOf<Int>()
            val handler1: (IRoute) -> Unit = { route ->
                intercepted.add(1)
                route.resume()
            }
            it.route("**/empty.html", handler1)
            it.route("**/empty.html") { route ->
                intercepted.add(2)
                route.resume()
            }
            it.route("**/empty.html") { route ->
                intercepted.add(3)
                route.resume()
            }
            val handler: (IRoute) -> Unit = { route ->
                intercepted.add(4)
                route.resume()
            }
            it.route("**/*", handler)
            pg.navigate(httpServer.emptyPage)
            assertEquals(listOf(1), intercepted)

            intercepted.clear()
            it.unRoute("**/empty.html", handler1)
            pg.navigate(httpServer.emptyPage)
            assertEquals(listOf(2), intercepted)

            intercepted.clear()
            it.unRoute("**/empty.html")
            pg.navigate(httpServer.emptyPage)
            assertEquals(listOf(4), intercepted)
        }
    }

    @Test
    fun `check yield to page route`() {
        browser.newContext().use {
            it.route("**/empty.html") { route ->
                route.fulfill(FulfillOptions { opt ->
                    opt.status = 200
                    opt.body = "context"
                })
            }
            val pg = it.newPage()
            pg.route("**/empty.html") { route ->
                route.fulfill(FulfillOptions { opt ->
                    opt.status = 200
                    opt.body = "page"
                })
            }
            val response = pg.navigate(httpServer.emptyPage)
            assertNotNull(response)
            assertTrue(response.ok())
            assertEquals("page", response.text())
        }
    }

    @Test
    fun `check to fall back to context route`() {
        browser.newContext().use {
            it.route("**/empty.html") { route ->
                route.fulfill(FulfillOptions { opt ->
                    opt.status = 200
                    opt.body = "context"
                })
            }
            val pg = it.newPage()
            pg.route("**/non-empty.html") { route ->
                route.fulfill(FulfillOptions { opt ->
                    opt.status = 200
                    opt.body = "page"
                })
            }
            val response = pg.navigate(httpServer.emptyPage)
            assertNotNull(response)
            assertTrue(response.ok())
            assertEquals("context", response.text())
        }
    }
}