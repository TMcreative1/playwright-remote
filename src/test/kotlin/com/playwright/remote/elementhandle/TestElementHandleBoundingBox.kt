package com.playwright.remote.elementhandle

import com.playwright.remote.base.BaseTest
import com.playwright.remote.engine.options.NewContextOptions
import com.playwright.remote.engine.options.ViewportSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import kotlin.math.round
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestElementHandleBoundingBox : BaseTest() {

    @Test
    fun `check correct work`() {
        page.setViewportSize(500, 500)
        page.navigate("${httpServer.prefixWithDomain}/grid.html")
        val elementHandle = page.querySelector(".box:nth-of-type(13)")
        assertNotNull(elementHandle)
        val box = elementHandle.boundingBox()
        assertNotNull(box)
        assertEquals(100.0, box.x)
        assertEquals(50.0, box.y)
        assertEquals(50.0, box.width)
        assertEquals(50.0, box.height)
    }

    @Test
    fun `check to handle nested frames`() {
        page.setViewportSize(500, 500)
        page.navigate("${httpServer.prefixWithDomain}/frames/nested-frames.html")
        val nestedFrame = page.frame("two")
        assertNotNull(nestedFrame)
        val elementHandle = nestedFrame.querySelector("div")
        assertNotNull(elementHandle)
        val box = elementHandle.boundingBox()
        assertNotNull(box)
        assertEquals(24.0, box.x)
        assertEquals(224.0, box.y)
        assertEquals(268.0, box.width)
        assertEquals(18.0, box.height)
    }

    @Test
    fun `check to return null for invisible elements`() {
        page.setContent("<div style='display:none'>hi</div>")
        val element = page.querySelector("div")
        assertNotNull(element)
        assertNull(element.boundingBox())
    }

    @Test
    fun `check to force a layout`() {
        page.setViewportSize(500, 500)
        page.setContent("<div style='width: 100px; height: 100px'>hello</div>")
        val elementHandle = page.querySelector("div")
        assertNotNull(elementHandle)
        page.evaluate("element => element.style.height = '200px'", elementHandle)
        val box = elementHandle.boundingBox()
        assertNotNull(box)
        assertEquals(8.0, box.x)
        assertEquals(8.0, box.y)
        assertEquals(100.0, box.width)
        assertEquals(200.0, box.height)
    }

    @Test
    fun `check correct work with svg nodes`() {
        val content = """<svg xmlns='http://www.w3.org/2000/svg' width='500' height='500'>
            |   <rect id='theRect' x='30' y='50' width='200' height='300'></rect>
            |</svg>
        """.trimMargin()
        page.setContent(content)
        val element = page.querySelector("#therect")
        assertNotNull(element)
        val pwBoundingBox = element.boundingBox()
        assertNotNull(pwBoundingBox)

        val jsScript = """e => {
            |   const rect = e.getBoundingClientRect();
            |   return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };
            |}
        """.trimMargin()
        val webBoundingBox = page.evaluate(jsScript, element) as Map<*, *>
        assertEquals(webBoundingBox["x"], pwBoundingBox.x.toInt())
        assertEquals(webBoundingBox["y"], pwBoundingBox.y.toInt())
        assertEquals(webBoundingBox["width"], pwBoundingBox.width.toInt())
        assertEquals(webBoundingBox["height"], pwBoundingBox.height.toInt())
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "firefox")
    fun `check correct work with page scale`() {
        val context = browser.newContext(NewContextOptions {
            it.viewportSize = ViewportSize { viewport ->
                viewport.width = 400
                viewport.height = 400
            }
            it.isMobile = true
        })
        val pg = context.newPage()
        pg.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val button = pg.querySelector("button")
        assertNotNull(button)

        val jsScript = """button => {
            |   document.body.style.margin = '0';
            |   button.style.borderWidth = '0';
            |   button.style.width = '200px';
            |   button.style.height = '20px';
            |   button.style.marginLeft = '17px';
            |   button.style.marginTop = '23px';
            |}
        """.trimMargin()
        button.evaluate(jsScript)
        val box = button.boundingBox()
        assertNotNull(box)
        assertEquals(1700.0, round(box.x * 100))
        assertEquals(2300.0, round(box.y * 100))
        assertEquals(20000.0, round(box.width * 100))
        assertEquals(2000.0, round(box.height * 100))
    }

    @Test
    fun `check correct work when inline box child is outside of viewport`() {
        val content = """<style>
            |   i {
            |       position: absolute;
            |       top: -1000px;
            |   }
            |   body {
            |       margin: 0;
            |       font-size: 12px;
            |   }
            |</style>
            |<span><i>foo</i><b>bar</b></span>
        """.trimMargin()
        page.setContent(content)
        val handle = page.querySelector("span")
        assertNotNull(handle)
        val pwBoundingBox = handle.boundingBox()
        assertNotNull(pwBoundingBox)

        val jsScript = """e => {
            |   const rect = e.getBoundingClientRect();
            |   return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };
            |}
        """.trimMargin()

        val webBoundingBox = handle.evaluate(jsScript) as Map<*, Double>

        assertEquals(round(webBoundingBox["x"]!!), round(pwBoundingBox.x))
        assertEquals(round(webBoundingBox["y"]!!), round(pwBoundingBox.y))
        assertEquals(round(webBoundingBox["width"]!!), round(pwBoundingBox.width))
        assertEquals(round(webBoundingBox["height"]!!), round(pwBoundingBox.height))
    }
}