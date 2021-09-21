package io.github.tmcreative1.playwright.remote.engine.browser.api

import io.github.tmcreative1.playwright.remote.engine.options.tracing.StartTracingOptions
import io.github.tmcreative1.playwright.remote.engine.options.tracing.StopChunkOptions
import io.github.tmcreative1.playwright.remote.engine.options.tracing.StopTracingOptions

/**
 * API for collecting and saving Playwright traces. Playwright traces can be opened using the Playwright CLI after
 * Playwright script runs.
 *
 * <p> Start with specifying the folder traces will be stored in:
 * <pre>{@code
 * Browser browser = chromium.launch();
 * BrowserContext context = browser.newContext();
 * context.tracing().start(new Tracing.StartOptions()
 *   .setScreenshots(true)
 *   .setSnapshots(true));
 * Page page = context.newPage();
 * page.navigate("https://playwright.dev");
 * context.tracing().stop(new Tracing.StopOptions()
 *   .setPath(Paths.get("trace.zip")));
 * }</pre>
 */
interface ITracing {
    /**
     * Start tracing.
     * <pre>{@code
     * context.tracing().start(new Tracing.StartOptions()
     *   .setScreenshots(true)
     *   .setSnapshots(true));
     * Page page = context.newPage();
     * page.navigate("https://playwright.dev");
     * context.tracing().stop(new Tracing.StopOptions()
     *   .setPath(Paths.get("trace.zip")));
     * }</pre>
     */
    fun start() = start(null)

    /**
     * Start tracing.
     * <pre>{@code
     * context.tracing().start(new Tracing.StartOptions()
     *   .setScreenshots(true)
     *   .setSnapshots(true));
     * Page page = context.newPage();
     * page.navigate("https://playwright.dev");
     * context.tracing().stop(new Tracing.StopOptions()
     *   .setPath(Paths.get("trace.zip")));
     * }</pre>
     */
    fun start(options: StartTracingOptions?)

    /**
     * Stop tracing.
     */
    fun stop() = stop(
        null
    )

    /**
     * Stop tracing.
     */
    fun stop(options: StopTracingOptions?)

    /**
     * Start a new trace chunk. If you'd like to record multiple traces on the same {@code BrowserContext}, use {@link Tracing#start
     * Tracing.start()} once, and then create multiple trace chunks with {@link Tracing#startChunk Tracing.startChunk()} and
     * {@link Tracing#stopChunk Tracing.stopChunk()}.
     * <pre>{@code
     * context.tracing().start(new Tracing.StartOptions()
     *   .setScreenshots(true)
     *   .setSnapshots(true));
     * Page page = context.newPage();
     * page.navigate("https://playwright.dev");
     *
     * context.tracing().startChunk();
     * page.click("text=Get Started");
     * // Everything between startChunk and stopChunk will be recorded in the trace.
     * context.tracing().stopChunk(new Tracing.StopChunkOptions()
     *   .setPath(Paths.get("trace1.zip")));
     *
     * context.tracing().startChunk();
     * page.navigate("http://example.com");
     * // Save a second trace file with different actions.
     * context.tracing().stopChunk(new Tracing.StopChunkOptions()
     *   .setPath(Paths.get("trace2.zip")));
     * }</pre>
     */
    fun startChunk()

    /**
     * Stop the trace chunk. See {@link Tracing#startChunk Tracing.startChunk()} for more details about multiple trace chunks.
     */
    fun stopChunk() = stopChunk(null)

    /**
     * Stop the trace chunk. See {@link Tracing#startChunk Tracing.startChunk()} for more details about multiple trace chunks.
     */
    fun stopChunk(options: StopChunkOptions?)
}