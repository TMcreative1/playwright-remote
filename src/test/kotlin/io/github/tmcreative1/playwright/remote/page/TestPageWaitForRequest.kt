package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForRequestOptions
import io.github.tmcreative1.playwright.remote.engine.route.request.api.IRequest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.Test
import java.util.regex.Pattern
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestPageWaitForRequest : BaseTest() {

    @Test
    fun `check correct work`() {
        page.navigate(httpServer.emptyPage)
        val request = page.waitForRequest("${httpServer.prefixWithDomain}/digits/2.png") {
            val jsScript = """() => {
                |   fetch('/digits/1.png');
                |   fetch('/digits/2.png');
                |   fetch('/digits/3.png');
                |}
            """.trimMargin()
            page.evaluate(jsScript)
        }
        assertNotNull(request)
        assertEquals("${httpServer.prefixWithDomain}/digits/2.png", request.url())
    }

    @Test
    fun `check correct work with predicate`() {
        page.navigate(httpServer.emptyPage)
        val predicate: (IRequest) -> Boolean = { r -> r.url() == "${httpServer.prefixWithDomain}/digits/2.png" }
        val request = page.waitForRequest(predicate) {
            val jsScript = """() => {
                |   fetch('/digits/1.png');
                |   fetch('/digits/2.png');
                |   fetch('/digits/3.png');
                |}
            """.trimMargin()
            page.evaluate(jsScript)
        }
        assertNotNull(request)
        assertEquals("${httpServer.prefixWithDomain}/digits/2.png", request.url())
    }

    @Test
    fun `check correct work with timeout`() {
        try {
            page.waitForRequest({ false }, WaitForRequestOptions { it.timeout = 1.0 }) {}
            fail("waitForRequest should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Timeout"), e.message)
        }
    }

    @Test
    fun `check correct work without timeout`() {
        page.navigate(httpServer.emptyPage)
        val request = page.waitForRequest(
            "${httpServer.prefixWithDomain}/digits/2.png",
            WaitForRequestOptions { it.timeout = 0.0 }) {
            val jsScript = """() => setTimeout(() => {
                | fetch('/digits/1.png');
                | fetch('/digits/2.png');
                | fetch('/digits/3.png');
                |}, 50)
            """.trimMargin()
            page.evaluate(jsScript)
        }
        assertNotNull(request)
        assertEquals("${httpServer.prefixWithDomain}/digits/2.png", request.url())
    }

    @Test
    fun `check correct work with url match`() {
        page.navigate(httpServer.emptyPage)
        val request = page.waitForRequest(Pattern.compile(".*digits/\\d\\.png")) {
            page.evaluate("""() => {
                |   fetch('/digits/1.png');
                |}
            """.trimMargin())
        }
        assertNotNull(request)
        assertEquals("${httpServer.prefixWithDomain}/digits/1.png", request.url())
    }
}