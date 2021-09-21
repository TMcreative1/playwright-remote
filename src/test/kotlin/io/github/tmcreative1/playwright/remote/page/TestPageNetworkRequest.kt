package io.github.tmcreative1.playwright.remote.page

import com.google.gson.Gson
import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.domain.request.HttpHeader
import org.junit.jupiter.api.Test
import java.util.concurrent.Semaphore
import kotlin.streams.toList
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestPageNetworkRequest : BaseTest() {

    @Test
    fun `check to report raw headers`() {
        val serverHeaders = arrayListOf<HttpHeader>()
        val responseWritten = Semaphore(0)
        httpServer.setRoute("/headers") {
            for (entry in it.requestHeaders.entries) {
                for (value in entry.value) {
                    val header = HttpHeader(entry.key, value)
                    serverHeaders.add(header)
                }
            }
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
            responseWritten.release()
        }
        page.navigate(httpServer.emptyPage)
        val request = page.waitForRequest("**/*") {
            val jsScript = """() => fetch('/headers', {
                |   headers: [
                |       ['header-a', 'value-a'],
                |       ['header-b', 'value-b'],
                |       ['header-a', 'value-a-1'],
                |       ['header-a', 'value-a-2'],
                |   ]
                |})
            """.trimMargin()
            page.evaluate(jsScript)
        }

        responseWritten.acquire()
        var expectedHeaders = serverHeaders
        if (isWebkit() && isWindows()) {
            expectedHeaders =
                expectedHeaders.filter { "accept-encoding" != it.name.lowercase() && "accept-language" != it.name.lowercase() } as ArrayList<HttpHeader>
        }

        assertNotNull(request)
        var headers = request.headersArray()

        expectedHeaders =
            expectedHeaders.stream().peek { it.name = it.name.lowercase() }.toList() as ArrayList<HttpHeader>
        headers = headers.stream().peek { it.name = it.name.lowercase() }.toList() as ArrayList<HttpHeader>
        assertEquals(
            Gson().toJsonTree(expectedHeaders.sortedBy { it.name }),
            Gson().toJsonTree(headers.sortedBy { it.name })
        )
        assertEquals("value-a, value-a-1, value-a-2", request.headerValue("header-a"))
        assertNull(request.headerValue("not-there"))
    }

    @Test
    fun `check to report all cookies in one header`() {
        page.navigate(httpServer.emptyPage)
        val jsScript = """() => {
            |   document.cookie = 'myCookie=myValue';
            |   document.cookie = 'myOtherCookie=myOtherValue';
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        val cookie = response.request().allHeaders()["cookie"]
        assertEquals("myCookie=myValue; myOtherCookie=myOtherValue", cookie)
    }
}