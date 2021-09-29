package io.github.tmcreative1.playwright.remote.engine.browser.selector.api

import io.github.tmcreative1.playwright.remote.engine.options.RegisterOptions
import java.nio.file.Path

interface ISharedSelectors : ISelectors {
    /**
     * An example of registering selector engine that queries elements based on a tag name:
     * <pre>{@code
     * // Script that evaluates to a selector engine instance.
     * String createTagNameEngine = "{\n" +
     *   "  // Returns the first element matching given selector in the root's subtree.\n" +
     *   "  query(root, selector) {\n" +
     *   "    return root.querySelector(selector);\n" +
     *   "  },\n" +
     *   "  // Returns all elements matching given selector in the root's subtree.\n" +
     *   "  queryAll(root, selector) {\n" +
     *   "    return Array.from(root.querySelectorAll(selector));\n" +
     *   "  }\n" +
     *   "}";
     * // Register the engine. Selectors will be prefixed with "tag=".
     * playwright.selectors().register("tag", createTagNameEngine);
     * Browser browser = playwright.firefox().launch();
     * Page page = browser.newPage();
     * page.setContent("<div><button>Click me</button></div>");
     * // Use the selector prefixed with its name.
     * ElementHandle button = page.querySelector("tag=button");
     * // Combine it with other selector engines.
     * page.click("tag=div >> text=\"Click me\"");
     * // Can use it in any methods supporting selectors.
     * int buttonCount = (int) page.evalOnSelectorAll("tag=button", "buttons => buttons.length");
     * browser.close();
     * }</pre>
     *
     * @param name Name that is used in selectors as a prefix, e.g. {@code {name: 'foo'}} enables {@code foo=myselectorbody} selectors. May only
     * contain {@code [a-zA-Z0-9_]} characters.
     * @param script Script that evaluates to a selector engine instance.
     */
    fun register(name: String, path: Path) = register(name, path, null)

    /**
     * An example of registering selector engine that queries elements based on a tag name:
     * <pre>{@code
     * // Script that evaluates to a selector engine instance.
     * String createTagNameEngine = "{\n" +
     *   "  // Returns the first element matching given selector in the root's subtree.\n" +
     *   "  query(root, selector) {\n" +
     *   "    return root.querySelector(selector);\n" +
     *   "  },\n" +
     *   "  // Returns all elements matching given selector in the root's subtree.\n" +
     *   "  queryAll(root, selector) {\n" +
     *   "    return Array.from(root.querySelectorAll(selector));\n" +
     *   "  }\n" +
     *   "}";
     * // Register the engine. Selectors will be prefixed with "tag=".
     * playwright.selectors().register("tag", createTagNameEngine);
     * Browser browser = playwright.firefox().launch();
     * Page page = browser.newPage();
     * page.setContent("<div><button>Click me</button></div>");
     * // Use the selector prefixed with its name.
     * ElementHandle button = page.querySelector("tag=button");
     * // Combine it with other selector engines.
     * page.click("tag=div >> text=\"Click me\"");
     * // Can use it in any methods supporting selectors.
     * int buttonCount = (int) page.evalOnSelectorAll("tag=button", "buttons => buttons.length");
     * browser.close();
     * }</pre>
     *
     * @param name Name that is used in selectors as a prefix, e.g. {@code {name: 'foo'}} enables {@code foo=myselectorbody} selectors. May only
     * contain {@code [a-zA-Z0-9_]} characters.
     * @param script Script that evaluates to a selector engine instance.
     */
    fun register(name: String, path: Path, options: RegisterOptions?)

    fun addChannel(channel: ISelectors)
    fun removeChannel(channel: ISelectors)
}