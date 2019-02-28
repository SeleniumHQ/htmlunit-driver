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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.interactions.internal.Locatable;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.Colors;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLInputElement;

import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

public class HtmlUnitWebElement implements WrapsDriver,
    FindsById, FindsByLinkText, FindsByXPath, FindsByTagName,
    FindsByCssSelector, Locatable, WebElement, Coordinates {

  protected final HtmlUnitDriver parent;
  protected final int id;
  protected final DomElement element;
  private static final String[] booleanAttributes = {
    "async",
    "autofocus",
    "autoplay",
    "checked",
    "compact",
    "complete",
    "controls",
    "declare",
    "defaultchecked",
    "defaultselected",
    "defer",
    "disabled",
    "draggable",
    "ended",
    "formnovalidate",
    "hidden",
    "indeterminate",
    "iscontenteditable",
    "ismap",
    "itemscope",
    "loop",
    "multiple",
    "muted",
    "nohref",
    "noresize",
    "noshade",
    "novalidate",
    "nowrap",
    "open",
    "paused",
    "pubdate",
    "readonly",
    "required",
    "reversed",
    "scoped",
    "seamless",
    "seeking",
    "selected",
    "spellcheck",
    "truespeed",
    "willvalidate"
    };

  private String toString;

  public HtmlUnitWebElement(HtmlUnitDriver parent, int id, DomElement element) {
    this.parent = parent;
    this.id = id;
    this.element = element;
  }

  @Override
  public void click() {
    verifyCanInteractWithElement(true);
    parent.click(element, true);
  }

  @Override
  public void submit() {
    parent.submit(this);
  }

  void submitImpl() {
    try {
      if (element instanceof HtmlForm) {
        submitForm((HtmlForm) element);
      } else if ((element instanceof HtmlSubmitInput) || (element instanceof HtmlImageInput)) {
        element.click();
      } else if (element instanceof HtmlInput) {
        HtmlForm form = ((HtmlElement) element).getEnclosingForm();
        if (form == null) {
          throw new NoSuchElementException("Unable to find the containing form");
        }
        submitForm(form);
      } else {
        HtmlUnitWebElement form = findParentForm();
        if (form == null) {
          throw new NoSuchElementException("Unable to find the containing form");
        }
        form.submitImpl();
      }
    } catch (IOException e) {
      throw new WebDriverException(e);
    }
  }

  private void submitForm(HtmlForm form) {
    assertElementNotStale();

    List<HtmlElement> allElements = new ArrayList<>();
    allElements.addAll(form.getElementsByTagName("input"));
    allElements.addAll(form.getElementsByTagName("button"));

    HtmlElement submit = null;
    for (HtmlElement e : allElements) {
      if (!isSubmitElement(e)) {
        continue;
      }

      if (submit == null) {
        submit = e;
      }
    }

    if (submit == null) {
      if (parent.isJavascriptEnabled()) {
        ScriptResult eventResult = form.fireEvent("submit");
        if (!ScriptResult.isFalse(eventResult)) {
          parent.executeScript("arguments[0].submit()", form);
        }
        return;
      }
      throw new WebDriverException("Cannot locate element used to submit form");
    }
    try {
      // this has to ignore the visibility like browsers are doing
      submit.click(false, false, false, true, true, false);
    } catch (IOException e) {
      throw new WebDriverException(e);
    }
  }

  private static boolean isSubmitElement(HtmlElement element) {
    HtmlElement candidate = null;

    if (element instanceof HtmlSubmitInput && !((HtmlSubmitInput) element).isDisabled()) {
      candidate = element;
    } else if (element instanceof HtmlImageInput && !((HtmlImageInput) element).isDisabled()) {
      candidate = element;
    } else if (element instanceof HtmlButton) {
      HtmlButton button = (HtmlButton) element;
      if ("submit".equalsIgnoreCase(button.getTypeAttribute()) && !button.isDisabled()) {
        candidate = element;
      }
    }

    return candidate != null;
  }

  @Override
  public void clear() {
    assertElementNotStale();

    if (element instanceof HtmlInput) {
      HtmlInput htmlInput = (HtmlInput) element;
      if (htmlInput.isReadOnly()) {
        throw new InvalidElementStateException("You may only edit editable elements");
      }
      if (htmlInput.isDisabled()) {
        throw new InvalidElementStateException("You may only interact with enabled elements");
      }
      htmlInput.setValueAttribute("");
      htmlInput.fireEvent("change");
    } else if (element instanceof HtmlTextArea) {
      HtmlTextArea htmlTextArea = (HtmlTextArea) element;
      if (htmlTextArea.isReadOnly()) {
        throw new InvalidElementStateException("You may only edit editable elements");
      }
      if (htmlTextArea.isDisabled()) {
        throw new InvalidElementStateException("You may only interact with enabled elements");
      }
      htmlTextArea.setText("");
    } else if (!element.getAttribute("contenteditable").equals(DomElement.ATTRIBUTE_NOT_DEFINED)) {
      element.setTextContent("");
    }
  }

  void verifyCanInteractWithElement(boolean ignoreDisabled) {
    assertElementNotStale();

    Boolean displayed = parent.implicitlyWaitFor(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return isDisplayed();
      }
    });

    if (displayed == null || !displayed) {
      throw new ElementNotInteractableException("You may only interact with visible elements");
    }

    if (!ignoreDisabled && !isEnabled()) {
      throw new InvalidElementStateException("You may only interact with enabled elements");
    }
  }

  void switchFocusToThisIfNeeded() {
    HtmlUnitWebElement oldActiveElement =
        ((HtmlUnitWebElement) parent.switchTo().activeElement());

    boolean jsEnabled = parent.isJavascriptEnabled();
    boolean oldActiveEqualsCurrent = oldActiveElement.equals(this);
    try {
      boolean isBody = oldActiveElement.getTagName().toLowerCase().equals("body");
      if (jsEnabled &&
          !oldActiveEqualsCurrent &&
          !isBody) {
        oldActiveElement.element.blur();
      }
    } catch (StaleElementReferenceException ex) {
      // old element has gone, do nothing
    }
    element.focus();
  }

  @Override
  public void sendKeys(CharSequence... value) {
    if (value == null) {
      throw new IllegalArgumentException("Keys to send should nor be null");
    }
    parent.sendKeys(this, value);
  }

  @Override
  public String getTagName() {
    assertElementNotStale();
    return element.getNodeName();
  }

  @Override
  public String getAttribute(String name) {
    assertElementNotStale();

    final String lowerName = name.toLowerCase();

    String value = element.getAttribute(name);

    if (element instanceof HtmlInput &&
        ("selected".equals(lowerName) || "checked".equals(lowerName))) {
      return trueOrNull(((HtmlInput) element).isChecked());
    }

    if ("href".equals(lowerName) || "src".equals(lowerName)) {
      String link = element.getAttribute(name);
      if (DomElement.ATTRIBUTE_NOT_DEFINED == link) {
          return null;
      }
      HtmlPage page = (HtmlPage) element.getPage();
      try {
        return page.getFullyQualifiedUrl(link.trim()).toString();
      } catch (MalformedURLException e) {
        return null;
      }
    }
    if ("disabled".equals(lowerName)) {
      return trueOrNull(!isEnabled());
    }

    if ("multiple".equals(lowerName) && element instanceof HtmlSelect) {
      String multipleAttribute = ((HtmlSelect) element).getMultipleAttribute();
      if ("".equals(multipleAttribute)) {
        return trueOrNull(element.hasAttribute("multiple"));
      }
      return "true";
    }

    for (String booleanAttribute : booleanAttributes) {
      if (booleanAttribute.equals(lowerName)) {
        return trueOrNull(element.hasAttribute(lowerName));
      }
    }
    if ("index".equals(lowerName) && element instanceof HtmlOption) {
      HtmlSelect select = ((HtmlOption) element).getEnclosingSelect();
      List<HtmlOption> allOptions = select.getOptions();
      for (int i = 0; i < allOptions.size(); i++) {
        HtmlOption option = select.getOption(i);
        if (element.equals(option)) {
          return String.valueOf(i);
        }
      }

      return null;
    }

    if ("value".equals(lowerName)) {
      if (element instanceof HtmlFileInput) {
        return ((HTMLInputElement) element.getScriptableObject()).getValue();
      }
      if (element instanceof HtmlTextArea) {
        return ((HtmlTextArea) element).getText();
      }

      // According to
      // http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-value-OPTION
      // if the value attribute doesn't exist, getting the "value" attribute defers to the
      // option's content.
      if (element instanceof HtmlOption && !element.hasAttribute("value")) {
        return element.getTextContent();
      }

      return value == null ? "" : value;
    }

    if (!value.isEmpty()) {
      return value;
    }

    if (element.hasAttribute(name)) {
      return "";
    }

    final Object scriptable = element.getScriptableObject();
    if (scriptable instanceof Scriptable) {
      final Object slotVal = ScriptableObject.getProperty((Scriptable) scriptable, name);
      if (slotVal instanceof String) {
        return (String) slotVal;
      }
    }

    return null;
  }

  private static String trueOrNull(boolean condition) {
    return condition ? "true" : null;
  }

  @Override
  public boolean isSelected() {
    assertElementNotStale();

    if (element instanceof HtmlInput) {
      return ((HtmlInput) element).isChecked();
    } else if (element instanceof HtmlOption) {
      return ((HtmlOption) element).isSelected();
    }

    throw new UnsupportedOperationException(
        "Unable to determine if element is selected. Tag name is: " + element.getTagName());
  }

  @Override
  public boolean isEnabled() {
    assertElementNotStale();

    return !element.hasAttribute("disabled");
  }

  @Override
  public boolean isDisplayed() {
    assertElementNotStale();

    return element.isDisplayed();
  }

  @Override
  public Point getLocation() {
    assertElementNotStale();

    try {
      return new Point(readAndRound("left"), readAndRound("top"));
    } catch (Exception e) {
      throw new WebDriverException("Cannot determine size of element", e);
    }
  }

  @Override
  public Dimension getSize() {
    assertElementNotStale();

    try {
      final int width = readAndRound("width");
      final int height = readAndRound("height");
      return new Dimension(width, height);
    } catch (Exception e) {
      throw new WebDriverException("Cannot determine size of element", e);
    }
  }

  @Override
  public Rectangle getRect() {
    return new Rectangle(getLocation(), getSize());
  }

  private int readAndRound(final String property) {
    final String cssValue = getCssValue(property).replaceAll("[^0-9\\.]", "");
    if (cssValue.isEmpty()) {
      return 5; // wrong... but better than nothing
    }
    return Math.round(Float.parseFloat(cssValue));
  }

  @Override
  public String getText() {
    assertElementNotStale();
    return HtmlSerializer.getText(element);
  }

  protected HtmlUnitDriver getParent() {
    return parent;
  }

  protected DomElement getElement() {
    return element;
  }

  @Deprecated // It's not a part of WebDriver API
  public List<WebElement> getElementsByTagName(String tagName) {
    assertElementNotStale();

    List<?> allChildren = element.getByXPath(".//" + tagName);
    List<WebElement> elements = new ArrayList<>();
    for (Object o : allChildren) {
      if (!(o instanceof HtmlElement)) {
        continue;
      }

      HtmlElement child = (HtmlElement) o;
      elements.add(getParent().toWebElement(child));
    }
    return elements;
  }

  @Override
  public WebElement findElement(By by) {
    assertElementNotStale();
    return parent.findElement(by, this);
  }

  @Override
  public List<WebElement> findElements(By by) {
    assertElementNotStale();
    return parent.findElements(by, this);
  }

  @Override
  public WebElement findElementById(String elemId) {
    assertElementNotStale();

    return findElementByXPath(".//*[@id = '" + elemId + "']");
  }

  @Override
  public List<WebElement> findElementsById(String elemId) {
    assertElementNotStale();

    return findElementsByXPath(".//*[@id = '" + elemId + "']");
  }

  @Override
  public List<WebElement> findElementsByCssSelector(String using) {
    List<WebElement> allElements = parent.findElementsByCssSelector(using);

    return findChildNodes(allElements);
  }

  @Override
  public WebElement findElementByCssSelector(String using) {
    List<WebElement> allElements = parent.findElementsByCssSelector(using);

    allElements = findChildNodes(allElements);

    if (allElements.isEmpty()) {
      throw new NoSuchElementException("Cannot find child element using css: " + using);
    }

    return allElements.get(0);
  }

  private List<WebElement> findChildNodes(List<WebElement> allElements) {
    List<WebElement> toReturn = new LinkedList<>();

    for (WebElement current : allElements) {
      DomElement candidate = ((HtmlUnitWebElement) current).element;
      if (element.isAncestorOf(candidate) && element != candidate) {
        toReturn.add(current);
      }
    }

    return toReturn;
  }

  @Override
  public WebElement findElementByXPath(String xpathExpr) {
    assertElementNotStale();

    Object node;
    try {
      node = element.getFirstByXPath(xpathExpr);
    } catch (Exception ex) {
      // The xpath expression cannot be evaluated, so the expression is invalid
      throw new InvalidSelectorException(
          String.format(HtmlUnitDriver.INVALIDXPATHERROR, xpathExpr), ex);
    }

    if (node == null) {
      throw new NoSuchElementException("Unable to find an element with xpath " + xpathExpr);
    }
    if (node instanceof HtmlElement) {
      return getParent().toWebElement((HtmlElement) node);
    }
    // The xpath selector selected something different than a WebElement. The selector is therefore
    // invalid
    throw new InvalidSelectorException(
        String.format(HtmlUnitDriver.INVALIDSELECTIONERROR, xpathExpr, node.getClass().toString()));
  }

  @Override
  public List<WebElement> findElementsByXPath(String xpathExpr) {
    assertElementNotStale();

    List<WebElement> webElements = new ArrayList<>();

    List<?> domElements;
    try {
      domElements = element.getByXPath(xpathExpr);
    } catch (Exception ex) {
      // The xpath expression cannot be evaluated, so the expression is invalid
      throw new InvalidSelectorException(
          String.format(HtmlUnitDriver.INVALIDXPATHERROR, xpathExpr), ex);
    }

    for (Object e : domElements) {
      if (e instanceof DomElement) {
        webElements.add(getParent().toWebElement((DomElement) e));
      }
      else {
        // The xpath selector selected something different than a WebElement. The selector is
        // therefore invalid
        throw new InvalidSelectorException(
            String.format(HtmlUnitDriver.INVALIDSELECTIONERROR,
                xpathExpr, e.getClass().toString()));
      }
    }
    return webElements;
  }

  @Override
  public WebElement findElementByLinkText(String linkText) {
    assertElementNotStale();

    List<WebElement> elements = findElementsByLinkText(linkText);
    if (elements.isEmpty()) {
      throw new NoSuchElementException("Unable to find element with linkText " + linkText);
    }
    return elements.get(0);
  }

  @Override
  public List<WebElement> findElementsByLinkText(String linkText) {
    assertElementNotStale();

    String expectedText = linkText.trim();
    List<? extends HtmlElement> htmlElements = ((HtmlElement) element).getElementsByTagName("a");
    List<WebElement> webElements = new ArrayList<>();
    for (DomElement e : htmlElements) {
      if (expectedText.equals(e.getTextContent().trim()) && e.getAttribute("href") != null) {
        webElements.add(getParent().toWebElement(e));
      }
    }
    return webElements;
  }

  @Override
  public WebElement findElementByPartialLinkText(String linkText) {
    assertElementNotStale();

    List<WebElement> elements = findElementsByPartialLinkText(linkText);
    if (elements.isEmpty()) {
      throw new NoSuchElementException(
          "Unable to find element with linkText " + linkText);
    }
    return elements.size() > 0 ? elements.get(0) : null;
  }

  @Override
  public List<WebElement> findElementsByPartialLinkText(String linkText) {
    assertElementNotStale();

    List<? extends HtmlElement> htmlElements = ((HtmlElement) element).getElementsByTagName("a");
    List<WebElement> webElements = new ArrayList<>();
    for (HtmlElement e : htmlElements) {
      if (e.getTextContent().contains(linkText)
          && e.getAttribute("href") != null) {
        webElements.add(getParent().toWebElement(e));
      }
    }
    return webElements;
  }

  @Override
  public WebElement findElementByTagName(String name) {
    assertElementNotStale();

    List<WebElement> elements = findElementsByTagName(name);
    if (elements.isEmpty()) {
      throw new NoSuchElementException("Cannot find element with tag name: " + name);
    }
    return elements.get(0);
  }

  @Override
  public List<WebElement> findElementsByTagName(String name) {
    assertElementNotStale();

    List<HtmlElement> elements = ((HtmlElement) element).getElementsByTagName(name);
    List<WebElement> toReturn = new ArrayList<>(elements.size());
    for (HtmlElement e : elements) {
      toReturn.add(parent.toWebElement(e));
    }

    return toReturn;
  }

  private HtmlUnitWebElement findParentForm() {
    DomNode current = element;
    while (!(current == null || current instanceof HtmlForm)) {
      current = current.getParentNode();
    }
    return getParent().toWebElement((HtmlForm) current);
  }

  @Override
  public String toString() {
    if (toString == null) {
      StringBuilder sb = new StringBuilder();
      sb.append('<').append(element.getTagName());
      NamedNodeMap attributes = element.getAttributes();
      int n = attributes.getLength();
      for (int i = 0; i < n; ++i) {
        Attr a = (Attr) attributes.item(i);
        sb.append(' ').append(a.getName()).append("=\"")
          .append(a.getValue().replace("\"", "&quot;")).append("\"");
      }
      if (element.hasChildNodes()) {
        sb.append('>');
      } else {
        sb.append(" />");
      }
      toString = sb.toString();
    }
    return toString;
  }

  protected void assertElementNotStale() {
    parent.assertElementNotStale(element);
  }

  @Override
  public String getCssValue(String propertyName) {
    assertElementNotStale();

    final HTMLElement elem = (HTMLElement) ((HtmlElement) element).getScriptableObject();

    String style = elem.getWindow().getComputedStyle(elem, null).getPropertyValue(propertyName);

    return getColor(style);
  }

  private static String getColor(String name) {
    if ("null".equals(name)) {
      return "transparent";
    }
    if (name.startsWith("rgb(")) {
      return Color.fromString(name).asRgba();
    }

    Colors colors = getColorsOf(name);
    if (colors != null) {
      return colors.getColorValue().asRgba();
    }
    return name;
  }

  private static Colors getColorsOf(String name) {
    name = name.toUpperCase();
    for (Colors colors : Colors.values()) {
      if (colors.name().equals(name)) {
        return colors;
      }
    }
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof WebElement)) {
      return false;
    }

    WebElement other = (WebElement) obj;
    if (other instanceof WrapsElement) {
      other = ((WrapsElement) obj).getWrappedElement();
    }

    return other instanceof HtmlUnitWebElement &&
        element.equals(((HtmlUnitWebElement) other).element);
  }

  @Override
  public int hashCode() {
    return element.hashCode();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.openqa.selenium.WrapsDriver#getContainingDriver()
   */
  @Override
  public WebDriver getWrappedDriver() {
    return parent;
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
    return element;
  }

  @Override
  public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
    throw new UnsupportedOperationException(
      "Screenshots are not enabled for HtmlUnitDriver");
  }

  public int getId() {
    return id;
  }
}
