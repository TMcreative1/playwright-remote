package com.playwright.remote.engine.dialog.api

interface IDialog {

    /**
     * Returns when the dialog has been accepted.
     *
     * @param promptText A text to enter in prompt. Does not cause any effects if the dialog's {@code type} is not prompt. Optional.
     */
    fun accept(promptText: String? = null)

    /**
     * If dialog is prompt, returns default prompt value. Otherwise, returns empty string.
     */
    fun defaultValue(): String

    /**
     * Returns when the dialog has been dismissed.
     */
    fun dismiss()

    /**
     * A message displayed in the dialog.
     */
    fun message(): String

    /**
     * Returns dialog's type, can be one of {@code alert}, {@code beforeunload}, {@code confirm} or {@code prompt}.
     */
    fun type(): String
}