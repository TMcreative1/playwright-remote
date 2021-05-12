package com.playwright.remote.engine.frame.api

import com.playwright.remote.engine.options.*
import com.playwright.remote.engine.options.element.HoverOptions
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
     * Returns frame's name attribute as specified in the tag.
     *
     * <p> If the name is empty, returns the id attribute instead.
     *
     * <p> <strong>NOTE:</strong> This value is calculated once when the frame is created, and will not update if the attribute is changed later.
     */
    fun name(): String

    /**
     * Returns frame's url.
     */
    fun url(): String

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
    fun navigate(url: String): IResponse? = navigate(url, NavigateOptions {})

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
    fun navigate(url: String, options: NavigateOptions): IResponse?

    /**
     * This method hovers over an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun hover(selector: String) {
        hover(selector, null)
    }

    /**
     * This method hovers over an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun hover(selector: String, options: HoverOptions?)

    /**
     * Returns {@code element.innerHTML}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun innerHTML(selector: String): String {
        return innerHTML(selector, null)
    }

    /**
     * Returns {@code element.innerHTML}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun innerHTML(selector: String, options: InnerHTMLOptions?): String

    /**
     * Returns {@code element.innerText}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun innerText(selector: String): String {
        return innerText(selector, null)
    }

    /**
     * Returns {@code element.innerText}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun innerText(selector: String, options: InnerTextOptions?): String

    /**
     * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isChecked(selector: String): Boolean {
        return isChecked(selector, null)
    }

    /**
     * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isChecked(selector: String, options: IsCheckedOptions?): Boolean

    /**
     * Returns {@code true} if the frame has been detached, or {@code false} otherwise.
     */
    fun isDetached(): Boolean

    /**
     * Returns whether the element is disabled, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isDisabled(selector: String): Boolean {
        return isDisabled(selector, null)
    }

    /**
     * Returns whether the element is disabled, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isDisabled(selector: String, options: IsDisabledOptions?): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#editable">editable</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isEditable(selector: String): Boolean {
        return isEditable(selector, null)
    }

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#editable">editable</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isEditable(selector: String, options: IsEditableOptions?): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isEnabled(selector: String): Boolean {
        return isEnabled(selector, null)
    }

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isEnabled(selector: String, options: IsEnabledOptions?): Boolean

    /**
     * Returns whether the element is hidden, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.  {@code selector} that does not match any elements
     * is considered hidden.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isHidden(selector: String): Boolean {
        return isHidden(selector, null)
    }

    /**
     * Returns whether the element is hidden, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.  {@code selector} that does not match any elements
     * is considered hidden.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isHidden(selector: String, options: IsHiddenOptions?): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>. {@code selector}
     * that does not match any elements is considered not visible.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isVisible(selector: String): Boolean {
        return isVisible(selector, null)
    }

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>. {@code selector}
     * that does not match any elements is considered not visible.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isVisible(selector: String, options: IsVisibleOptions?): Boolean
}