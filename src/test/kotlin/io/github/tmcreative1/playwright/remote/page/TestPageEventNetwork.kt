package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.enums.Platform
import io.github.tmcreative1.playwright.remote.engine.route.request.api.IRequest
import io.github.tmcreative1.playwright.remote.engine.route.response.api.IResponse
import io.github.tmcreative1.playwright.remote.utils.PlatformUtils
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestPageEventNetwork : BaseTest() {

    @Test
    fun `check request events`() {
        val requests = arrayListOf<IRequest>()
        page.onRequest { requests.add(it) }
        page.navigate(httpServer.emptyPage)
        assertEquals(1, requests.size)
        assertEquals(httpServer.emptyPage, requests[0].url())
        assertEquals("document", requests[0].resourceType())
        assertEquals("GET", requests[0].method())
        assertNotNull(requests[0].response())
        assertEquals(page.mainFrame(), requests[0].frame())
        assertEquals(httpServer.emptyPage, requests[0].frame().url())
    }

    @Test
    fun `check response events`() {
        val responses = arrayListOf<IResponse>()
        page.onResponse { responses.add(it) }
        page.navigate(httpServer.emptyPage)
        assertEquals(1, responses.size)
        assertEquals(httpServer.emptyPage, responses[0].url())
        assertEquals(200, responses[0].status())
        assertTrue(responses[0].ok())
        assertNotNull(responses[0].request())
    }

    @Test
    fun `check request failed events`() {
        httpServer.setRoute("/one-style.css") { it.responseBody.close() }
        val failedRequests = arrayListOf<IRequest>()
        page.onRequestFailed { failedRequests.add(it) }
        page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        assertEquals(1, failedRequests.size)
        assertTrue(failedRequests[0].url().contains("one-style.css"))
        assertNull(failedRequests[0].response())
        assertEquals("stylesheet", failedRequests[0].resourceType())
        when {
            isChromium() -> assertEquals("net::ERR_EMPTY_RESPONSE", failedRequests[0].failure())
            isWebkit() -> when (PlatformUtils.getCurrentPlatform()) {
                Platform.MAC -> assertEquals("The network connection was lost.", failedRequests[0].failure())
                Platform.WINDOWS64, Platform.WINDOWS32 -> assertEquals(
                    "Server returned nothing (no headers, no data)",
                    failedRequests[0].failure()
                )
                else -> assertEquals("Message Corrupt", failedRequests[0].failure())
            }
            else -> assertEquals("NS_ERROR_NET_RESET", failedRequests[0].failure())
        }
        assertNotNull(failedRequests[0].frame())
    }

    @Test
    fun `check request finished events`() {
        val finishedRequests = arrayListOf<IRequest>()
        page.onRequestFinished { finishedRequests.add(it) }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertTrue(response.finished().isEmpty())
        assertNotNull(finishedRequests[0])
        assertEquals(response.request(), finishedRequests[0])
        assertEquals(httpServer.emptyPage, finishedRequests[0].url())
        assertNotNull(finishedRequests[0].response())
        assertEquals(page.mainFrame(), finishedRequests[0].frame())
        assertEquals(httpServer.emptyPage, finishedRequests[0].frame().url())
        assertTrue(finishedRequests[0].failure().isEmpty())
    }

    @Test
    fun `check events in proper order`() {
        val events = arrayListOf<String>()
        page.onRequest { events.add("request") }
        page.onResponse { events.add("response") }
        page.onRequestFinished { events.add("requestFinished") }
        val response = page.navigate(httpServer.emptyPage)
        assertTrue(response!!.finished().isEmpty())
        assertEquals(listOf("request", "response", "requestFinished"), events)
    }

    @Test
    fun `check to support redirects`() {
        val events = arrayListOf<String>()
        page.onRequest { events.add("${it.method()} ${it.url()}") }
        page.onResponse { events.add("${it.status()} ${it.url()}") }
        page.onRequestFinished { events.add("DONE ${it.url()}") }
        page.onRequestFailed { events.add("FAILED ${it.url()}") }
        httpServer.setRedirect("/em.html", "/empty.html")
        val emUrl = "${httpServer.prefixWithDomain}/em.html"
        val response = page.navigate(emUrl)
        response!!.finished()
        assertEquals(
            listOf(
                "GET $emUrl",
                "302 $emUrl",
                "DONE $emUrl",
                "GET ${httpServer.emptyPage}",
                "200 ${httpServer.emptyPage}",
                "DONE ${httpServer.emptyPage}",
            ), events
        )
        val redirectFrom = response.request().redirectedFrom()
        assertNotNull(redirectFrom)
        assertTrue(redirectFrom.url().contains("/em.html"))
        assertNull(redirectFrom.redirectedFrom())
        assertEquals(response.request(), redirectFrom.redirectedTo())
    }
}