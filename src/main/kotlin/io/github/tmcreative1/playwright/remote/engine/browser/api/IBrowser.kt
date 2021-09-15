package io.github.tmcreative1.playwright.remote.engine.browser.api

import io.github.tmcreative1.playwright.remote.core.enums.DeviceDescriptors
import io.github.tmcreative1.playwright.remote.engine.browser.selector.api.ISharedSelectors
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.engine.options.NewPageOptions
import io.github.tmcreative1.playwright.remote.engine.options.StartTracingOptions
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage

interface IBrowser : AutoCloseable {

    fun onDisconnected(handler: (IBrowser) -> Unit)

    fun offDisconnected(handler: (IBrowser) -> Unit)

    fun newContext(): IBrowserContext = newContext(null)

    fun newContext(options: NewContextOptions?): IBrowserContext

    fun newPage(): IPage = newPage(null)

    fun newPage(options: NewPageOptions?): IPage = newPage(options, null)

    fun newPage(options: NewPageOptions?, device: DeviceDescriptors?): IPage

    fun name(): String

    fun contexts(): List<IBrowserContext>

    /**
     * Returns the browser version.
     */
    fun version(): String

    /**
     * Selectors can be used to install custom selector engines. See <a
     * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more information.
     */
    fun selectors(): ISharedSelectors

    /**
     * <strong>NOTE:</strong> This API controls <a href="https://www.chromium.org/developers/how-tos/trace-event-profiling-tool">Chromium Tracing</a>
     * which is a low-level chromium-specific debugging tool. API to control <a href="../trace-viewer">Playwright Tracing</a>
     * could be found <a href="https://playwright.dev/java/docs/class-tracing">here</a>.
     *
     * <p> You can use {@link Browser#startTracing Browser.startTracing()} and {@link Browser#stopTracing Browser.stopTracing()} to
     * create a trace file that can be opened in Chrome DevTools performance panel.
     * <pre>{@code
     * browser.startTracing(page, new Browser.StartTracingOptions()
     *   .setPath(Paths.get("trace.json")));
     * page.goto('https://www.google.com');
     * browser.stopTracing();
     * }</pre>
     */
    fun startTracing() = startTracing(null)

    /**
     * <strong>NOTE:</strong> This API controls <a href="https://www.chromium.org/developers/how-tos/trace-event-profiling-tool">Chromium Tracing</a>
     * which is a low-level chromium-specific debugging tool. API to control <a href="../trace-viewer">Playwright Tracing</a>
     * could be found <a href="https://playwright.dev/java/docs/class-tracing">here</a>.
     *
     * <p> You can use {@link Browser#startTracing Browser.startTracing()} and {@link Browser#stopTracing Browser.stopTracing()} to
     * create a trace file that can be opened in Chrome DevTools performance panel.
     * <pre>{@code
     * browser.startTracing(page, new Browser.StartTracingOptions()
     *   .setPath(Paths.get("trace.json")));
     * page.goto('https://www.google.com');
     * browser.stopTracing();
     * }</pre>
     *
     * @param page Optional, if specified, tracing includes screenshots of the given page.
     */
    fun startTracing(page: IPage?) = startTracing(page, null)

    /**
     * <strong>NOTE:</strong> This API controls <a href="https://www.chromium.org/developers/how-tos/trace-event-profiling-tool">Chromium Tracing</a>
     * which is a low-level chromium-specific debugging tool. API to control <a href="../trace-viewer">Playwright Tracing</a>
     * could be found <a href="https://playwright.dev/java/docs/class-tracing">here</a>.
     *
     * <p> You can use {@link Browser#startTracing Browser.startTracing()} and {@link Browser#stopTracing Browser.stopTracing()} to
     * create a trace file that can be opened in Chrome DevTools performance panel.
     * <pre>{@code
     * browser.startTracing(page, new Browser.StartTracingOptions()
     *   .setPath(Paths.get("trace.json")));
     * page.goto('https://www.google.com');
     * browser.stopTracing();
     * }</pre>
     *
     * @param page Optional, if specified, tracing includes screenshots of the given page.
     */
    fun startTracing(page: IPage?, options: StartTracingOptions?)

    /**
     * <strong>NOTE:</strong> This API controls <a href="https://www.chromium.org/developers/how-tos/trace-event-profiling-tool">Chromium Tracing</a>
     * which is a low-level chromium-specific debugging tool. API to control <a href="../trace-viewer">Playwright Tracing</a>
     * could be found <a href="https://playwright.dev/java/docs/class-tracing">here</a>.
     *
     * <p> Returns the buffer with trace data.
     */
    fun stopTracing(): ByteArray
}