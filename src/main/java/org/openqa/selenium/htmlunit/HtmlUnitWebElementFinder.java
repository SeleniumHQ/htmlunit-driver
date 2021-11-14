package org.openqa.selenium.htmlunit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class HtmlUnitWebElementFinder {

    public static WebElement findElement(HtmlUnitDriver driver, DomElement element, By locator) {
        return ElementConverter.HtmlUnitWebElementConverter.getElementObject(driver, element, locator).findElement(locator);
    }

    public static List<WebElement> findElements(HtmlUnitDriver driver, DomElement element, By locator) {
        return ElementConverter.HtmlUnitWebElementConverter.getElementObject(driver, element, locator).findElements(locator);
    }

    public static class WebFindById extends WebElementObject {
        public WebFindById(HtmlUnitDriver driver, DomElement element) {
            super(driver, element);
        }

        protected By.ByXPath getXpath(By locator) {
            return new By.ByXPath(".//*[@id = '" + getValue(locator) + "']");
        }

        @Override
        public WebElement findElement(By locator) {
            return new HtmlUnitWebElementFinder.WebFindByXPath(getDriver(), getElement()).findElement(getXpath(locator));
        }

        @Override
        public List<WebElement> findElements(By locator) {
            return new HtmlUnitWebElementFinder.WebFindByXPath(getDriver(), getElement()).findElements(getXpath(locator));
        }
    }

    public static class WebFindByName extends WebElementObject {

        public WebFindByName(HtmlUnitDriver driver, DomElement element) {
            super(driver, element);
        }

        protected By.ByXPath getXpath(By locator) {
            return new By.ByXPath(".//*[@name = '" + getValue(locator) + "']");
        }

        @Override
        public WebElement findElement(By locator) {
            return new HtmlUnitWebElementFinder.WebFindByXPath(getDriver(), getElement()).findElement(getXpath(locator));
        }

        @Override
        public List<WebElement> findElements(By locator) {
            return new HtmlUnitWebElementFinder.WebFindByXPath(getDriver(), getElement()).findElements(getXpath(locator));
        }
    }

    public static class WebFindByClassName extends WebElementObject {

        public WebFindByClassName(HtmlUnitDriver driver, DomElement element) {
            super(driver, element);
        }

        protected By.ByXPath getXpath(By locator) {
            return new By.ByXPath(".//*[@class = '" + getValue(locator) + "']");
        }

        @Override
        public WebElement findElement(By locator) {
            return new HtmlUnitWebElementFinder.WebFindByXPath(getDriver(), getElement()).findElement(getXpath(locator));
        }

        @Override
        public List<WebElement> findElements(By locator) {
            return new HtmlUnitWebElementFinder.WebFindByXPath(getDriver(), getElement()).findElements(getXpath(locator));
        }
    }

    public static class WebFindByTagName extends WebElementObject {

        public WebFindByTagName(HtmlUnitDriver driver, DomElement element) {
            super(driver, element);
        }

        @Override
        public List<WebElement> findElements(By locator) {
            List<HtmlElement> elements = getElement().getElementsByTagName(getValue(locator));
            List<WebElement> toReturn = new ArrayList<>(elements.size());
            for (HtmlElement e : elements) {
                toReturn.add(getDriver().toWebElement(e));
            }
            return toReturn;
        }
    }

    public static class WebFindByCssSelector extends WebElementObject {
        public WebFindByCssSelector(HtmlUnitDriver driver, DomElement element) {
            super(driver, element);
        }

        @Override
        public WebElement findElement(By locator) {
            List<WebElement> elements = (new HtmlUnitElementFinder.FindByCssSelector(getDriver())).findElements(locator);
            elements = findChildNodes(elements);
            if (elements.isEmpty()) {
                throw new NoSuchElementException("Cannot find child element using css: " + getValue(locator));
            }

            return elements.get(0);
        }

        @Override
        public List<WebElement> findElements(By locator) {
            List<WebElement> elements = (new HtmlUnitElementFinder.FindByCssSelector(getDriver())).findElements(locator);
            return findChildNodes(elements);
        }

        private List<WebElement> findChildNodes(List<WebElement> allElements) {
            List<WebElement> toReturn = new LinkedList<>();

            for (WebElement current : allElements) {
                DomElement candidate = ((HtmlUnitWebElement) current).element;
                if (getElement().isAncestorOf(candidate) && getElement() != candidate) {
                    toReturn.add(current);
                }
            }

            return toReturn;
        }
    }

    public static class WebFindByLinkText extends WebElementObject {
        public WebFindByLinkText(HtmlUnitDriver driver, DomElement element) {
            super(driver, element);
        }

        @Override
        public List<WebElement> findElements(By locator) {
            String expectedText = getValue(locator).trim();
            List<? extends HtmlElement> htmlElements = getElement().getElementsByTagName("a");

            List<WebElement> webElements = new ArrayList<>();
            for (DomElement e : htmlElements) {
                if (expectedText.equals(e.asNormalizedText()) && e.getAttribute("href") != null) {
                    webElements.add(getDriver().toWebElement(e));
                }
            }
            return webElements;
        }
    }

    public static class WebFindByPartialLinkText extends WebElementObject {
        public WebFindByPartialLinkText(HtmlUnitDriver driver, DomElement element) {
            super(driver, element);
        }

        @Override
        public List<WebElement> findElements(By locator) {
            List<? extends HtmlElement> htmlElements = getElement().getElementsByTagName("a");
            List<WebElement> webElements = new ArrayList<>();
            final String value = getValue(locator);
            for (HtmlElement e : htmlElements) {
                if (e.asNormalizedText().contains(value) && e.getAttribute("href") != null) {
                    webElements.add(getDriver().toWebElement(e));
                }
            }
            return webElements;
        }
    }

    public static class WebFindByXPath extends WebElementObject {
        public WebFindByXPath(HtmlUnitDriver driver, DomElement element) {
            super(driver, element);
        }

        @Override
        public WebElement findElement(By locator) {
            String value = getValue(locator);
            Object node;
            try {
                node = getElement().getFirstByXPath(value);
            } catch (Exception ex) {
                // The xpath expression cannot be evaluated, so the expression is invalid
                throw new InvalidSelectorException(
                        String.format(HtmlUnitDriver.INVALIDXPATHERROR, value), ex);
            }

            if (node == null) {
                throw new NoSuchElementException("Unable to find an element with xpath " + value);
            }
            if (node instanceof HtmlElement) {
                return getDriver().toWebElement((HtmlElement) node);
            }
            // The xpath selector selected something different than a WebElement. The selector is therefore
            // invalid
            throw new InvalidSelectorException(
                    String.format(HtmlUnitDriver.INVALIDSELECTIONERROR, value, node.getClass().toString()));
        }

        @Override
        public List<WebElement> findElements(By locator) {
            final String value = getValue(locator);
            List<WebElement> webElements = new ArrayList<>();

            List<?> domElements;
            try {
                domElements = getElement().getByXPath(value);
            } catch (Exception ex) {
                // The xpath expression cannot be evaluated, so the expression is invalid
                throw new InvalidSelectorException(
                        String.format(HtmlUnitDriver.INVALIDXPATHERROR, value), ex);
            }

            for (Object e : domElements) {
                if (e instanceof DomElement) {
                    webElements.add(getDriver().toWebElement((DomElement) e));
                } else {
                    // The xpath selector selected something different than a WebElement. The selector is
                    // therefore invalid
                    throw new InvalidSelectorException(
                            String.format(HtmlUnitDriver.INVALIDSELECTIONERROR,
                                    value, e.getClass().toString()));
                }
            }
            return webElements;
        }
    }

    public abstract static class WebElementObject extends HtmlUnitElementFinder.ElementObject {
        private final DomElement element;

        public WebElementObject(HtmlUnitDriver driver, DomElement element) {
            super(driver);
            this.element = element;
        }

        public DomElement getElement() {
            return element;
        }
    }
}
