package io.github.tmcreative1.playwright.remote.engine.video.api

import java.nio.file.Path

/**
 * When browser context is created with the {@code recordVideo} option, each page has a video object associated with it.
 * <pre>{@code
 * System.out.println(page.video().path());
 * }</pre>
 */
interface IVideo {
    /**
     * Returns the file system path this video will be recorded to. The video is guaranteed to be written to the filesystem
     * upon closing the browser context.
     */
    fun path(): Path?

    fun setRelativePath(path: String)
}