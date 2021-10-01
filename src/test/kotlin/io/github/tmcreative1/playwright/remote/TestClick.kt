package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.enums.KeyboardModifier
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.engine.options.Position
import io.github.tmcreative1.playwright.remote.engine.options.ViewportSize
import io.github.tmcreative1.playwright.remote.engine.options.element.ClickOptions
import io.github.tmcreative1.playwright.remote.engine.options.enum.MouseButton.RIGHT
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import kotlin.math.roundToInt
import kotlin.test.*

class TestClick : BaseTest() {

    @Test
    fun `check to click the button`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.click("button")
        assertEquals("Clicked", page.evaluate("result"))
    }

    @Test
    fun `check to click svg`() {
        val content = """<svg height='100' width='100'>
            |   <circle onclick='javascript:window.__CLICKED=23' cx='50' cy='50' r='40' stroke='black' stroke-width='3' fill='red'/>
            |</svg>
        """.trimMargin()
        page.setContent(content)
        page.click("circle")
        assertEquals(23, page.evaluate("__CLICKED"))
    }

    @Test
    fun `check to click the button if window node is removed`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.evaluate("() => delete window.Node")
        page.click("button")
        assertEquals("Clicked", page.evaluate("result"))
    }

    @Test
    fun `check to click on a span with an inline element inside`() {
        val content = """<style>
            |  span::before {
            |    content: 'q';
            |  }
            |</style>
            |<span onclick='javascript:window.CLICKED=23'></span>
        """.trimMargin()
        page.setContent(content)
        page.click("span")
        assertEquals(23, page.evaluate("CLICKED"))
    }

    @Test
    fun `check to click the div 1x1`() {
        page.setContent("<div style='width: 1px; height: 1px;' onclick='window.__clicked = true'></div>")
        page.click("div")
        assertTrue(page.evaluate("window.__clicked") as Boolean)
    }

    @Test
    fun `check to click the button after navigation`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.click("button")
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.click("button")
        assertEquals("Clicked", page.evaluate("result"))
    }

    @Test
    fun `check to click with disabled javascript`() {
        browser.newContext(NewContextOptions { it.javaScriptEnabled = false }).use {
            val pg = it.newPage()
            pg.navigate("${httpServer.prefixWithDomain}/wrapped-link.html")
            pg.waitForNavigation { pg.click("a") }
            assertEquals("${httpServer.prefixWithDomain}/wrapped-link.html#clicked", pg.url())
        }
    }

    @Test
    fun `check to click when one if inline box children is outside of viewport`() {
        val content = """<style>
            |   i {
            |     position: absolute;
            |     top: -1000px;
            |   }
            |</style>
            |<span onclick='javascript:window.CLICKED = 23;'><i>woof</i><b>doggo</b></span>
        """.trimMargin()
        page.setContent(content)
        page.click("span")
        assertEquals(23, page.evaluate("CLICKED"))
    }

    @Test
    fun `check to select the text by triple clicking`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val text = "This is the text that we are going to try to select. Let's see how it goes."
        page.fill("textarea", text)
        page.click("textarea", ClickOptions { it.clickCount = 3 })
        val jsScript = """() => {
            |   const textarea = document.querySelector('textarea');
            |   return textarea.value.substring(textarea.selectionStart, textarea.selectionEnd);
            |}
        """.trimMargin()
        assertEquals(text, page.evaluate(jsScript))
    }

    @Test
    fun `check to click off screen buttons`() {
        page.navigate("${httpServer.prefixWithDomain}/off-screen-buttons.html")
        val messages = arrayListOf<String>()
        page.onConsoleMessage { messages.add(it.text()) }
        for (index in 0..10) {
            page.evaluate("() => window.scrollTo(0, 0)")
            page.click("#btn${index}")
        }
        assertEquals(
            listOf(
                "button #0 clicked",
                "button #1 clicked",
                "button #2 clicked",
                "button #3 clicked",
                "button #4 clicked",
                "button #5 clicked",
                "button #6 clicked",
                "button #7 clicked",
                "button #8 clicked",
                "button #9 clicked",
                "button #10 clicked",
            ),
            messages
        )
    }

    @Test
    fun `check to wait for visible when already visible`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.click("button")
        assertEquals("Clicked", page.evaluate("result"))
    }

    @Test
    fun `check to not wait with force`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.evalOnSelector("button", "b => b.style.display = 'none'")
        try {
            page.click("button", ClickOptions { it.force = true })
            fail("click should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Element is not visible"))
            assertEquals("Was not clicked", page.evaluate("result"))
        }
    }

    @Test
    fun `check to click wrapped links`() {
        page.navigate("${httpServer.prefixWithDomain}/wrapped-link.html")
        page.click("a")
        assertTrue(page.evaluate("__clicked") as Boolean)
    }

    @Test
    fun `check to click on checkbox input and toggle`() {
        page.navigate("${httpServer.prefixWithDomain}/input/checkbox.html")
        assertNull(page.evaluate("() => window['result'].check"))
        page.click("input#agree")
        assertTrue(page.evaluate("() => window['result'].check") as Boolean)
        assertEquals(
            listOf(
                "mouseover",
                "mouseenter",
                "mousemove",
                "mousedown",
                "mouseup",
                "click",
                "input",
                "change",
            ), page.evaluate("() => window['result'].events")
        )
        page.click("input#agree")
        assertFalse(page.evaluate("() => window['result'].check") as Boolean)
    }

    @Test
    fun `check to click on checkbox label and toggle`() {
        page.navigate("${httpServer.prefixWithDomain}/input/checkbox.html")
        assertNull(page.evaluate("() => window['result'].check"))
        page.click("label[for='agree']")
        assertTrue(page.evaluate("() => window['result'].check") as Boolean)
        assertEquals(
            listOf(
                "click",
                "input",
                "change"
            ),
            page.evaluate("() => window['result'].events")
        )
        page.click("label[for='agree']")
        assertFalse(page.evaluate("() => window['result'].check") as Boolean)
    }

    @Test
    fun `check to not hang with touch enabled viewports`() {
        browser.newContext(NewContextOptions {
            it.viewportSize = ViewportSize { viewport ->
                viewport.width = 375
                viewport.height = 667
            }
            it.hasTouch = true
        }).use {
            val pg = it.newPage()
            pg.mouse().down()
            pg.mouse().move(100.0, 10.0)
            pg.mouse().up()
        }
    }

    @Test
    fun `check to scroll and click the button`() {
        page.navigate("${httpServer.prefixWithDomain}/input/scrollable.html")
        page.click("#button-5")
        assertEquals("clicked", page.evaluate("() => document.querySelector('#button-5').textContent"))
        page.click("#button-80")
        assertEquals("clicked", page.evaluate("() => document.querySelector('#button-80').textContent"))
    }

    @Test
    fun `check to double click the button`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val jsScript = """() => {
            |   window['double'] = false;
            |   const button = document.querySelector('button');
            |   button.addEventListener('dblclick', event => {
            |       window['double'] = true;
            |   });
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        page.doubleClick("button")
        assertEquals(true, page.evaluate("double"))
        assertEquals("Clicked", page.evaluate("result"))
    }

    @Test
    fun `check to click a partially obscured button`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val jsScript = """() => {
            |   const button = document.querySelector('button');
            |   button.textContent = 'Some really long text that will go offscreen';
            |   button.style.position = 'absolute';
            |   button.style.left = '368px';
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        page.click("button")
        assertEquals("Clicked", page.evaluate("() => window['result']"))
    }

    @Test
    fun `check to click a rotated button`() {
        page.navigate("${httpServer.prefixWithDomain}/input/rotated-button.html")
        page.click("button")
        assertEquals("Clicked", page.evaluate("result"))
    }

    @Test
    fun `check to fire context menu event on right click`() {
        page.navigate("${httpServer.prefixWithDomain}/input/scrollable.html")
        page.click("#button-8", ClickOptions { it.button = RIGHT })
        assertEquals("context menu", page.evaluate("() => document.querySelector('#button-8').textContent"))
    }

    @Test
    fun `check to click links which cause navigation`() {
        page.setContent("<a href='${httpServer.emptyPage}'>empty.html</a>")
        page.click("a")
        assertEquals(httpServer.emptyPage, page.url())
    }

    @Test
    fun `check to click the button inside an iframe`() {
        page.navigate(httpServer.emptyPage)
        page.setContent("<div style='width:100px;height:100px'>spacer</div>")
        attachFrame(page, "button-test", "${httpServer.prefixWithDomain}/input/button.html")
        val frame = page.frames()[1]
        val button = frame.querySelector("button")
        assertNotNull(button)
        button.click()
        assertEquals("Clicked", frame.evaluate("() => window['result']"))
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "chromium")
    fun `check to click the button with fixed position inside an iframe`() {
        page.navigate(httpServer.emptyPage)
        page.setViewportSize(500, 500)
        page.setContent("<div style='width:100px;height:2000px'>spacer</div>")
        attachFrame(page, "button-test", "${httpServer.prefixWithIP}/input/button.html")
        val frame = page.frames()[1]
        frame.evalOnSelector("button", "button => button.style.setProperty('position', 'fixed')")
        frame.click("button")
        assertEquals("Clicked", frame.evaluate("() => window['result']"))
    }

    @Test
    fun `check to click the button with device scale factor set`() {
        browser.newContext(NewContextOptions {
            it.viewportSize = ViewportSize { viewport ->
                viewport.width = 400
                viewport.height = 400
            }
            it.deviceScaleFactor = 5.0
        }).use {
            val pg = it.newPage()
            assertEquals(5, pg.evaluate("() => window.devicePixelRatio"))
            pg.setContent("<div style='width:100px;height:100px'>spacer</div>")
            attachFrame(pg, "button-test", "${httpServer.prefixWithDomain}/input/button.html")
            val frame = pg.frames()[1]
            val button = frame.querySelector("button")
            assertNotNull(button)
            button.click()
            assertEquals("Clicked", frame.evaluate("window['result']"))
        }
    }

    @Test
    fun `check to click the button with px border with offset`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.evalOnSelector("button", "button => button.style.borderWidth = '8px'")
        page.click("button", ClickOptions {
            it.position = Position(20.0, 10.0)
        })
        assertEquals("Clicked", page.evaluate("result"))
        assertEquals(if (isWebkit()) 28 else 20, page.evaluate("offsetX"))
        assertEquals(if (isWebkit()) 18 else 10, page.evaluate("offsetY"))
    }

    @Test
    fun `check to click the button with em border with offset`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.evalOnSelector("button", "button => button.style.borderWidth = '2em'")
        page.evalOnSelector("button", "button => button.style.fontSize = '12px'")
        page.click("button", ClickOptions { it.position = Position(20.0, 10.0) })
        assertEquals("Clicked", page.evaluate("result"))
        assertEquals(if (isWebkit()) 44 else 20, page.evaluate("offsetX"))
        assertEquals(if (isWebkit()) 34 else 10, page.evaluate("offsetY"))
    }

    @Test
    fun `check to click a very large button with offset`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.evalOnSelector("button", "button => button.style.borderWidth = '8px'")
        page.evalOnSelector("button", "button => button.style.height = button.style.width = '2000px'")
        page.click("button", ClickOptions { it.position = Position(1900.0, 1910.0) })
        assertEquals("Clicked", page.evaluate("() => window['result']"))
        assertEquals(if (isWebkit()) 1908 else 1900, page.evaluate("offsetX"))
        assertEquals(if (isWebkit()) 1918 else 1910, page.evaluate("offsetY"))
    }

    @Test
    fun `check to click a button in scrolling container with offset`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val jsScript = """button => {
            |   const container = document.createElement('div');
            |   container.style.overflow = 'auto';
            |   container.style.width = '200px';
            |   container.style.height = '200px';
            |   button.parentElement.insertBefore(container, button);
            |   container.appendChild(button);
            |   button.style.height = '2000px';
            |   button.style.width = '2000px';
            |   button.style.borderWidth = '8px';
            |}
        """.trimMargin()
        page.evalOnSelector("button", jsScript)
        page.click("button", ClickOptions { it.position = Position(1900.0, 1910.0) })
        assertEquals("Clicked", page.evaluate("() => window['result']"))
        assertEquals(if (isWebkit()) 1908 else 1900, page.evaluate("offsetX"))
        assertEquals(if (isWebkit()) 1918 else 1910, page.evaluate("offsetY"))
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "firefox")
    fun `check to click the button with offset with page scale`() {
        browser.newContext(NewContextOptions {
            it.viewportSize = ViewportSize { viewport ->
                viewport.width = 400
                viewport.height = 400
            }
            it.isMobile = true
        }).use {
            val pg = it.newPage()
            pg.navigate("${httpServer.prefixWithDomain}/input/button.html")
            val jsScript = """button => {
                |   button.style.borderWidth = '8px';
                |   document.body.style.margin = '0';
                |}
            """.trimMargin()
            pg.evalOnSelector("button", jsScript)
            pg.click("button", ClickOptions { opt -> opt.position = Position(20.0, 10.0) })
            assertEquals("Clicked", pg.evaluate("result"))
            var expectedX = 28
            var expectedY = 18
            if (isWebkit()) {
                expectedX = 29
                expectedY = 19
            }
            if (isChromium()) {
                expectedX = 27
                expectedY = 18
            }
            assertEquals(expectedX, (pg.evaluate("pageX") as Int + 0.01).roundToInt())
            assertEquals(expectedY, (pg.evaluate("pageY") as Int + 0.01).roundToInt())
        }
    }

    @Test
    fun `check to wait for stable position`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val jsScript = """button => {
            |   button.style.transition = 'margin 500ms linear 0s';
            |   button.style.marginLeft = '200px';
            |   button.style.borderWidth = '0';
            |   button.style.width = '200px';
            |   button.style.height = '20px';
            |   button.style.display = 'block';
            |   document.body.style.margin = '0';
            |}
        """.trimMargin()
        page.evalOnSelector("button", jsScript)
        page.click("button")
        assertEquals("Clicked", page.evaluate("result"))
        assertEquals(300, page.evaluate("pageX"))
        assertEquals(10, page.evaluate("pageY"))
    }

    @Test
    fun `check to fail when obscured and not waiting for hit target`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val button = page.querySelector("button")
        val jsScript = """() => {
            |   document.body.style.position = 'relative';
            |   const blocker = document.createElement('div');
            |   blocker.style.position = 'absolute';
            |   blocker.style.width = '400px';
            |   blocker.style.height = '20px';
            |   blocker.style.left = '0';
            |   blocker.style.top = '0';
            |   document.body.appendChild(blocker);
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        assertNotNull(button)
        button.click(ClickOptions { it.force = true })
        assertEquals("Was not clicked", page.evaluate("window['result']"))
    }

    @Test
    fun `check to click disable div`() {
        page.setContent("<div onclick='javascript:window.__CLICKED=true;' disabled>Click target</div>")
        page.click("text=Click target")
        assertEquals(true, page.evaluate("__CLICKED"))
    }

    @Test
    fun `check to climb dom for inner label with pointer events none`() {
        page.setContent("<button onclick='javascript:window.__CLICKED=true;'><label style='pointer-events:none'>Click target</label></button>")
        page.click("text=Click target")
        assertEquals(true, page.evaluate("__CLICKED"))
    }

    @Test
    fun `check to work with unicode selectors`() {
        page.setContent("<button onclick='javascript:window.__CLICKED=true;'><label style='pointer-events:none'>Найти</label></button>")
        page.click("text=Найти")
        assertEquals(true, page.evaluate("__CLICKED"))
    }

    @Test
    fun `check to climb up to role button`() {
        page.setContent("<div role=button onclick='javascript:window.__CLICKED=true;'><div style='pointer-events:none'><span><div>Click target</div></span></div>")
        page.click("text=Click target")
        assertEquals(true, page.evaluate("__CLICKED"))
    }

    @Test
    fun `check to update modifiers correctly`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.click("button", ClickOptions { it.modifiers = listOf(KeyboardModifier.SHIFT) })
        assertEquals(true, page.evaluate("shiftKey"))
        page.click("button", ClickOptions { it.modifiers = emptyList() })
        assertEquals(false, page.evaluate("shiftKey"))

        page.keyboard().down("Shift")
        page.click("button", ClickOptions { it.modifiers = emptyList() })
        assertEquals(false, page.evaluate("shiftKey"))
        page.click("button")
        assertEquals(true, page.evaluate("shiftKey"))
        page.keyboard().up("Shift")
        page.click("button")
        assertEquals(false, page.evaluate("shiftKey"))
    }

    @Test
    fun `check to click an offscreen element when scroll behaviour is smooth`() {
        val content =
            """<div style='border: 1px solid black; height: 500px; overflow: auto; width: 500px; scroll-behavior: smooth'>
            |   <button style='margin-top: 2000px' onClick='window.clicked = true'>hi</button>
            |</div>
        """.trimMargin()
        page.setContent(content)
        page.click("button")
        assertEquals(true, page.evaluate("window.clicked"))
    }

    @Test
    fun `check to report nice error when element is detached and force clicked`() {
        page.navigate("${httpServer.prefixWithDomain}/input/animating-button.html")
        page.evaluate("addButton()")
        val handle = page.querySelector("button")
        assertNotNull(handle)
        page.evaluate("stopButton(true)")
        try {
            handle.click(ClickOptions { it.force = true })
            fail("click should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Element is not attached to the DOM"))
            assertNull(page.evaluate("window.clicked"))
        }
    }

    @Test
    fun `check to dispatch microtasks in order`() {
        val content = """<button id=button>Click me</button>
            |<script>
            |   let mutationCount = 0;
            |   const observer = new MutationObserver((mutationsList, observer) => {
            |       for (let mutation of mutationsList)
            |           ++mutationCount;
            |   });
            |   observer.observe(document.body, { attributes: true, childList: true, subtree: true });
            |   button.addEventListener('mousedown', () => {
            |       mutationCount = 0;
            |       document.body.appendChild(document.createElement('div'));
            |   });
            |   button.addEventListener('mouseup', () => {
            |       window['result'] = mutationCount;
            |   });
            |</script>
        """.trimMargin()
        page.setContent(content)
        page.click("button")
        assertEquals(1, page.evaluate("() => window['result']"))
    }

    @Test
    fun `check to click the button when window inner width is corrupted`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.evaluate("() => Object.defineProperty(window, 'innerWidth', {value: 0})")
        page.click("button")
        assertEquals("Clicked", page.evaluate("result"))
    }
}