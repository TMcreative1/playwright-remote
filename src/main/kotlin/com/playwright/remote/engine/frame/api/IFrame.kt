package com.playwright.remote.engine.frame.api

import com.playwright.remote.engine.options.NavigateOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.route.response.api.IResponse

/**
 * At every point of time, page exposes its current frame tree via the {@link Page#mainFrame Page.mainFrame()} and {@link
 * Frame#childFrames Frame.childFrames()} methods.
 *
 * <p> {@code Frame} object's lifecycle is controlled by three events, dispatched on the page object:
 * <ul>
 * <li> {@link Page#onFrameAttached Page.onFrameAttached()} - fired when the frame gets attached to the page. A Frame can be
 * attached to the page only once.</li>
 * <li> {@link Page#onFrameNavigated Page.onFrameNavigated()} - fired when the frame commits navigation to a different URL.</li>
 * <li> {@link Page#onFrameDetached Page.onFrameDetached()} - fired when the frame gets detached from the page.  A Frame can be
 * detached from the page only once.</li>
 * </ul>
 *
 * <p> An example of dumping frame tree:
 * <pre>{@code
 * import com.microsoft.playwright.*;
 *
 * public class Example {
 *   public static void main(String[] args) {
 *     try (Playwright playwright = Playwright.create()) {
 *       BrowserType firefox = playwright.firefox();
 *       Browser browser = firefox.launch();
 *       Page page = browser.newPage();
 *       page.navigate("https://www.google.com/chrome/browser/canary.html");
 *       dumpFrameTree(page.mainFrame(), "");
 *       browser.close();
 *     }
 *   }
 *   static void dumpFrameTree(Frame frame, String indent) {
 *     System.out.println(indent + frame.url());
 *     for (Frame child : frame.childFrames()) {
 *       dumpFrameTree(child, indent + "  ");
 *     }
 *   }
 * }
 * }</pre>
 */
interface IFrame {

    /**
     * Returns the page containing this frame.
     */
    fun page(): IPage?

    /**
     * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
     * last redirect.
     *
     * <p> {@code frame.goto} will throw an error if:
     * <ul>
     * <li> there's an SSL error (e.g. in case of self-signed certificates).</li>
     * <li> target URL is invalid.</li>
     * <li> the {@code timeout} is exceeded during navigation.</li>
     * <li> the remote server does not respond or is unreachable.</li>
     * <li> the main resource failed to load.</li>
     * </ul>
     *
     * <p> {@code frame.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404
     * "Not Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@link
     * Response#status Response.status()}.
     *
     * <p> <strong>NOTE:</strong> {@code frame.goto} either throws an error or returns a main resource response. The only exceptions are navigation to
     * {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
     *
     * <p> <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the <a
     * href="https://bugs.chromium.org/p/chromium/issues/detail?id=761295">upstream issue</a>.
     *
     * @param url URL to navigate frame to. The url should include scheme, e.g. {@code https://}.
     */
    fun navigate(url: String, options: NavigateOptions = NavigateOptions {}): IResponse?
}