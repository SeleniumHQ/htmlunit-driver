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

package org.openqa.selenium.htmlunit.remote;

import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.local.HtmlUnitKeyboard;
import org.openqa.selenium.htmlunit.local.HtmlUnitLocalDriver;
import org.openqa.selenium.htmlunit.local.HtmlUnitWebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;

public class RemoteDriverWrapper implements WebDriver, JavascriptExecutor, HasInputDevices {

  private HtmlUnitLocalDriver driver;
  private HtmlUnitKeyboard keyboard;

  private HtmlUnitWebElement lastElement;

  public RemoteDriverWrapper(Capabilities desiredCapabilities, Capabilities requiredCapabilities) {
    driver = new HtmlUnitLocalDriver(desiredCapabilities, requiredCapabilities);
    keyboard = (HtmlUnitKeyboard) driver.getKeyboard();
  }

  public HtmlUnitWebElement getElementById(int id) {
    return driver.getElementById(id);
  }

  public void moveTo(int elementId) {
    lastElement = getElementById(elementId);
    new Actions(this).moveToElement(lastElement).perform();
  }

  public void click(int button) {
    if (button == 2) {
      new Actions(this).contextClick(lastElement).perform();
    }
    else {
      new Actions(this).click(lastElement).perform();
    }
  }

  public void doubleclick() {
    new Actions(this).doubleClick(lastElement).perform();
  }

  public void click(HtmlUnitWebElement element) {
    this.lastElement = element;
    click(0);
  }

  public void buttondown() {
    new Actions(this).clickAndHold(lastElement).perform();
  }

  public void buttonup() {
    new Actions(this).release(lastElement).perform();
  }

  public void keys(String string) {
    for (int i = 0; i < string.length(); i++) {
      char ch = string.charAt(i);
      if (ch == Keys.NULL.charAt(0)) {
        if (keyboard.isPressed(Keys.CONTROL)) {
          new Actions(this).keyUp(Keys.CONTROL).perform();
        }
        if (keyboard.isPressed(Keys.ALT)) {
          new Actions(this).keyUp(Keys.ALT).perform();
        }
        if (keyboard.isPressed(Keys.SHIFT)) {
          new Actions(this).keyUp(Keys.SHIFT).perform();
        }
      }
      else {
        if (ch == Keys.SHIFT.charAt(0) || ch == Keys.CONTROL.charAt(0) || ch == Keys.ALT.charAt(0)) {
          Keys keys = Keys.getKeyFromUnicode(ch);
          if (keyboard.isPressed(ch)) {
            new Actions(this).keyUp(keys).perform();
          }
          else {
            new Actions(this).keyDown(keys).perform();
          }
        }
        else {
          new Actions(this).sendKeys(lastElement, String.valueOf(ch)).perform();
        }
      }
    }
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

  @Override
  public Keyboard getKeyboard() {
    return driver.getKeyboard();
  }

  @Override
  public Mouse getMouse() {
    return driver.getMouse();
  }

}
