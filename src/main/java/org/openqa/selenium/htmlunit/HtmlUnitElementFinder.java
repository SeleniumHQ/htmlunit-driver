// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit;

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
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
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
    }

    public WebElement findElement(final HtmlUnitDriver driver, final By locator) {
        final HtmlUnitElementLocator elementLocator = finders_.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElement(driver);
        }

        return elementLocator.findElement(driver, locator);
    }

    public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
        final HtmlUnitElementLocator elementLocator = finders_.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElements(driver);
        }

        return elementLocator.findElements(driver, locator);
    }

    public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
        final HtmlUnitElementLocator elementLocator = finders_.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElement(element);
        }

        return elementLocator.findElement(element, locator);
    }

    public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
        final HtmlUnitElementLocator elementLocator = finders_.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElements(element);
        }

        return elementLocator.findElements(element, locator);
    }

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

    public static class FindByClassName extends HtmlUnitElementLocator {

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

    public abstract static class HtmlUnitElementLocator {

        public WebElement findElement(final HtmlUnitDriver driver, final By locator) {
            final List<WebElement> toReturn = findElements(driver, locator);
            if (!toReturn.isEmpty()) {
                return toReturn.get(0);
            }
            throw new NoSuchElementException("Unable to locate element");
        }

        public abstract List<WebElement> findElements(HtmlUnitDriver driver, By locator);

        public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
            final List<WebElement> toReturn = findElements(element, locator);
            if (!toReturn.isEmpty()) {
                return toReturn.get(0);
            }
            throw new NoSuchElementException("Unable to locate element");
        }

        public abstract List<WebElement> findElements(HtmlUnitWebElement element, By locator);

        protected static By.Remotable getRemotable(final By locator) {
            if (!(locator instanceof By.Remotable)) {
                throw new IllegalStateException("Cannot convert locator to Remotable");
            }
            return (By.Remotable) locator;
        }

        protected SgmlPage getLastPage(final HtmlUnitDriver driver) {
            final Page lastPage = driver.getCurrentWindow().lastPage();
            if (!(lastPage instanceof SgmlPage)) {
                throw new IllegalStateException("Current page is not a SgmlPage");
            }
            return (SgmlPage) lastPage;
        }

        protected static String getValue(final By locator) {
            final By.Remotable remote = getRemotable(locator);
            return (String) remote.getRemoteParameters().value();
        }
    }

    private static List<WebElement> convertRawDomElementsToWebElements(
            final HtmlUnitDriver driver, final List<DomElement> nodes) {
        final List<WebElement> toReturn = new ArrayList<>(nodes.size());

        for (final DomElement node : nodes) {
            toReturn.add(driver.toWebElement(node));
        }

        return toReturn;
    }
}
