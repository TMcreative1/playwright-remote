package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.core.exceptions.TimeoutException
import io.github.tmcreative1.playwright.remote.domain.file.FilePayload
import io.github.tmcreative1.playwright.remote.engine.filechooser.api.IFileChooser
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForFileChooserOptions
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.io.path.Path
import kotlin.test.*

class TestPageSetInputFiles : BaseTest() {
    private val fileToUpload = Path("src/test/resources/file-to-upload.txt")
    private val fileName = "file-to-upload.txt"

    @Test
    fun `check to upload the file`() {
        page.navigate("${httpServer.prefixWithDomain}/input/fileupload.html")
        val input = page.querySelector("input")
        assertNotNull(input)
        input.setInputFiles(fileToUpload)
        assertEquals(fileName, page.evaluate("e => e.files[0].name", input))
        val jsScript = """e => {
            |   const reader = new FileReader();
            |   const promise = new Promise(fulfill => reader.onload = fulfill);
            |   reader.readAsText(e.files[0]);
            |   return promise.then(() => reader.result);
            |}
        """.trimMargin()
        assertEquals("content of file", page.evaluate(jsScript, input))
    }

    @Test
    fun `check correct work of upload file`() {
        page.setContent("<input type=file>")
        page.setInputFiles("input", fileToUpload)
        assertEquals(1, page.evalOnSelector("input", "input => input.files.length"))
        assertEquals(fileName, page.evalOnSelector("input", "input => input.files[0].name"))
    }

    @Test
    fun `check set from memory`() {
        page.setContent("<input type=file>")
        page.setInputFiles("input", FilePayload {
            it.name = "text.txt"
            it.mimeType = "text/plain"
            it.buffer = "test".toByteArray()
        })
        assertEquals(1, page.evalOnSelector("input", "input => input.files.length"))
        assertEquals("text.txt", page.evalOnSelector("input", "input => input.files[0].name"))
    }

    @Test
    fun `check to emit event once`() {
        page.setContent("<input type=file>")
        val chooser = page.waitForFileChooser {
            page.click("input")
        }
        assertNotNull(chooser)
    }

    @Test
    fun `check to emit event add listener and remove listener`() {
        page.setContent("<input type=file>")
        val chooser: Array<IFileChooser?> = arrayOf(null)
        page.onFileChooser(object : ((IFileChooser) -> Unit) {
            override fun invoke(p1: IFileChooser) {
                chooser[0] = p1
                page.offFileChooser(this)
            }
        })
        page.click("input")
        val start = Instant.now()
        while (chooser[0] == null && Duration.between(start, Instant.now()).toMillis() < 10_000) {
            page.waitForTimeout(100.0)
        }
        assertNotNull(chooser[0])
    }

    @Test
    fun `check correct work when file input is attached to DOM`() {
        page.setContent("<input type=file>")
        val chooser = page.waitForFileChooser { page.click("input") }
        assertNotNull(chooser)
    }

    @Test
    fun `check correct work when file input is not attached to DOM`() {
        val chooser = page.waitForFileChooser {
            val jsScript = """() => {
                |   const el = document.createElement('input');
                |   el.type = 'file';
                |   el.click();
                |}
            """.trimMargin()
            page.evaluate(jsScript)
        }
        assertNotNull(chooser)
    }

    @Test
    fun `check correct work input file with CSP`() {
        httpServer.setCSP("/empty.html", "default-src 'none'")
        page.navigate(httpServer.emptyPage)
        page.setContent("<input type=file>")
        page.setInputFiles("input", fileToUpload)
        assertEquals(1, page.evalOnSelector("input", "input => input.files.length"))
        assertEquals(fileName, page.evalOnSelector("input", "input => input.files[0].name"))
    }

    @Test
    fun `check correct work with timeout`() {
        try {
            page.waitForFileChooser(WaitForFileChooserOptions { it.timeout = 1.0 }) {}
            fail("waitForFileChooser should throw")
        } catch (e: TimeoutException) {
            assertTrue(e.message!!.contains("Timeout 1 ms exceeded"))
        }
    }

    @Test
    fun `check correct work without custom timeout`() {
        page.setDefaultTimeout(1.0)
        try {
            page.waitForFileChooser {}
            fail("waitForFileChooser should throw")
        } catch (e: TimeoutException) {
            assertTrue(e.message!!.contains("Timeout 1 ms exceeded"))
        }
    }

    @Test
    fun `check correct work of prioritizing timeout`() {
        page.setDefaultTimeout(0.0)
        try {
            page.waitForFileChooser(WaitForFileChooserOptions { it.timeout = 1.0 }) {}
            fail("waitForFileChooser should throw")
        } catch (e: TimeoutException) {
            assertTrue(e.message!!.contains("Timeout 1 ms exceeded"))
        }
    }

    @Test
    fun `check correct work without timeout`() {
        val fileChooser = page.waitForFileChooser(WaitForFileChooserOptions { it.timeout = 0.0 }) {
            val jsScript = """() => setTimeout(() => {
                |   const el = document.createElement('input');
                |   el.type = 'file';
                |   el.click();
                |}, 50)
            """.trimMargin()
            page.evaluate(jsScript)
        }
        assertNotNull(fileChooser)
    }

    @Test
    fun `check to return the same file chooser when there are many waiters`() {
        page.setContent("<input type=file>")
        val fileChooser: Array<IFileChooser?> = arrayOf(null)
        val fileChooser1 = page.waitForFileChooser {
            fileChooser[0] = page.waitForFileChooser {
                page.evalOnSelector("input", "input => input.click()")
            }
        }
        assertEquals(fileChooser[0], fileChooser1)
    }

    @Test
    fun `check to accept single file`() {
        page.setContent("<input type=file oninput='javascript:console.timeStamp()'>")
        val fileChooser = page.waitForFileChooser { page.click("input") }
        assertNotNull(fileChooser)
        assertEquals(page, fileChooser.page())
        assertNotNull(fileChooser.element())
        fileChooser.setFiles(fileToUpload)
        assertEquals(1, page.evalOnSelector("input", "input => input.files.length"))
        assertEquals(fileName, page.evalOnSelector("input", "input => input.files[0].name"))
    }

    @Test
    fun `check to able to read selected file`() {
        page.setContent("<input type=file>")
        page.onFileChooser {
            it.setFiles(fileToUpload)
        }
        val jsScript = """async picker => {
            |   picker.click();
            |   await new Promise(x => picker.oninput = x);
            |   const reader = new FileReader();
            |   const promise = new Promise(fulfill => reader.onload = fulfill);
            |   reader.readAsText(picker.files[0]);
            |   return promise.then(() => reader.result);
            |}
        """.trimMargin()
        val content = page.evalOnSelector("input", jsScript)
        assertEquals("content of file", content)
    }

    @Test
    fun `check to able to reset selected files with empty file list`() {
        page.setContent("<input type=file>")
        page.onFileChooser(object : ((IFileChooser) -> Unit) {
            override fun invoke(p1: IFileChooser) {
                p1.setFiles(fileToUpload)
                page.offFileChooser(this)
            }
        })
        val jsScript = """async picker => {
            |   picker.click();
            |   await new Promise(x => picker.oninput = x);
            |   return picker.files.length;
            |}
        """.trimMargin()
        val fileLength = page.evalOnSelector("input", jsScript)
        assertEquals(1, fileLength)

        page.onFileChooser(object : ((IFileChooser) -> Unit) {
            override fun invoke(p1: IFileChooser) {
                p1.setFiles(arrayOf<Path>())
                page.offFileChooser(this)
            }
        })
        val fileLength2 = page.evalOnSelector("input", jsScript)
        assertEquals(0, fileLength2)
    }

    @Test
    fun `check to not accept multiple files for single file input`() {
        page.setContent("<input type=file>")
        val fileChooser = page.waitForFileChooser { page.click("input") }
        assertNotNull(fileChooser)
        try {
            fileChooser.setFiles(arrayOf(fileToUpload, Path("src/test/resources/playwright.png")))
            fail("setFile should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Non-multiple file input can only accept single file"))
        }
    }

    @Test
    fun `check to emit input and change events`() {
        val events = arrayListOf<Any>()
        page.exposeFunction("eventHandled") { args -> events.add(args[0]) }
        val content = """<input id=input type=file></input>
            |   <script>
            |       input.addEventListener('input', e => eventHandled(e.type));
            |       input.addEventListener('change', e => eventHandled(e.type));
            |   </script>
        """.trimMargin()
        page.setContent(content)
        val input = page.querySelector("input")
        assertNotNull(input)
        input.setInputFiles(fileToUpload)
        assertEquals(listOf<Any>("input", "change"), events)
    }

    @Test
    fun `check correct work for single file pick`() {
        page.setContent("<input type=file>")
        val fileChooser = page.waitForFileChooser { page.click("input") }
        assertNotNull(fileChooser)
        assertFalse(fileChooser.isMultiple())
    }

    @Test
    fun `check corret work for multiple files`() {
        page.setContent("<input multiple type=file>")
        val fileChooser = page.waitForFileChooser { page.click("input") }
        assertNotNull(fileChooser)
        assertTrue(fileChooser.isMultiple())
    }

    @Test
    fun `check correct work for webkit directory`() {
        page.setContent("<input multiple webkitdirectory type=file>")
        val fileChooser = page.waitForFileChooser { page.click("input") }
        assertNotNull(fileChooser)
        assertTrue(fileChooser.isMultiple())
    }
}