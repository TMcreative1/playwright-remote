package com.playwright.remote.engine.frame.api

import com.playwright.remote.core.enums.LoadState
import com.playwright.remote.domain.file.FilePayload
import com.playwright.remote.engine.handle.element.api.IElementHandle
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.options.*
import com.playwright.remote.engine.options.element.*
import com.playwright.remote.engine.options.element.PressOptions
import com.playwright.remote.engine.options.element.TypeOptions
import com.playwright.remote.engine.options.wait.WaitForFunctionOptions
import com.playwright.remote.engine.options.wait.WaitForLoadStateOptions
import com.playwright.remote.engine.options.wait.WaitForNavigationOptions
import com.playwright.remote.engine.options.wait.WaitForURLOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.route.response.api.IResponse
import java.nio.file.Path
import java.util.regex.Pattern

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

    /**
     * {@code key} can specify the intended <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
     * character to generate the text for. A superset of the {@code key} values can be found <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
     *
     * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus}, {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab},
     * {@code Delete}, {@code Escape}, {@code ArrowDown}, {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code ArrowUp}, etc.
     *
     * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code ShiftLeft}.
     *
     * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
     *
     * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate different respective
     * texts.
     *
     * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with the
     * modifier, modifier is pressed and being held while the subsequent key is being pressed.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun press(selector: String, key: String) {
        press(selector, key, null)
    }

    /**
     * {@code key} can specify the intended <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
     * character to generate the text for. A superset of the {@code key} values can be found <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
     *
     * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus}, {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab},
     * {@code Delete}, {@code Escape}, {@code ArrowDown}, {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code ArrowUp}, etc.
     *
     * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code ShiftLeft}.
     *
     * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
     *
     * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate different respective
     * texts.
     *
     * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with the
     * modifier, modifier is pressed and being held while the subsequent key is being pressed.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun press(selector: String, key: String, options: PressOptions?)

    /**
     * Returns the ElementHandle pointing to the frame element.
     *
     * <p> The method finds an element matching the specified selector within the frame. See <a
     * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If no elements match the
     * selector, returns {@code null}.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun querySelector(selector: String): IElementHandle

    /**
     * Returns the ElementHandles pointing to the frame elements.
     *
     * <p> The method finds all elements matching the specified selector within the frame. See <a
     * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If no elements match the
     * selector, returns empty array.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun querySelectorAll(selector: String): List<IElementHandle>

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: String): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: String, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: IElementHandle): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: IElementHandle, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<String>): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<String>, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: SelectOption): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: SelectOption, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<IElementHandle>): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<IElementHandle>?, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<SelectOption>): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<SelectOption>?, options: SelectOptionOptions?): List<String>

    /**
     *
     *
     * @param html HTML markup to assign to the page.
     */
    fun setContent(html: String) {
        setContent(html, null)
    }

    /**
     *
     *
     * @param html HTML markup to assign to the page.
     */
    fun setContent(html: String, options: SetContentOptions?)

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Path) {
        setInputFiles(selector, files, null)
    }

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Path, options: SetInputFilesOptions?)

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Array<Path>) {
        setInputFiles(selector, files, null)
    }

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Array<Path>, options: SetInputFilesOptions?)

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: FilePayload) {
        setInputFiles(selector, files, null)
    }

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: FilePayload, options: SetInputFilesOptions?)

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Array<FilePayload>) {
        setInputFiles(selector, files, null)
    }

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Array<FilePayload>, options: SetInputFilesOptions?)

    /**
     * This method taps an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> <strong>NOTE:</strong> {@code frame.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun tap(selector: String) {
        tap(selector, null)
    }

    /**
     * This method taps an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> <strong>NOTE:</strong> {@code frame.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun tap(selector: String, options: TapOptions?)

    /**
     * Returns {@code element.textContent}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun textContent(selector: String): String {
        return textContent(selector, null)
    }

    /**
     * Returns {@code element.textContent}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun textContent(selector: String, options: TextContentOptions?): String

    /**
     * Returns the page title.
     */
    fun title(): String

    /**
     * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code frame.type} can be used to
     * send fine-grained keyboard events. To fill values in form fields, use {@link Frame#fill Frame.fill()}.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
     * <pre>{@code
     * // Types instantly
     * frame.type("#mytextarea", "Hello");
     * // Types slower, like a user
     * frame.type("#mytextarea", "World", new Frame.TypeOptions().setDelay(100));
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param text A text to type into a focused element.
     */
    fun type(selector: String, text: String) {
        type(selector, text, null)
    }

    /**
     * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code frame.type} can be used to
     * send fine-grained keyboard events. To fill values in form fields, use {@link Frame#fill Frame.fill()}.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
     * <pre>{@code
     * // Types instantly
     * frame.type("#mytextarea", "Hello");
     * // Types slower, like a user
     * frame.type("#mytextarea", "World", new Frame.TypeOptions().setDelay(100));
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param text A text to type into a focused element.
     */
    fun type(selector: String, text: String, options: TypeOptions?)

    /**
     * This method checks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
     * unchecked, this method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun uncheck(selector: String) {
        uncheck(selector, null)
    }

    /**
     * This method checks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
     * unchecked, this method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun uncheck(selector: String, options: UncheckOptions?)

    /**
     * Returns when the {@code expression} returns a truthy value, returns that value.
     *
     * <p> The {@link Frame#waitForFunction Frame.waitForFunction()} can be used to observe viewport size change:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType firefox = playwright.firefox();
     *       Browser browser = firefox.launch();
     *       Page page = browser.newPage();
     *       page.setViewportSize(50, 50);
     *       page.mainFrame().waitForFunction("window.innerWidth < 100");
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p> To pass an argument to the predicate of {@code frame.waitForFunction} function:
     * <pre>{@code
     * String selector = ".foo";
     * frame.waitForFunction("selector => !!document.querySelector(selector)", selector);
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun waitForFunction(expression: String): IJSHandle {
        return waitForFunction(expression, null)
    }

    /**
     * Returns when the {@code expression} returns a truthy value, returns that value.
     *
     * <p> The {@link Frame#waitForFunction Frame.waitForFunction()} can be used to observe viewport size change:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType firefox = playwright.firefox();
     *       Browser browser = firefox.launch();
     *       Page page = browser.newPage();
     *       page.setViewportSize(50, 50);
     *       page.mainFrame().waitForFunction("window.innerWidth < 100");
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p> To pass an argument to the predicate of {@code frame.waitForFunction} function:
     * <pre>{@code
     * String selector = ".foo";
     * frame.waitForFunction("selector => !!document.querySelector(selector)", selector);
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun waitForFunction(expression: String, arg: Any?): IJSHandle {
        return waitForFunction(expression, arg, null)
    }

    /**
     * Returns when the {@code expression} returns a truthy value, returns that value.
     *
     * <p> The {@link Frame#waitForFunction Frame.waitForFunction()} can be used to observe viewport size change:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType firefox = playwright.firefox();
     *       Browser browser = firefox.launch();
     *       Page page = browser.newPage();
     *       page.setViewportSize(50, 50);
     *       page.mainFrame().waitForFunction("window.innerWidth < 100");
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p> To pass an argument to the predicate of {@code frame.waitForFunction} function:
     * <pre>{@code
     * String selector = ".foo";
     * frame.waitForFunction("selector => !!document.querySelector(selector)", selector);
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun waitForFunction(expression: String, arg: Any?, options: WaitForFunctionOptions?): IJSHandle

    /**
     * Waits for the required load state to be reached.
     *
     * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been committed
     * when this method is called. If current document has already reached the required state, resolves immediately.
     * <pre>{@code
     * frame.click("button"); // Click triggers navigation.
     * frame.waitForLoadState(); // Waits for "load" state by default.
     * }</pre>
     */
    fun waitForLoadState() {
        waitForLoadState(null)
    }

    /**
     * Waits for the required load state to be reached.
     *
     * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been committed
     * when this method is called. If current document has already reached the required state, resolves immediately.
     * <pre>{@code
     * frame.click("button"); // Click triggers navigation.
     * frame.waitForLoadState(); // Waits for "load" state by default.
     * }</pre>
     *
     * @param state Optional load state to wait for, defaults to {@code load}. If the state has been already reached while loading current
     * document, the method resolves immediately. Can be one of:
     * <ul>
     * <li> {@code "load"} - wait for the {@code load} event to be fired.</li>
     * <li> {@code "domcontentloaded"} - wait for the {@code DOMContentLoaded} event to be fired.</li>
     * <li> {@code "networkidle"} - wait until there are no network connections for at least {@code 500} ms.</li>
     * </ul>
     */
    fun waitForLoadState(state: LoadState?) {
        waitForLoadState(state, null)
    }

    /**
     * Waits for the required load state to be reached.
     *
     * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been committed
     * when this method is called. If current document has already reached the required state, resolves immediately.
     * <pre>{@code
     * frame.click("button"); // Click triggers navigation.
     * frame.waitForLoadState(); // Waits for "load" state by default.
     * }</pre>
     *
     * @param state Optional load state to wait for, defaults to {@code load}. If the state has been already reached while loading current
     * document, the method resolves immediately. Can be one of:
     * <ul>
     * <li> {@code "load"} - wait for the {@code load} event to be fired.</li>
     * <li> {@code "domcontentloaded"} - wait for the {@code DOMContentLoaded} event to be fired.</li>
     * <li> {@code "networkidle"} - wait until there are no network connections for at least {@code 500} ms.</li>
     * </ul>
     */
    fun waitForLoadState(state: LoadState?, options: WaitForLoadStateOptions?)

    /**
     * Waits for the frame navigation and returns the main resource response. In case of multiple redirects, the navigation
     * will resolve with the response of the last redirect. In case of navigation to a different anchor or navigation due to
     * History API usage, the navigation will resolve with {@code null}.
     *
     * <p> This method waits for the frame to navigate to a new URL. It is useful for when you run code which will indirectly cause
     * the frame to navigate. Consider this example:
     * <pre>{@code
     * // The method returns after navigation has finished
     * Response response = frame.waitForNavigation(() -> {
     *   // Clicking the link will indirectly cause a navigation
     *   frame.click("a.delayed-navigation");
     * });
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> Usage of the <a href="https://developer.mozilla.org/en-US/docs/Web/API/History_API">History API</a> to change the URL is
     * considered a navigation.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForNavigation(callback: () -> Unit): IResponse {
        return waitForNavigation(null, callback)
    }

    /**
     * Waits for the frame navigation and returns the main resource response. In case of multiple redirects, the navigation
     * will resolve with the response of the last redirect. In case of navigation to a different anchor or navigation due to
     * History API usage, the navigation will resolve with {@code null}.
     *
     * <p> This method waits for the frame to navigate to a new URL. It is useful for when you run code which will indirectly cause
     * the frame to navigate. Consider this example:
     * <pre>{@code
     * // The method returns after navigation has finished
     * Response response = frame.waitForNavigation(() -> {
     *   // Clicking the link will indirectly cause a navigation
     *   frame.click("a.delayed-navigation");
     * });
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> Usage of the <a href="https://developer.mozilla.org/en-US/docs/Web/API/History_API">History API</a> to change the URL is
     * considered a navigation.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForNavigation(options: WaitForNavigationOptions?, callback: () -> Unit): IResponse

    /**
     * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or
     * {@code detached}.
     *
     * <p> Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become visible/hidden). If at
     * the moment of calling the method {@code selector} already satisfies the condition, the method will return immediately. If the
     * selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will throw.
     *
     * <p> This method works across navigations:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType chromium = playwright.chromium();
     *       Browser browser = chromium.launch();
     *       Page page = browser.newPage();
     *       for (String currentURL : Arrays.asList("https://google.com", "https://bbc.com")) {
     *         page.navigate(currentURL);
     *         ElementHandle element = page.mainFrame().waitForSelector("img");
     *         System.out.println("Loaded image: " + element.getAttribute("src"));
     *       }
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun waitForSelector(selector: String): IElementHandle {
        return waitForSelector(selector, null)
    }

    /**
     * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or
     * {@code detached}.
     *
     * <p> Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become visible/hidden). If at
     * the moment of calling the method {@code selector} already satisfies the condition, the method will return immediately. If the
     * selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will throw.
     *
     * <p> This method works across navigations:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType chromium = playwright.chromium();
     *       Browser browser = chromium.launch();
     *       Page page = browser.newPage();
     *       for (String currentURL : Arrays.asList("https://google.com", "https://bbc.com")) {
     *         page.navigate(currentURL);
     *         ElementHandle element = page.mainFrame().waitForSelector("img");
     *         System.out.println("Loaded image: " + element.getAttribute("src"));
     *       }
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun waitForSelector(selector: String, options: WaitForSelectorOptions?): IElementHandle

    /**
     * Waits for the given {@code timeout} in milliseconds.
     *
     * <p> Note that {@code frame.waitForTimeout()} should only be used for debugging. Tests using the timer in production are going to
     * be flaky. Use signals such as network events, selectors becoming visible and others instead.
     *
     * @param timeout A timeout to wait for
     */
    fun waitForTimeout(timeout: Double)

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: String) {
        waitForURL(url, null)
    }

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: String, options: WaitForURLOptions?)

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waifForURL(url: Pattern) {
        waitForURL(url, null)
    }

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: Pattern, options: WaitForURLOptions?)

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: (String) -> Boolean) {
        waitForURL(url, null)
    }

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: (String) -> Boolean, options: WaitForURLOptions?)
}