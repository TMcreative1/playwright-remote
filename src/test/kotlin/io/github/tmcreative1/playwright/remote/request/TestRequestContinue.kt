package io.github.tmcreative1.playwright.remote.request

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.ResumeOptions
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestRequestContinue : BaseTest() {

    @Test
    fun `check correct work`() {
        val expectedUrl = arrayListOf("")
        page.route("**/*") {
            expectedUrl[0] = it.request().url()
            it.resume()
        }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertEquals(200, response.status())
        assertEquals(expectedUrl[0], httpServer.emptyPage)
    }

    @Test
    fun `check to amend http headers`() {
        page.route("**/*") {
            val headers = it.request().headers().toMutableMap()
            headers["FOO"] = "bar"
            it.resume(ResumeOptions { it.headers = headers })
        }
        page.navigate(httpServer.emptyPage)
        val request = httpServer.futureRequest("/sleep.zzz")
        page.evaluate("() => fetch('/sleep.zzz')")
        assertEquals(listOf("bar"), request.get().headers["foo"])
    }

    @Test
    fun `check to amend method`() {
        val sRequest = httpServer.futureRequest("/sleep.zzz")
        page.navigate(httpServer.emptyPage)
        page.route("**/*") {
            it.resume(ResumeOptions { opt -> opt.method = "POST" })
        }
        val request = httpServer.futureRequest("/sleep.zzz")
        page.evaluate("() => fetch('/sleep.zzz')")
        assertEquals("POST", request.get().method)
        assertEquals("POST", sRequest.get().method)
    }

    @Test
    fun `check to override request url`() {
        val request = httpServer.futureRequest("/global-var.html")
        page.route("**/foo") {
            it.resume(ResumeOptions { opt -> opt.url = "${httpServer.prefixWithDomain}/global-var.html" })
        }
        val response = page.navigate("${httpServer.prefixWithDomain}/foo")
        assertNotNull(response)
        assertEquals("${httpServer.prefixWithDomain}/foo", response.url())
        assertEquals(321, page.evaluate("globalVar"))
        assertEquals("GET", request.get().method)
    }

    @Test
    fun `check to not allow changing protocol when overriding url`() {
        val error = arrayListOf<PlaywrightException?>(null)
        page.route("**/*") {
            try {
                it.resume(ResumeOptions { opt -> opt.url = "file:///tmp/foo" })
            } catch (e: PlaywrightException) {
                error[0] = e
                it.resume()
            }
        }
        page.navigate(httpServer.emptyPage)
        assertNotNull(error[0])
        assertTrue(
            error[0]!!.message!!.contains("New URL must have same protocol as overridden URL"),
            error[0]!!.message
        )
    }

    @Test
    fun `check to override method along with url`() {
        val request = httpServer.futureRequest("/empty.html")
        page.route("**/foo") {
            it.resume(ResumeOptions { opt ->
                opt.url = httpServer.emptyPage
                opt.method = "POST"
            })
        }
        page.navigate("${httpServer.prefixWithDomain}/foo")
        assertEquals("POST", request.get().method)
    }

    @Test
    fun `check to amend method on main request`() {
        val request = httpServer.futureRequest("/empty.html")
        page.route("**/*") {
            it.resume(ResumeOptions { opt -> opt.method = "POST" })
        }
        page.navigate(httpServer.emptyPage)
        assertEquals("POST", request.get().method)
    }

    @Test
    fun `check to amend post data`() {
        page.navigate(httpServer.emptyPage)
        page.route("**/*") {
            it.resume(ResumeOptions { opt -> opt.postData = "doggo" })
        }
        val request = httpServer.futureRequest("/sleep.zzz")
        page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })")
        assertEquals("doggo", String(request.get().postBody, StandardCharsets.UTF_8))
    }

    @Test
    fun `check to amend utf8 post data`() {
        page.navigate(httpServer.emptyPage)
        page.route("**/*") {
            it.resume(ResumeOptions { opt -> opt.postData = "пушкин" })
        }
        val request = httpServer.futureRequest("/sleep.zzz")
        page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })")
        assertEquals("POST", request.get().method)
        assertEquals("пушкин", String(request.get().postBody, StandardCharsets.UTF_8))
    }

    @Test
    fun `check to amend longer post data`() {
        page.navigate(httpServer.emptyPage)
        page.route("**/*") {
            it.resume(ResumeOptions { opt -> opt.postData = "doggo-is-longer-than-birdy" })
        }
        val request = httpServer.futureRequest("/sleep.zzz")
        page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })")
        assertEquals("POST", request.get().method)
        assertEquals("doggo-is-longer-than-birdy", String(request.get().postBody, StandardCharsets.UTF_8))
    }

    @Test
    fun `check to amend binary post data`() {
        page.navigate(httpServer.emptyPage)
        val arr = ByteArray(256)
        for (index in arr.indices) {
            arr[index] = index.toByte()
        }
        page.route("**/*") {
            it.resume(ResumeOptions { opt -> opt.postData = arr })
        }
        val request = httpServer.futureRequest("/sleep.zzz")
        page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })")
        assertEquals("POST", request.get().method)
        val buffer = request.get().postBody
        assertEquals(arr.size, buffer.size)
        assertArrayEquals(arr, buffer)
    }
}