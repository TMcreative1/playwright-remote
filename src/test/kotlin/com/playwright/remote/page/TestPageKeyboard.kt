package com.playwright.remote.page

import com.playwright.remote.base.BaseTest
import com.playwright.remote.engine.handle.js.api.IJSHandle
import jdk.jfr.Description
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import kotlin.test.*

class TestPageKeyboard : BaseTest() {
    @Test
    fun `check to type into a textarea`() {
        val jsScript = """() => {
            |   const textarea = document.createElement('textarea');
            |   document.body.appendChild(textarea);
            |   textarea.focus();
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        val text = "Hello world. I am the next that was typed!"
        page.keyboard().type(text)
        val result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(text, result)
    }

    @Test
    fun `check to move with the arrow keys`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        var text = "Age 23"
        page.type("textarea", text)
        var result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(text, result)

        for (i in 0 until "23".length) {
            page.keyboard().press("ArrowLeft")
        }
        page.keyboard().type("inserted ")
        text = "Age inserted 23"
        result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(text, result)

        page.keyboard().down("Shift")
        for (i in 0 until "inserted ".length) {
            page.keyboard().press("ArrowLeft")
        }
        page.keyboard().up("Shift")
        page.keyboard().press("Backspace")
        text = "Age 23"
        result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(text, result)
    }

    @Test
    fun `check to send a character by element handle press`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val textarea = page.querySelector("textarea")
        assertNotNull(textarea)
        val character = "a"
        textarea.press(character)
        var result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(character, result)

        page.evaluate("() => window.addEventListener('keydown', e => e.preventDefault(), true)")
        textarea.press("b")
        result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(character, result)
    }

    @Test
    fun `check to send a character by insert text`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.focus("textarea")
        var character = "嗨"
        page.keyboard().insertText(character)
        var result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(character, result)

        page.evaluate("() => window.addEventListener('keydown', e => e.preventDefault(), true)")
        page.keyboard().insertText("a")
        character += "a"
        result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(character, result)
    }

    @Test
    fun `check to insert text should only emit input event`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.focus("textarea")
        val jsScript = """() => {
            |   const events = [];
            |   document.addEventListener('keydown', e => events.push(e.type));
            |   document.addEventListener('keyup', e => events.push(e.type));
            |   document.addEventListener('keypress', e => events.push(e.type));
            |   document.addEventListener('input', e => events.push(e.type));
            |   return events;
            |}
        """.trimMargin()
        val events = page.evaluateHandle(jsScript)
        page.keyboard().insertText("Hello!")
        assertEquals(arrayListOf("input"), events.jsonValue())
    }

    @Test
    fun `check to report shift key`() {
        // Don't test on MacOs Firefox
        Assumptions.assumeFalse(isFirefox() && isMac())
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        val keyboard = page.keyboard()
        val codeKey = hashMapOf(
            "Shift" to 16,
            "Alt" to 18,
            "Control" to 17
        )
        codeKey.forEach {
            keyboard.down(it.key)
            var expectedValue = "Keydown: ${it.key} ${it.key}Left ${it.value} [${it.key}]"
            var result = page.evaluate("getResult()")
            assertEquals(expectedValue, result)

            keyboard.down("!")
            expectedValue = "Keydown: ! Digit1 49 [${it.key}]"
            result = page.evaluate("getResult()")
            if ("Shift" == it.key) {
                expectedValue += "\nKeypress: ! Digit1 33 33 [${it.key}]"
            }
            assertEquals(expectedValue, result)

            keyboard.up("!")
            expectedValue = "Keyup: ! Digit1 49 [${it.key}]"
            result = page.evaluate("getResult()")
            assertEquals(expectedValue, result)

            keyboard.up(it.key)
            expectedValue = "Keyup: ${it.key} ${it.key}Left ${it.value} []"
            result = page.evaluate("getResult()")
            assertEquals(expectedValue, result)
        }
    }

    @Test
    fun `check to report multiple modifiers`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        val keyboard = page.keyboard()
        keyboard.down("Control")
        var expectedResult = "Keydown: Control ControlLeft 17 [Control]"
        var result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        keyboard.down("Alt")
        expectedResult = "Keydown: Alt AltLeft 18 [Alt Control]"
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        keyboard.down(";")
        expectedResult = "Keydown: ; Semicolon 186 [Alt Control]"
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        keyboard.up(";")
        expectedResult = "Keyup: ; Semicolon 186 [Alt Control]"
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        keyboard.up("Control")
        expectedResult = "Keyup: Control ControlLeft 17 [Alt]"
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        keyboard.up("Alt")
        expectedResult = "Keyup: Alt AltLeft 18 []"
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to send proper codes while typing`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().type("!")
        var expectedResult = arrayListOf(
            "Keydown: ! Digit1 49 []",
            "Keypress: ! Digit1 33 33 []",
            "Keyup: ! Digit1 49 []"
        ).joinToString(separator = "\n")
        var result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        page.keyboard().type("^")
        expectedResult = arrayListOf(
            "Keydown: ^ Digit6 54 []",
            "Keypress: ^ Digit6 94 94 []",
            "Keyup: ^ Digit6 54 []"
        ).joinToString(separator = "\n")
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to send proper codes while typing with shift`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        val keyboard = page.keyboard()
        keyboard.down("Shift")
        page.keyboard().type("~")
        val expectedResult = arrayListOf(
            "Keydown: Shift ShiftLeft 16 [Shift]",
            "Keydown: ~ Backquote 192 [Shift]",
            "Keypress: ~ Backquote 126 126 [Shift]",
            "Keyup: ~ Backquote 192 [Shift]"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to not type canceled events`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.focus("textarea")
        val jsScript = """() => {
            |   window.addEventListener('keydown', event => {
            |       event.stopPropagation();
            |       event.stopImmediatePropagation();
            |       if (event.key == 'l')
            |           event.preventDefault();
            |       if (event.key == 'o')
            |           event.preventDefault();
            |   }, false);
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        page.keyboard().type("Hello World!")
        val result = page.evalOnSelector("textarea", "textarea => textarea.value")
        assertEquals("He Wrd!", result)
    }

    @Test
    fun `check to press plus`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().press("+")
        val expectedResult = arrayListOf(
            "Keydown: + Equal 187 []",
            "Keypress: + Equal 43 43 []",
            "Keyup: + Equal 187 []"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to press shift plus`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().press("Shift++")
        val expectedResult = arrayListOf(
            "Keydown: Shift ShiftLeft 16 [Shift]",
            "Keydown: + Equal 187 [Shift]",
            "Keypress: + Equal 43 43 [Shift]",
            "Keyup: + Equal 187 [Shift]",
            "Keyup: Shift ShiftLeft 16 []"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to support plus separated modifier`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().press("Shift+~")
        val expectedResult = arrayListOf(
            "Keydown: Shift ShiftLeft 16 [Shift]",
            "Keydown: ~ Backquote 192 [Shift]",
            "Keypress: ~ Backquote 126 126 [Shift]",
            "Keyup: ~ Backquote 192 [Shift]",
            "Keyup: Shift ShiftLeft 16 []"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to support multiple plus separated modifier`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().press("Control+Shift+~")
        val expectedResult = arrayListOf(
            "Keydown: Control ControlLeft 17 [Control]",
            "Keydown: Shift ShiftLeft 16 [Control Shift]",
            "Keydown: ~ Backquote 192 [Control Shift]",
            "Keyup: ~ Backquote 192 [Control Shift]",
            "Keyup: Shift ShiftLeft 16 [Control]",
            "Keyup: Control ControlLeft 17 []"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to shift raw codes`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().press("Shift+Digit3")
        val expectedResult = arrayListOf(
            "Keydown: Shift ShiftLeft 16 [Shift]",
            "Keydown: # Digit3 51 [Shift]",
            "Keypress: # Digit3 35 35 [Shift]",
            "Keyup: # Digit3 51 [Shift]",
            "Keyup: Shift ShiftLeft 16 []"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to specify repeat property`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.focus("textarea")
        val lastEvent = captureLastKeyDown()
        page.keyboard().down("a")
        assertEquals(false, lastEvent.evaluate("e => e.repeat"))

        page.keyboard().press("a")
        assertEquals(true, lastEvent.evaluate("e => e.repeat"))

        page.keyboard().down("b")
        assertEquals(false, lastEvent.evaluate("e => e.repeat"))

        page.keyboard().press("b")
        assertEquals(true, lastEvent.evaluate("e => e.repeat"))

        page.keyboard().up("a")
        page.keyboard().down("a")
        assertEquals(false, lastEvent.evaluate("e => e.repeat"))
    }

    @Test
    fun `check to type all kinds of characters`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.focus("textarea")
        val text = """This text goes onto two lines.
            |This is character is 嗨.
        """.trimMargin()
        page.keyboard().type(text)
        val result = page.evalOnSelector("textarea", "t => t.value")
        assertEquals(text, result)
    }

    @Test
    fun `check to specify location`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val lastEvent = captureLastKeyDown()
        val textarea = page.querySelector("textarea")
        assertNotNull(textarea)
        textarea.press("Digit5")
        assertEquals(0, lastEvent.evaluate("e => e.location"))

        textarea.press("ControlLeft")
        assertEquals(1, lastEvent.evaluate("e => e.location"))

        textarea.press("ControlRight")
        assertEquals(2, lastEvent.evaluate("e => e.location"))

        textarea.press("NumpadSubtract")
        assertEquals(3, lastEvent.evaluate("e => e.location"))
    }

    @Test
    fun `check to press enter`() {
        page.setContent("<textarea></textarea>")
        page.focus("textarea")
        val lastEvent = captureLastKeyDown()
        testEnterKey(lastEvent, "Enter", "Enter")
        testEnterKey(lastEvent, "NumpadEnter", "NumpadEnter")
        testEnterKey(lastEvent, "\n", "Enter")
        testEnterKey(lastEvent, "\n", "Enter")
    }

    private fun testEnterKey(lastEventHandle: IJSHandle, key: String, expectedCode: String) {
        page.keyboard().press(key)
        val lastEvent = lastEventHandle.jsonValue() as LinkedHashMap<*, *>
        assertEquals("Enter", lastEvent["key"], lastEvent.toString())
        assertEquals(expectedCode, lastEvent["code"], lastEvent.values.joinToString(separator = ","))

        val value = page.evalOnSelector("textarea", "t => t.value")
        assertEquals("\n", value)
        page.evalOnSelector("textarea", "t => t.value = ''")
    }

    @Test
    fun `check to throw on unknown keys`() {
        testUnknownKey("NotARealKey")
        testUnknownKey("ё")
        testUnknownKey("☺")
    }

    private fun testUnknownKey(key: String) {
        try {
            page.keyboard().press(key)
            fail("press should throw")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Unknown key: \"${key}\""))
        }
    }

    @Test
    fun `check to type emoji`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val expectedText = "👹 Tokyo street Japan 🇯🇵"
        page.type("textarea", expectedText)
        val result = page.evalOnSelector("textarea", "textarea => textarea.value")
        assertEquals(expectedText, result)
    }

    @Test
    fun `check to type emoji into an iframe`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "emoji-test", "${httpServer.prefixWithDomain}/input/textarea.html")
        val frame = page.frames()[1]
        val textarea = frame.querySelector("textarea")
        assertNotNull(textarea)
        val expectedText = "👹 Tokyo street Japan 🇯🇵"
        textarea.type(expectedText)
        val result = frame.evalOnSelector("textarea", "textarea => textarea.value")
        assertEquals(expectedText, result)
    }

    @Test
    fun `check to handle select all`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val textarea = page.querySelector("textarea")
        val expectedText = "my text"
        assertNotNull(textarea)
        textarea.type(expectedText)

        val modifier = if (isMac()) "Meta" else "Control"
        page.keyboard().down(modifier)
        page.keyboard().press("a")
        page.keyboard().up(modifier)
        page.keyboard().press("Backspace")

        val result = page.evalOnSelector("textarea", "textarea => textarea.value") as String
        assertTrue(result.isEmpty())
    }

    @Test
    fun `check to prevent select all`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val textarea = page.querySelector("textarea")
        assertNotNull(textarea)
        val expectedText = "my text"
        textarea.type(expectedText)

        val jsScript = """textarea => {
            |   textarea.addEventListener('keydown', event => {
            |       if (event.key === 'a' && (event.metaKey || event.ctrlKey))
            |           event.preventDefault();
            |   }, false);
            |}
        """.trimMargin()
        page.evalOnSelector("textarea", jsScript)
        val modifier = if (isMac()) "Meta" else "Control"
        page.keyboard().down(modifier)
        page.keyboard().press("a")
        page.keyboard().up(modifier)
        page.keyboard().press("Backspace")
        val result = page.evalOnSelector("textarea", "textarea => textarea.value")
        assertEquals(expectedText.substring(0, expectedText.lastIndex), result)
    }

    @Test
    @Description("Test only for MacOs")
    fun `check to support macOs shortcuts`() {
        Assumptions.assumeTrue(isMac())
        Assumptions.assumeFalse(isFirefox())
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val textarea = page.querySelector("textarea")
        assertNotNull(textarea)
        val expectedText = "my text"
        textarea.type(expectedText)
        page.keyboard().press("Shift+Control+Alt+KeyB")
        page.keyboard().press("Backspace")
        val result = page.evalOnSelector("textarea", "textarea => textarea.value")
        assertEquals(expectedText, result)
    }

    @Test
    fun `check to press metaKey`() {
        val lastEvent = captureLastKeyDown()
        page.keyboard().press("Meta")
        val eventData = lastEvent.jsonValue() as LinkedHashMap<*, *>
        if (isFirefox() && !isMac()) {
            assertEquals("OS", eventData["key"])
            assertFalse(eventData["metaKey"] as Boolean)
        } else {
            assertEquals("Meta", eventData["key"])
            assertTrue(eventData["metaKey"] as Boolean)
        }
        if (isFirefox()) {
            assertEquals("OSLeft", eventData["code"])
        } else {
            assertEquals("MetaLeft", eventData["code"])
        }
    }

    @Test
    fun `check correct work after a cross origin navigation for keyboard`() {
        page.navigate(httpServer.emptyPage)
        page.navigate("${httpServer.prefixWithIP}/empty.html")
        val lastEvent = captureLastKeyDown()
        page.keyboard().press("a")
        val result = lastEvent.evaluate("l => l.key")
        assertEquals("a", result)
    }

    @Test
    @Description("Test only for WebKit")
    @DisabledIfSystemProperty(named = "browser", matches = "^chromium|firefox")
    fun `check to expose key identifier in web kit`() {
        val lastEvent = captureLastKeyDown()
        val keyMap = hashMapOf(
            "ArrowUp" to "Up",
            "ArrowDown" to "Down",
            "ArrowLeft" to "Left",
            "ArrowRight" to "Right",
            "Backspace" to "U+0008",
            "Tab" to "U+0009",
            "Delete" to "U+007F",
            "a" to "U+0041",
            "b" to "U+0042",
            "F12" to "F12",
        )
        keyMap.forEach {
            page.keyboard().press(it.key)
            val result = lastEvent.evaluate("e => e.keyIdentifier")
            assertEquals(it.value, result)
        }
    }

    @Test
    fun `check to scroll by page down`() {
        page.navigate("${httpServer.prefixWithDomain}/input/scrollable.html")
        page.click("body")
        page.keyboard().press("PageDown")
        page.waitForFunction("() => scrollY > 0")
    }
}