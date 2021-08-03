package com.playwright.remote.engine.video.impl

import com.playwright.remote.engine.browser.impl.BrowserContext
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.page.impl.Page
import com.playwright.remote.engine.video.api.IVideo
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