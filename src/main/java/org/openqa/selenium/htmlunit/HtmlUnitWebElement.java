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

import static com.gargoylesoftware.htmlunit.html.DomElement.ATTRIBUTE_NOT_DEFINED;
import static com.gargoylesoftware.htmlunit.html.DomElement.ATTRIBUTE_VALUE_EMPTY;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptException;
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
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.Colors;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.DisabledElement;
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
import com.gargoylesoftware.htmlunit.html.impl.SelectableTextInput;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLInputElement;

import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

public class HtmlUnitWebElement implements WrapsDriver, WebElement, Coordinates, Locatable {

  protected final HtmlUnitDriver driver;
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

  public HtmlUnitWebElement(HtmlUnitDriver driver, int id, DomElement element) {
    this.driver = driver;
    this.id = id;
    this.element = element;
  }

  @Override
  public void click() {
    verifyCanInteractWithElement(true);
    driver.click(element,true);
  }

  @Override
  public void submit() {
    driver.submit(this);
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
          throw new JavascriptException("Unable to find the containing form");
        }
        submitForm(form);
      } else {
        HtmlUnitWebElement form = findParentForm();
        if (form == null) {
          throw new JavascriptException("Unable to find the containing form");
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
      if (driver.isJavascriptEnabled()) {
        ScriptResult eventResult = form.fireEvent("submit");
        if (!ScriptResult.isFalse(eventResult)) {
          driver.executeScript("arguments[0].submit()", form);
        }
        return;
      }
      throw new WebDriverException("Cannot locate element used to submit form");
    }
    try {
      // this has to ignore the visibility like browsers are doing
      submit.click(false, false, false, true, true, true, false);
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
      htmlInput.setValue("");
      if (htmlInput instanceof SelectableTextInput) {
          ((SelectableTextInput) htmlInput).setSelectionEnd(0);
      }
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
    } else if (!element.getAttribute("contenteditable").equals(ATTRIBUTE_NOT_DEFINED)) {
      element.setTextContent("");
    }
  }

  void verifyCanInteractWithElement(boolean ignoreDisabled) {
    assertElementNotStale();

    Boolean displayed = driver.implicitlyWaitFor(new Callable<Boolean>() {
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
        ((HtmlUnitWebElement) driver.switchTo().activeElement());

    boolean jsEnabled = driver.isJavascriptEnabled();
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
    driver.sendKeys(this, value);
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
      if (ATTRIBUTE_NOT_DEFINED == link) {
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
        if (element instanceof DisabledElement) {
            return trueOrNull(((DisabledElement) element).isDisabled());
        }
        return "true";
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
        return getText();
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

  @Override
  public String getDomProperty(String name) {
    assertElementNotStale();

    final String lowerName = name.toLowerCase();
    String value = element.getAttribute(lowerName);
    if (ATTRIBUTE_NOT_DEFINED == value) {
        return null;
    }

    if ("disabled".equals(lowerName)) {
        if (element instanceof DisabledElement) {
            return trueOrNull(((DisabledElement) element).isDisabled());
        }
    }

    if (ATTRIBUTE_VALUE_EMPTY == value) {
        return null;
    }

    return value;
  }

  @Override
  public String getDomAttribute(String name) {
    assertElementNotStale();

    final String lowerName = name.toLowerCase();
    String value = element.getAttribute(lowerName);
    if (ATTRIBUTE_NOT_DEFINED == value) {
        return null;
    }

    if ("disabled".equals(lowerName)) {
        if (element instanceof DisabledElement) {
            return trueOrNull(((DisabledElement) element).isDisabled());
        }
    }

    return value;
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

    if (element instanceof DisabledElement) {
        return !((DisabledElement) element).isDisabled();
    }
    return true;
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
    return element.getVisibleText();
  }

  protected HtmlUnitDriver getDriver() {
    return driver;
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
      elements.add(getDriver().toWebElement(child));
    }
    return elements;
  }

  @Override
  public WebElement findElement(By by) {
    driver.getAlert().ensureUnlocked();
    return driver.implicitlyWaitFor(() -> {
      assertElementNotStale();
      return driver.findElement(this, by);
    });
  }

  @Override
  public List<WebElement> findElements(By by) {
    driver.getAlert().ensureUnlocked();
    return driver.implicitlyWaitFor(() -> {
      assertElementNotStale();
      return driver.findElements(this, by);
    });
  }

  private HtmlUnitWebElement findParentForm() {
    DomNode current = element;
    while (!(current == null || current instanceof HtmlForm)) {
      current = current.getParentNode();
    }
    return getDriver().toWebElement((HtmlForm) current);
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
    driver.assertElementNotStale(element);
  }

  @Override
  public String getCssValue(String propertyName) {
    assertElementNotStale();

    final HTMLElement elem = element.getScriptableObject();

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
    return driver;
  }


  public Coordinates getCoordinates() {
    return this;
  }


  public Point onScreen() {
    throw new UnsupportedOperationException("Not displayed, no screen location.");
  }


  public Point inViewPort() {
    return getLocation();
  }


  public Point onPage() {
    return getLocation();
  }


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
