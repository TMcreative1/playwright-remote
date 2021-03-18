package com.playwright.remote.playwright.options

class RecordVideoSize(
    var width: Int? = null,
    var height: Int? = null,
    fn: RecordVideoSize.() -> Unit
) {
    init {
        fn()
    }
}