package com.playwright.remote

import com.google.gson.Gson
import com.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestNetworkPostData : BaseTest() {

    @Test
    fun `check to return correct post data buffer for utf-8 body`() {
        page.navigate(httpServer.emptyPage)
        val value = "baẞ"
        val jsScript = """({url, value}) => {
            |   const request = new Request(url, {
            |       method: 'POST',
            |       body: JSON.stringify(value),
            |   });
            |   request.headers.set('content-type', 'application/json;charset=UTF-8');
            |   return fetch(request);
            |}
        """.trimMargin()
        val request = page.waitForRequest("**") {
            page.evaluate(jsScript, mapOf("url" to "${httpServer.prefixWithDomain}/title.html", "value" to value))
        }
        assertNotNull(request)
        assertEquals(Gson().toJson(value), request.postData())
        assertTrue(Arrays.equals(Gson().toJson(value).toByteArray(Charsets.UTF_8), request.postDataBuffer()))
    }

    @Test
    fun `check to return post data with out content type`() {
        page.navigate(httpServer.emptyPage)
        val jsScript = """({url}) => {
            |   const request = new Request(url, {
            |       method: 'POST',
            |       body: JSON.stringify({ value: 42 }),
            |   });
            |   request.headers.set('content-type', '');
            |   return fetch(request);
            |}
        """.trimMargin()
        val request = page.waitForRequest("**") {
            page.evaluate(jsScript, mapOf("url" to "${httpServer.prefixWithDomain}/title.html"))
        }
        assertNotNull(request)
        assertEquals(Gson().toJson(mapOf("value" to 42)), request.postData())
    }

    @Test
    fun `check to return post data for put requests`() {
        page.navigate(httpServer.emptyPage)
        val jsScript = """({url}) => {
            |   const request = new Request(url, {
            |       method: 'PUT',
            |       body: JSON.stringify({ value: 42 }),
            |   });
            |   return fetch(request);
            |}
        """.trimMargin()
        val request = page.waitForRequest("**") {
            page.evaluate(jsScript, mapOf("url" to "${httpServer.prefixWithDomain}/title.html"))
        }
        assertNotNull(request)
        assertEquals(Gson().toJson(mapOf("value" to 42)), request.postData())
    }
}