package com.playwright.remote.browser.context.cookies

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.enums.SameSiteAttribute
import com.playwright.remote.engine.options.Cookie
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestBrowserContextCookies : BaseTest() {

    @Test
    fun `check to get a cookie`() {
        page.navigate(httpServer.emptyPage)
        val jsScript = """() => {
            |   document.cookie = 'username=John Doe';
            |   return document.cookie;
            |}
        """.trimMargin()
        val documentCookie = page.evaluate(jsScript)
        assertEquals("username=John Doe", documentCookie)
        val cookies = browserContext.cookies()
        val expectedValue = """[
              {
                "name": "username",
                "value": "John Doe",
                "domain": "localhost",
                "path": "/",
                "expires": -1.0,
                "httpOnly": false,
                "secure": false,
                "sameSite": "None"
              }
        ]"""
        assertJsonEquals(expectedValue, cookies)
    }

    @Test
    fun `check ti get a non session cookie`() {
        page.navigate(httpServer.emptyPage)
        val dollar = "$"
        val jsScript = """() => {
            |   const date= new Date('1/1/2038');
            |   document.cookie = `username=John Doe;expires=${dollar}{date.toUTCString()}`;
            |   return document.cookie;
            |}
        """.trimMargin()
        val documentCookie = page.evaluate(jsScript)
        assertEquals("username=John Doe", documentCookie)

        val timestamp = page.evaluate("+(new Date('1/1/2038'))/1000") as Int
        val cookie = browserContext.cookies()[0]
        assertEquals("username", cookie.name)
        assertEquals("John Doe", cookie.value)
        assertEquals("localhost", cookie.domain)
        assertEquals("/", cookie.path)
        assertEquals(timestamp.toDouble(), cookie.expires)
        assertEquals(false, cookie.httpOnly)
        assertEquals(false, cookie.secure)
        assertEquals(SameSiteAttribute.NONE, cookie.sameSite)
    }

    @Test
    fun `check to report http only cookie`() {
        httpServer.setRoute("/empty.html") {
            it.responseHeaders.add("Set-Cookie", "name=value;HttpOnly; Path=/")
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }
        page.navigate(httpServer.emptyPage)
        val cookies = browserContext.cookies()
        assertEquals(1, cookies.size)
        assertTrue(cookies[0].httpOnly!!)
    }

    @Test
    fun `check to report strict same site cookie`() {
        Assumptions.assumeFalse(isWebkit() && isWindows())
        httpServer.setRoute("/empty.html") {
            it.responseHeaders.add("Set-Cookie", "name=value;SameSite=Strict")
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }
        page.navigate(httpServer.emptyPage)
        val cookies = browserContext.cookies()
        assertEquals(1, cookies.size)
        assertEquals(SameSiteAttribute.STRICT, cookies[0].sameSite)
    }

    @Test
    fun `check to report lax same site cookie`() {
        Assumptions.assumeFalse(isWebkit() && isWindows())
        httpServer.setRoute("/empty.html") {
            it.responseHeaders.add("Set-Cookie", "name=value;SameSite=Lax")
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }
        page.navigate(httpServer.emptyPage)
        val cookies = browserContext.cookies()
        assertEquals(1, cookies.size)
        assertEquals(SameSiteAttribute.LAX, cookies[0].sameSite)
    }

    @Test
    fun `check to get multiple cookies`() {
        page.navigate(httpServer.emptyPage)
        val jsScript = """() => {
            |   document.cookie = 'username=John Doe';
            |   document.cookie = 'password=1234';
            |   return document.cookie.split('; ').sort().join('; ');
            |}
        """.trimMargin()
        val documentCookie = page.evaluate(jsScript)
        val cookies = browserContext.cookies()
        assertEquals("password=1234; username=John Doe", documentCookie)
        val expectedValue = """[
              {
                "name": "password",
                "value": "1234",
                "domain": "localhost",
                "path": "/",
                "expires": -1.0,
                "httpOnly": false,
                "secure": false,
                "sameSite": "None"
              },
              {
                "name": "username",
                "value": "John Doe",
                "domain": "localhost",
                "path": "/",
                "expires": -1.0,
                "httpOnly": false,
                "secure": false,
                "sameSite": "None"
              }
        ]"""
        assertJsonEquals(expectedValue, cookies.sortedBy { it.name })
    }

    @Test
    fun `check to get cookies from multiple urls`() {
        browserContext.addCookies(listOf(
            Cookie {
                it.name = "doggo"
                it.value = "woofs"
                it.url = "https://foo.com"
            },
            Cookie {
                it.name = "catto"
                it.value = "purrs"
                it.url = "https://bar.com"
            },
            Cookie {
                it.name = "birdo"
                it.value = "tweets"
                it.url = "https://baz.com"
            }
        ))
        val cookies = browserContext.cookies(listOf("https://foo.com", "https://baz.com"))
        val expectedValue = """[
              {
                "name": "birdo",
                "value": "tweets",
                "domain": "baz.com",
                "path": "/",
                "expires": -1.0,
                "httpOnly": false,
                "secure": true,
                "sameSite": "None"
              },
              {
                "name": "doggo",
                "value": "woofs",
                "domain": "foo.com",
                "path": "/",
                "expires": -1.0,
                "httpOnly": false,
                "secure": true,
                "sameSite": "None"
              }
        ]"""
        assertJsonEquals(expectedValue, cookies.sortedBy { it.name })
    }

    @Test
    fun `check to accept same site attribute`() {
        Assumptions.assumeFalse(isWebkit() && isWindows())
        browserContext.addCookies(listOf(
            Cookie {
                it.name = "one"
                it.value = "uno"
                it.url = httpServer.emptyPage
                it.sameSite = SameSiteAttribute.LAX
            },
            Cookie {
                it.name = "two"
                it.value = "dos"
                it.url = httpServer.emptyPage
                it.sameSite = SameSiteAttribute.STRICT
            },
            Cookie {
                it.name = "three"
                it.value = "tres"
                it.url = httpServer.emptyPage
                it.sameSite = SameSiteAttribute.NONE
            }
        ))
        page.navigate(httpServer.emptyPage)
        val documentCookie = page.evaluate("document.cookie.split('; ').sort().join('; ')")
        assertEquals("one=uno; three=tres; two=dos", documentCookie)
        val list = browserContext.cookies().map { it.sameSite }.sortedBy { it!!.ordinal }
        assertEquals(listOf(SameSiteAttribute.STRICT, SameSiteAttribute.LAX, SameSiteAttribute.NONE), list)
    }
}