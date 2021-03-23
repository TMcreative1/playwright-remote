package com.playwright.remote.engine.browser.api

import com.playwright.remote.engine.options.Cookie
import com.playwright.remote.engine.options.WaitForPageOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.route.api.IRoute
import java.nio.file.Path
import java.util.regex.Pattern

interface IBrowserContext : AutoCloseable {
    /**
     * Emitted when Browser context gets closed. This might happen because of one of the following:
     * <ul>
     * <li> Browser context is closed.</li>
     * <li> Browser application is closed or crashed.</li>
     * <li> The {@link Browser#close Browser.close()} method was called.</li>
     * </ul>
     */
    fun onClose(handler: (IBrowserContext) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onClose onClose(handler)}.
     */
    fun offClose(handler: (IBrowserContext) -> Unit)

    /**
     * The event is emitted when a new Page is created in the BrowserContext. The page may still be loading. The event will
     * also fire for popup pages. See also {@link Page#onPopup Page.onPopup()} to receive events about popups relevant to a
     * specific page.
     *
     * <p> The earliest moment that page is available is when it has navigated to the initial url. For example, when opening a
     * popup with {@code window.open('http://example.com')}, this event will fire when the network request to "http://example.com" is
     * done and its response has started loading in the popup.
     * <pre>{@code
     * Page newPage = context.waitForPage(() -> {
     *   page.click("a[target=_blank]");
     * });
     * System.out.println(newPage.evaluate("location.href"));
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> Use {@link Page#waitForLoadState Page.waitForLoadState()} to wait until the page gets to a particular state (you should
     * not need it in most cases).
     */
    fun onPage(handler: (IPage) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onPage onPage(handler)}.
     */
    fun offPage(handler: (IPage) -> Unit)

    /**
     * Creates a new page in the browser context.
     */
    fun newPage(): IPage

    /**
     * Performs action and waits for a new {@code Page} to be created in the context. If predicate is provided, it passes {@code Page}
     * value into the {@code predicate} function and waits for {@code predicate(event)} to return a truthy value. Will throw an error if
     * the context closes before new {@code Page} is created.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForPage(options: WaitForPageOptions? = null, callback: () -> Unit): IPage

    /**
     * Adds cookies into this browser context. All pages within this context will have these cookies installed. Cookies can be
     * obtained via {@link BrowserContext#cookies BrowserContext.cookies()}.
     * <pre>{@code
     * browserContext.addCookies(Arrays.asList(cookieObject1, cookieObject2));
     * }</pre>
     */
    fun addCookies(cookies: List<Cookie>)

    /**
     * Adds a script which would be evaluated in one of the following scenarios:
     * <ul>
     * <li> Whenever a page is created in the browser context or is navigated.</li>
     * <li> Whenever a child frame is attached or navigated in any page in the browser context. In this case, the script is
     * evaluated in the context of the newly attached frame.</li>
     * </ul>
     *
     * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
     * the JavaScript environment, e.g. to seed {@code Math.random}.
     *
     * <p> An example of overriding {@code Math.random} before the page loads:
     * <pre>{@code
     * // In your playwright script, assuming the preload.js file is in same directory.
     * browserContext.addInitScript(Paths.get("preload.js"));
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> The order of evaluation of multiple scripts installed via {@link BrowserContext#addInitScript
     * BrowserContext.addInitScript()} and {@link Page#addInitScript Page.addInitScript()} is not defined.
     *
     * @param script Script to be evaluated in all pages in the browser context.
     */
    fun addInitScript(script: String)

    /**
     * Adds a script which would be evaluated in one of the following scenarios:
     * <ul>
     * <li> Whenever a page is created in the browser context or is navigated.</li>
     * <li> Whenever a child frame is attached or navigated in any page in the browser context. In this case, the script is
     * evaluated in the context of the newly attached frame.</li>
     * </ul>
     *
     * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
     * the JavaScript environment, e.g. to seed {@code Math.random}.
     *
     * <p> An example of overriding {@code Math.random} before the page loads:
     * <pre>{@code
     * // In your playwright script, assuming the preload.js file is in same directory.
     * browserContext.addInitScript(Paths.get("preload.js"));
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> The order of evaluation of multiple scripts installed via {@link BrowserContext#addInitScript
     * BrowserContext.addInitScript()} and {@link Page#addInitScript Page.addInitScript()} is not defined.
     *
     * @param path Script to be evaluated in all pages in the browser context.
     */
    fun addInitScript(path: Path)

    /**
     * Returns the browser instance of the context. If it was launched as a persistent context null gets returned.
     */
    fun browser(): IBrowser

    /**
     * Clears context cookies.
     */
    fun clearCookies()


    /**
     * Clears all permission overrides for the browser context.
     * <pre>{@code
     * BrowserContext context = browser.newContext();
     * context.grantPermissions(Arrays.asList("clipboard-read"));
     * // do stuff ..
     * context.clearPermissions();
     * }</pre>
     */
    fun clearPermissions()

    /**
     * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
     * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
     *
     * <p> An example of a nave handler that aborts all image requests:
     * <pre>{@code
     * BrowserContext context = browser.newContext();
     * context.route("**\/ *.{png,jpg,jpeg}", route -> route.abort());
     * Page page = context.newPage();
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> or the same snippet using a regex pattern instead:
     * <pre>{@code
     * BrowserContext context = browser.newContext();
     * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
     * Page page = context.newPage();
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
     * matches both handlers.
     *
     * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     * @param handler handler function to route the request.
     */
    fun route(url: String, handler: (IRoute) -> Unit)

    /**
     * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
     * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
     *
     * <p> An example of a naïve handler that aborts all image requests:
     * <pre>{@code
     * BrowserContext context = browser.newContext();
     * context.route("**\/'*.{png,jpg,jpeg}", route -> route.abort());
     * Page page = context.newPage();
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> or the same snippet using a regex pattern instead:
     * <pre>{@code
     * BrowserContext context = browser.newContext();
     * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
     * Page page = context.newPage();
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
     * matches both handlers.
     *
     * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     * @param handler handler function to route the request.
     */
    fun route(url: Pattern, handler: (IRoute) -> Unit)

    /**
     * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
     * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
     *
     * <p> An example of a naïve handler that aborts all image requests:
     * <pre>{@code
     * BrowserContext context = browser.newContext();
     * context.route("**\/'*.{png,jpg,jpeg}", route -> route.abort());
     * Page page = context.newPage();
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> or the same snippet using a regex pattern instead:
     * <pre>{@code
     * BrowserContext context = browser.newContext();
     * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
     * Page page = context.newPage();
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
     * matches both handlers.
     *
     * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     * @param handler handler function to route the request.
     */
    fun route(url: (String) -> Boolean, handler: (IRoute) -> Unit)
}