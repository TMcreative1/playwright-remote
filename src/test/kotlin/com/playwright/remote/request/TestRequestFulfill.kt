package com.playwright.remote.request

import com.playwright.remote.base.BaseTest
import com.playwright.remote.engine.options.FulfillOptions
import okio.IOException
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestRequestFulfill : BaseTest() {

    @Test
    fun `check correct work`() {
        page.route("**/*") {
            it.fulfill(FulfillOptions { opt ->
                opt.status = 201
                opt.contentType = "text/html"
                opt.headers = mapOf("foo" to "bar")
                opt.body = "Yo, page!"
            })
        }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertEquals(201, response.status())
        assertEquals("bar", response.headers()["foo"])
        assertEquals("Yo, page!", page.evaluate("() => document.body.textContent"))
    }

    @Test
    fun `check correct with status code 422`() {
        page.route("**/*") {
            it.fulfill(FulfillOptions { opt ->
                opt.status = 422
                opt.body = "Yo, page!"
            })
        }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertEquals(422, response.status())
        assertEquals("Unprocessable Entity", response.statusText())
        assertEquals("Yo, page!", page.evaluate("document.body.textContent"))
    }

    @Test
    fun `check to allow mocking binary responses`() {
        page.route("**/*") {
            val imageBuffer: ByteArray
            try {
                imageBuffer = Files.readAllBytes(Paths.get("src/test/resources/playwright.png"))
            } catch (e: IOException) {
                e.printStackTrace()
                throw RuntimeException(e)
            }
            it.fulfill(FulfillOptions { opt ->
                opt.contentType = "image/png"
                opt.bodyBytes = imageBuffer
            })
        }
        val jsScript = """PREFIX => {
            |   const img = document.createElement('img');
            |   img.src = PREFIX + '/does-not-exist.png';
            |   document.body.appendChild(img);
            |   return new Promise(fulfill => img.onload = fulfill);
            |}
        """.trimMargin()
        page.evaluate(jsScript, httpServer.prefixWithDomain)
        val img = page.querySelector("img")
        assertNotNull(img)
        assertNotNull(img.screenshot())
    }

    @Test
    fun `check to allow mocking svg with charset`() {
        page.route("**/*") {
            it.fulfill(FulfillOptions { opt ->
                opt.contentType = "image/svg+xml ; charset=utf-8"
                opt.body =
                    "<svg width=\"50\" height=\"50\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\"><rect x=\"10\" y=\"10\" width=\"30\" height=\"30\" stroke=\"black\" fill=\"transparent\" stroke-width=\"5\"/></svg>"
            })
        }
        val jsScript = """PREFIX => {
            |   const img = document.createElement('img');
            |   img.src = PREFIX + '/does-not-exist.svg';
            |   document.body.appendChild(img);
            |   return new Promise((f, r) => { img.onload = f; img.onerror = r; });
            |}
        """.trimMargin()
        page.evaluate(jsScript, httpServer.prefixWithDomain)
        val img = page.querySelector("img")
        assertNotNull(img)
        assertNotNull(img.screenshot())
    }
}