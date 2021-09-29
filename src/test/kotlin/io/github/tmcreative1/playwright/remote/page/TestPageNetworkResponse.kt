package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestPageNetworkResponse : BaseTest() {

    @Test
    fun `check to report multiple set cookie headers`() {
        httpServer.setRoute("/headers") {
            it.responseHeaders.add("Set-Cookie", "a=b")
            it.responseHeaders.add("Set-Cookie", "c=d")
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }
        page.navigate(httpServer.emptyPage)
        val response = page.waitForResponse("**/*") {
            page.evaluate("fetch('/headers')")
        }
        assertNotNull(response)
        val headers = response.headersArray()
        val cookies = headers.filter { "set-cookie" == it.name.lowercase() }.map { it.value }.toList()
        assertEquals(listOf("a=b", "c=d"), cookies)
        assertNull(response.headerValue("not-there"))
        assertEquals("a=b\nc=d", response.headerValue("set-cookie"))
        assertEquals(listOf("a=b", "c=d"), response.headerValues("set-cookie"))
    }
}