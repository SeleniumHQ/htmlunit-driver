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

import com.gargoylesoftware.css.parser.CSSException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.openqa.selenium.htmlunit.HtmlUnitDriver.INVALIDSELECTIONERROR;
import static org.openqa.selenium.htmlunit.HtmlUnitDriver.INVALIDXPATHERROR;

public class HtmlUnitElementFinder {

    public static WebElement findElement(HtmlUnitDriver driver, By locator) {
        return ElementConverter.HtmlUnitElementConverter.getElementObject(driver, locator).findElement(locator);
    }

    public static List<WebElement> findElements(HtmlUnitDriver driver, By locator) {
        return ElementConverter.HtmlUnitElementConverter.getElementObject(driver, locator).findElements(locator);
    }

    public static class FindByID extends ElementObject {

        public FindByID(HtmlUnitDriver driver) {
            super(driver);
        }

        @Override
        public WebElement findElement(By locator) {
            HtmlPage htmlPage = getLastPage();
            By.Remotable remote = getRemotable(locator);

            String id = (String) remote.getRemoteParameters().value();
            DomElement element = htmlPage.getElementById(id);

            if (element == null) {
                throw new NoSuchElementException("Unable to locate element with ID: '" + id + "'");
            }
            return getDriver().toWebElement(element);
        }

        @Override
        public List<WebElement> findElements(By locator) {
            final List<DomElement> allElements = getLastPage().getElementsById(getValue(locator));
            return convertRawDomElementsToWebElements(getDriver(), allElements);
        }
    }

    public static class FindByName extends ElementObject {

        public FindByName(HtmlUnitDriver driver) {
            super(driver);
        }

        @Override
        public List<WebElement> findElements(By locator) {
            final List<DomElement> allElements = getLastPage().getElementsByName(getValue(locator));
            return convertRawDomElementsToWebElements(getDriver(), allElements);
        }
    }

    public static class FindByLinkText extends ElementObject {

        public FindByLinkText(HtmlUnitDriver driver) {
            super(driver);
        }

        @Override
        public List<WebElement> findElements(By locator) {
            List<WebElement> elements = new ArrayList<>();

            List<HtmlAnchor> anchors = getLastPage().getAnchors();
            String value = getValue(locator);

            for (HtmlAnchor anchor : anchors) {
                if (value.trim().equals(anchor.asNormalizedText().trim())) {
                    elements.add(getDriver().toWebElement(anchor));
                }
            }

            return elements;
        }
    }

    public static class FindByPartialLinkText extends ElementObject {

        public FindByPartialLinkText(HtmlUnitDriver driver) {
            super(driver);
        }

        @Override
        public List<WebElement> findElements(By locator) {
            List<HtmlAnchor> anchors = getLastPage().getAnchors();
            List<WebElement> elements = new ArrayList<>();
            for (HtmlAnchor anchor : anchors) {
                if (anchor.asNormalizedText().contains(getValue(locator))) {
                    elements.add(getDriver().toWebElement(anchor));
                }
            }
            return elements;
        }
    }

    public static class FindByClassName extends ElementObject {

        public FindByClassName(HtmlUnitDriver driver) {
            super(driver);
        }

        private String checkValue(By locator) {
            String value = getValue(locator);

            if (value.indexOf(' ') != -1) {
                throw new NoSuchElementException("Compound class names not permitted");
            }
            return value;
        }

        @Override
        public WebElement findElement(By locator) {
            return new FindByCssSelector(getDriver()).findElement(By.cssSelector("." + checkValue(locator)));
        }

        @Override
        public List<WebElement> findElements(By locator) {
            return new FindByCssSelector(getDriver()).findElements(By.cssSelector("." + checkValue(locator)));
        }
    }

    public static class FindByCssSelector extends ElementObject {

        public FindByCssSelector(HtmlUnitDriver driver) {
            super(driver);
        }

        @Override
        public WebElement findElement(By locator) {
            DomNode node;

            try {
                node = getLastPage().querySelector(getValue(locator));
            } catch (CSSException ex) {
                throw new NoSuchElementException("Unable to locate element using css", ex);
            }

            if (node instanceof DomElement) {
                return getDriver().toWebElement((DomElement) node);
            }

            throw new NoSuchElementException("Returned node (" + node + ") was not a DOM element");
        }

        @Override
        public List<WebElement> findElements(By locator) {
            DomNodeList<DomNode> allNodes;

            try {
                allNodes = getLastPage().querySelectorAll(getValue(locator));
            } catch (CSSException ex) {
                throw new NoSuchElementException("Unable to locate element using css", ex);
            }

            List<WebElement> toReturn = new ArrayList<>();

            for (DomNode node : allNodes) {
                if (node instanceof DomElement) {
                    toReturn.add(getDriver().toWebElement((DomElement) node));
                } else {
                    throw new NoSuchElementException("Returned node was not a DOM element");
                }
            }

            return toReturn;
        }
    }

    public static class FindByTagName extends ElementObject {

        public FindByTagName(HtmlUnitDriver driver) {
            super(driver);
        }

        @Override
        public WebElement findElement(By locator) {
            NodeList allElements = getLastPage().getElementsByTagName(getValue(locator));
            if (allElements.getLength() > 0) {
                return getDriver().toWebElement((HtmlElement) allElements.item(0));
            }

            throw new NoSuchElementException("Unable to locate element with name: " + getValue(locator));
        }

        @Override
        public List<WebElement> findElements(By locator) {
            final String name = getValue(locator);
            if ("".equals(name)) {
                throw new InvalidSelectorException("Unable to locate element by xpath for " + getLastPage());
            }

            HtmlPage lastPage;
            try {
                lastPage = getLastPage();
            } catch (IllegalStateException e) {
                return Collections.emptyList();
            }

            NodeList allElements = lastPage.getElementsByTagName(name);
            List<WebElement> toReturn = new ArrayList<>(allElements.getLength());
            for (int i = 0; i < allElements.getLength(); i++) {
                Node item = allElements.item(i);
                if (item instanceof DomElement) {
                    toReturn.add(getDriver().toWebElement((DomElement) item));
                }
            }
            return toReturn;
        }
    }

    public static class FindByXPath extends ElementObject {

        public FindByXPath(HtmlUnitDriver driver) {
            super(driver);
        }

        @Override
        public WebElement findElement(By locator) {
            Object node;
            final HtmlPage lastPage = getLastPage();
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
                return getDriver().toWebElement((DomElement) node);
            }
            // The xpath expression selected something different than a WebElement.
            // The selector is therefore invalid
            throw new InvalidSelectorException(
                    String.format(INVALIDSELECTIONERROR, value, node.getClass()));
        }

        @Override
        public List<WebElement> findElements(By locator) {
            HtmlPage lastPage;
            try {
                lastPage = getLastPage();
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

            List<WebElement> elements = new ArrayList<>(nodes.size());
            for (Object node : nodes) {
                // There exist elements in the nodes list which could not be converted to WebElements.
                // A valid xpath selector should only select WebElements.
                if (!(node instanceof DomElement)) {
                    // We only want to know the type of one invalid element so that we can give this
                    // information in the exception. We can throw the exception immediately.
                    throw new InvalidSelectorException(String.format(INVALIDSELECTIONERROR, value, node.getClass()));
                }
                elements.add(getDriver().toWebElement((DomElement) node));
            }

            return elements;
        }
    }

    public abstract static class ElementObject {
        private final HtmlUnitDriver driver;

        public ElementObject(HtmlUnitDriver driver) {
            this.driver = driver;
        }

        protected HtmlUnitDriver getDriver() {
            return driver;
        }

        public WebElement findElement(By locator) {
            List<WebElement> elements = findElements(locator);
            if (!elements.isEmpty()) {
                return elements.get(0);
            }
            throw new NoSuchElementException("Unable to locate element");
        }

        abstract public List<WebElement> findElements(By locator);

        protected static By.Remotable getRemotable(By locator) {
            if (!(locator instanceof By.Remotable)) {
                throw new IllegalStateException("Cannot convert locator to Remotable");
            }
            return (By.Remotable) locator;
        }

        protected HtmlPage getLastPage() {
            Page lastPage = driver.getWindowManager().lastPage();
            if (!(lastPage instanceof HtmlPage)) {
                throw new IllegalStateException("Cannot find links for " + lastPage);
            }
            return (HtmlPage) lastPage;
        }

        protected static String getValue(By locator) {
            By.Remotable remote = getRemotable(locator);
            return (String) remote.getRemoteParameters().value();
        }
    }

    private static List<WebElement> convertRawDomElementsToWebElements(HtmlUnitDriver driver, List<DomElement> nodes) {
        List<WebElement> elements = new ArrayList<>(nodes.size());

        for (DomElement node : nodes) {
            elements.add(driver.toWebElement(node));
        }

        return elements;
    }
}
