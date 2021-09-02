package io.github.tmcreative1.playwright.remote.engine.video.impl

import io.github.tmcreative1.playwright.remote.engine.browser.impl.BrowserContext
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import io.github.tmcreative1.playwright.remote.engine.page.impl.Page
import io.github.tmcreative1.playwright.remote.engine.video.api.IVideo
import java.nio.file.Path

class Video(private val page: IPage) : IVideo {
    private var fullPath: Path? = null

    override fun path(): Path? {
        while (fullPath == null) {
            (page as Page).messageProcessor.processMessage()
        }
        return fullPath
    }

    override fun setRelativePath(path: String) {
        fullPath = (page.context() as BrowserContext).videosDir!!.resolve(path)
    }
}