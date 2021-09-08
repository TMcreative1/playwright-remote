package com.playwright.remote.elementhandle

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.enums.LoadState
import com.playwright.remote.engine.frame.impl.Frame
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestElementHandleOwnerFrame : BaseTest() {

    @Test
    fun `check correct work`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", httpServer.emptyPage)
        val frame = page.frames()[1]
        val jsHandle = frame.evaluateHandle("() => document.body")
        val elementHandle = jsHandle.asElement()
        assertNotNull(elementHandle)
        assertEquals(frame, elementHandle.ownerFrame())
    }

    @Test
    fun `check correct work for cross process iframes`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", "${httpServer.prefixWithIP}/empty.html")
        val frame = page.frames()[1]
        val jsHandle = frame.evaluateHandle("() => document.body")
        val elementHandle = jsHandle.asElement()
        assertNotNull(elementHandle)
        assertEquals(frame, elementHandle.ownerFrame())
    }

    @Test
    fun `check correct work for document`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", httpServer.emptyPage)
        val frame = page.frames()[1]
        val jsHandle = frame.evaluateHandle("document")
        val elementHandle = jsHandle.asElement()
        assertNotNull(elementHandle)
        assertEquals(frame, elementHandle.ownerFrame())
    }

    @Test
    fun `check correct work for iframe elements`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", httpServer.emptyPage)
        val frame = page.mainFrame()
        val jsHandle = frame.evaluateHandle("() => document.querySelector('#frame1')")
        val elementHandle = jsHandle.asElement()
        assertNotNull(elementHandle)
        assertEquals(frame, elementHandle.ownerFrame())
    }

    @Test
    fun `check correct work for cross frame evaluations`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", httpServer.emptyPage)
        val frame = page.mainFrame()
        val elementHandle = frame.evaluateHandle("() => document.querySelector('iframe').contentWindow.document.body")
        assertEquals((frame as Frame).childFrames.toList()[0], elementHandle.asElement()!!.ownerFrame())
    }

    @Test
    fun `check correct work for detached elements`() {
        page.navigate(httpServer.emptyPage)
        var jsScript = """() => {
            |   const div = document.createElement('div');
            |   document.body.appendChild(div);
            |   return div;
            |}
        """.trimMargin()
        val divHandle = page.evaluateHandle(jsScript)
        assertEquals(page.mainFrame(), divHandle.asElement()!!.ownerFrame())

        jsScript = """() => {
            |   const div = document.querySelector('div');
            |   document.body.removeChild(div);
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        assertEquals(page.mainFrame(), divHandle.asElement()!!.ownerFrame())
    }

    @Test
    fun `check correct work for adopted elements`() {
        page.navigate(httpServer.emptyPage)
        val popup =
            page.waitForPopup { page.evaluate("url => window['__popup'] = window.open(url)", httpServer.emptyPage) }
        var jsScript = """() => {
            |   const div = document.createElement('div');
            |   document.body.appendChild(div);
            |   return div;
            |}
        """.trimMargin()
        val divHandle = page.evaluateHandle(jsScript)
        assertEquals(page.mainFrame(), divHandle.asElement()!!.ownerFrame())
        assertNotNull(popup)
        popup.waitForLoadState(LoadState.DOMCONTENTLOADED)

        jsScript = """() => {
            |   const div = document.querySelector('div');
            |   window['__popup'].document.body.appendChild(div);
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        assertEquals(popup.mainFrame(), divHandle.asElement()!!.ownerFrame())
    }
}