package io.github.tmcreative1.playwright.remote.engine.filechooser.api

import io.github.tmcreative1.playwright.remote.domain.file.FilePayload
import io.github.tmcreative1.playwright.remote.engine.handle.element.api.IElementHandle
import io.github.tmcreative1.playwright.remote.engine.options.SetFilesOptions
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import java.nio.file.Path

interface IFileChooser {
    /**
     * Returns input element associated with this file chooser.
     */
    fun element(): IElementHandle

    /**
     * Returns whether this file chooser accepts multiple files.
     */
    fun isMultiple(): Boolean

    /**
     * Returns page this file chooser belongs to.
     */
    fun page(): IPage

    /**
     * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths, then
     * they are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setFiles(files: Path) = setFiles(files, null)

    /**
     * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths, then
     * they are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setFiles(files: Path, options: SetFilesOptions?)

    /**
     * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths, then
     * they are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setFiles(files: Array<Path>) = setFiles(files, null)

    /**
     * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths, then
     * they are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setFiles(files: Array<Path>, options: SetFilesOptions?)

    /**
     * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths, then
     * they are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setFiles(files: FilePayload) = setFiles(files, null)

    /**
     * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths, then
     * they are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setFiles(files: FilePayload, options: SetFilesOptions?)

    /**
     * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths, then
     * they are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setFiles(files: Array<FilePayload>) = setFiles(files, null)

    /**
     * Sets the value of the file input this chooser is associated with. If some of the {@code filePaths} are relative paths, then
     * they are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setFiles(files: Array<FilePayload>, options: SetFilesOptions?)
}