package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.ExposeBindingOptions
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForNavigationOptions
import io.github.tmcreative1.playwright.remote.core.enums.WaitUntilState
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.callback.api.IBindingCallback
import io.github.tmcreative1.playwright.remote.engine.handle.js.api.IJSHandle
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TestPageExposeFunction : BaseTest() {

    @Test
    fun `check correct work of expose function`() {
        val context = browser.newContext()
        val page = context.newPage()
        val bindingSource = arrayListOf<IBindingCallback.ISource?>(null)
        page.exposeBinding("add") { source, args ->
            bindingSource[0] = source
            args[0] as Int + args[1] as Int
        }
        val jsScript = """async function() {
            |   return window['add'](10, 13);
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(context, bindingSource[0]!!.context())
        assertEquals(page, bindingSource[0]!!.page())
        assertEquals(page.mainFrame(), bindingSource[0]!!.frame())
        assertEquals(23, result)
        context.close()
    }

    @Test
    fun `check correct work of expose function with await`() {
        val jsScript = """async function() {
            |   return await window['compute'](2, 12);
            |}
        """.trimMargin()
        page.exposeBinding("compute") { _, args -> args[0] as Int * args[1] as Int }
        val result = page.evaluate(jsScript)
        assertEquals(24, result)
    }

    @Test
    fun `check correct work with handles and complex objects`() {
        var jsScript = """() => {
            |   window['fooValue'] = { bar: 2 };
            |   return window['fooValue'];
            |}
        """.trimMargin()
        val handle = page.evaluateHandle(jsScript)
        page.exposeFunction("handle") { arrayListOf(mapOf("foo" to handle)) }
        jsScript = """async function() {
            |   const value = await window['handle']();
            |   const [{ foo }] = value;
            |   return foo === window['fooValue'];
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(true, result)
    }

    @Test
    fun `check to throw exception in page context`() {
        page.exposeFunction("callException") {
            throw RuntimeException("Exception called")
        }
        val jsScript = """async () => {
            |   try {
            |       await window["callException"]();
            |   } catch (e) {
            |       return { message: e.message, stack: e.stack };
            |   }
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertTrue(result is Map<*, *>)
        assertEquals("Exception called", result["message"])
        assertTrue((result["stack"] as String).contains("check to throw exception in page context"))
    }

    @Test
    fun `check of call inside function`() {
        val called = arrayListOf(false)
        page.exposeFunction("callMe") {
            called[0] = true
            called
        }
        page.addInitScript("window['callMe']()")
        page.reload()
        assertTrue(called[0])
    }

    @Test
    fun `check correct work with navigation`() {
        page.exposeFunction("compute") { args -> args[0] as Int * args[1] as Double }
        val jsScript = """async function() {
            |   return await window['compute'](10, 2.3);
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        page.navigate(httpServer.emptyPage)
        assertEquals(23, result)
    }

    @Test
    fun `check correct work with frames`() {
        page.exposeFunction("compute") { args -> args[0] as Int + args[1] as Int }
        page.navigate("${httpServer.prefixWithDomain}/frames/nested-frames.html")
        val frame = page.frames()[1]
        val jsScript = """async function() {
            |   return window['compute'](20, 3);
            |}
        """.trimMargin()
        val result = frame.evaluate(jsScript)
        assertEquals(23, result)
    }

    @Test
    fun `check correct work with frame before navigation`() {
        page.navigate("${httpServer.prefixWithDomain}/frames/nested-frames.html")
        page.exposeFunction("compute") { args -> args[0] as Int * args[1] as Double }
        val frame = page.frames()[1]
        val jsScript = """async function() {
            |   return window['compute'](10, 2.3);
            |}
        """.trimMargin()
        val result = frame.evaluate(jsScript)
        assertEquals(23, result)
    }

    @Test
    fun `check correct work after cross origin navigation`() {
        page.navigate(httpServer.emptyPage)
        page.exposeFunction("compute") { args -> args[0] as Int + args[1] as Int }

        page.navigate("${httpServer.prefixWithIP}/empty.html")
        val result = page.evaluate("window['compute'](3, 20)")
        assertEquals(23, result)
    }

    @Test
    fun `check correct work with complex objects`() {
        page.exposeFunction("complexObject") { args ->
            val a = args[0] as Map<*, *>
            val b = args[1] as Map<*, *>
            val sum = a["x"] as Int + b["x"] as Int
            mapOf("x" to sum)
        }
        val result = page.evaluate("async () => window['complexObject']({x: 18}, {x: 5})")
        assertTrue(result is Map<*, *>)
        assertEquals(23, result["x"])
    }

    @Test
    fun `check handle of expose binding`() {
        val target = arrayListOf<IJSHandle?>(null)
        page.exposeBinding("logMe", { _, args ->
            target[0] = args[0] as IJSHandle
            21
        }, ExposeBindingOptions { it.handle = true })
        val jsScript = """async function() {
            |   return window['logMe']({ age: 23 });
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(23, target[0]!!.evaluate("x => x.age"))
        assertEquals(21, result)
    }

    @Test
    fun `check to not throw exception during navigation`() {
        page.exposeBinding("logMe", { _, _ -> 23 }, ExposeBindingOptions { it.handle = true })
        page.navigate(httpServer.emptyPage)

        page.waitForNavigation(WaitForNavigationOptions { it.waitUntil = WaitUntilState.LOAD }) {
            val jsScript = """async url => {
                |   window['logMe']({ foo: 24 });
                |   window.location.href = url;
                |}
            """.trimMargin()
            page.evaluate(jsScript, "${httpServer.prefixWithDomain}/page-with-one-style.html")
        }
    }

    @Test
    fun `check to throw for duplicate registrations`() {
        page.exposeFunction("fun1") { "response" }
        try {
            page.exposeFunction("fun1") { "response" }
            fail("exposeFunction should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Function fun1 has been already registered"))
        }
    }

    @Test
    fun `check to throw for multiple arguments`() {
        var jsScript = """async function() {
            |   return window['logMe']({ age: 23 });
            |}
        """.trimMargin()
        page.exposeBinding("logMe", { _, _ -> 23 }, ExposeBindingOptions { it.handle = true })
        assertEquals(23, page.evaluate(jsScript))
        jsScript = """async function() {
            |   return window['logMe']({ age: 23 }, undefined, undefined);
            |}
        """.trimMargin()
        assertEquals(23, page.evaluate(jsScript))
        jsScript = """async function() {
            |   return window['logMe'](undefined, undefined, undefined);
            |}
        """.trimMargin()
        assertEquals(23, page.evaluate(jsScript))
        try {
            jsScript = """async function() {
                |   return window['logMe'](1, 2);
                |}
            """.trimMargin()
            page.evaluate(jsScript)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("exposeBindingHandle supports a single argument, 2 received"))
        }
    }
}