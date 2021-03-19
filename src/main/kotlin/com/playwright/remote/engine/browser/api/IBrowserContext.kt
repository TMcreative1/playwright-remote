package com.playwright.remote.playwright.browser.api

import com.playwright.remote.playwright.page.api.IPage

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
}