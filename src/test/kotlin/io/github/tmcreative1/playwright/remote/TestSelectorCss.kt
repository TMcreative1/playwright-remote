package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.engine.handle.element.api.IElementHandle
import com.playwright.remote.engine.page.api.IPage
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestSelectorCss : BaseTest() {

    @Test
    fun `check correct work with large DOM`() {
        val jsScript = """() => {
            |   let id = 0;
            |   const next = (tag) => {
            |       const e = document.createElement(tag);
            |       const eid = ++id;
            |       e.textContent = 'id' + eid;
            |       e.id = '' + eid;
            |       return e;
            |   };
            |   const generate = (depth) => {
            |       const div = next('div');
            |       const span1 = next('span');
            |       const span2 = next('span');
            |       div.appendChild(span1);
            |       div.appendChild(span2);
            |       if (depth > 0) {
            |           div.appendChild(generate(depth - 1));
            |           div.appendChild(generate(depth - 1));
            |       }
            |       return div;
            |   };
            |   document.body.appendChild(generate(12));
            |}
        """.trimMargin()
        page.evaluate(jsScript)

        val selectors = arrayListOf(
            "div div div span",
            "div > div div > span",
            "div + div div div span + span",
            "div ~ div div > span ~ span",
            "div > div > div + div > div + div > span ~ span",
            "div div div div div div div div div div span",
            "div > div > div > div > div > div > div > div > div > div > span",
            "div ~ div div ~ div div ~ div div ~ div div ~ div span",
            "span"
        )

        for (selector in selectors) {
            val counts = arrayListOf<Int>()
            counts.add(page.evalOnSelectorAll(selector, "els => els.length") as Int)
            val counts2 = arrayListOf<Int>()
            counts2.add(page.evaluate("selector => document.querySelectorAll(selector).length", selector) as Int)
            assertEquals(counts, counts2)
        }
    }

    @Test
    fun `check correct work fo open shadow roots`() {
        page.navigate("${httpServer.prefixWithDomain}/deep-shadow.html")
        assertEquals("Hello from root1", page.evalOnSelector("css=span", "e => e.textContent"))
        assertEquals("Hello from root3 #2", page.evalOnSelector("css=[attr=\"value\\ space\"]", "e => e.textContent"))
        assertEquals("Hello from root3 #2", page.evalOnSelector("css=[attr='value\\ \\space']", "e => e.textContent"))
        assertEquals("Hello from root2", page.evalOnSelector("css=div div span", "e => e.textContent"))
        assertEquals("Hello from root3 #2", page.evalOnSelector("css=div span + span", "e => e.textContent"))
        assertEquals("Hello from root3 #2", page.evalOnSelector("css=span + [attr*=\"value\"]", "e => e.textContent"))
        assertEquals(
            "Hello from root3 #2",
            page.evalOnSelector("css=[data-testid=\"foo\"] + [attr*=\"value\"]", "e => e.textContent")
        )
        assertEquals("Hello from root2", page.evalOnSelector("css=#target", "e => e.textContent"))
        assertEquals("Hello from root2", page.evalOnSelector("css=div #target", "e => e.textContent"))
        assertEquals("Hello from root2", page.evalOnSelector("css=div div #target", "e => e.textContent"))
        assertNull(page.querySelector("css=div div div #target"))
        assertEquals("Hello from root2", page.evalOnSelector("css=section > div div span", "e => e.textContent"))
        assertEquals(
            "Hello from root3 #2",
            page.evalOnSelector("css=section > div div span:nth-child(2)", "e => e.textContent")
        )
        assertNull(page.querySelector("css=section div div div div"))

        val root2 = page.querySelector("css=div div")
        assertNotNull(root2)
        assertEquals("Hello from root2", root2.evalOnSelector("css=#target", "e => e.textContent"))
        assertNull(root2.querySelector("css:light=#target"))
        val rootShadow = root2.evaluateHandle("r => r.shadowRoot")
        assertEquals(
            "Hello from root2",
            rootShadow.asElement()!!.evalOnSelector("css:light=#target", "e => e.textContent")
        )

        val root3 = page.querySelectorAll("css=div div")!![1]
        assertEquals("Hello from root3", page.evalOnSelector("text=root3", "e => e.textContent"))
        assertEquals("Hello from root3 #2", page.evalOnSelector("css=[attr*=\"value\"]", "e => e.textContent"))
        assertNull(root3.querySelector("css:light=[attr*=\"value\"]"))
    }

    @Test
    fun `check correct work with greater than combinator and spaces`() {
        page.setContent("<div foo=\"bar\" bar=\"baz\"><span></span></div>")
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"] > span", "e => e.outerHTML"))
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"]> span", "e => e.outerHTML"))
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"] >span", "e => e.outerHTML"))
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"]>span", "e => e.outerHTML"))
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"]   >    span", "e => e.outerHTML"))
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"]>    span", "e => e.outerHTML"))
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"]     >span", "e => e.outerHTML"))
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"] > span", "e => e.outerHTML"))
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"]> span", "e => e.outerHTML"))
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"] >span", "e => e.outerHTML"))
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"]>span", "e => e.outerHTML"))
        assertEquals(
            "<span></span>",
            page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"]   >    span", "e => e.outerHTML")
        )
        assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"]>    span", "e => e.outerHTML"))
        assertEquals(
            "<span></span>",
            page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"]     >span", "e => e.outerHTML")
        )
    }

    @Test
    fun `check correct work with comma separated list`() {
        page.navigate("${httpServer.prefixWithDomain}/deep-shadow.html")
        assertEquals(5, page.evalOnSelectorAll("css=span,section #root1", "els => els.length"))
        assertEquals(5, page.evalOnSelectorAll("css=section #root1, div span", "els => els.length"))
        assertEquals("root1", page.evalOnSelector("css=doesnotexist , section #root1", "e => e.id"))
        assertEquals(1, page.evalOnSelectorAll("css=doesnotexist ,section #root1", "els => els.length"))
        assertEquals(4, page.evalOnSelectorAll("css=span,div span", "els => els.length"))
        assertEquals(4, page.evalOnSelectorAll("css=span,div span,div div span", "els => els.length"))
        assertEquals(2, page.evalOnSelectorAll("css=#target,[attr=\"value\\ space\"]", "els => els.length"))
        assertEquals(
            4,
            page.evalOnSelectorAll("css=#target,[data-testid=\"foo\"],[attr=\"value\\ space\"]", "els => els.length")
        )
        assertEquals(
            4,
            page.evalOnSelectorAll(
                "css=#target,[data-testid=\"foo\"],[attr=\"value\\ space\"],span",
                "els => els.length"
            )
        )
    }

    @Test
    fun `check to keep dom order with comma separated list`() {
        page.setContent("<section><span><div><x></x><y></y></div></span></section>")
        assertEquals("SPAN,DIV", page.evalOnSelectorAll("css=span,div", "els => els.map(e => e.nodeName).join(',')"))
        assertEquals("SPAN,DIV", page.evalOnSelectorAll("css=div,span", "els => els.map(e => e.nodeName).join(',')"))
        assertEquals("DIV", page.evalOnSelectorAll("css=span div, div", "els => els.map(e => e.nodeName).join(',')"))
        assertEquals(
            "SECTION",
            page.evalOnSelectorAll("*css=section >> css=div,span", "els => els.map(e => e.nodeName).join(',')")
        )
        assertEquals(
            "DIV",
            page.evalOnSelectorAll("css=section >> *css=div >> css=x,y", "els => els.map(e => e.nodeName).join(',')")
        )
        assertEquals(
            "SPAN,DIV",
            page.evalOnSelectorAll(
                "css=section >> *css=div,span >> css=x,y",
                "els => els.map(e => e.nodeName).join(',')"
            )
        )
        assertEquals(
            "SPAN,DIV",
            page.evalOnSelectorAll("css=section >> *css=div,span >> css=y", "els => els.map(e => e.nodeName).join(',')")
        )
    }

    @Test
    fun `check correct work with comma separated list in various position`() {
        page.setContent("<section><span><div><x></x><y></y></div></span></section>")
        assertEquals(
            "X,Y",
            page.evalOnSelectorAll("css=span,div >> css=x,y", "els => els.map(e => e.nodeName).join(',')")
        )
        assertEquals("X", page.evalOnSelectorAll("css=span,div >> css=x", "els => els.map(e => e.nodeName).join(',')"))
        assertEquals("X,Y", page.evalOnSelectorAll("css=div >> css=x,y", "els => els.map(e => e.nodeName).join(',')"))
        assertEquals("X", page.evalOnSelectorAll("css=div >> css=x", "els => els.map(e => e.nodeName).join(',')"))
        assertEquals(
            "X",
            page.evalOnSelectorAll("css=section >> css=div >> css=x", "els => els.map(e => e.nodeName).join(',')")
        )
        assertEquals(
            "Y",
            page.evalOnSelectorAll(
                "css=section >> css=span >> css=div >> css=y",
                "els => els.map(e => e.nodeName).join(',')"
            )
        )
        assertEquals(
            "X,Y",
            page.evalOnSelectorAll("css=section >> css=div >> css=x,y", "els => els.map(e => e.nodeName).join(',')")
        )
        assertEquals(
            "X,Y",
            page.evalOnSelectorAll(
                "css=section >> css=div,span >> css=x,y",
                "els => els.map(e => e.nodeName).join(',')"
            )
        )
        assertEquals(
            "X,Y",
            page.evalOnSelectorAll("css=section >> css=span >> css=x,y", "els => els.map(e => e.nodeName).join(',')")
        )
    }

    @Test
    fun `check correct wotk with comma inside text`() {
        page.setContent("<span></span><div attr=\"hello,world!\"></div>")
        assertEquals(
            "<div attr=\"hello,world!\"></div>",
            page.evalOnSelector("css=div[attr=\"hello,world!\"]", "e => e.outerHTML")
        )
        assertEquals(
            "<div attr=\"hello,world!\"></div>",
            page.evalOnSelector("css=[attr=\"hello,world!\"]", "e => e.outerHTML")
        )
        assertEquals(
            "<div attr=\"hello,world!\"></div>",
            page.evalOnSelector("css=div[attr='hello,world!']", "e => e.outerHTML")
        )
        assertEquals(
            "<div attr=\"hello,world!\"></div>",
            page.evalOnSelector("css=[attr='hello,world!']", "e => e.outerHTML")
        )
        assertEquals("<span></span>", page.evalOnSelector("css=div[attr=\"hello,world!\"],span", "e => e.outerHTML"))
    }

    @Test
    fun `check correct work with attribute selectors`() {
        page.setContent("<div attr=\"hello world\" attr2=\"hello-''>>foo=bar[]\" attr3=\"] span\"><span></span></div>")
        page.evaluate("() => window['div'] = document.querySelector('div')")
        val selectors = arrayListOf(
            "[attr=\"hello world\"]",
            "[attr = \"hello world\"]",
            "[attr ~= world]",
            "[attr ^=hello ]",
            "[attr \$= world ]",
            "[attr *= \"llo wor\" ]",
            "[attr2 |= hello]",
            "[attr = \"Hello World\" i ]",
            "[attr *= \"llo WOR\"i]",
            "[attr \$= woRLD i]",
            "[attr2 = \"hello-''>>foo=bar[]\"]",
            "[attr2 \$=\"foo=bar[]\"]"
        )
        for (selector in selectors) {
            assertTrue(page.evalOnSelector(selector, "e => e === window['div']") as Boolean)
        }
        assertTrue(page.evalOnSelector("[attr*=hello] span", "e => e.parentNode === window['div']") as Boolean)
        assertTrue(page.evalOnSelector("[attr*=hello] >> span", "e => e.parentNode === window['div']") as Boolean)
        assertTrue(page.evalOnSelector("[attr3=\"] span\"] >> span", "e => e.parentNode === window['div']") as Boolean)
    }

    @Test
    fun `check to not match root after greater and then greater`() {
        page.setContent("<section><div>test</div></section>")
        val element = page.querySelector("css=section >> css=section")
        assertNull(element)
    }

    @Test
    fun `check correct work with numerical id`() {
        page.setContent("<section id=\"123\"></section>")
        val element = page.querySelector("#\\31\\32\\33")
        assertNotNull(element)
    }

    @Test
    fun `check correct work with wrong case id`() {
        page.setContent("<section id=\"Hello\"></section>")
        assertEquals("SECTION", page.evalOnSelector("#Hello", "e => e.tagName"))
        assertEquals("SECTION", page.evalOnSelector("#hello", "e => e.tagName"))
        assertEquals("SECTION", page.evalOnSelector("#HELLO", "e => e.tagName"))
        assertEquals("SECTION", page.evalOnSelector("#helLO", "e => e.tagName"))
    }

    @Test
    fun `check correct work with asterisk`() {
        page.setContent("<div id=div1></div><div id=div2><span><span></span></span></div>")
        assertEquals(7, page.evalOnSelectorAll("*", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("*#div1", "els => els.length"))
        assertEquals(6, page.evalOnSelectorAll("*:not(#div1)", "els => els.length"))
        assertEquals(5, page.evalOnSelectorAll("*:not(div)", "els => els.length"))
        assertEquals(5, page.evalOnSelectorAll("*:not(span)", "els => els.length"))
        assertEquals(0, page.evalOnSelectorAll("*:not(*)", "els => els.length"))
        assertEquals(7, page.evalOnSelectorAll("*:is(*)", "els => els.length"))
        assertEquals(6, page.evalOnSelectorAll("* *", "els => els.length"))
        assertEquals(4, page.evalOnSelectorAll("* *:not(span)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("div > *", "els => els.length"))
        assertEquals(2, page.evalOnSelectorAll("div *", "els => els.length"))
        assertEquals(6, page.evalOnSelectorAll("* > *", "els => els.length"))

        val body = page.querySelector("body")
        assertNotNull(body)
        assertEquals(4, body.evalOnSelectorAll("*", "els => els.length"))
        assertEquals(1, body.evalOnSelectorAll("*#div1", "els => els.length"))
        assertEquals(3, body.evalOnSelectorAll("*:not(#div1)", "els => els.length"))
        assertEquals(2, body.evalOnSelectorAll("*:not(div)", "els => els.length"))
        assertEquals(2, body.evalOnSelectorAll("*:not(span)", "els => els.length"))
        assertEquals(0, body.evalOnSelectorAll("*:not(*)", "els => els.length"))
        assertEquals(4, body.evalOnSelectorAll("*:is(*)", "els => els.length"))
        assertEquals(1, body.evalOnSelectorAll("div > *", "els => els.length"))
        assertEquals(2, body.evalOnSelectorAll("div *", "els => els.length"))

        assertEquals(2, body.evalOnSelectorAll("* > *", "els => els.length"))
        assertEquals(2, body.evalOnSelectorAll(":scope * > *", "els => els.length"))
        assertEquals(2, body.evalOnSelectorAll("* *", "els => els.length"))
        assertEquals(0, body.evalOnSelectorAll("* *:not(span)", "els => els.length"))
    }

    @Test
    fun `check correct work with colon nth child`() {
        page.navigate("${httpServer.prefixWithDomain}/deep-shadow.html")
        assertEquals(3, page.evalOnSelectorAll("css=span:nth-child(odd)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(even)", "els => els.length"))
        assertEquals(4, page.evalOnSelectorAll("css=span:nth-child(n+1)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(n+2)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(2n)", "els => els.length"))
        assertEquals(3, page.evalOnSelectorAll("css=span:nth-child(2n+1)", "els => els.length"))
        assertEquals(0, page.evalOnSelectorAll("css=span:nth-child(-n)", "els => els.length"))
        assertEquals(3, page.evalOnSelectorAll("css=span:nth-child(-n+1)", "els => els.length"))
        assertEquals(4, page.evalOnSelectorAll("css=span:nth-child(-n+2)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(23n+2)", "els => els.length"))
    }

    @Test
    fun `check correct work with colon not`() {
        page.navigate("${httpServer.prefixWithDomain}/deep-shadow.html")
        assertEquals(2, page.evalOnSelectorAll("css=div:not(#root1)", "els => els.length"))
        assertEquals(5, page.evalOnSelectorAll("css=body :not(span)", "els => els.length"))
        assertEquals(0, page.evalOnSelectorAll("css=div > :not(span):not(div)", "els => els.length"))
    }

    @Test
    fun `check correct work with tilde`() {
        val content = """
            <div id=div1></div>
            <div id=div2></div>
            <div id=div3></div>
            <div id=div4></div>
            <div id=div5></div>
            <div id=div6></div>
        """.trimIndent()
        page.setContent(content)

        assertEquals(1, page.evalOnSelectorAll("css=#div1 ~ div ~ #div6", "els => els.length"))
        assertEquals(4, page.evalOnSelectorAll("css=#div1 ~ div ~ div", "els => els.length"))
        assertEquals(2, page.evalOnSelectorAll("css=#div3 ~ div ~ div", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=#div4 ~ div ~ div", "els => els.length"))
        assertEquals(0, page.evalOnSelectorAll("css=#div5 ~ div ~ div", "els => els.length"))
        assertEquals(0, page.evalOnSelectorAll("css=#div3 ~ #div2 ~ #div6", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=#div3 ~ #div4 ~ #div5", "els => els.length"))
    }

    @Test
    fun `check correct work with plus`() {
        val content = """
            <section>
                <div id=div1></div>
                <div id=div2></div>
                <div id=div3></div>
                <div id=div4></div>
                <div id=div5></div>
                <div id=div6></div>
            </section>
        """.trimIndent()
        page.setContent(content)

        assertEquals(1, page.evalOnSelectorAll("css=#div1 ~ div + #div6", "els => els.length"))
        assertEquals(4, page.evalOnSelectorAll("css=#div1 ~ div + div", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=#div3 + div + div", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=#div4 ~ #div5 + div", "els => els.length"))
        assertEquals(0, page.evalOnSelectorAll("css=#div5 + div + div", "els => els.length"))
        assertEquals(0, page.evalOnSelectorAll("css=#div3 ~ #div2 + #div6", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=#div3 + #div4 + #div5", "els => els.length"))
        assertEquals(0, page.evalOnSelectorAll("css=div + #div1", "els => els.length"))
        assertEquals(4, page.evalOnSelectorAll("css=section > div + div ~ div", "els => els.length"))
        assertEquals(2, page.evalOnSelectorAll("css=section > div + #div4 ~ div", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=section:has(:scope > div + #div2)", "els => els.length"))
        assertEquals(0, page.evalOnSelectorAll("css=section:has(:scope > div + #div1)", "els => els.length"))
    }

    @Test
    fun `check correct work with spaces in colon nth child and colon not`() {
        page.navigate("${httpServer.prefixWithDomain}/deep-shadow.html")
        assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(23n +2)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(23n+ 2)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=span:nth-child( 23n + 2 )", "els => els.length"))
        assertEquals(3, page.evalOnSelectorAll("css=span:not(#root1 #target)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=span:not(:not(#root1 #target))", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=span:not(span:not(#root1 #target))", "els => els.length"))
        assertEquals(2, page.evalOnSelectorAll("css=div > :not(span)", "els => els.length"))
        assertEquals(2, page.evalOnSelectorAll("css=body :not(span, div)", "els => els.length"))
        assertEquals(5, page.evalOnSelectorAll("css=span, section:not(span, div)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("span:nth-child(23n+ 2) >> xpath=.", "els => els.length"))
    }

    @Test
    fun `check correct work with colon is`() {
        page.navigate("${httpServer.prefixWithDomain}/deep-shadow.html")
        assertEquals(1, page.evalOnSelectorAll("css=div:is(#root1)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=div:is(#root1, #target)", "els => els.length"))
        assertEquals(0, page.evalOnSelectorAll("css=div:is(span, #target)", "els => els.length"))
        assertEquals(2, page.evalOnSelectorAll("css=div:is(span, #root1 > *)", "els => els.length"))
        assertEquals(3, page.evalOnSelectorAll("css=div:is(section div)", "els => els.length"))
        assertEquals(7, page.evalOnSelectorAll("css=:is(div, span)", "els => els.length"))
        assertEquals(3, page.evalOnSelectorAll("css=section:is(section) div:is(section div)", "els => els.length"))
        assertEquals(6, page.evalOnSelectorAll("css=:is(div, span) > *", "els => els.length"))
        assertEquals(0, page.evalOnSelectorAll("css=#root1:has(:is(#root1))", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=#root1:has(:is(:scope, #root1))", "els => els.length"))
    }

    @Test
    fun `check correct work with colon has`() {
        page.navigate("${httpServer.prefixWithDomain}/deep-shadow.html")
        assertEquals(2, page.evalOnSelectorAll("css=div:has(#target)", "els => els.length"))
        assertEquals(3, page.evalOnSelectorAll("css=div:has([data-testid=foo])", "els => els.length"))
        assertEquals(2, page.evalOnSelectorAll("css=div:has([attr*=value])", "els => els.length"))
    }

    @Test
    fun `check correct work with colon scope`() {
        page.navigate("${httpServer.prefixWithDomain}/deep-shadow.html")
        assertEquals(0, page.evalOnSelectorAll("css=div:is(:scope#root1)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=div:is(:scope #root1)", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("css=div:has(:scope > #target)", "els => els.length"))

        val handle = page.querySelector("css=span")
        assertNotNull(handle)
        for (scope in listOf(page, handle)) {
            assertEquals(1, evalOnSelectorAllBaseOnContext(scope, "css=:scope"))
            assertEquals(0, evalOnSelectorAllBaseOnContext(scope, "css=* :scope"))
            assertEquals(0, evalOnSelectorAllBaseOnContext(scope, "css=* + :scope"))
            assertEquals(0, evalOnSelectorAllBaseOnContext(scope, "css=* > :scope"))
            assertEquals(0, evalOnSelectorAllBaseOnContext(scope, "css=* ~ :scope"))
        }
    }

    private fun evalOnSelectorAllBaseOnContext(context: Any, selector: String): Int =
        when (context) {
            is IPage -> context.evalOnSelectorAll(selector, "els => els.length") as Int
            is IElementHandle -> context.evalOnSelectorAll(selector, "els => els.length") as Int
            else -> -1
        }
}