package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import kotlin.test.*

class TestPageEvaluate : BaseTest() {

    @Test
    fun `check the returning result of evaluate`() {
        val result = page.evaluate("() => 5 * 2")
        assertEquals(10, result)
    }

    @Test
    fun `check transferring of nan`() {
        val result = page.evaluate("a => a", Double.NaN)
        assertTrue((result as Double).isNaN())
    }

    @Test
    fun `check transferring of 0`() {
        val result = page.evaluate("a => a", -0.0)
        assertEquals(Double.NEGATIVE_INFINITY, 1 / (result as Double))
    }

    @Test
    fun `check transferring of infinity`() {
        val result = page.evaluate("a => a", Double.POSITIVE_INFINITY)
        assertEquals(Double.POSITIVE_INFINITY, result)
    }

    @Test
    fun `check transferring of negative infinity`() {
        val result = page.evaluate("a => a", Double.NEGATIVE_INFINITY)
        assertEquals(Double.NEGATIVE_INFINITY, result)
    }

    @Test
    fun `check transferring of unserializable values`() {
        val value = mapOf<String, Any>(
            "infinity" to Double.POSITIVE_INFINITY,
            "nInfinity" to Double.NEGATIVE_INFINITY,
            "nZero" to -0.0,
            "nan" to Double.NaN
        )
        val result = page.evaluate("value => value", value)
        assertEquals(value, result)
    }

    @Test
    fun `check transferring of promise`() {
        val jsScript = "value => Promise.resolve(value)"
        var result = page.evaluate(jsScript, null)
        assertNull(result)

        result = page.evaluate(jsScript, Double.POSITIVE_INFINITY)
        assertEquals(Double.POSITIVE_INFINITY, result)

        result = page.evaluate(jsScript, -0.0)
        assertEquals(Double.NEGATIVE_INFINITY, 1 / (result as Double))
    }

    @Test
    fun `check transferring of promise with unserializable values`() {
        val value = mapOf<String, Any>(
            "infinity" to Double.POSITIVE_INFINITY,
            "nInfinity" to Double.NEGATIVE_INFINITY,
            "nZero" to -0.0,
            "nan" to Double.NaN
        )
        val result = page.evaluate("value => Promise.resolve(value)", value)
        assertEquals(value, result)
    }

    @Test
    fun `check transferring of array`() {
        val expectedList = listOf(1, 2, 3)
        val result = page.evaluate("a => a", expectedList)
        assertEquals(expectedList, result)
    }

    @Test
    fun `check transferring of array with boolean result`() {
        val result = page.evaluate("a => Array.isArray(a)", listOf(1, 2, 3))
        assertEquals(true, result)
    }

    @Test
    fun `check to modify the global environment`() {
        page.evaluate("() => window['globalVar'] = 123")
        assertEquals(123, page.evaluate("globalVar"))
    }

    @Test
    fun `check to evaluate the global environment on the page context`() {
        page.navigate("${httpServer.prefixWithDomain}/global-var.html")
        assertEquals(321, page.evaluate("globalVar"))
    }

    @Test
    fun `check to return undefined for objects with symbols`() {
        var jsScript = "() => [Symbol('foo4')]"
        assertEquals(listOf(null), page.evaluate(jsScript))
        jsScript = """() => {
            |   const a = {};
            |   a[Symbol('foo4')] = 42;
            |   return a;
            |}
        """.trimMargin()
        assertEquals(emptyMap<Any, Any>(), page.evaluate(jsScript))
        jsScript = """() => {
            |   return { foo: [{ a: Symbol('foo4') }] };
            |}
        """.trimMargin()
        assertEquals(mapOf("foo" to listOf(mapOf("a" to null))), page.evaluate(jsScript))
    }

    @Test
    fun `check to return value with unicode chars`() {
        val result = page.evaluate("a => a['中文字符']", mapOf("中文字符" to 10))
        assertEquals(10, result)
    }

    @Test
    fun `check throw error when evaluation triggers reload`() {
        try {
            val jsScript = """() => {
                |   location.reload();
                |   return new Promise(() => { });
                |}
            """.trimMargin()
            page.evaluate(jsScript)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("navigation"))
        }
    }

    @Test
    fun `check to await promise`() {
        val result = page.evaluate("() => Promise.resolve(5 * 5)")
        assertEquals(25, result)
    }

    @Test
    fun `check correct work after frame navigated`() {
        val frameEvaluation = arrayListOf<Any>(0)
        page.onFrameNavigated {
            frameEvaluation[0] = it.evaluate("() => 2 * 3")
        }
        page.navigate(httpServer.emptyPage)
        assertEquals(6, frameEvaluation[0])
    }

    @Test
    fun `check correct work after a cross origin navigation`() {
        page.navigate(httpServer.emptyPage)
        val frameEvaluation = arrayListOf<Any>(0)
        page.onFrameNavigated {
            frameEvaluation[0] = it.evaluate("() => 4 * 3")
        }
        page.navigate("${httpServer.prefixWithIP}/empty.page")
        assertEquals(12, frameEvaluation[0])
    }

    @Test
    fun `check correct work from inside an exposed function`() {
        page.exposeFunction("callController") {
            page.evaluate("({ a, b}) => a * b", mapOf("a" to it[0], "b" to it[1]))
        }
        val jsScript = """async function() {
            |   return await window['callController'](3, 6);
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(18, result)
    }

    @Test
    fun `check to reject promise with exception`() {
        try {
            page.evaluate("() => not_existing_object.property")
            fail("evaluate should throw ")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("not_existing_object"))
        }
    }

    @Test
    fun `check thrown string as the error message`() {
        try {
            page.evaluate("() => { throw 'err'; }")
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("err"))
        }
    }

    @Test
    fun `check thrown number as the error message`() {
        try {
            page.evaluate("() => { throw 404; }")
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("404"))
        }
    }

    @Test
    fun `check to return complex object`() {
        val map = mapOf<String, Any>("foo" to "bar!")
        val result = page.evaluate("a => a", map)
        assertNotSame(map, result)
        assertEquals(map, result)
    }

    @Test
    fun `check to return NaN`() {
        val result = page.evaluate("() => NaN")
        assertEquals(Double.NaN, result)
    }

    @Test
    fun `check to return 0`() {
        val result = page.evaluate("() => -0")
        assertEquals(Double.NEGATIVE_INFINITY, 1 / (result as Double))
    }

    @Test
    fun `check to return infinity`() {
        val result = page.evaluate("() => Infinity")
        assertEquals(Double.POSITIVE_INFINITY, result)
    }

    @Test
    fun `check to return negative infinity`() {
        val result = page.evaluate("() => -Infinity")
        assertEquals(Double.NEGATIVE_INFINITY, result)
    }

    @Test
    fun `check correct work with overwritten promise`() {
        var jsScript = """() => {
            |   const initialPromise = window.Promise;
            |   class Promise2 {
            |       static all(arg) {
            |           return wrap(initialPromise.all(arg));
            |       }
            |       static race(arg) {
            |           return wrap(initialPromise.race(arg));
            |       }
            |       static resolve(arg) {
            |           return wrap(initialPromise.resolve(arg));
            |       }
            |       constructor(f) {
            |           this._promise = new initialPromise(f);
            |       }
            |       then(f, r) {
            |           return wrap(this._promise.then(f, r));
            |       }
            |       catch(f) {
            |           return wrap(this._promise.catch(f));
            |       }
            |       finally(f) {
            |           return wrap(this._promise.finally(f));
            |       }
            |   }
            |   const wrap = p => {
            |       const result = new Promise2(() => { });
            |       result._promise = p;
            |       return result;
            |   };
            |   window.Promise = Promise2;
            |   window['__Promise2'] = Promise2;
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        jsScript = """() => {
            |   const p = Promise.all([Promise.race([]), new Promise(() => { }).then(() => { })]);
            |   return p instanceof window['__Promise2'];
            |}
        """.trimMargin()
        assertEquals(true, page.evaluate(jsScript))
        assertEquals(23, page.evaluate("() => Promise.resolve(23)"))
    }

    @Test
    fun `check to serialize undefined fields`() {
        val result = page.evaluate("() => ({ a: undefined })")
        assertEquals(mapOf("a" to null), result)
    }

    @Test
    fun `check to return null`() {
        val result = page.evaluate("x => x", null)
        assertNull(result)
    }

    @Test
    fun `check to serialize null fields`() {
        val result = page.evaluate("() => ({ a: null })")
        assertEquals(mapOf("a" to null), result)
    }

    @Test
    fun `check to return undefined values for unserializable objects`() {
        val result = page.evaluate("() => window")
        assertNull(result)
    }

    @Test
    fun `check to return value for the looped object`() {
        val jsScript = """() => {
            |   const a = {};
            |   const b = { a };
            |   a.b = b;
            |   return a;
            |}
        """.trimMargin()
        assertNull(page.evaluate(jsScript))
    }

    @Test
    fun `check to able to throw a tricky error`() {
        val windowHandle = page.evaluateHandle("() => window")
        val errorText: String?
        try {
            windowHandle.jsonValue()
            fail("jsonValue should throw")
        } catch (e: PlaywrightException) {
            errorText = e.message
        }
        assertNotNull(errorText)
        try {
            val jsScript = """errorText => {
                |   throw new Error(errorText);
                |}
            """.trimMargin()
            page.evaluate(jsScript, errorText)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains(errorText))
        }
    }

    @Test
    fun `check to accept value as string`() {
        val result = page.evaluate("1 + 2")
        assertEquals(3, result)
    }

    @Test
    fun `check to accept value with semicolon`() {
        val result = page.evaluate("1 + 5;")
        assertEquals(6, result)
    }

    @Test
    fun `check to accept values with comments`() {
        val result = page.evaluate("2 + 5;\n//test comments")
        assertEquals(7, result)
    }

    @Test
    fun `check to accept element handle as an argument`() {
        page.setContent("<section>23</section>")
        val element = page.querySelector("section")
        val text = page.evaluate("e => e.textContent", element)
        assertEquals("23", text)
    }

    @Test
    fun `check to throw if the underlying element was disposed`() {
        page.setContent("<section>23</section>")
        val element = page.querySelector("section")
        assertNotNull(element)
        element.dispose()
        try {
            page.evaluate("e => e.textContent", element)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("JSHandle is disposed"))
        }
    }

    @Test
    fun `check to simulate a user gesture`() {
        val jsScript = """() => {
            |   document.body.appendChild(document.createTextNode('test'));
            |   document.execCommand('selectAll');
            |   return document.execCommand('copy');
            |}
        """.trimMargin()
        assertEquals(true, page.evaluate(jsScript))
    }

    @Test
    fun `check to throw error after navigation`() {
        try {
            val jsScript = """() => {
                |   const promise = new Promise(f => window['__resolve'] = f);
                |   window.location.reload();
                |   setTimeout(() => window['__resolve'](23), 1000);
                |   return promise;
                |}
            """.trimMargin()
            page.waitForNavigation {
                page.evaluate(jsScript)
            }
            fail("navigation should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("navigation"))
        }
    }

    @Test
    fun `check to not throw an error when evaluation does a navigation`() {
        page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        val jsScript = """() => {
            |   window.location.href = '/empty.html';
            |   return [23];
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(listOf(23), result)
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit")
    fun `check not throw an error when evaluation does a synchronous navigation and returns and object`() {
        val jsScript = """() => {
            |   window.location.reload();
            |   return { a: 23 };
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(mapOf("a" to 23), result)
    }

    @Test
    fun `check to not throw an error when evaluation does a synchronous navigation and returns undefined`() {
        val jsScript = """() => {
            |   window.location.reload();
            |   return undefined;
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertNull(result)
    }

    @Test
    fun `check to transfer 100mb of data from page to node js`() {
        val result = page.evaluate("() => Array(100* 1024 * 1024 + 1).join('a')")
        result as String
        assertEquals(100 * 1024 * 1024, result.length)
        result.forEachIndexed { index, char ->
            if ('a' != char) {
                fail("Unexpected char at position $index")
            }
        }
    }

    @Test
    fun `check to throw error with detailed information inside promise`() {
        try {
            val jsScript = """() => new Promise(() => {
                |   throw new Error('Error in promise');
                |})""".trimMargin()
            page.evaluate(jsScript)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Error in promise"))
        }
    }

    @Test
    fun `check correct work when json is set to null`() {
        val jsScript = "() => { window.JSON.stringify = null; window.JSON = null; }"
        page.evaluate(jsScript)
        val result = page.evaluate("() => ({ abc: 123 })")
        assertEquals(mapOf("abc" to 123), result)
    }

    @Test
    fun `check to await promise from popup`() {
        page.navigate(httpServer.emptyPage)
        val jsScript = """() => {
            |   const win = window.open('about:blank');
            |   return new win['Promise'](f => f(23));
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(23, result)
    }

    @Test
    fun `check correct work with new function and CSP`() {
        httpServer.setCSP("/empty.html", "script-src ${httpServer.prefixWithDomain}")
        page.navigate(httpServer.emptyPage)
        val result = page.evaluate("() => new Function('return true')()")
        assertEquals(true, result)
    }

    @Test
    fun `check correct work with non strict expression`() {
        val jsScript = """() => {
            |   y = 3.14;
            |   return y;
            |}
        """.trimMargin()
        assertEquals(3.14, page.evaluate(jsScript))
    }

    @Test
    fun `check to throw with strict expression`() {
        try {
            val jsScript = """() => {
                |   'use strict';
                |   // @ts-ignore
                |   variableY = 3.14;
                |   // @ts-ignore
                |   return variableY;
                |}
            """.trimMargin()
            page.evaluate(jsScript)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("variableY"))
        }
    }

    @Test
    fun `check not to leak utility script`() {
        assertEquals(true, page.evaluate("() => this == window"))
    }

    @Test
    fun `check not to leak handles`() {
        try {
            page.evaluate("handles.length")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("handles"))
        }
    }

    @Test
    fun `check correct work with CSP`() {
        httpServer.setCSP("/empty.html", "script-src 'self'")
        page.navigate(httpServer.emptyPage)
        assertEquals(4, page.evaluate("() => 2 + 2"))
    }

    @Test
    fun `check to evaluate exception`() {
        val jsScript = """() => {
                |   return (function functionOnStack() {
                |       return new Error('error message');
                |   })();
                |}
            """.trimMargin()
        val result = page.evaluate(jsScript)
        result as String
        assertTrue(result.contains("Error: error message"))
        assertTrue(result.contains("functionOnStack"))
    }

    @Test
    fun `check to not use json when evaluating`() {
        val result = page.evaluate("() => ({ toJSON: () => 'string', data: 'data' })")
        assertEquals(mapOf("data" to "data", "toJSON" to emptyMap<Any, Any>()), result)
    }

    @Test
    fun `check to not use json in json value`() {
        val result = page.evaluateHandle("() => ({ toJSON: () => 'string', data: 'data' })")
        assertEquals(mapOf("data" to "data", "toJSON" to emptyMap<Any, Any>()), result.jsonValue())
    }
}