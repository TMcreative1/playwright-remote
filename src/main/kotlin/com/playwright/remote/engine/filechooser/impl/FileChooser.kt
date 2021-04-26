package com.playwright.remote.engine.filechooser.impl

import com.playwright.remote.domain.file.FilePayload
import com.playwright.remote.engine.filechooser.api.IFileChooser
import com.playwright.remote.engine.handle.element.api.IElementHandle
import com.playwright.remote.engine.options.SetFilesOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.utils.Utils.Companion.toFilePayloads
import java.nio.file.Path

class FileChooser(
    private val page: IPage,
    private val element: IElementHandle,
    private val isMultiple: Boolean
) : IFileChooser {
    override fun element(): IElementHandle {
        return element
    }

    override fun isMultiple(): Boolean {
        return isMultiple
    }

    override fun page(): IPage {
        return page
    }

    override fun setFiles(files: Path, options: SetFilesOptions?) {
        setFiles(arrayOf(files), options)
    }

    override fun setFiles(files: Array<Path>, options: SetFilesOptions?) {
        setFiles(toFilePayloads(files), options)
    }

    override fun setFiles(files: FilePayload, options: SetFilesOptions?) {
        setFiles(arrayOf(files), options)
    }

    override fun setFiles(files: Array<FilePayload>, options: SetFilesOptions?) {
        TODO("Not yet implemented")
    }
}