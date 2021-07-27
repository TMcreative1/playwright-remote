package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.options.NewContextOptions
import com.playwright.remote.engine.route.request.api.IRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestWorker : BaseTest() {

    @Test
    fun `check correct work`() {
        val worker = page.waitForWorker {
            page.navigate("${httpServer.prefixWithDomain}/worker/worker.html")
        }

        assertNotNull(worker)
        assertTrue(worker.url().contains("worker.js"))
        assertEquals("worker function result", worker.evaluate("() => self['workerFunction']()"))
        page.navigate(httpServer.emptyPage)
        assertEquals(0, page.workers().size)
    }

    @Test
    fun `check to emit created and destroyed events`() {
        val workerObj = arrayListOf<IJSHandle?>(null)
        val worker = page.waitForWorker {
            workerObj[0] =
                page.evaluateHandle("() => new Worker(URL.createObjectURL(new Blob(['1'], {type: 'application/javascript'})))")
        }
        assertNotNull(worker)

        val workerObjThis = worker.evaluateHandle("() => this")
        val closedWorker = worker.waitForClose {
            page.evaluate("workerObj => workerObj.terminate()", workerObj[0])
        }
        assertEquals(worker, closedWorker)
        try {
            workerObjThis.getProperty("self")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Most likely the worker has been closed."))
        }
    }

    @Test
    fun `check to report to console logs`() {
        val message = page.waitForConsoleMessage {
            page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))")
        }
        assertNotNull(message)
        assertEquals("1", message.text())
    }

    @Test
    fun `check to handle js for console logs`() {
        val log = page.waitForConsoleMessage {
            page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1,2,3,this)'], {type: 'application/javascript'})))")
        }
        assertNotNull(log)
        assertEquals("1 2 3 JSHandle@object", log.text())
        assertEquals(4, log.args().size)
        assertEquals("null", log.args()[3].getProperty("origin").jsonValue())
    }

    @Test
    fun `check to evaluate`() {
        val worker = page.waitForWorker {
            page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))")
        }
        assertNotNull(worker)
        assertEquals(2, worker.evaluate("1 + 1"))
    }

    @Test
    fun `check to report error`() {
        val errorLog = arrayListOf<String?>(null)
        page.onPageError { errorLog[0] = it }
        val jsScript = """() =>new Worker(URL.createObjectURL(new Blob([`
            |   setTimeout(() => {
            |       console.log('hey');
            |       throw new Error('this is my error');
            |   })
            |`], { type: 'application/javascript' })))
        """.trimMargin()
        page.evaluate(jsScript)
        val start = Instant.now()
        while (errorLog[0] == null) {
            page.waitForTimeout(100.0)
            assertTrue(Duration.between(start, Instant.now()).seconds < 30, "Timed out")
        }
        assertTrue(errorLog[0]!!.contains("this is my error"))
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "firefox")
    fun `check to clear upon navigation`() {
        page.navigate(httpServer.emptyPage)
        val worker = page.waitForWorker {
            page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))")
        }
        assertEquals(1, page.workers().size)
        assertNotNull(worker)
        val destroyed = worker.waitForClose {
            page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        }
        assertEquals(worker, destroyed)
        assertEquals(0, page.workers().size)
    }

    @Test
    fun `check to clear upon cross process navigation`() {
        page.navigate(httpServer.emptyPage)
        val worker = page.waitForWorker {
            page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))")
        }
        assertNotNull(worker)
        assertEquals(1, page.workers().size)
        val destroyed = arrayListOf(false)
        worker.onClose { destroyed[0] = true }
        page.navigate("${httpServer.prefixWithIP}/empty.html")
        assertTrue(destroyed[0])
        assertEquals(0, page.workers().size)
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^chromium|firefox")
    fun `check attribute network activity for worker inside iframe`() {
        page.navigate(httpServer.emptyPage)
        val frame = arrayListOf<IFrame?>(null)
        val worker = page.waitForWorker {
            frame[0] = attachFrame(page, "frame1", "${httpServer.prefixWithIP}/worker/worker.html")
        }
        assertNotNull(frame[0])
        assertNotNull(worker)
        val url = "${httpServer.prefixWithIP}/one-style.css"
        val request = page.waitForRequest(url) {
            worker.evaluate("url => fetch(url).then(response => response.text()).then(console.log)", url)
        }
        assertNotNull(request)
        assertEquals(url, request.url())
        assertEquals(frame[0], request.frame())
    }

    @Test
    fun `check to report network activity`() {
        val worker = page.waitForWorker {
            page.navigate("${httpServer.prefixWithIP}/worker/worker.html")
        }
        assertNotNull(worker)
        val url = "${httpServer.prefixWithIP}/one-style.css"
        val request = arrayListOf<IRequest?>(null)
        val response = page.waitForResponse(url) {
            request[0] = page.waitForRequest(url) {
                worker.evaluate("url => fetch(url).then(response => response.text()).then(console.log)", url)
            }
            assertEquals(url, request[0]!!.url())
        }
        assertNotNull(response)
        assertEquals(request[0], response.request())
        assertTrue(response.ok())
    }

    @Test
    fun `check to report network activity on worker creation`() {
        page.navigate(httpServer.emptyPage)
        val url = "${httpServer.prefixWithDomain}/one-style.css"
        val request = arrayListOf<IRequest?>(null)
        val response = page.waitForResponse(url) {
            request[0] = page.waitForRequest(url) {
                val jsScript = """url => new Worker(URL.createObjectURL(new Blob([`
                    |   fetch('${url}').then(response => response.text()).then(console.log);
                    |`], {type: 'application/javascript'})))
                """.trimMargin()
                page.evaluate(jsScript, url)
            }
            assertEquals(url, request[0]!!.url())
        }
        assertNotNull(response)
        assertEquals(request[0], response.request())
        assertTrue(response.ok())
    }

    @Test
    fun `check format number using context locale`() {
        val context = browser.newContext(NewContextOptions { it.locale = "ru-Ru" })
        val newPage = context.newPage()
        newPage.navigate(httpServer.emptyPage)
        val worker = page.waitForWorker {
            page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(1)'], {type: 'application/javascript'})))")
        }
        assertNotNull(worker)
        val expectedVal = if (isFirefox()) "10000.2" else "10,000.2"
        assertEquals(expectedVal, worker.evaluate("() => (10000.20).toLocaleString()"))
        context.close()
    }
}