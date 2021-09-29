package io.github.tmcreative1.playwright.remote.engine.options.tracing

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class StartTracingOptions @JvmOverloads constructor(
    /**
     * If specified, the trace is going to be saved into the file with the given name inside the {@code tracesDir} folder specified
     * in {@link BrowserType#launch BrowserType.launch()}.
     */
    var name: String? = null,
    /**
     * Whether to capture screenshots during tracing. Screenshots are used to build a timeline preview.
     */
    var screenshots: Boolean? = null,
    /**
     * Whether to capture DOM snapshot on every action.
     */
    var snapshots: Boolean? = null,
    @Transient private val builder: IBuilder<StartTracingOptions>
) {
    init {
        builder.build(this)
    }
}