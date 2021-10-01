package io.github.tmcreative1.playwright.remote.engine.browser.api

import io.github.tmcreative1.playwright.remote.engine.options.tracing.StartTracingOptions
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
    fun stop() = stop(null
    )

    /**
     * Stop tracing.
     */
    fun stop(options: StopTracingOptions?)
}