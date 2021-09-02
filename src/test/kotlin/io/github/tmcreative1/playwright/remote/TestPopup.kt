package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.enums.DeviceDescriptors.GALAXY_S8
import io.github.tmcreative1.playwright.remote.core.enums.LoadState.DOMCONTENTLOADED
import io.github.tmcreative1.playwright.remote.engine.options.HttpCredentials
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.engine.options.ViewportSize
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestPopup : BaseTest() {

    @Test
    fun `check to inherit user agent from browser context`() {
        browser.newContext(NewContextOptions { it.userAgent = "myAgent" }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            pg.setContent("<a target=_blank rel=noopener href='/popup/popup.html'>link</a>")
            val futureRequest = httpServer.futureRequest("/popup/popup.html")
            val popup = it.waitForPage { pg.click("a") }
            assertNotNull(popup)
            popup.waitForLoadState(DOMCONTENTLOADED)
            val userAgent = popup.evaluate("() => window['initialUserAgent']")
            val request = futureRequest.get()
            assertEquals("myAgent", userAgent)
            assertEquals(listOf("myAgent"), request.headers["user-agent"])
        }
    }

    @Test
    fun `check correct work with routes from browser context`() {
        browser.newContext().use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            pg.setContent("<a target=_blank rel=noopener href='empty.html'>link</a>")
            val intercepted = arrayOf(false)
            it.route("**/empty.html") { route ->
                route.resume()
                intercepted[0] = true
            }
            it.waitForPage { pg.click("a") }

            assertTrue(intercepted[0])
        }
    }

    @Test
    fun `check to inherit offline from browser context`() {
        browser.newContext().use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            it.setOffline(true)
            val jsScript = """url => {
            |   const win = window.open(url);
            |   return win.navigator.onLine;
            |}
        """.trimMargin()
            val isOnline = pg.evaluate(jsScript, "${httpServer.prefixWithDomain}/dummy.html") as Boolean
            assertFalse(isOnline)
        }
    }

    @Test
    fun `check to inherit http credentials from browser context`() {
        httpServer.setAuth("/title.html", "user", "pass")
        browser.newContext(NewContextOptions {
            it.httpCredentials = HttpCredentials { cred ->
                cred.username = "user"
                cred.password = "pass"
            }
        }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            val popup = pg.waitForPopup {
                pg.evaluate("url => window['_popup'] = window.open(url)", "${httpServer.prefixWithDomain}/title.html")
            }
            assertNotNull(popup)
            popup.waitForLoadState(DOMCONTENTLOADED)
            assertEquals("Title Page", popup.title())
        }
    }

    @Test
    fun `check to inherit touch support from browser context`() {
        browser.newContext(NewContextOptions {
            it.viewportSize = ViewportSize { view ->
                view.width = 400
                view.height = 500
            }
            it.hasTouch = true
        }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            val jsScript = """() => {
            |   const win = window.open('');
            |   return 'ontouchstart' in win;
            |}
        """.trimMargin()
            val hasTouch = pg.evaluate(jsScript)
            assertEquals(true, hasTouch)
        }
    }

    @Test
    fun `check to inherit viewport size from browser context`() {
        browser.newContext(NewContextOptions {
            it.viewportSize = ViewportSize { view ->
                view.width = 400
                view.height = 500
            }
        }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            val jsScript = """() => {
            |   const win = window.open('about:blank');
            |   return { width: win.innerWidth, height: win.innerHeight };
            |}
        """.trimMargin()
            val size = pg.evaluate(jsScript)
            assertEquals(mapOf("width" to 400, "height" to 500), size)
        }
    }

    @Test
    fun `check to use viewport size from window features`() {
        browser.newContext(NewContextOptions { it.viewportSize = GALAXY_S8.viewport }).use {
            val pg = it.newPage()
            page.navigate(httpServer.emptyPage)
            val size = arrayListOf<Any?>(null)
            val popup = pg.waitForPopup {
                val jsScript = """() => {
                |  const win = window.open(window.location.href, 'Title', 'toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=600,height=300,top=0,left=0');
                |  return { width: win.innerWidth, height: win.innerHeight };
                |}
            """.trimMargin()
                size[0] = pg.evaluate(jsScript)
            }
            assertNotNull(popup)
            popup.setViewportSize(500, 400)
            popup.waitForLoadState()
            val resized = popup.evaluate("() => ({ width: window.innerWidth, height: window.innerHeight })")
            assertEquals(mapOf("width" to 600, "height" to 300), size[0])
            assertEquals(mapOf("width" to 500, "height" to 400), resized)
        }
    }

    @Test
    fun `check correct work with routes from browser context with window open`() {
        browser.newContext().use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            val intercepted = arrayOf(false)
            it.route("**/empty.html") { route ->
                route.resume()
                intercepted[0] = true
            }
            pg.waitForPopup {
                pg.evaluate("url => window['__popup'] = window.open(url)", httpServer.emptyPage)
            }
            assertTrue(intercepted[0])
        }
    }

    @Test
    fun `check to add init script in browser context should apply to an in-process popup`() {
        browser.newContext().use {
            it.addInitScript("window['injected'] = 123")
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            val jsScript = """() => {
            |   const win = window.open('about:blank');
            |   return win['injected'];
            |}
        """.trimMargin()
            val injected = pg.evaluate(jsScript)
            assertEquals(123, injected)
        }
    }

    @Test
    fun `check to add init script in browser context should apply to a cross-process popup`() {
        browser.newContext().use {
            it.addInitScript("(() => window['injected'] = 123)()")
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            val popup = pg.waitForPopup {
                pg.evaluate("url => window.open(url)", "${httpServer.prefixWithIP}/title.html")
            }
            assertNotNull(popup)
            assertEquals(123, popup.evaluate("injected"))

            popup.reload()

            assertEquals(123, popup.evaluate("injected"))
        }
    }

    @Test
    fun `check to expose function from browser context`() {
        browser.newContext().use {
            val messages = arrayListOf<String>()
            it.exposeFunction("add") { args ->
                messages.add("binding")
                args[0] as Int + args[1] as Int
            }
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
            val jsScript = """async () => {
            |   const win = window.open('about:blank');
            |   return win['add'](9, 4);
            |}
        """.trimMargin()
            val added = pg.evaluate(jsScript)
            assertEquals(13, added)
        }
    }
}