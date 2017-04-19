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

package org.openqa.selenium.htmlunit.server;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitWebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.testing.JUnit4TestBase;
import org.openqa.selenium.testing.NotYetImplemented;

public class HtmlUnitElementTest extends JUnit4TestBase {

  @Test
  @Ignore
  public void canGetWrappedDriver() {
    HtmlUnitWebElement body = (HtmlUnitWebElement) driver.findElement(By.tagName("body"));
    assertThat(body.getWrappedDriver(), sameInstance((WebDriver) driver));
  }

  @Test
  public void canSendKeysToAnInput() {
    driver.get(appServer.whereIs("form.html"));
    WebElement input = driver.findElement(By.name("text"));
    assertThat(input.getAttribute("value"), equalTo("default text"));
    input.sendKeys(" changed");
    assertThat(input.getAttribute("value"), equalTo("default text changed"));
    input.clear();
    assertThat(input.getAttribute("value"), equalTo(""));
  }

  @Test
  public void canSendKeysToAFileInput() {
    driver.get(appServer.whereIs("formPage.html"));
    WebElement input = driver.findElement(By.id("upload"));
    input.sendKeys("example.txt");
    assertThat(input.getAttribute("value"), equalTo("C:\\fakepath\\example.txt"));
  }

  @Test
  public void canSendKeysUsingActions() {
    driver.get(appServer.whereIs("form.html"));
    WebElement input = driver.findElement(By.name("text"));
    assertThat(input.getAttribute("value"), equalTo("default text"));
    new Actions(driver).click(input).keyDown(Keys.SHIFT).sendKeys(" changed").keyUp(Keys.SHIFT).perform();
    assertThat(input.getAttribute("value"), equalTo("default text CHANGED"));
  }

  @Test
  @Ignore
  public void canClickUsingActions() {
    driver.get(appServer.whereIs("form.html"));
    WebElement checkbox = driver.findElement(By.name("checkbox"));
    assertThat(checkbox.isSelected(), is(false));
    new Actions(driver).clickAndHold(checkbox).release().perform();
    assertThat(checkbox.isSelected(), is(true));
  }

  @Test
  public void canClickACheckbox() {
    driver.get(appServer.whereIs("form.html"));
    WebElement input = driver.findElement(By.name("checkbox"));
    assertThat(input.getAttribute("selected"), is(nullValue()));
    assertThat(input.isSelected(), is(false));
    input.click();
    assertThat(input.getAttribute("selected"), is("true"));
    assertThat(input.isSelected(), is(true));
    input.click();
    assertThat(input.getAttribute("selected"), is(nullValue()));
    assertThat(input.isSelected(), is(false));
  }

  @Test
  @NotYetImplemented
  public void canGetHrefAttribute() {
    driver.get(appServer.whereIs("link.html"));
    WebElement link = driver.findElement(By.id("link"));
    assertThat(link.getAttribute("href"), equalTo(appServer.whereIs("index.html")));
  }

  @Test
  public void canGetBooleanAttribute() {
    driver.get(appServer.whereIs("disabled.html"));
    WebElement disabled = driver.findElement(By.name("disabled"));
    assertThat(disabled.getAttribute("disabled"), equalTo("true"));
    WebElement enabled = driver.findElement(By.name("enabled"));
    assertThat(enabled.getAttribute("enabled"), is(nullValue()));
  }

  @Test(expected = ElementNotInteractableException.class)
  public void throwsOnClickingInvisible() {
    driver.get(appServer.whereIs("invisible.html"));
    driver.findElement(By.id("link")).click();
  }

  @Test
  public void textOfInvisibleIsEmptyString() {
    driver.get(appServer.whereIs("invisible.html"));
    assertThat(driver.findElement(By.id("link")).getText(), equalTo(""));
  }

  @Test
  public void newline() {
    driver.get(appServer.whereIs("htmlunit/newline.html"));
    assertThat(driver.findElement(By.id("textArea1")).getText(),
            equalTo(" foo \n bar\n test\n a <p>html snippet</p>"));
  }

  @Test
  public void newlineForm() {
    driver.get(appServer.whereIs("htmlunit/newline.html"));
    assertThat(driver.findElement(By.id("form1")).getText(),
            equalTo(" foo \n bar\n test\n a <p>html snippet</p>"));
  }

  @Test
  public void newline2() {
    driver.get(appServer.whereIs("htmlunit/newline2.html"));
    assertThat(driver.findElement(By.id("foo")).getText(), equalTo("  hello   abc"));
  }

  @Test
  public void newline3() {
    driver.get(appServer.whereIs("htmlunit/newline3.html"));
    assertThat(driver.findElement(By.id("foo")).getText(), equalTo("start\n  hello   abc \nend"));
  }

  @Test
  public void canGetPreformattedText() {
    driver.get(appServer.whereIs("preformatted.html"));
    assertThat(driver.findElement(By.id("pre")).getText(), equalTo("Preformatted\n    text"));
  }

  @Test
  public void canGetPreformattedTextInAChild() {
    driver.get(appServer.whereIs("preformatted.html"));
    assertThat(driver.findElement(By.tagName("body")).getText(), equalTo("Preformatted\n    text"));
  }

  @Test
  public void canSubmitAForm() {
    driver.get(appServer.whereIs("form.html"));
    driver.findElement(By.tagName("form")).submit();
    assertThat(driver.getCurrentUrl(), equalTo(appServer.whereIs("index.html")));
  }

  @Test
  public void canSubmitAFormFromAnInput() {
    driver.get(appServer.whereIs("form.html"));
    driver.findElement(By.name("text")).submit();
    assertThat(driver.getCurrentUrl(), equalTo(appServer.whereIs("index.html")));
  }

  @Test
  public void canSubmitAFormFromAnyElementInTheForm() {
    driver.get(appServer.whereIs("form.html"));
    driver.findElement(By.id("div")).submit();
    assertThat(driver.getCurrentUrl(), equalTo(appServer.whereIs("index.html")));
  }

  @Test
  public void canGetSize() {
    driver.get(appServer.whereIs("box.html"));
    WebElement redBox = driver.findElement(By.id("red_box"));
    assertThat(redBox.getSize(), equalTo(new Dimension(200, 201)));
  }

  @Test
  public void canGetLocation() {
    driver.get(appServer.whereIs("box.html"));
    WebElement redBox = driver.findElement(By.id("red_box"));
    assertThat(redBox.getLocation(), equalTo(new Point(100, 101)));
  }

  @Test
  public void canGetRectangle() {
    driver.get(appServer.whereIs("box.html"));
    WebElement redBox = driver.findElement(By.id("red_box"));
    assertThat(redBox.getRect(), equalTo(new Rectangle(new Point(100, 101), new Dimension(200, 201))));
  }

  @Test
  public void canGetCoordinatesOnPage() {
    driver.get(appServer.whereIs("box.html"));
    WebElement redBox = driver.findElement(By.id("red_box"));
    assertThat(((Locatable) redBox).getCoordinates().onPage(), equalTo(new Point(100, 101)));
  }

  @Test
  public void canGetCoordinatesInViewport() {
    driver.get(appServer.whereIs("box.html"));
    WebElement redBox = driver.findElement(By.id("red_box"));
    assertThat(((Locatable) redBox).getCoordinates().inViewPort(), equalTo(new Point(100, 101)));
  }

  @Test
  public void elementToStringShouldLookNice() {
    driver.get(appServer.whereIs("link.html"));
    WebElement a = driver.findElement(By.id("link"));
    assertThat(a.toString(), is("<a id=\"link\" href=\"/index.html\">"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void elementScreenshotIsNotSupported() {
    driver.findElement(By.tagName("body")).getScreenshotAs(OutputType.BASE64);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void coordinatesOnScreenAreNotSupported() {
    ((Locatable) driver.findElement(By.tagName("body"))).getCoordinates().onScreen();
  }

}
