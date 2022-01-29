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

import static org.openqa.selenium.htmlunit.HtmlUnitDriver.INVALIDSELECTIONERROR;
import static org.openqa.selenium.htmlunit.HtmlUnitDriver.INVALIDXPATHERROR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.css.parser.CSSException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HtmlUnitElementFinder {

    private final Map<Class<? extends By>, HtmlUnitElementLocator> finders = new HashMap<>();

    HtmlUnitElementFinder() {
      finders.put(By.id("a").getClass(), new FindByID());
      finders.put(By.name("a").getClass(), new FindByName());
      finders.put(By.linkText("a").getClass(), new FindByLinkText());
      finders.put(By.partialLinkText("a").getClass(), new FindByPartialLinkText());
      finders.put(By.className("a").getClass(), new FindByClassName());
      finders.put(By.cssSelector("a").getClass(), new FindByCssSelector());
      finders.put(By.tagName("a").getClass(), new FindByTagName());
      finders.put(By.xpath("//a").getClass(), new FindByXPath());
    }

    public WebElement findElement(HtmlUnitDriver driver, By locator) {
        HtmlUnitElementLocator elementLocator = finders.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElement(driver);
        }

        return elementLocator.findElement(driver, locator);
    }

    public List<WebElement> findElements(HtmlUnitDriver driver, By locator) {
        HtmlUnitElementLocator elementLocator = finders.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElements(driver);
        }

        return elementLocator.findElements(driver, locator);
    }

    public WebElement findElement(HtmlUnitWebElement element, By locator) {
        HtmlUnitElementLocator elementLocator = finders.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElement(element);
        }

        return elementLocator.findElement(element, locator);
    }

    public List<WebElement> findElements(HtmlUnitWebElement element, By locator) {
        HtmlUnitElementLocator elementLocator = finders.get(locator.getClass());
        if (elementLocator == null) {
            return locator.findElements(element);
        }

        return elementLocator.findElements(element, locator);
    }

    public static class FindByID extends HtmlUnitElementLocator {

        @Override
        public WebElement findElement(final HtmlUnitDriver driver, final By locator) {
            SgmlPage lastPage = getLastPage(driver);
            if (!(lastPage instanceof HtmlPage)) {
                throw new IllegalStateException("Cannot find elements by id for " + lastPage);
            }

            String id = getValue(locator);
            DomElement element = ((HtmlPage) lastPage).getElementById(id);

            if (element == null) {
                throw new NoSuchElementException("Unable to locate element with ID: '" + id + "'");
            }
            return driver.toWebElement(element);
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            SgmlPage lastPage = getLastPage(driver);
            if (!(lastPage instanceof HtmlPage)) {
                throw new IllegalStateException("Cannot find elements by id for " + lastPage);
            }

            final List<DomElement> allElements = ((HtmlPage) lastPage).getElementsById(getValue(locator));
            return convertRawDomElementsToWebElements(driver, allElements);
        }

        @Override
        public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
            String id = getValue(locator);
            return new FindByXPath().findElement(element, By.xpath(".//*[@id = '" + id + "']"));
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            String id = getValue(locator);
            return new FindByXPath().findElements(element, By.xpath(".//*[@id = '" + id + "']"));
        }
    }

    public static class FindByName extends HtmlUnitElementLocator {

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            SgmlPage lastPage = getLastPage(driver);
            if (!(lastPage instanceof HtmlPage)) {
                throw new IllegalStateException("Cannot find elements by id for " + lastPage);
            }

            final List<DomElement> allElements = ((HtmlPage) lastPage).getElementsByName(getValue(locator));
            return convertRawDomElementsToWebElements(driver, allElements);
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            String name = getValue(locator);
            return new FindByXPath().findElements(element, By.xpath(".//*[@name = '" + name + "']"));
        }
    }

    public static class FindByLinkText extends HtmlUnitElementLocator {

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            SgmlPage lastPage = getLastPage(driver);
            if (!(lastPage instanceof HtmlPage)) {
                throw new IllegalStateException("Cannot find links for " + lastPage);
            }

            String expectedText = getValue(locator);
            List<HtmlAnchor> anchors = ((HtmlPage) lastPage).getAnchors();

            List<WebElement> toReturn = new ArrayList<>();
            for (HtmlAnchor anchor : anchors) {
                if (expectedText.equals(anchor.asNormalizedText())) {
                    toReturn.add(driver.toWebElement(anchor));
                }
            }
            return toReturn;
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            String expectedText = getValue(locator);
            List<? extends HtmlElement> htmlElements = element.getElement().getElementsByTagName("a");

            List<WebElement> toReturn = new ArrayList<>();
            for (DomElement e : htmlElements) {
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
            SgmlPage lastPage = getLastPage(driver);
            if (!(lastPage instanceof HtmlPage)) {
                throw new IllegalStateException("Cannot find links for " + lastPage);
            }

            String expectedText = getValue(locator);
            List<HtmlAnchor> anchors = ((HtmlPage) lastPage).getAnchors();
            List<WebElement> toReturn = new ArrayList<>();
            for (HtmlAnchor anchor : anchors) {
                if (anchor.asNormalizedText().contains(expectedText)) {
                    toReturn.add(driver.toWebElement(anchor));
                }
            }
            return toReturn;
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            String expectedText = getValue(locator);
            DomNodeList<HtmlElement> anchors = element.getElement().getElementsByTagName("a");

            List<WebElement> toReturn = new ArrayList<>();
            for (HtmlElement anchor : anchors) {
                if (anchor.asNormalizedText().contains(expectedText)) {
                    toReturn.add(element.getDriver().toWebElement(anchor));
                }
            }
            return toReturn;
        }
    }

    public static class FindByClassName extends HtmlUnitElementLocator {

        private String checkValue(By locator) {
            String value = getValue(locator);

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
            DomNode node;

            try {
                node = getLastPage(driver).querySelector(getValue(locator));
            } catch (CSSException ex) {
                throw new NoSuchElementException("Unable to locate element using css", ex);
            }

            if (node instanceof DomElement) {
                return driver.toWebElement((DomElement) node);
            }

            throw new NoSuchElementException("Returned node (" + node + ") was not a DOM element");
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitDriver driver, final By locator) {
            DomNodeList<DomNode> allNodes;

            try {
                allNodes = getLastPage(driver).querySelectorAll(getValue(locator));
            } catch (CSSException ex) {
                throw new NoSuchElementException("Unable to locate element using css", ex);
            }

            List<WebElement> toReturn = new ArrayList<>();

            for (DomNode node : allNodes) {
                if (node instanceof DomElement) {
                    toReturn.add(driver.toWebElement((DomElement) node));
                } else {
                    throw new NoSuchElementException("Returned node was not a DOM element");
                }
            }

            return toReturn;
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            DomNodeList<DomNode> allNodes;

            try {
                allNodes = element.getElement().querySelectorAll(getValue(locator));
            } catch (CSSException ex) {
                throw new NoSuchElementException("Unable to locate element using css", ex);
            }

            List<WebElement> toReturn = new ArrayList<>();

            for (DomNode node : allNodes) {
                if (node instanceof DomElement) {
                    toReturn.add(element.getDriver().toWebElement((DomElement) node));
                } else {
                    throw new NoSuchElementException("Returned node was not a DOM element");
                }
            }

            return toReturn;
        }

        @Override
        public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
            DomNode node;

            try {
                node = element.getElement().querySelector(getValue(locator));
            } catch (CSSException ex) {
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
            NodeList allElements = getLastPage(driver).getElementsByTagName(getValue(locator));
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

            SgmlPage lastPage;
            try {
                lastPage = getLastPage(driver);
            } catch (IllegalStateException e) {
                return Collections.emptyList();
            }

            NodeList allElements = lastPage.getElementsByTagName(name);
            List<WebElement> toReturn = new ArrayList<>(allElements.getLength());
            for (int i = 0; i < allElements.getLength(); i++) {
                Node item = allElements.item(i);
                if (item instanceof DomElement) {
                    toReturn.add(driver.toWebElement((DomElement) item));
                }
            }
            return toReturn;
        }

        @Override
        public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
            NodeList allElements = element.getElement().getElementsByTagName(getValue(locator));
            if (allElements.getLength() > 0) {
                return element.getDriver().toWebElement((HtmlElement) allElements.item(0));
            }

            throw new NoSuchElementException("Unable to locate element with name: " + getValue(locator));
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            NodeList allElements = element.getElement().getElementsByTagName(getValue(locator));
            List<WebElement> toReturn = new ArrayList<>(allElements.getLength());
            for (int i = 0; i < allElements.getLength(); i++) {
                Node item = allElements.item(i);
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
            Object node;
            final SgmlPage lastPage = getLastPage(driver);
            final String value = getValue(locator);

            try {
                node = lastPage.getFirstByXPath(value);
            } catch (Exception ex) {
                // The xpath expression cannot be evaluated, so the expression is invalid
                throw new InvalidSelectorException(
                        String.format(INVALIDXPATHERROR, value),
                        ex);
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
            SgmlPage lastPage;
            try {
                lastPage = getLastPage(driver);
            } catch (IllegalStateException e) {
                return Collections.emptyList();
            }

            final String value = getValue(locator);
            final List<?> nodes;
            try {
                nodes = lastPage.getByXPath(value);
            } catch (RuntimeException ex) {
                // The xpath expression cannot be evaluated, so the expression is invalid
                throw new InvalidSelectorException(String.format(INVALIDXPATHERROR, value), ex);
            }

            List<WebElement> toReturn = new ArrayList<>(nodes.size());
            for (Object node : nodes) {
                // There exist elements in the nodes list which could not be converted to WebElements.
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
            String value = getValue(locator);
            Object node;
            try {
                node = element.getElement().getFirstByXPath(value);
            } catch (Exception ex) {
                // The xpath expression cannot be evaluated, so the expression is invalid
                throw new InvalidSelectorException(
                        String.format(HtmlUnitDriver.INVALIDXPATHERROR, value), ex);
            }

            if (node == null) {
                throw new NoSuchElementException("Unable to find an element with xpath " + value);
            }
            if (node instanceof HtmlElement) {
                return element.getDriver().toWebElement((HtmlElement) node);
            }
            // The xpath selector selected something different than a WebElement. The selector is therefore
            // invalid
            throw new InvalidSelectorException(
                    String.format(HtmlUnitDriver.INVALIDSELECTIONERROR, value, node.getClass().toString()));
        }

        @Override
        public List<WebElement> findElements(final HtmlUnitWebElement element, final By locator) {
            final String value = getValue(locator);
            List<WebElement> toReturn = new ArrayList<>();

            List<?> domElements;
            try {
                domElements = element.getElement().getByXPath(value);
            } catch (Exception ex) {
                // The xpath expression cannot be evaluated, so the expression is invalid
                throw new InvalidSelectorException(
                        String.format(HtmlUnitDriver.INVALIDXPATHERROR, value), ex);
            }

            for (Object e : domElements) {
                if (e instanceof DomElement) {
                    toReturn.add(element.getDriver().toWebElement((DomElement) e));
                } else {
                    // The xpath selector selected something different than a WebElement. The selector is
                    // therefore invalid
                    throw new InvalidSelectorException(
                            String.format(HtmlUnitDriver.INVALIDSELECTIONERROR,
                                    value, e.getClass().toString()));
                }
            }
            return toReturn;
        }
    }

    public abstract static class HtmlUnitElementLocator {

        public WebElement findElement(final HtmlUnitDriver driver, final By locator) {
            List<WebElement> toReturn = findElements(driver, locator);
            if (!toReturn.isEmpty()) {
                return toReturn.get(0);
            }
            throw new NoSuchElementException("Unable to locate element");
        }

        abstract public List<WebElement> findElements(final HtmlUnitDriver driver, By locator);

        public WebElement findElement(final HtmlUnitWebElement element, final By locator) {
            List<WebElement> toReturn = findElements(element, locator);
            if (!toReturn.isEmpty()) {
                return toReturn.get(0);
            }
            throw new NoSuchElementException("Unable to locate element");
        }

        abstract public List<WebElement> findElements(final HtmlUnitWebElement element, By locator);

        protected static By.Remotable getRemotable(By locator) {
            if (!(locator instanceof By.Remotable)) {
                throw new IllegalStateException("Cannot convert locator to Remotable");
            }
            return (By.Remotable) locator;
        }

        protected SgmlPage getLastPage(final HtmlUnitDriver driver) {
            Page lastPage = driver.getWindowManager().lastPage();
            if (!(lastPage instanceof SgmlPage)) {
                throw new IllegalStateException("Current page is not a SgmlPage");
            }
            return (SgmlPage) lastPage;
        }

        protected static String getValue(By locator) {
            By.Remotable remote = getRemotable(locator);
            return (String) remote.getRemoteParameters().value();
        }
    }

    private static List<WebElement> convertRawDomElementsToWebElements(HtmlUnitDriver driver, List<DomElement> nodes) {
        List<WebElement> toReturn = new ArrayList<>(nodes.size());

        for (DomElement node : nodes) {
            toReturn.add(driver.toWebElement(node));
        }

        return toReturn;
    }
}
