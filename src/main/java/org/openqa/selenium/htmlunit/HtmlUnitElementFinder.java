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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlunit.Page;
import org.htmlunit.SgmlPage;
import org.htmlunit.cssparser.parser.CSSException;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.DomNodeList;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.openqa.selenium.By;
import org.openqa.selenium.By.Remotable.Parameters;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.openqa.selenium.support.locators.RelativeLocator.RelativeBy;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Martin Bartoš
 * @author Ronald Brill
 */
public class HtmlUnitElementFinder {

    private static final String INVALIDXPATHERROR = "The xpath expression '%s' cannot be evaluated";
    private static final String INVALIDSELECTIONERROR =
            "The xpath expression '%s' selected an object of type '%s' instead of a WebElement";

    private static final String FIND_ELEMENTS_JS;

    static {
        try {
            final String location =
                    String.format(
                            "/%s/%s",
                            HtmlUnitDriver.class.getPackage().getName().replace(".", "/"), "findElements.js");

            try (InputStream stream = HtmlUnitDriver.class.getResourceAsStream(location)) {
                FIND_ELEMENTS_JS = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final Map<Class<? extends By>, HtmlUnitElementLocator> finders_ = new HashMap<>();

    HtmlUnitElementFinder() {
        finders_.put(By.id("a").getClass(), new FindByID());
        finders_.put(By.name("a").getClass(), new FindByName());
        finders_.put(By.linkText("a").getClass(), new FindByLinkText());
        finders_.put(By.partialLinkText("a").getClass(), new FindByPartialLinkText());
        finders_.put(By.className("a").getClass(), new FindByClassName());
        finders_.put(By.cssSelector("a").getClass(), new FindByCssSelector());
        finders_.put(By.tagName("a").getClass(), new FindByTagName());
        finders_.put(By.xpath("//a").getClass(), new FindByXPath());

        finders_.put(RelativeLocator.with(By.id("id")).getClass(), new FindByRelativeLocator());
    }

    /**
     * Finds a single {@link WebElement} on the current page using the specified locator.
     *
     * @param driver  the {@link HtmlUnitDriver} to search with
     * @param locator the {@link By} locator to use
     * @return the first matching {@link WebElement}
     * @throws NoSuchElementException if no matching element is found
     */
    public WebElement findElement(final HtmlUnitDriver driver, final By locator) {
        final HtmlUnitElementLocator elementLocator = finders_.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElement(driver);
        }
        return elementLocator.findElement(driver, locator);
    }

    /**
     * Finds all {@link WebElement} instances on the current page matching the specified locator.
     *
     * @param driver  the {@link HtmlUnitDriver} to search with
     * @param locator the {@link By} locator to use
     * @return a {@link List} of matching {@link WebElement} instances; may be empty if none found
     */
    public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
        final HtmlUnitElementLocator elementLocator = finders_.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElements(driver);
        }
        return elementLocator.findElements(driver, locator);
    }

    /**
     * Finds a single {@link WebElement} within the context of the specified parent element.
     *
     * @param element the parent {@link HtmlUnitWebElement} to search within
     * @param locator the {@link By} locator to use
     * @return the first matching child {@link WebElement}
     * @throws NoSuchElementException if no matching element is found
     */
    public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
        final HtmlUnitElementLocator elementLocator = finders_.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElement(element);
        }
        return elementLocator.findElement(element, locator);
    }

    /**
     * Finds all {@link WebElement} instances within the context of the specified parent element.
     *
     * @param element the parent {@link HtmlUnitWebElement} to search within
     * @param locator the {@link By} locator to use
     * @return a {@link List} of matching child {@link WebElement} instances; may be empty if none found
     */
    public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
        final HtmlUnitElementLocator elementLocator = finders_.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElements(element);
        }
        return elementLocator.findElements(element, locator);
    }

    /**
     * Locator strategy for finding elements by their {@code id} attribute.
     * <p>
     * Supports searching both on the {@link HtmlUnitDriver} page and within a
     * {@link HtmlUnitWebElement} context.
     */
    public static class FindByID extends HtmlUnitElementLocator {

        @Override
        public WebElement findElement(final HtmlUnitDriver driver, final By locator) {
            final SgmlPage lastPage = getLastPage(driver);
            if (!(lastPage instanceof HtmlPage)) {
                throw new IllegalStateException("Cannot find elements by id for " + lastPage);
            }

            final String id = getValue(locator);
            final DomElement element = ((HtmlPage) lastPage).getElementById(id);

            if (element == null) {
                throw new NoSuchElementException("Unable to locate element with ID: '" + id + "'");
            }
            return driver.toWebElement(element);
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            final SgmlPage lastPage = getLastPage(driver);
            if (!(lastPage instanceof HtmlPage)) {
                throw new IllegalStateException("Cannot find elements by id for " + lastPage);
            }

            final List<DomElement> allElements = ((HtmlPage) lastPage).getElementsById(getValue(locator));
            return convertRawDomElementsToWebElements(driver, allElements);
        }

        @Override
        public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
            final String id = getValue(locator);
            return new FindByXPath().findElement(element, By.xpath(".//*[@id = '" + id + "']"));
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            final String id = getValue(locator);
            return new FindByXPath().findElements(element, By.xpath(".//*[@id = '" + id + "']"));
        }
    }

    /**
     * Locator strategy for finding elements by their {@code name} attribute.
     * <p>
     * Supports searching both on the {@link HtmlUnitDriver} page and within a
     * {@link HtmlUnitWebElement} context.
     */
    public static class FindByName extends HtmlUnitElementLocator {

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            final SgmlPage lastPage = getLastPage(driver);
            if (!(lastPage instanceof HtmlPage)) {
                throw new IllegalStateException("Cannot find elements by id for " + lastPage);
            }

            final List<DomElement> allElements = ((HtmlPage) lastPage).getElementsByName(getValue(locator));
            return convertRawDomElementsToWebElements(driver, allElements);
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            final String name = getValue(locator);
            return new FindByXPath().findElements(element, By.xpath(".//*[@name = '" + name + "']"));
        }
    }

    /**
     * Locator strategy for finding anchor elements (&lt;a&gt;) by their exact visible text.
     * <p>
     * Supports searching both on the {@link HtmlUnitDriver} page and within a
     * {@link HtmlUnitWebElement} context.
     */
    public static class FindByLinkText extends HtmlUnitElementLocator {

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            final SgmlPage lastPage = getLastPage(driver);
            if (!(lastPage instanceof HtmlPage)) {
                throw new IllegalStateException("Cannot find links for " + lastPage);
            }

            final String expectedText = getValue(locator);
            final List<HtmlAnchor> anchors = ((HtmlPage) lastPage).getAnchors();

            final List<WebElement> toReturn = new ArrayList<>();
            for (final HtmlAnchor anchor : anchors) {
                if (expectedText.equals(anchor.asNormalizedText())) {
                    toReturn.add(driver.toWebElement(anchor));
                }
            }
            return toReturn;
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            final String expectedText = getValue(locator);
            final List<? extends HtmlElement> htmlElements = element.getElement().getElementsByTagName("a");

            final List<WebElement> toReturn = new ArrayList<>();
            for (final DomElement e : htmlElements) {
                if (expectedText.equals(e.asNormalizedText())) {
                    toReturn.add(element.getDriver().toWebElement(e));
                }
            }
            return toReturn;
        }
    }

    /**
     * Locator strategy for finding anchor elements (&lt;a&gt;) by partial visible text.
     * <p>
     * Supports searching both on the {@link HtmlUnitDriver} page and within a
     * {@link HtmlUnitWebElement} context. Matches anchors whose text contains
     * the specified substring.
     */
    public static class FindByPartialLinkText extends HtmlUnitElementLocator {

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            final SgmlPage lastPage = getLastPage(driver);
            if (!(lastPage instanceof HtmlPage)) {
                throw new IllegalStateException("Cannot find links for " + lastPage);
            }

            final String expectedText = getValue(locator);
            final List<HtmlAnchor> anchors = ((HtmlPage) lastPage).getAnchors();
            final List<WebElement> toReturn = new ArrayList<>();
            for (final HtmlAnchor anchor : anchors) {
                if (anchor.asNormalizedText().contains(expectedText)) {
                    toReturn.add(driver.toWebElement(anchor));
                }
            }
            return toReturn;
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            final String expectedText = getValue(locator);
            final DomNodeList<HtmlElement> anchors = element.getElement().getElementsByTagName("a");

            final List<WebElement> toReturn = new ArrayList<>();
            for (final HtmlElement anchor : anchors) {
                if (anchor.asNormalizedText().contains(expectedText)) {
                    toReturn.add(element.getDriver().toWebElement(anchor));
                }
            }
            return toReturn;
        }
    }

    /**
     * Locator strategy for finding elements by their CSS class name.
     * <p>
     * This class ensures that only single class names are used. Compound class names
     * (containing spaces) are not permitted. Internally, the search is delegated
     * to {@link FindByCssSelector} using a CSS class selector.
     */
    public static class FindByClassName extends HtmlUnitElementLocator {

        /**
         * Validates the provided locator value and ensures it represents a single class name.
         *
         * @param locator the {@link By} locator specifying the class name
         * @return the validated class name
         * @throws NoSuchElementException if the class name contains spaces
         */
        private String checkValue(final By locator) {
            final String value = getValue(locator);

            if (value.indexOf(' ') != -1) {
                throw new NoSuchElementException("Compound class names not permitted");
            }
            return value;
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            return new FindByCssSelector().findElements(driver, By.cssSelector("." + checkValue(locator)));
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            return new FindByCssSelector().findElements(element, By.cssSelector("." + checkValue(locator)));
        }
    }

    /**
     * Locator strategy for finding elements using CSS selectors.
     * <p>
     * This class provides methods to locate a single element or multiple elements
     * on a page or within a specific element by applying a CSS selector string.
     * If the CSS selector is invalid or does not match any {@link DomElement}, 
     * a {@link NoSuchElementException} is thrown.
     */
    public static class FindByCssSelector extends HtmlUnitElementLocator {

        @Override
        public WebElement findElement(final HtmlUnitDriver driver, final By locator) {
            final DomNode node;

            try {
                node = getLastPage(driver).querySelector(getValue(locator));
            }
            catch (final CSSException ex) {
                throw new NoSuchElementException("Unable to locate element using css", ex);
            }

            if (node instanceof DomElement) {
                return driver.toWebElement((DomElement) node);
            }

            throw new NoSuchElementException("Returned node (" + node + ") was not a DOM element");
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            final DomNodeList<DomNode> allNodes;

            try {
                allNodes = getLastPage(driver).querySelectorAll(getValue(locator));
            }
            catch (final CSSException ex) {
                throw new NoSuchElementException("Unable to locate element using css", ex);
            }

            final List<WebElement> toReturn = new ArrayList<>();

            for (final DomNode node : allNodes) {
                if (node instanceof DomElement) {
                    toReturn.add(driver.toWebElement((DomElement) node));
                }
                else {
                    throw new NoSuchElementException("Returned node was not a DOM element");
                }
            }

            return toReturn;
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            final DomNodeList<DomNode> allNodes;

            try {
                allNodes = element.getElement().querySelectorAll(getValue(locator));
            }
            catch (final CSSException ex) {
                throw new NoSuchElementException("Unable to locate element using css", ex);
            }

            final List<WebElement> toReturn = new ArrayList<>();

            for (final DomNode node : allNodes) {
                if (node instanceof DomElement) {
                    toReturn.add(element.getDriver().toWebElement((DomElement) node));
                }
                else {
                    throw new NoSuchElementException("Returned node was not a DOM element");
                }
            }

            return toReturn;
        }

        @Override
        public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
            final DomNode node;

            try {
                node = element.getElement().querySelector(getValue(locator));
            }
            catch (final CSSException ex) {
                throw new NoSuchElementException("Unable to locate element using css", ex);
            }

            if (node instanceof DomElement) {
                return element.getDriver().toWebElement((DomElement) node);
            }

            throw new NoSuchElementException("Returned node (" + node + ") was not a DOM element");
        }
    }

    /**
     * Locator strategy for finding elements by their tag name.
     * <p>
     * This class provides methods to locate a single element or multiple elements
     * on a page or within a specific element by tag name. If no matching elements
     * are found, a {@link NoSuchElementException} is thrown. If the tag name is
     * empty, an {@link InvalidSelectorException} is thrown.
     */
    public static class FindByTagName extends HtmlUnitElementLocator {

        @Override
        public WebElement findElement(final HtmlUnitDriver driver, final By locator) {
            final NodeList allElements = getLastPage(driver).getElementsByTagName(getValue(locator));
            if (allElements.getLength() > 0) {
                return driver.toWebElement((HtmlElement) allElements.item(0));
            }

            throw new NoSuchElementException("Unable to locate element with name: " + getValue(locator));
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            final String name = getValue(locator);
            if ("".equals(name)) {
                throw new InvalidSelectorException("Unable to locate element by xpath for " + getLastPage(driver));
            }

            final SgmlPage lastPage;
            try {
                lastPage = getLastPage(driver);
            }
            catch (final IllegalStateException e) {
                return Collections.emptyList();
            }

            final NodeList allElements = lastPage.getElementsByTagName(name);
            final List<WebElement> toReturn = new ArrayList<>(allElements.getLength());
            for (int i = 0; i < allElements.getLength(); i++) {
                final Node item = allElements.item(i);
                if (item instanceof DomElement) {
                    toReturn.add(driver.toWebElement((DomElement) item));
                }
            }
            return toReturn;
        }

        @Override
        public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
            final NodeList allElements = element.getElement().getElementsByTagName(getValue(locator));
            if (allElements.getLength() > 0) {
                return element.getDriver().toWebElement((HtmlElement) allElements.item(0));
            }

            throw new NoSuchElementException("Unable to locate element with name: " + getValue(locator));
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            final NodeList allElements = element.getElement().getElementsByTagName(getValue(locator));
            final List<WebElement> toReturn = new ArrayList<>(allElements.getLength());
            for (int i = 0; i < allElements.getLength(); i++) {
                final Node item = allElements.item(i);
                if (item instanceof DomElement) {
                    toReturn.add(element.getDriver().toWebElement((DomElement) item));
                }
            }
            return toReturn;
        }
    }

    /**
     * Locator strategy for finding elements using XPath expressions.
     * <p>
     * This class allows locating a single element or multiple elements on a page
     * or within a specific element using an XPath query. If the XPath expression
     * is invalid or cannot be evaluated, an {@link InvalidSelectorException} is thrown.
     * If no matching elements are found, a {@link NoSuchElementException} is thrown.
     */
    public static class FindByXPath extends HtmlUnitElementLocator {

        @Override
        public WebElement findElement(final HtmlUnitDriver driver, final By locator) {
            final Object node;
            final SgmlPage lastPage = getLastPage(driver);
            final String value = getValue(locator);

            try {
                node = lastPage.getFirstByXPath(value);
            }
            catch (final Exception ex) {
                // The xpath expression cannot be evaluated, so the expression is invalid
                throw new InvalidSelectorException(String.format(INVALIDXPATHERROR, value), ex);
            }

            if (node == null) {
                throw new NoSuchElementException("Unable to locate a node using " + value);
            }
            if (node instanceof DomElement) {
                return driver.toWebElement((DomElement) node);
            }
            // The xpath expression selected something different than a WebElement.
            // The selector is therefore invalid
            throw new InvalidSelectorException(String.format(INVALIDSELECTIONERROR, value, node.getClass()));
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            final SgmlPage lastPage;
            try {
                lastPage = getLastPage(driver);
            }
            catch (final IllegalStateException e) {
                return Collections.emptyList();
            }

            final String value = getValue(locator);
            final List<?> nodes;
            try {
                nodes = lastPage.getByXPath(value);
            }
            catch (final RuntimeException ex) {
                // The xpath expression cannot be evaluated, so the expression is invalid
                throw new InvalidSelectorException(String.format(INVALIDXPATHERROR, value), ex);
            }

            final List<WebElement> toReturn = new ArrayList<>(nodes.size());
            for (final Object node : nodes) {
                // There exist elements in the nodes list which could not be converted to
                // WebElements.
                // A valid xpath selector should only select WebElements.
                if (!(node instanceof DomElement)) {
                    // We only want to know the type of one invalid element so that we can give this
                    // information in the exception. We can throw the exception immediately.
                    throw new InvalidSelectorException(String.format(INVALIDSELECTIONERROR, value, node.getClass()));
                }
                toReturn.add(driver.toWebElement((DomElement) node));
            }

            return toReturn;
        }

        @Override
        public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
            final String value = getValue(locator);
            final Object node;
            try {
                node = element.getElement().getFirstByXPath(value);
            }
            catch (final Exception ex) {
                // The xpath expression cannot be evaluated, so the expression is invalid
                throw new InvalidSelectorException(String.format(INVALIDXPATHERROR, value), ex);
            }

            if (node == null) {
                throw new NoSuchElementException("Unable to find an element with xpath " + value);
            }
            if (node instanceof HtmlElement) {
                return element.getDriver().toWebElement((HtmlElement) node);
            }
            // The xpath selector selected something different than a WebElement. The
            // selector is therefore
            // invalid
            throw new InvalidSelectorException(
                    String.format(INVALIDSELECTIONERROR, value, node.getClass().toString()));
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            final String value = getValue(locator);
            final List<WebElement> toReturn = new ArrayList<>();

            final List<?> domElements;
            try {
                domElements = element.getElement().getByXPath(value);
            }
            catch (final Exception ex) {
                // The xpath expression cannot be evaluated, so the expression is invalid
                throw new InvalidSelectorException(String.format(INVALIDXPATHERROR, value), ex);
            }

            for (final Object e : domElements) {
                if (e instanceof DomElement) {
                    toReturn.add(element.getDriver().toWebElement((DomElement) e));
                }
                else {
                    // The xpath selector selected something different than a WebElement. The
                    // selector is
                    // therefore invalid
                    throw new InvalidSelectorException(
                            String.format(INVALIDSELECTIONERROR, value, e.getClass().toString()));
                }
            }
            return toReturn;
        }
    }

    /**
     * Base class for element locators used by {@link HtmlUnitElementFinder}.
     * <p>
     * Provides common utilities for finding elements within a page or another element,
     * as well as methods for retrieving the last loaded page and extracting values from
     * {@link By} locators.
     */
    public abstract static class HtmlUnitElementLocator {

        /**
         * Finds a single element on the given page using the provided locator.
         * <p>
         * If multiple elements match, only the first is returned. If no element is found,
         * a {@link NoSuchElementException} is thrown.
         *
         * @param driver the driver representing the browser/page context
         * @param locator the locator strategy to use
         * @return the first matching {@link WebElement}
         * @throws NoSuchElementException if no element matches the locator
         */
        public WebElement findElement(final HtmlUnitDriver driver, final By locator) {
            final List<WebElement> toReturn = findElements(driver, locator);
            if (!toReturn.isEmpty()) {
                return toReturn.get(0);
            }
            throw new NoSuchElementException("Unable to locate element");
        }

        /**
         * Finds multiple elements on the given page using the provided locator.
         *
         * @param driver the driver representing the browser/page context
         * @param locator the locator strategy to use
         * @return a list of matching {@link WebElement} objects
         */
        public abstract List<WebElement> findElements(HtmlUnitDriver driver, By locator);

        /**
         * Finds a single element within a specific {@link HtmlUnitWebElement}.
         * <p>
         * If multiple elements match, only the first is returned. If no element is found,
         * a {@link NoSuchElementException} is thrown.
         *
         * @param element the element within which to search
         * @param locator the locator strategy to use
         * @return the first matching {@link WebElement}
         * @throws NoSuchElementException if no element matches the locator
         */
        public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
            final List<WebElement> toReturn = findElements(element, locator);
            if (!toReturn.isEmpty()) {
                return toReturn.get(0);
            }
            throw new NoSuchElementException("Unable to locate element");
        }

        /**
         * Finds multiple elements within a specific {@link HtmlUnitWebElement}.
         *
         * @param element the element within which to search
         * @param locator the locator strategy to use
         * @return a list of matching {@link WebElement} objects
         */
        public abstract List<WebElement> findElements(HtmlUnitWebElement element, By locator);

        /**
         * Converts a {@link By} locator to a {@link By.Remotable} locator.
         *
         * @param locator the locator to convert
         * @return the remotable locator
         * @throws IllegalStateException if the locator is not remotable
         */
        protected static By.Remotable getRemotable(final By locator) {
            if (!(locator instanceof By.Remotable)) {
                throw new IllegalStateException("Cannot convert locator to Remotable");
            }
            return (By.Remotable) locator;
        }

        /**
         * Retrieves the last loaded {@link SgmlPage} from the driver.
         *
         * @param driver the driver representing the browser/page context
         * @return the last loaded {@link SgmlPage}
         * @throws IllegalStateException if the current page is not an SgmlPage
         */
        protected SgmlPage getLastPage(final HtmlUnitDriver driver) {
            final Page lastPage = driver.getCurrentWindow().lastPage();
            if (!(lastPage instanceof SgmlPage)) {
                throw new IllegalStateException("Current page is not a SgmlPage");
            }
            return (SgmlPage) lastPage;
        }

        /**
         * Extracts the string value from a remotable {@link By} locator.
         *
         * @param locator the locator from which to extract the value
         * @return the string value of the locator
         */
        protected static String getValue(final By locator) {
            final By.Remotable remote = getRemotable(locator);
            return (String) remote.getRemoteParameters().value();
        }
    }

    /**
     * Converts a list of raw {@link DomElement} objects into {@link WebElement} instances
     * using the provided {@link HtmlUnitDriver}.
     *
     * @param driver the driver used to convert DOM elements into WebElements
     * @param nodes the list of {@link DomElement} objects to convert
     * @return a list of corresponding {@link WebElement} objects
     */
    private static List<WebElement> convertRawDomElementsToWebElements(
            final HtmlUnitDriver driver, final List<DomElement> nodes) {
        final List<WebElement> toReturn = new ArrayList<>(nodes.size());

        for (final DomElement node : nodes) {
            toReturn.add(driver.toWebElement(node));
        }

        return toReturn;
    }

    /**
     * Locator implementation that handles {@link RelativeBy} selectors for
     * finding elements relative to other elements.
     */
    public static class FindByRelativeLocator extends HtmlUnitElementLocator {

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            // Executes a JavaScript snippet to find elements according to the relative locator
            return (List<WebElement>) driver.executeScript(FIND_ELEMENTS_JS, asParameter(locator));
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
        	// TODO Auto-generated method stub
            return null;
        }

        /**
         * Converts a {@link RelativeBy} locator into a parameter map suitable for JavaScript execution.
         *
         * @param locator the relative locator
         * @return a map of the locator's key and value
         */
        private static Object asParameter(final By locator) {
            final Parameters params = ((RelativeBy) locator).getRemoteParameters();
            return Map.of(params.using(), params.value());
        }
    }
}
