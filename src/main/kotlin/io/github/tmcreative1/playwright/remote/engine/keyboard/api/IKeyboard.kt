package io.github.tmcreative1.playwright.remote.engine.keyboard.api

import io.github.tmcreative1.playwright.remote.engine.options.PressOptions
import io.github.tmcreative1.playwright.remote.engine.options.TypeOptions

/**
 * Keyboard provides an api for managing a virtual keyboard. The high level api is {@link Keyboard#type Keyboard.type()},
 * which takes raw characters and generates proper keydown, keypress/input, and keyup events on your page.
 *
 * <p> For finer control, you can use {@link Keyboard#down Keyboard.down()}, {@link Keyboard#up Keyboard.up()}, and {@link
 * Keyboard#insertText Keyboard.insertText()} to manually fire events as if they were generated from a real keyboard.
 *
 * <p> An example of holding down {@code Shift} in order to select and delete some text:
 * <pre>{@code
 * page.keyboard().type("Hello World!");
 * page.keyboard().press("ArrowLeft");
 * page.keyboard().down("Shift");
 * for (int i = 0; i < " World".length(); i++)
 *   page.keyboard().press("ArrowLeft");
 * page.keyboard().up("Shift");
 * page.keyboard().press("Backspace");
 * // Result text will end up saying "Hello!"
 * }</pre>
 *
 * <p> An example of pressing uppercase {@code A}
 * <pre>{@code
 * page.keyboard().press("Shift+KeyA");
 * // or
 * page.keyboard().press("Shift+A");
 * }</pre>
 *
 * <p> An example to trigger select-all with the keyboard
 * <pre>{@code
 * // on Windows and Linux
 * page.keyboard().press("Control+A");
 * // on macOS
 * page.keyboard().press("Meta+A");
 * }</pre>
 */
interface IKeyboard {
    /**
     * Dispatches a {@code keydown} event.
     *
     * <p> {@code key} can specify the intended <a
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
     * <p> If {@code key} is a modifier key, {@code Shift}, {@code Meta}, {@code Control}, or {@code Alt}, subsequent key presses will be sent with that modifier
     * active. To release the modifier key, use {@link Keyboard#up Keyboard.up()}.
     *
     * <p> After the key is pressed once, subsequent calls to {@link Keyboard#down Keyboard.down()} will have <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/repeat">repeat</a> set to true. To release the key,
     * use {@link Keyboard#up Keyboard.up()}.
     *
     * <p> <strong>NOTE:</strong> Modifier keys DO influence {@code keyboard.down}. Holding down {@code Shift} will type the text in upper case.
     *
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun down(key: String)

    /**
     * Dispatches only {@code input} event, does not emit the {@code keydown}, {@code keyup} or {@code keypress} events.
     * <pre>{@code
     * page.keyboard().insertText("???");
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> Modifier keys DO NOT effect {@code keyboard.insertText}. Holding down {@code Shift} will not type the text in upper case.
     *
     * @param text Sets input to the specified text value.
     */
    fun insertText(text: String)

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
     * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When speficied with the
     * modifier, modifier is pressed and being held while the subsequent key is being pressed.
     * <pre>{@code
     * Page page = browser.newPage();
     * page.navigate("https://keycode.info");
     * page.keyboard().press("A");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("A.png"));
     * page.keyboard().press("ArrowLeft");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ArrowLeft.png")));
     * page.keyboard().press("Shift+O");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("O.png")));
     * browser.close();
     * }</pre>
     *
     * <p> Shortcut for {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
     *
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun press(key: String) = press(key, PressOptions {})

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
     * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When speficied with the
     * modifier, modifier is pressed and being held while the subsequent key is being pressed.
     * <pre>{@code
     * Page page = browser.newPage();
     * page.navigate("https://keycode.info");
     * page.keyboard().press("A");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("A.png"));
     * page.keyboard().press("ArrowLeft");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ArrowLeft.png")));
     * page.keyboard().press("Shift+O");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("O.png")));
     * browser.close();
     * }</pre>
     *
     * <p> Shortcut for {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
     *
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun press(key: String, options: PressOptions = PressOptions {})

    /**
     * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
     * <pre>{@code
     * // Types instantly
     * page.keyboard().type("Hello");
     * // Types slower, like a user
     * page.keyboard().type("World", new Keyboard.TypeOptions().setDelay(100));
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> Modifier keys DO NOT effect {@code keyboard.type}. Holding down {@code Shift} will not type the text in upper case.
     *
     * @param text A text to type into a focused element.
     */
    fun type(text: String) = type(text, TypeOptions {})

    /**
     * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
     * <pre>{@code
     * // Types instantly
     * page.keyboard().type("Hello");
     * // Types slower, like a user
     * page.keyboard().type("World", new Keyboard.TypeOptions().setDelay(100));
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> Modifier keys DO NOT effect {@code keyboard.type}. Holding down {@code Shift} will not type the text in upper case.
     *
     * @param text A text to type into a focused element.
     */
    fun type(text: String, options: TypeOptions = TypeOptions {})

    /**
     * Dispatches a {@code keyup} event.
     *
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun up(key: String)
}