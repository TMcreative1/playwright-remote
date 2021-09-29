package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.handle.element.api.IElementHandle
import io.github.tmcreative1.playwright.remote.engine.handle.js.api.IJSHandle
import io.github.tmcreative1.playwright.remote.engine.options.DragAndDropOptions
import io.github.tmcreative1.playwright.remote.engine.options.Position
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestPageDrag : BaseTest() {

    @Test
    fun `check correct work if the drag is cancelled`() {
        page.navigate("${httpServer.prefixWithDomain}/drag-n-drop.html")
        val jsScript = """() => {
            |   document.body.addEventListener('dragstart', event => {
            |       event.preventDefault();
            |   }, false);
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        page.hover("#source")
        page.mouse().down()
        page.hover("#target")
        page.mouse().up()
        assertEquals(
            false,
            page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")
        )
    }

    @Test
    fun `check correct work if the drag event is captured but not cancelled`() {
        page.navigate("${httpServer.prefixWithDomain}/drag-n-drop.html")
        val jsScript = """() => {
            |   document.body.addEventListener('dragstart', event => {
            |       event.stopImmediatePropagation();
            |   }, false);
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        page.hover("#source")
        page.mouse().down()
        page.hover("#target")
        page.mouse().up()
        assertEquals(
            true,
            page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")
        )
    }

    @Test
    fun `check to able to drag the mouse in frame`() {
        page.navigate("${httpServer.prefixWithDomain}/frames/one-frame.html")
        val eventsHandle = trackEvents(page.frames()[1].querySelector("html")!!)
        page.mouse().move(30.0, 30.0)
        page.mouse().down()
        page.mouse().move(60.0, 60.0)
        page.mouse().up()
        assertEquals(listOf("mousemove", "mousedown", "mousemove", "mouseup"), eventsHandle.jsonValue())
    }

    @Test
    fun `check correct work of drag and drop method`() {
        page.navigate("${httpServer.prefixWithDomain}/drag-n-drop.html")
        page.dragAndDrop("#source", "#target")
        assertEquals(
            true,
            page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")
        )
    }

    @Test
    fun `check to allow specifying the position`() {
        val content = """<div style='width:100px;height:100px;background:red;' id='red'>
            |</div>
            |<div style='width:100px;height:100px;background:blue;' id='blue'>
            |</div>
        """.trimMargin()
        page.setContent(content)
        val jsScript = """() => {
            |   const events = [];
            |   document.getElementById('red').addEventListener('mousedown', event => {
            |       events.push({
            |           type: 'mousedown',
            |           x: event.offsetX,
            |           y: event.offsetY,
            |       });
            |   });
            |   document.getElementById('blue').addEventListener('mouseup', event => {
            |       events.push({
            |           type: 'mouseup',
            |           x: event.offsetX,
            |           y: event.offsetY,
            |       });
            |   });
            |   return events;
            |}
        """.trimMargin()
        val eventsHandle = page.evaluateHandle(jsScript)
        page.dragAndDrop("#red", "#blue", DragAndDropOptions {
            it.sourcePosition = Position(34.0, 7.0)
            it.targetPosition = Position(10.0, 20.0)
        })
        val json = eventsHandle.jsonValue()
        assertJsonEquals("[{type: \"mousedown\", x: 34, y: 7},{type: \"mouseup\", x: 10, y: 20}]", json)
    }

    private fun trackEvents(target: IElementHandle): IJSHandle {
        val jsScript = """target => {
            |   const events = [];
            |   for (const event of [
            |       'mousedown', 'mousemove', 'mouseup',
            |       'dragstart', 'dragend', 'dragover', 'dragenter', 'dragleave', 'dragexit',
            |       'drop'])
            |       target.addEventListener(event, () => events.push(event), false);
            |   return events;
            |}
        """.trimMargin()
        return target.evaluateHandle(jsScript)
    }
}