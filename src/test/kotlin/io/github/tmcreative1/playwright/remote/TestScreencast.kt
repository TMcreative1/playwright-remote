package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.engine.options.RecordVideoSize
import io.github.tmcreative1.playwright.remote.engine.options.ViewportSize
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestScreencast : BaseTest() {

    @Test
    fun `check to save as video`(@TempDir videosDir: Path) {
        val pg: IPage
        browser.newContext(NewContextOptions {
            it.recordVideoDir = videosDir
            it.recordVideoSize = RecordVideoSize { opt ->
                opt.width = 320
                opt.height = 240
            }
            it.viewportSize = ViewportSize { opt ->
                opt.width = 320
                opt.height = 240
            }
        }).use {
            pg = it.newPage()
            pg.evaluate("() => document.body.style.backgroundColor = 'red'")
            pg.waitForTimeout(1000.0)
        }
        val saveAsPath = videosDir.resolve("my-video.webm")
        val video = pg.video()
        assertNotNull(video)
        video.saveAs(saveAsPath)
        assertTrue(Files.exists(saveAsPath))
    }

    @Test
    fun `check to throw error when no video frames`(@TempDir videosDir: Path) {
        browser.newContext(NewContextOptions {
            it.recordVideoDir = videosDir
            it.recordVideoSize = RecordVideoSize { opt ->
                opt.width = 320
                opt.height = 240
            }
            it.viewportSize = ViewportSize { opt ->
                opt.width = 320
                opt.height = 240
            }
        }).use {
            val pg = it.newPage()
            val popup = it.waitForPage {
                val jsScript = """() => {
                    |   const win = window.open('about:blank');
                    |   win.close();
                    |}
                """.trimMargin()
                pg.evaluate(jsScript)
            }
            assertNotNull(popup)
            pg.close()
            val saveAsPath = videosDir.resolve("my-video.webm")
            try {
                val video = popup.video()
                assertNotNull(video)
                video.saveAs(saveAsPath)
                if (isWebkit()) {
                    assertTrue(Files.exists(saveAsPath))
                } else {
                    fail("should throw")
                }
            } catch (e: PlaywrightException) {
                assertTrue(e.message!!.contains("Page did not produce any video frames"))
            }
        }
    }

    @Test
    fun `check to delete video`(@TempDir videosDir: Path) {
        val pg: IPage
        browser.newContext(NewContextOptions {
            it.recordVideoDir = videosDir
            it.recordVideoSize = RecordVideoSize { opt ->
                opt.width = 320
                opt.height = 240
            }
            it.viewportSize = ViewportSize { opt ->
                opt.width = 320
                opt.height = 240
            }
        }).use {
            pg = it.newPage()
            pg.evaluate("() => document.body.style.backgroundColor = 'red'")
            pg.waitForTimeout(1000.0)
        }
        val video = pg.video()
        val saveAsPath = videosDir.resolve("my-video.webm")
        assertNotNull(video)
        video.saveAs(saveAsPath)
        assertTrue(Files.exists(saveAsPath))
        video.delete()
        assertFalse(Files.exists(saveAsPath))
    }
}