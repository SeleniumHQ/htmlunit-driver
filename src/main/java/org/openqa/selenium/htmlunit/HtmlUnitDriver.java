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

import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.local.HtmlUnitLocalDriver;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.internal.FindsByClassName;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * An implementation of {@link WebDriver} that drives <a href="http://htmlunit.sourceforge.net/">HtmlUnit</a>,
 * which is a headless (GUI-less) browser simulator.
 * <p>The main supported browsers are Chrome, Firefox and Internet Explorer.
 */
public class HtmlUnitDriver implements WebDriver, JavascriptExecutor,
    FindsById, FindsByLinkText, FindsByXPath, FindsByName, FindsByCssSelector,
    FindsByTagName, FindsByClassName, HasCapabilities, HasInputDevices {

  protected HtmlUnitLocalDriver driver;
  protected Object lock = new Object();

  /**
   * Constructs a new instance with JavaScript disabled,
   * and the {@link BrowserVersion#getDefault() default} BrowserVersion.
   */
  public HtmlUnitDriver() {
    this(false);
  }

  /**
   * Constructs a new instance, specify JavaScript support
   * and using the {@link BrowserVersion#getDefault() default} BrowserVersion.
   *
   * @param enableJavascript whether to enable JavaScript support or not
   */
  public HtmlUnitDriver(boolean enableJavascript) {
    this(BrowserVersion.getDefault(), enableJavascript);
  }

  /**
   * Constructs a new instance with the specified {@link BrowserVersion} and the JavaScript support.
   *
   * @param version the browser version to use
   * @param enableJavascript whether to enable JavaScript support or not
   */
  public HtmlUnitDriver(BrowserVersion version, boolean enableJavascript) {
    driver = new HtmlUnitLocalDriver(version, enableJavascript);
  }

  /**
   * Constructs a new instance with the specified {@link BrowserVersion}.
   *
   * @param version the browser version to use
   */
  public HtmlUnitDriver(BrowserVersion version) {
    driver = new HtmlUnitLocalDriver(version);
  }

  /**
   * Note: There are two configuration modes for the HtmlUnitDriver using this constructor.
   * <ol>
   *   <li>The first is where the browserName is "chrome", "firefox" or "internet explorer"
   *       and browserVersion denotes the desired version.</li>
   *   <li>The second one is where the browserName is "htmlunit" and the browserVersion
   *       denotes the required browser AND its version. In this mode the browserVersion could be
   *       "chrome" for Chrome, "firefox-38" for Firefox 38 or "internet explorer-11" for IE 11.</li>
   * </ol>
   * <p>The Remote WebDriver uses the second mode - the first mode is deprecated and should not be used.
   *
   * @param capabilities desired capabilities requested for the htmlunit driver session
   */
  public HtmlUnitDriver(Capabilities capabilities) {
    driver = new HtmlUnitLocalDriver(capabilities);
  }

  public HtmlUnitDriver(Capabilities desiredCapabilities, Capabilities requiredCapabilities) {
    driver = new HtmlUnitLocalDriver(desiredCapabilities, requiredCapabilities);
  }

  @Override
  public Keyboard getKeyboard() {
    return driver.getKeyboard();
  }

  @Override
  public Mouse getMouse() {
    return driver.getMouse();
  }

  @Override
  public Capabilities getCapabilities() {
    return driver.getCapabilities();
  }

  @Override
  public WebElement findElementByClassName(String using) {
    return driver.findElementByClassName(using);
  }

  @Override
  public List<WebElement> findElementsByClassName(String using) {
    return driver.findElementsByClassName(using);
  }

  @Override
  public WebElement findElementByTagName(String using) {
    return driver.findElementByTagName(using);
  }

  @Override
  public List<WebElement> findElementsByTagName(String using) {
    return driver.findElementsByTagName(using);
  }

  @Override
  public WebElement findElementByCssSelector(String using) {
    return driver.findElementByCssSelector(using);
  }

  @Override
  public List<WebElement> findElementsByCssSelector(String using) {
    return driver.findElementsByCssSelector(using);
  }

  @Override
  public WebElement findElementByName(String using) {
    return driver.findElementByName(using);
  }

  @Override
  public List<WebElement> findElementsByName(String using) {
    return driver.findElementsByName(using);
  }

  @Override
  public WebElement findElementByXPath(String using) {
    return driver.findElementByXPath(using);
  }

  @Override
  public List<WebElement> findElementsByXPath(String using) {
    return driver.findElementsByXPath(using);
  }

  @Override
  public WebElement findElementByLinkText(String using) {
    return driver.findElementByLinkText(using);
  }

  @Override
  public List<WebElement> findElementsByLinkText(String using) {
    return driver.findElementsByLinkText(using);
  }

  @Override
  public WebElement findElementByPartialLinkText(String using) {
    return driver.findElementByPartialLinkText(using);
  }

  @Override
  public List<WebElement> findElementsByPartialLinkText(String using) {
    return driver.findElementsByPartialLinkText(using);
  }

  @Override
  public WebElement findElementById(String using) {
    return driver.findElementById(using);
  }

  @Override
  public List<WebElement> findElementsById(String using) {
    return driver.findElementsById(using);
  }

  @Override
  public Object executeScript(String script, Object... args) {
    return driver.executeScript(script, args);
  }

  @Override
  public Object executeAsyncScript(String script, Object... args) {
    return driver.executeAsyncScript(script, args);
  }

  @Override
  public void get(String url) {
    driver.get(url);
  }

  @Override
  public String getCurrentUrl() {
    return driver.getCurrentUrl();
  }

  @Override
  public String getTitle() {
    return driver.getTitle();
  }

  @Override
  public List<WebElement> findElements(By by) {
    return driver.findElements(by);
  }

  @Override
  public WebElement findElement(By by) {
    return driver.findElement(by);
  }

  @Override
  public String getPageSource() {
    return driver.getPageSource();
  }

  @Override
  public void close() {
    driver.close();
  }

  @Override
  public void quit() {
    driver.quit();
  }

  @Override
  public Set<String> getWindowHandles() {
    return driver.getWindowHandles();
  }

  @Override
  public String getWindowHandle() {
    return driver.getWindowHandle();
  }

  @Override
  public TargetLocator switchTo() {
    return driver.switchTo();
  }

  @Override
  public Navigation navigate() {
    return driver.navigate();
  }

  @Override
  public Options manage() {
    return driver.manage();
  }
  
}
