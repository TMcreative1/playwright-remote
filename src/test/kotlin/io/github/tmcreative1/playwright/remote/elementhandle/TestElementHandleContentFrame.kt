package io.github.tmcreative1.playwright.remote.elementhandle

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestElementHandleContentFrame : BaseTest() {

    @Test
    fun `check correct work`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", httpServer.emptyPage)
        val element = page.querySelector("#frame1")
        assertNotNull(element)
        val frame = element.contentFrame()
        assertEquals(page.frames()[1], frame)
    }

    @Test
    fun `check correct work for cross process iframes`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", "${httpServer.prefixWithIP}/empty.html")
        val element = page.querySelector("#frame1")
        assertNotNull(element)
        val frame = element.contentFrame()
        assertEquals(page.frames()[1], frame)
    }

    @Test
    fun `check correct work for cross frame evaluations`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", httpServer.emptyPage)
        val frame = page.frames()[1]
        assertNotNull(frame)
        val jsHandle = frame.evaluateHandle("() => window.top.document.querySelector('#frame1')")
        assertNotNull(jsHandle)
        val element = jsHandle.asElement()
        assertNotNull(element)
        assertEquals(frame, element.contentFrame())
    }

    @Test
    fun `check to return null for non iframes`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", httpServer.emptyPage)
        val frame = page.frames()[1]
        assertNotNull(frame)
        val jsHandle = frame.evaluateHandle("() => document.body")
        val element = jsHandle.asElement()
        assertNotNull(element)
        assertNull(element.contentFrame())
    }

    @Test
    fun `check to return null for document element`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", httpServer.emptyPage)
        val frame = page.frames()[1]
        assertNotNull(frame)
        val jsHandle = frame.evaluateHandle("() => document.documentElement")
        assertNotNull(jsHandle)
        val element = jsHandle.asElement()
        assertNotNull(element)
        assertNull(element.contentFrame())
    }
}