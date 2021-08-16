package com.playwright.remote.browser.context

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.browser.RemoteBrowser
import com.playwright.remote.engine.options.Cookie
import com.playwright.remote.engine.parser.IParser
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.*

class TestBrowserContextAddCookies : BaseTest() {

    @Test
    fun `check correct work`() {
        page.navigate(httpServer.emptyPage)
        browserContext.addCookies(listOf(Cookie {
            it.name = "password"
            it.value = "123456"
            it.url = httpServer.emptyPage
        }))
        assertEquals("password=123456", page.evaluate("document.cookie"))
    }

    @Test
    fun `check to round trip cookie`() {
        page.navigate(httpServer.emptyPage)
        val dollar = "$"
        val jsScript = """() => {
            |   const date = new Date('1/1/2038');
            |   document.cookie = `username=John Doe;expires=${dollar}{date.toUTCString()}`;
            |   return document.cookie;
            |}
        """.trimMargin()
        val documentCookie = page.evaluate(jsScript)
        assertEquals("username=John Doe", documentCookie)

        val cookies = browserContext.cookies()
        browserContext.clearCookies()
        assertEquals(emptyList(), browserContext.cookies())
        browserContext.addCookies(listOf(Cookie {
            it.name = cookies[0].name
            it.value = cookies[0].value
            it.domain = cookies[0].domain
            it.path = cookies[0].path
            it.expires = cookies[0].expires
        }))
        assertJsonEquals(IParser.toJson(cookies), browserContext.cookies())
    }

    @Test
    fun `check send cookie header`() {
        val request = httpServer.futureRequest("/empty.html")
        browserContext.addCookies(listOf(Cookie {
            it.name = "cookie"
            it.value = "value"
            it.url = httpServer.emptyPage
        }))
        val pg = browserContext.newPage()
        pg.navigate(httpServer.emptyPage)
        val cookies = request.get().headers["cookie"]
        assertEquals(listOf("cookie=value"), cookies)
    }

    @Test
    fun `check to isolate cookies in browser contexts`() {
        val anotherContext = browser.newContext()
        browserContext.addCookies(listOf(
            Cookie {
                it.name = "isolatecookie"
                it.value = "page1value"
                it.url = httpServer.emptyPage
            }
        ))
        anotherContext.addCookies(listOf(
            Cookie {
                it.name = "isolatecookie"
                it.value = "page2value"
                it.url = httpServer.emptyPage
            }
        ))
        val cookies1 = browserContext.cookies()
        val cookies2 = anotherContext.cookies()
        assertEquals(1, cookies1.size)
        assertEquals(1, cookies2.size)

        assertEquals("isolatecookie", cookies1[0].name)
        assertEquals("page1value", cookies1[0].value)

        assertEquals("isolatecookie", cookies2[0].name)
        assertEquals("page2value", cookies2[0].value)
        anotherContext.close()
    }

    @Test
    fun `check to isolate session cookies`() {
        httpServer.setRoute("/setcookie.html") {
            it.responseHeaders.add("Set-Cookie", "session=value")
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }

        val pg = browserContext.newPage()
        pg.navigate("${httpServer.prefixWithDomain}/setcookie.html")

        val pg2 = browserContext.newPage()
        pg2.navigate(httpServer.emptyPage)
        var cookies = browserContext.cookies()
        assertEquals(1, cookies.size)
        assertEquals("value", cookies[0].value)

        val context = browser.newContext()
        val pg3 = context.newPage()
        pg3.navigate(httpServer.emptyPage)
        cookies = context.cookies()
        assertEquals(0, cookies.size)
        context.close()
    }

    @Test
    fun `check to isolate persistent cookies`() {
        httpServer.setRoute("/setcookie.html") {
            it.responseHeaders.add("Set-Cookie", "persistent=persistent-value; max-age=3600")
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }

        val pg = browserContext.newPage()
        pg.navigate("${httpServer.prefixWithDomain}/setcookie.html")

        val context1 = browserContext
        val context2 = browser.newContext()

        val pg1 = context1.newPage()
        val pg2 = context2.newPage()
        pg1.navigate(httpServer.emptyPage)
        pg2.navigate(httpServer.emptyPage)

        val cookies1 = context1.cookies()
        val cookies2 = context2.cookies()

        assertEquals(1, cookies1.size)
        assertEquals("persistent", cookies1[0].name)
        assertEquals("persistent-value", cookies1[0].value)
        assertEquals(0, cookies2.size)
        context2.close()
    }

    @Test
    fun `check isolate send cookie header`() {
        browserContext.addCookies(listOf(Cookie {
            it.name = "sendcookie"
            it.value = "value"
            it.url = httpServer.emptyPage
        }))

        val pg = browserContext.newPage()
        val request = httpServer.futureRequest("/empty.html")
        pg.navigate(httpServer.emptyPage)
        var cookies = request.get().headers["cookie"]
        assertEquals(listOf("sendcookie=value"), cookies)

        val context = browser.newContext()
        val pg2 = context.newPage()
        val request2 = httpServer.futureRequest("/empty.html")
        pg2.navigate(httpServer.emptyPage)
        cookies = request2.get().headers["cookie"]
        assertNull(cookies)
        context.clearCookies()
    }

    @Test
    fun `check isolate cookies between browsers`() {
        val browser1 = RemoteBrowser.connectWs(wsUrl)
        val context = browser1.newContext()
        context.addCookies(listOf(Cookie {
            it.name = "cookie-in-context-1"
            it.value = "value"
            it.url = httpServer.emptyPage
            it.expires = Instant.now().epochSecond + 10000.0
        }))
        browser1.close()

        val browser2 = RemoteBrowser.connectWs(wsUrl)
        val context2 = browser2.newContext()
        val cookies = context2.cookies()
        assertEquals(0, cookies.size)
        browser2.close()
    }

    @Test
    fun `check to set multiple cookies`() {
        page.navigate(httpServer.emptyPage)
        browserContext.addCookies(listOf(
            Cookie {
                it.name = "multiple-1"
                it.value = "123456"
                it.url = httpServer.emptyPage
            },
            Cookie {
                it.name = "multiple-2"
                it.value = "bar"
                it.url = httpServer.emptyPage
            }
        ))
        val jsScript = """() => {
            |   const cookies = document.cookie.split(';');
            |   return cookies.map(cookie => cookie.trim()).sort();
            |}
        """.trimMargin()
        assertEquals(listOf("multiple-1=123456", "multiple-2=bar"), page.evaluate(jsScript))
    }

    @Test
    fun `check to have expires set to 1 for session cookies`() {
        browserContext.addCookies(listOf(Cookie {
            it.name = "expires"
            it.value = "123456"
            it.url = httpServer.emptyPage
        }))
        val cookies = browserContext.cookies()
        assertEquals(-1.0, cookies[0].expires)
    }

    @Test
    fun `check to set cookie with reasonable defaults`() {
        browserContext.addCookies(listOf(Cookie {
            it.name = "defaults"
            it.value = "123456"
            it.url = httpServer.emptyPage
        }))
        val cookies = browserContext.cookies()
        val expectedValue = """[
              {
                "name": "defaults",
                "value": "123456",
                "domain": "localhost",
                "path": "/",
                "expires": -1.0,
                "httpOnly": false,
                "secure": false,
                "sameSite": "${if (isChromium()) "Lax" else "None"}"
              }
        ]"""
        assertJsonEquals(expectedValue, cookies)
    }

    @Test
    fun `check to set a cookie with a path`() {
        page.navigate("${httpServer.prefixWithDomain}/grid.html")
        browserContext.addCookies(listOf(Cookie {
            it.name = "gridcookie"
            it.value = "GRID"
            it.domain = "localhost"
            it.path = "/grid.html"
        }))
        val cookies = browserContext.cookies()
        val expectedValue = """[
              {
                "name": "gridcookie",
                "value": "GRID",
                "domain": "localhost",
                "path": "/grid.html",
                "expires": -1.0,
                "httpOnly": false,
                "secure": false,
                "sameSite": "${if (isChromium()) "Lax" else "None"}"
              }
        ]"""
        assertJsonEquals(expectedValue, cookies)
        assertEquals("gridcookie=GRID", page.evaluate("document.cookie"))
        page.navigate(httpServer.emptyPage)
        assertEquals("", page.evaluate("document.cookie"))
        page.navigate("${httpServer.prefixWithDomain}/grid.html")
        assertEquals("gridcookie=GRID", page.evaluate("document.cookie"))
    }

    @Test
    fun `check to throw error when set a cookie with blank page url`() {
        try {
            browserContext.addCookies(listOf(
                Cookie {
                    it.name = "example-cookie"
                    it.value = "best"
                    it.url = httpServer.emptyPage
                },
                Cookie {
                    it.name = "example-cookie-blank"
                    it.value = "best"
                    it.url = "about:blank"
                }
            ))
            fail("addCookies should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Blank page can not have cookie \"example-cookie-blank\""))
        }
    }

    @Test
    fun `check to throw error when set a cookie on a data url page`() {
        try {
            browserContext.addCookies(listOf(
                Cookie {
                    it.name = "example-cookie"
                    it.value = "best"
                    it.url = "data:,Hello%2C%20World!"
                }
            ))
            fail("addCookies should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Data URL page can not have cookie \"example-cookie\""))
        }
    }

    @Test
    fun `check default settings of secure cookie for https websites`() {
        page.navigate(httpServer.emptyPage)
        val url = "https://example.com"
        browserContext.addCookies(listOf(Cookie {
            it.name = "foo"
            it.value = "bar"
            it.url = url
        }))
        val cookies = browserContext.cookies(url)
        assertEquals(1, cookies.size)
        assertTrue(cookies[0].secure!!)
    }

    @Test
    fun `check to be able to set unsecure cookie for http website`() {
        page.navigate(httpServer.emptyPage)
        val url = "http://example.com"
        browserContext.addCookies(listOf(Cookie {
            it.name = "foo"
            it.value = "bar"
            it.url = url
        }))
        val cookies = browserContext.cookies(url)
        assertEquals(1, cookies.size)
        assertFalse(cookies[0].secure!!)
    }

    @Test
    fun `check to set a cookie on a different domain`() {
        page.navigate(httpServer.emptyPage)
        val url = "https://www.example.com"
        browserContext.addCookies(listOf(Cookie {
            it.name = "example-cookie"
            it.value = "best"
            it.url = url
        }))
        assertEquals("", page.evaluate("document.cookie"))
        val expectedValue = """[
              {
                "name": "example-cookie",
                "value": "best",
                "domain": "www.example.com",
                "path": "/",
                "expires": -1.0,
                "httpOnly": false,
                "secure": true,
                "sameSite": "${if (isChromium()) "Lax" else "None"}"
              }
        ]"""
        assertJsonEquals(expectedValue, browserContext.cookies(url))
    }

    @Test
    fun `check to set cookie for a frame`() {
        page.navigate(httpServer.emptyPage)
        browserContext.addCookies(listOf(Cookie {
            it.name = "frame-cookie"
            it.value = "value"
            it.url = httpServer.prefixWithDomain
        }))
        val jsScript = """src => {
            |   let fulfill;
            |   const promise = new Promise(x => fulfill = x);
            |   const iframe = document.createElement('iframe');
            |   document.body.appendChild(iframe);
            |   iframe.onload = fulfill;
            |   iframe.src = src;
            |   return promise;
            |}
        """.trimMargin()
        page.evaluate(jsScript, "${httpServer.prefixWithDomain}/grid.html")
        assertEquals("frame-cookie=value", page.frames()[1].evaluate("document.cookie"))
    }

    @Test
    fun `check to not block third party cookies`() {
        page.navigate(httpServer.emptyPage)
        val jsScript = """src => {
            |   let fulfill;
            |   const promise = new Promise(x => fulfill = x);
            |   const iframe = document.createElement('iframe');
            |   document.body.appendChild(iframe);
            |   iframe.onload = fulfill;
            |   iframe.src = src;
            |   return promise;
            |}
        """.trimMargin()
        page.evaluate(jsScript, "${httpServer.prefixWithIP}/grid.html")
        page.frames()[1].evaluate("document.cookie = 'username=John Doe'")
        page.waitForTimeout(2000.0)
        val allowsThirdParty = isFirefox()
        val cookies = browserContext.cookies("${httpServer.prefixWithIP}/grid.html")
        if (allowsThirdParty) {
            val expectedValue = """[
                  {
                    "name": "username",
                    "value": "John Doe",
                    "domain": "127.0.0.1",
                    "path": "/",
                    "expires": -1.0,
                    "httpOnly": false,
                    "secure": false,
                    "sameSite": "None"
                  }
            ]"""
            assertJsonEquals(expectedValue, cookies)
        } else {
            assertEquals(0, cookies.size)
        }
    }
}