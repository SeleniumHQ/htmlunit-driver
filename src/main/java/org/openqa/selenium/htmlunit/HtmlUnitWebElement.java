// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit;

import static org.htmlunit.html.DomElement.ATTRIBUTE_NOT_DEFINED;
import static org.htmlunit.html.DomElement.ATTRIBUTE_VALUE_EMPTY;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.ScriptResult;
import org.htmlunit.corejs.javascript.ScriptRuntime;
import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.corejs.javascript.ScriptableObject;
import org.htmlunit.html.DisabledElement;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlCheckBoxInput;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlImageInput;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlRadioButtonInput;
import org.htmlunit.html.HtmlSelect;
import org.htmlunit.html.HtmlSubmitInput;
import org.htmlunit.html.HtmlTextArea;
import org.htmlunit.html.impl.SelectableTextInput;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.host.css.CSSStyleDeclaration;
import org.htmlunit.javascript.host.dom.DOMTokenList;
import org.htmlunit.javascript.host.html.HTMLElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.Colors;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

/**
 * Represents an HTML element in the context of {@link HtmlUnitDriver}.
 *
 * <p>Implements {@link WebElement}, {@link Coordinates}, and {@link Locatable}
 * to provide standard Selenium interactions: clicking, submitting forms, reading
 * attributes and CSS values, and determining location and size.</p>
 *
 * <p>This class wraps a {@link DomElement} and delegates operations to
 * {@link HtmlUnitDriver} where appropriate. It also handles form-submit logic,
 * element-state checks (visibility, enabled/disabled, staleness), and DOM
 * property access.</p>
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Javier Neira
 * @author Ronald Brill
 * @author Andrei Solntsev
 * @author Martin Bartoš
 * @author Scott Babcock
 */
public class HtmlUnitWebElement implements WrapsDriver, WebElement, Coordinates, Locatable {

    /**
     * Set of HTML boolean attribute names used to correctly determine whether an
     * attribute is present. Stored as an immutable {@link Set} for O(1) lookup
     * instead of the previous linear array scan.
     */
    private static final Set<String> BOOLEAN_ATTRIBUTES;

    static {
        final Set<String> attrs = new HashSet<>();
        for (final String a : new String[]{
                "async", "autofocus", "autoplay", "checked", "compact",
                "complete", "controls", "declare", "defaultchecked", "defaultselected",
                "defer", "disabled", "draggable", "ended", "formnovalidate", "hidden",
                "indeterminate", "iscontenteditable", "ismap", "itemscope", "loop",
                "multiple", "muted", "nohref", "noresize", "noshade", "novalidate",
                "nowrap", "open", "paused", "pubdate", "readonly", "required",
                "reversed", "scoped", "seamless", "seeking", "selected", "spellcheck",
                "truespeed", "willvalidate"}) {
            attrs.add(a);
        }
        BOOLEAN_ATTRIBUTES = Collections.unmodifiableSet(attrs);
    }

    /**
     * Pre-compiled pattern for stripping non-numeric characters from CSS
     * dimension values. Compiled once to avoid repeated regex compilation in
     * {@link #readAndRound(String)}.
     */
    private static final Pattern NON_NUMERIC = Pattern.compile("[^0-9.]");

    /** The {@link HtmlUnitDriver} instance associated with this element. */
    private final HtmlUnitDriver driver_;

    /** Unique identifier for this element within the driver. */
    private final int id_;

    /** The underlying {@link DomElement} that this WebElement wraps. */
    private final DomElement element_;

    /**
     * Constructs a new {@link HtmlUnitWebElement} that wraps the given {@link DomElement}.
     *
     * @param driver  the {@link HtmlUnitDriver} instance this element belongs to;
     *                must not be {@code null}
     * @param id      a unique identifier for this element within the driver
     * @param element the underlying {@link DomElement} to wrap; must not be {@code null}
     */
    public HtmlUnitWebElement(final HtmlUnitDriver driver, final int id, final DomElement element) {
        driver_ = driver;
        id_ = id;
        element_ = element;
    }

    @Override
    public void click() {
        verifyCanInteractWithElement(true);
        driver_.click(element_, true);
    }

    @Override
    public void submit() {
        driver_.submit(this);
    }

    /**
     * Submits the form associated with this element.
     *
     * <ul>
     *   <li>If the element itself is a {@link HtmlForm}, that form is submitted.</li>
     *   <li>If the element is a {@link HtmlSubmitInput} or {@link HtmlImageInput},
     *       the element is clicked.</li>
     *   <li>If the element is an {@link HtmlInput} nested in a form, that form is
     *       submitted.</li>
     *   <li>Otherwise, the nearest ancestor {@link HtmlForm} is located and
     *       submitted recursively.</li>
     * </ul>
     *
     * @throws UnsupportedOperationException if no enclosing form can be found
     * @throws WebDriverException            wrapping any {@link IOException} from HtmlUnit
     */
    void submitImpl() {
        try {
            if (element_ instanceof HtmlForm) {
                submitForm((HtmlForm) element_);
            }
            else if ((element_ instanceof HtmlSubmitInput) || (element_ instanceof HtmlImageInput)) {
                element_.click();
            }
            else if (element_ instanceof HtmlInput) {
                final HtmlForm form = ((HtmlElement) element_).getEnclosingForm();
                if (form == null) {
                    throw new UnsupportedOperationException(
                            "To submit an element, it must be nested inside a form element");
                }
                submitForm(form);
            }
            else {
                final HtmlUnitWebElement form = findParentForm();
                if (form == null) {
                    throw new UnsupportedOperationException(
                            "To submit an element, it must be nested inside a form element");
                }
                form.submitImpl();
            }
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }
    }

    private void submitForm(final HtmlForm form) {
        assertElementNotStale();

        final List<HtmlElement> allElements = new ArrayList<>();
        allElements.addAll(form.getElementsByTagName("input"));
        allElements.addAll(form.getElementsByTagName("button"));

        HtmlElement submit = null;
        for (final HtmlElement e : allElements) {
            if (!isSubmitElement(e)) {
                continue;
            }
            if (submit == null) {
                submit = e;
            }
        }

        if (submit == null) {
            if (driver_.isJavascriptEnabled()) {
                final ScriptResult eventResult = form.fireEvent("submit");
                if (!ScriptResult.isFalse(eventResult)) {
                    driver_.executeScript("arguments[0].submit()", form);
                }
                return;
            }
            throw new WebDriverException("Cannot locate element used to submit form");
        }
        try {
            // Ignore visibility here — browsers submit even hidden submit buttons.
            submit.click(false, false, false, true, true, true, false);
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }
    }

    private static boolean isSubmitElement(final HtmlElement element) {
        if (element instanceof HtmlSubmitInput && !((HtmlSubmitInput) element).isDisabled()) {
            return true;
        }
        if (element instanceof HtmlImageInput && !((HtmlImageInput) element).isDisabled()) {
            return true;
        }
        if (element instanceof HtmlButton) {
            final HtmlButton button = (HtmlButton) element;
            return "submit".equalsIgnoreCase(button.getTypeAttribute()) && !button.isDisabled();
        }
        return false;
    }

    @Override
    public void clear() {
        assertElementNotStale();

        if (element_ instanceof HtmlInput) {
            final HtmlInput htmlInput = (HtmlInput) element_;
            if (htmlInput.isReadOnly()) {
                throw new InvalidElementStateException("You may only edit editable elements");
            }
            if (htmlInput.isDisabled()) {
                throw new InvalidElementStateException("You may only interact with enabled elements");
            }
            htmlInput.setValue("");
            if (htmlInput instanceof SelectableTextInput) {
                ((SelectableTextInput) htmlInput).setSelectionEnd(0);
            }
            htmlInput.fireEvent("change");
        }
        else if (element_ instanceof HtmlTextArea) {
            final HtmlTextArea htmlTextArea = (HtmlTextArea) element_;
            if (htmlTextArea.isReadOnly()) {
                throw new InvalidElementStateException("You may only edit editable elements");
            }
            if (htmlTextArea.isDisabled()) {
                throw new InvalidElementStateException("You may only interact with enabled elements");
            }
            htmlTextArea.setText("");
            // setText fires the onchange event already
        }
        else if (!element_.getAttribute("contenteditable").equals(ATTRIBUTE_NOT_DEFINED)) {
            element_.setTextContent("");
        }
    }

    /**
     * Verifies that this element can be interacted with.
     *
     * <p>Checks that the element is not stale, is displayed (waiting up to the
     * implicit wait timeout), and — unless {@code ignoreDisabled} is {@code true}
     * — is enabled.</p>
     *
     * @param ignoreDisabled if {@code true}, the enabled/disabled state is not checked
     * @throws ElementNotInteractableException if the element is not visible
     * @throws InvalidElementStateException    if the element is disabled and
     *                                         {@code ignoreDisabled} is {@code false}
     */
    void verifyCanInteractWithElement(final boolean ignoreDisabled) {
        assertElementNotStale();

        final Boolean displayed = driver_.implicitlyWaitFor(this::isDisplayed);

        if (displayed == null || !displayed) {
            throw new ElementNotInteractableException("You may only interact with visible elements");
        }

        if (!ignoreDisabled && !isEnabled()) {
            throw new InvalidElementStateException("You may only interact with enabled elements");
        }
    }

    /**
     * Transfers focus to this element when it is different from the currently
     * active element.
     *
     * <p>When JavaScript is enabled and the previously active element is not the
     * body, a {@code blur} event is fired on that element before focusing this
     * one. A {@link StaleElementReferenceException} from the blur call is silently
     * swallowed — the old element has already been removed from the DOM.</p>
     */
    void switchFocusToThisIfNeeded() {
        final HtmlUnitWebElement oldActiveElement = (HtmlUnitWebElement) driver_.switchTo().activeElement();

        final boolean jsEnabled = driver_.isJavascriptEnabled();
        final boolean oldActiveEqualsCurrent = oldActiveElement.equals(this);
        try {
            final boolean isBody = "body".equalsIgnoreCase(oldActiveElement.getTagName());
            if (jsEnabled && !oldActiveEqualsCurrent && !isBody) {
                oldActiveElement.element_.blur();
            }
        }
        catch (final StaleElementReferenceException ex) {
            // Old element has gone; nothing to blur.
        }
        element_.focus();
    }

    @Override
    public void sendKeys(final CharSequence... value) {
        if (value == null) {
            throw new IllegalArgumentException("Keys to send should not be null");
        }
        driver_.sendKeys(this, value);
    }

    @Override
    public String getTagName() {
        assertElementNotStale();
        return element_.getNodeName();
    }

    /**
     * Returns the value of the named attribute, following WebDriver semantics.
     *
     * <p>Special cases handled:
     * <ul>
     *   <li>{@code href} and {@code src} — resolved to absolute URLs.</li>
     *   <li>{@code value} — read from the live element state for inputs and
     *       textareas; falls back to the content text for {@code <option>} elements
     *       without a {@code value} attribute.</li>
     *   <li>{@code disabled} — returns {@code "true"} if a {@link DisabledElement}
     *       is disabled; {@code null} if the element cannot be disabled (i.e. is not
     *       a {@link DisabledElement}).</li>
     *   <li>HTML boolean attributes — returns {@code "true"} if present, {@code null}
     *       if absent.</li>
     * </ul>
     * </p>
     *
     * @param name the attribute name; case-insensitive
     * @return the attribute value, or {@code null} if not present
     */
    @Override
    public String getAttribute(final String name) {
        assertElementNotStale();

        final String lowerName = name.toLowerCase(Locale.ROOT);

        if (element_ instanceof HtmlInput && ("selected".equals(lowerName) || "checked".equals(lowerName))) {
            return trueOrNull(((HtmlInput) element_).isChecked());
        }

        switch (lowerName) {
            case "href": {
                final String href = element_.getAttribute(name);
                if (ATTRIBUTE_NOT_DEFINED == href) {
                    return null;
                }
                final HtmlPage page = (HtmlPage) element_.getPage();
                try {
                    return page.getFullyQualifiedUrl(href.trim()).toString();
                }
                catch (final MalformedURLException e) {
                    return null;
                }
            }
            case "src": {
                final String link = element_.getAttribute(name);
                if (ATTRIBUTE_NOT_DEFINED == link) {
                    return "";
                }
                final HtmlPage page = (HtmlPage) element_.getPage();
                try {
                    return page.getFullyQualifiedUrl(link.trim()).toString();
                }
                catch (final MalformedURLException e) {
                    return null;
                }
            }
            case "value":
                if (element_ instanceof HtmlInput) {
                    return ((HtmlInput) element_).getValue();
                }
                if (element_ instanceof HtmlTextArea) {
                    return ((HtmlTextArea) element_).getText();
                }
                // Per the HTML spec, if an <option> has no value attribute its
                // text content is used as the value.
                if (element_ instanceof HtmlOption && !element_.hasAttribute("value")) {
                    return getText();
                }
                final String attributeValue = element_.getAttribute(name);
                if (ATTRIBUTE_NOT_DEFINED == attributeValue) {
                    return null;
                }
                return attributeValue;

            case "disabled":
                if (element_ instanceof DisabledElement) {
                    return trueOrNull(((DisabledElement) element_).isDisabled());
                }
                // Elements that cannot be disabled (e.g. <div>) have no disabled
                // attribute - fall back.
        }

        if ("multiple".equals(lowerName) && element_ instanceof HtmlSelect) {
            final String multipleAttribute = ((HtmlSelect) element_).getMultipleAttribute();
            if ("".equals(multipleAttribute)) {
                return trueOrNull(element_.hasAttribute("multiple"));
            }
            return "true";
        }

        if ("index".equals(lowerName) && element_ instanceof HtmlOption) {
            final HtmlSelect select = ((HtmlOption) element_).getEnclosingSelect();
            final List<HtmlOption> allOptions = select.getOptions();
            for (int i = 0; i < allOptions.size(); i++) {
                if (element_.equals(select.getOption(i))) {
                    return String.valueOf(i);
                }
            }
            return null;
        }

        if (BOOLEAN_ATTRIBUTES.contains(lowerName)) {
            return trueOrNull(element_.hasAttribute(lowerName));
        }

        // Fall back to the raw attribute value.
        // Use reference-equality sentinel checks consistent with the switch cases above.
        final String rawValue = element_.getAttribute(name);
        if (ATTRIBUTE_NOT_DEFINED == rawValue) {
            // Attribute is absent — try JS property fallback before returning null.
            if (driver_.getWebClient().isJavaScriptEngineEnabled()) {
                final HtmlUnitScriptable scriptable = element_.getScriptableObject();
                if (scriptable != null) {
                    final Object slotVal = ScriptableObject.getProperty(scriptable, name);
                    if (slotVal instanceof String) {
                        return (String) slotVal;
                    }
                }
            }
            return null;
        }
        // ATTRIBUTE_VALUE_EMPTY means the attribute is present but has no value ("").
        if (ATTRIBUTE_VALUE_EMPTY == rawValue) {
            return "";
        }
        return rawValue;
    }

    @Override
    public String getDomProperty(final String name) {
        assertElementNotStale();

        final HtmlUnitScriptable scriptable = element_.getScriptableObject();
        if (scriptable != null) {
            final Object propValue = ScriptableObject.getProperty(scriptable, name);
            if (Scriptable.NOT_FOUND == propValue) {
                return null;
            }

            if (propValue instanceof CSSStyleDeclaration) {
                return ((CSSStyleDeclaration) propValue).getCssText();
            }

            if (propValue instanceof DOMTokenList) {
                final String value = ((DOMTokenList) propValue).getValue();
                if (value != null) {
                    return '[' + String.join(", ", StringUtils.split(value, " \t\r\n\u000C")) + ']';
                }
                return "";
            }

            return ScriptRuntime.toString(propValue);
        }

        // JS disabled — fall back to direct DOM queries for the most common properties.
        if ("disabled".equals(name)) {
            if (element_ instanceof DisabledElement) {
                return trueOrFalse(((DisabledElement) element_).isDisabled());
            }
        }

        if ("checked".equals(name)) {
            if (element_ instanceof HtmlCheckBoxInput) {
                return trueOrFalse(((HtmlCheckBoxInput) element_).isChecked());
            }
            else if (element_ instanceof HtmlRadioButtonInput) {
                return trueOrFalse(((HtmlRadioButtonInput) element_).isChecked());
            }
        }

        final String value = element_.getAttribute(name);
        if (ATTRIBUTE_NOT_DEFINED == value) {
            return null;
        }

        if (ATTRIBUTE_VALUE_EMPTY == value) {
            return null;
        }

        return value;
    }

    /**
     * Returns the serialised value of the named DOM attribute as it appears in the
     * markup, without resolving URLs or applying other WebDriver transformations.
     *
     * <p>Notable behaviour:
     * <ul>
     *   <li>{@code disabled} — returns {@code "true"}/{@code null} based on the
     *       actual disabled state of {@link DisabledElement}s.</li>
     *   <li>{@code checked} — returns {@code "true"}/{@code null} based on the
     *       checked state of checkbox and radio inputs.</li>
     *   <li>{@code multiple} — for {@code <select>} elements, returns {@code "true"}
     *       only if the {@code multiple} attribute is actually present in the markup.</li>
     *   <li>{@code selected} — returns {@code "true"}/{@code null} based on the
     *       selected state of {@code <option>} elements.</li>
     * </ul>
     * </p>
     */
    @Override
    public String getDomAttribute(final String name) {
        assertElementNotStale();

        final String lowerName = name.toLowerCase(Locale.ROOT);
        final String value = element_.getAttribute(lowerName);
        if (ATTRIBUTE_NOT_DEFINED == value) {
            return null;
        }

        if ("disabled".equals(lowerName)) {
            if (element_ instanceof DisabledElement) {
                return trueOrNull(((DisabledElement) element_).isDisabled());
            }
        }

        if ("checked".equals(lowerName)) {
            if (element_ instanceof HtmlCheckBoxInput) {
                return trueOrNull(((HtmlCheckBoxInput) element_).isChecked());
            }
            else if (element_ instanceof HtmlRadioButtonInput) {
                return trueOrNull(((HtmlRadioButtonInput) element_).isChecked());
            }
        }

        if ("multiple".equals(lowerName) && element_ instanceof HtmlSelect) {
            // Only report "true" if the attribute is actually present in the markup;
            // a <select> without the multiple attribute is a single-selection list.
            return trueOrNull(element_.hasAttribute("multiple"));
        }

        if ("selected".equals(lowerName)) {
            if (element_ instanceof HtmlOption) {
                return trueOrNull(((HtmlOption) element_).isSelected());
            }
        }

        return value;
    }

    private static String trueOrNull(final boolean condition) {
        return condition ? "true" : null;
    }

    private static String trueOrFalse(final boolean condition) {
        return condition ? "true" : "false";
    }

    @Override
    public boolean isSelected() {
        assertElementNotStale();

        if (element_ instanceof HtmlInput) {
            return ((HtmlInput) element_).isChecked();
        }
        else if (element_ instanceof HtmlOption) {
            return ((HtmlOption) element_).isSelected();
        }

        throw new UnsupportedOperationException(
                "Unable to determine if element is selected. Tag name is: " + element_.getTagName());
    }

    @Override
    public boolean isEnabled() {
        assertElementNotStale();

        if (element_ instanceof DisabledElement) {
            return !((DisabledElement) element_).isDisabled();
        }
        return true;
    }

    @Override
    public boolean isDisplayed() {
        assertElementNotStale();

        return element_.isDisplayed();
    }

    @Override
    public Point getLocation() {
        assertElementNotStale();

        try {
            return new Point(readAndRound("left"), readAndRound("top"));
        }
        catch (final Exception e) {
            throw new WebDriverException("Cannot determine location of element", e);
        }
    }

    @Override
    public Dimension getSize() {
        assertElementNotStale();
        try {
            final int width = readAndRound("width");
            final int height = readAndRound("height");
            return new Dimension(width, height);
        }
        catch (final Exception e) {
            throw new WebDriverException("Cannot determine size of element", e);
        }
    }

    @Override
    public Rectangle getRect() {
        return new Rectangle(getLocation(), getSize());
    }

    /**
     * Reads the named CSS property via {@link #getCssValue(String)}, strips any
     * non-numeric characters (unit suffixes such as {@code "px"}), and rounds the
     * result to the nearest integer.
     *
     * <p>The non-numeric stripping uses a pre-compiled {@link Pattern} constant to
     * avoid repeated regex compilation. If the CSS value is empty or cannot be
     * parsed, {@code 5} is returned as a best-effort fallback.</p>
     *
     * @param property the CSS property name (e.g. {@code "left"}, {@code "width"})
     * @return the rounded pixel value
     */
    private int readAndRound(final String property) {
        final String cssValue = NON_NUMERIC.matcher(getCssValue(property)).replaceAll("");
        if (cssValue.isEmpty()) {
            return 5; // wrong, but better than nothing
        }
        return Math.round(Float.parseFloat(cssValue));
    }

    @Override
    public String getText() {
        assertElementNotStale();
        return element_.getVisibleText();
    }

    /**
     * Returns the {@link HtmlUnitDriver} associated with this element.
     *
     * @return the driver instance that owns this element
     */
    protected HtmlUnitDriver getDriver() {
        return driver_;
    }

    /**
     * Returns the underlying {@link DomElement} wrapped by this element.
     *
     * @return the wrapped {@link DomElement}; never {@code null}
     */
    public DomElement getElement() {
        return element_;
    }

    /**
     * Returns a list of child elements with the specified tag name.
     *
     * @param tagName the tag name to search for among descendants
     * @return a list of matching {@link WebElement} instances
     * @deprecated Not part of the WebDriver API — use {@link #findElements(By)} instead
     */
    @Deprecated
    public List<WebElement> getElementsByTagName(final String tagName) {
        assertElementNotStale();

        final List<?> allChildren = element_.getByXPath(".//" + tagName);
        final List<WebElement> elements = new ArrayList<>();
        for (final Object o : allChildren) {
            if (!(o instanceof HtmlElement)) {
                continue;
            }
            elements.add(getDriver().toWebElement((HtmlElement) o));
        }
        return elements;
    }

    @Override
    public WebElement findElement(final By by) {
        driver_.getAlert().ensureUnlocked();
        return driver_.implicitlyWaitFor(() -> {
            assertElementNotStale();
            return driver_.findElement(this, by);
        });
    }

    @Override
    public List<WebElement> findElements(final By by) {
        driver_.getAlert().ensureUnlocked();
        return driver_.implicitlyWaitFor(() -> {
            assertElementNotStale();
            return driver_.findElements(this, by);
        });
    }

    /**
     * Walks up the DOM tree to find the nearest enclosing {@link HtmlForm}.
     *
     * @return the nearest ancestor form as an {@link HtmlUnitWebElement}, or
     *         {@code null} if no enclosing form exists
     */
    private HtmlUnitWebElement findParentForm() {
        DomNode current = element_;
        while (!(current == null || current instanceof HtmlForm)) {
            current = current.getParentNode();
        }
        // Guard against null before casting — the original code passed null directly
        // to toWebElement, which caused an NPE or incorrect behaviour.
        if (current == null) {
            return null;
        }
        return getDriver().toWebElement((HtmlForm) current);
    }

    /**
     * Returns a concise opening-tag representation of this element, e.g.
     * {@code <input type="text" id="username">}.
     *
     * <p>Note: this value is <em>not</em> cached because the element's attributes
     * may change after construction (e.g. via JavaScript). Caching would return
     * stale attribute values for dynamic pages.</p>
     *
     * @return the opening-tag string
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('<').append(element_.getTagName());
        final NamedNodeMap attributes = element_.getAttributes();
        final int n = attributes.getLength();
        for (int i = 0; i < n; ++i) {
            final Attr a = (Attr) attributes.item(i);
            sb.append(' ').append(a.getName())
              .append("=\"").append(a.getValue().replace("\"", "&quot;")).append("\"");
        }
        if (element_.hasChildNodes()) {
            sb.append('>');
        }
        else {
            sb.append(" />");
        }
        return sb.toString();
    }

    /**
     * Asserts that this element is not stale (i.e. still attached to the DOM of
     * the current page). Delegates to {@link HtmlUnitDriver#assertElementNotStale}.
     *
     * @throws StaleElementReferenceException if the element is stale
     */
    protected void assertElementNotStale() {
        driver_.assertElementNotStale(element_);
    }

    @Override
    public String getCssValue(final String propertyName) {
        assertElementNotStale();

        // TODO switch to the JS-free version:
        // final ComputedCssStyleDeclaration cssStyle =
        //     element_.getPage().getEnclosingWindow().getComputedStyle(element_, null);
        // final Definition definition =
        //     StyleAttributes.getDefinition(propertyName, driver_.getBrowserVersion());
        // return cssStyle.getStyleAttribute(definition, true);

        final HTMLElement elem = element_.getScriptableObject();
        final String style = elem.getWindow().getComputedStyle(elem, null).getPropertyValue(propertyName);
        return getColor(style);
    }

    private static String getColor(final String name) {
        if ("null".equals(name)) {
            return "transparent";
        }
        if (name.startsWith("rgb(")) {
            return Color.fromString(name).asRgba();
        }
        final Colors colors = getColorsOf(name);
        if (colors != null) {
            return colors.getColorValue().asRgba();
        }
        return name;
    }

    private static Colors getColorsOf(String name) {
        name = name.toUpperCase();
        for (final Colors colors : Colors.values()) {
            if (colors.name().equals(name)) {
                return colors;
            }
        }
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof WebElement)) {
            return false;
        }

        WebElement other = (WebElement) obj;
        if (other instanceof WrapsElement) {
            other = ((WrapsElement) obj).getWrappedElement();
        }

        return other instanceof HtmlUnitWebElement
                && element_.equals(((HtmlUnitWebElement) other).element_);
    }

    @Override
    public int hashCode() {
        return element_.hashCode();
    }

    @Override
    public WebDriver getWrappedDriver() {
        return driver_;
    }

    @Override
    public Coordinates getCoordinates() {
        return this;
    }

    @Override
    public Point onScreen() {
        throw new UnsupportedOperationException("Not displayed, no screen location.");
    }

    @Override
    public Point inViewPort() {
        return getLocation();
    }

    @Override
    public Point onPage() {
        return getLocation();
    }

    @Override
    public Object getAuxiliary() {
        return element_;
    }

    @Override
    public <X> X getScreenshotAs(final OutputType<X> outputType) throws WebDriverException {
        throw new UnsupportedOperationException("Screenshots are not enabled for HtmlUnitDriver");
    }

    /**
     * Returns the unique identifier assigned to this element within the driver.
     *
     * @return the element's unique ID
     */
    public int getId() {
        return id_;
    }

    /**
     * Converts this element into a JSON representation suitable for W3C WebDriver
     * wire protocol communication.
     *
     * @return a {@link Map} containing the W3C element key and this element's ID
     */
    public Map<String, Object> toJson() {
        return Map.of(Dialect.W3C.getEncodedElementKey(), getId());
    }
}
