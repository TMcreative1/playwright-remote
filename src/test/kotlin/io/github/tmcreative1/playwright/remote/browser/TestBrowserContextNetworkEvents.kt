package io.github.tmcreative1.playwright.remote.browser

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.route.request.api.IRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestBrowserContextNetworkEvents : BaseTest() {

    @Test
    fun `check correct work of browser context events request`() {
        val requests = arrayListOf<String>()
        browserContext.onRequest { requests.add(it.url()) }
        page.navigate(httpServer.emptyPage)
        page.setContent("<a target=_blank rel=noopener href='/page-with-one-style.html'>Click me</a>")
        val pg1 = browserContext.waitForPage { page.click("a") }
        assertNotNull(pg1)
        pg1.waitForLoadState()
        assertEquals(
            listOf(
                httpServer.emptyPage,
                "${httpServer.prefixWithDomain}/page-with-one-style.html",
                "${httpServer.prefixWithDomain}/one-style.css"
            ), requests
        )
    }

    @Test
    fun `check correct work of browser context events response`() {
        val responses = arrayListOf<String>()
        browserContext.onResponse { responses.add(it.url()) }
        page.navigate(httpServer.emptyPage)
        page.setContent("<a target=_blank rel=noopener href='/page-with-one-style.html'>Click me</a>")
        val pg1 = browserContext.waitForPage { page.click("a") }
        assertNotNull(pg1)
        pg1.waitForLoadState()
        assertEquals(
            listOf(
                httpServer.emptyPage,
                "${httpServer.prefixWithDomain}/page-with-one-style.html",
                "${httpServer.prefixWithDomain}/one-style.css"
            ), responses
        )
    }

    @Test
    fun `check correct work of browser context events request failed`() {
        httpServer.setRoute("/one-style.css") { it.responseBody.close() }
        val failedRequests = arrayListOf<IRequest>()
        browserContext.onRequestFailed { failedRequests.add(it) }
        page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        assertEquals(1, failedRequests.size)
        assertTrue(failedRequests[0].url().contains("one-style.css"))
        assertNull(failedRequests[0].response())
        assertEquals("stylesheet", failedRequests[0].resourceType())
        assertNotNull(failedRequests[0].frame())
    }

    @Test
    fun `check correct work of browser context events request finished`() {
        val finishedRequests = arrayListOf<IRequest?>(null)
        browserContext.onRequestFinished { finishedRequests[0] = it }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        val request = response.request()
        assertEquals(httpServer.emptyPage, request.url())
        assertNotNull(request.response())
        assertEquals(request.frame(), page.mainFrame())
        assertEquals(httpServer.emptyPage, request.frame().url())
        assertTrue(request.failure().isEmpty())
    }

    @Test
    fun `check to fire events in proper order`() {
        val events = arrayListOf<String>()
        browserContext.onRequest { events.add("request") }
        browserContext.onResponse { events.add("response") }
        browserContext.onRequestFinished { events.add("request finished") }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertNull(response.finished())
        assertEquals(listOf("request", "response", "request finished"), events)
    }
}