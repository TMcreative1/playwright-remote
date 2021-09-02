package io.github.tmcreative1.playwright.remote.browser.context

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.ExposeBindingOptions
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.callback.api.IBindingCallback
import io.github.tmcreative1.playwright.remote.engine.handle.js.api.IJSHandle
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestBrowserContextExposeFunction : BaseTest() {

    @Test
    fun `check correct work of expose binding`() {
        val bindingSource = arrayListOf<IBindingCallback.ISource?>(null)
        browserContext.exposeBinding("add") { source, args ->
            bindingSource[0] = source
            args[0] as Int + args[1] as Int
        }
        val pg = browserContext.newPage()
        val result = pg.evaluate("add(20, 3)")
        assertEquals(browserContext, bindingSource[0]!!.context())
        assertEquals(pg, bindingSource[0]!!.page())
        assertEquals(pg.mainFrame(), bindingSource[0]!!.frame())
        assertEquals(23, result)
    }

    @Test
    fun `check correct work`() {
        browserContext.exposeFunction("add") { args -> args[0] as Int + args[1] as Int }
        val pg = browserContext.newPage()
        pg.exposeFunction("mul") { args -> args[0] as Int * args[1] as Int }
        browserContext.exposeFunction("sub") { args -> args[0] as Int - args[1] as Int }
        browserContext.exposeBinding("addHandle") { source, args ->
            source.frame().evaluateHandle("([a, b]) => a + b", args)
        }
        val result =
            pg.evaluate("async () => ({ mul: await mul(12, 2), add: await add(3, 20), sub: await sub(25, 10), addHandle: await addHandle(10,11) })")
        assertEquals(mapOf("mul" to 24, "add" to 23, "sub" to 15, "addHandle" to 21), result)
    }

    @Test
    fun `check to throw error for duplicate registrations`() {
        browserContext.exposeFunction("foo") { }
        browserContext.exposeFunction("bar") { }
        try {
            browserContext.exposeFunction("foo") {}
            fail("exposeFunction should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Function foo has been already registered"))
        }

        val pg = browserContext.newPage()
        try {
            pg.exposeFunction("foo") {}
            fail("exposeFunction should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Function foo has been already registered in browser context"))
        }

        pg.exposeFunction("baz") {}
        try {
            browserContext.exposeFunction("baz") {}
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Function baz has been already registered in one of the pages"))
        }
    }

    @Test
    fun `check to call from inside add init script`() {
        val actualArgs = arrayListOf<Any>()
        browserContext.exposeFunction("woof") { args -> actualArgs.add(args[0]) }
        browserContext.addInitScript("window['woof']('context')")
        val pg = browserContext.newPage()
        pg.evaluate("undefined")
        assertEquals(listOf<Any>("context"), actualArgs)
        actualArgs.clear()
        pg.addInitScript("window['woof']('page')")
        pg.reload()
        assertEquals(listOf<Any>("context", "page"), actualArgs)
    }

    @Test
    fun `check to expose binding handle`() {
        val target = arrayListOf<IJSHandle?>(null)
        browserContext.exposeBinding("logme",
            { _, args ->
                target[0] = args[0] as IJSHandle
                23
            }, ExposeBindingOptions { it.handle = true })
        val pg = browserContext.newPage()
        val jsScript = """async function() {
            |   return window['logme']({ foo: 46 });
            |}
        """.trimMargin()
        val result = pg.evaluate(jsScript)
        assertNotNull(target[0])
        assertEquals(46, target[0]!!.evaluate("x => x.foo"))
        assertEquals(23, result)
    }
}