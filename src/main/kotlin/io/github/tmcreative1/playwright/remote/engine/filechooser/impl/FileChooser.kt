package io.github.tmcreative1.playwright.remote.engine.filechooser.impl

import io.github.tmcreative1.playwright.remote.domain.file.FilePayload
import io.github.tmcreative1.playwright.remote.engine.filechooser.api.IFileChooser
import io.github.tmcreative1.playwright.remote.engine.handle.element.api.IElementHandle
import io.github.tmcreative1.playwright.remote.engine.options.SetFilesOptions
import io.github.tmcreative1.playwright.remote.engine.options.element.SetInputFilesOptions
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import io.github.tmcreative1.playwright.remote.engine.parser.IParser.Companion.convert
import io.github.tmcreative1.playwright.remote.utils.Utils.Companion.toFilePayloads
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
        element.setInputFiles(files, convert(options, SetInputFilesOptions::class.java))
    }
}