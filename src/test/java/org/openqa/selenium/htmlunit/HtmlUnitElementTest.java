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

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class HtmlUnitElementTest extends TestBase {

  @Test
  public void canGetWrappedDriver() {
    HtmlUnitWebElement body = (HtmlUnitWebElement) driver.findElement(By.tagName("body"));
    assertThat(body.getWrappedDriver(), sameInstance((WebDriver) driver));
  }

  @Test
  public void canSendKeysToAnInput() {
    driver.get(testServer.page("form.html"));
    WebElement input = driver.findElement(By.name("text"));
    assertThat(input.getAttribute("value"), equalTo("default text"));
    input.sendKeys(" changed");
    assertThat(input.getAttribute("value"), equalTo("default text changed"));
    input.clear();
    assertThat(input.getAttribute("value"), equalTo(""));
  }

  @Test
  public void canSendKeysUsingActions() {
    driver.get(testServer.page("form.html"));
    WebElement input = driver.findElement(By.name("text"));
    assertThat(input.getAttribute("value"), equalTo("default text"));
    new Actions(driver).click(input).keyDown(Keys.SHIFT).sendKeys(" changed").keyUp(Keys.SHIFT).perform();
    assertThat(input.getAttribute("value"), equalTo("default text CHANGED"));
  }

  @Test
  @Ignore
  public void canClickUsingActions() {
    driver.get(testServer.page("form.html"));
    WebElement checkbox = driver.findElement(By.name("checkbox"));
    assertThat(checkbox.isSelected(), is(false));
    new Actions(driver).clickAndHold(checkbox).release().perform();
    assertThat(checkbox.isSelected(), is(true));
  }

  @Test
  public void canClickACheckbox() {
    driver.get(testServer.page("form.html"));
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
  public void canGetHrefAttribute() {
    driver.get(testServer.page("link.html"));
    WebElement link = driver.findElement(By.id("link"));
    assertThat(link.getAttribute("href"), equalTo(testServer.page("index.html")));
  }

  @Test
  public void canGetBooleanAttribute() {
    driver.get(testServer.page("disabled.html"));
    WebElement disabled = driver.findElement(By.name("disabled"));
    assertThat(disabled.getAttribute("disabled"), equalTo("true"));
    WebElement enabled = driver.findElement(By.name("enabled"));
    assertThat(enabled.getAttribute("enabled"), is(nullValue()));
  }

  @Test
  public void throwsOnClickingInvisible() {
    driver.get(testServer.page("invisible.html"));
    thrown.expect(ElementNotVisibleException.class);
    driver.findElement(By.id("link")).click();
  }

  @Test
  public void textOfInvisibleIsEmptyString() {
    driver.get(testServer.page("invisible.html"));
    assertThat(driver.findElement(By.id("link")).getText(), equalTo(""));
  }

  @Test
  public void canGetPreformattedText() {
    driver.get(testServer.page("preformatted.html"));
    assertThat(driver.findElement(By.id("pre")).getText(), equalTo("Preformatted\n    text"));
  }

  @Test
  public void canGetPreformattedTextInAChild() {
    driver.get(testServer.page("preformatted.html"));
    assertThat(driver.findElement(By.tagName("body")).getText(), equalTo("Preformatted\n    text"));
  }

  @Test
  public void canSubmitAForm() {
    driver.get(testServer.page("form.html"));
    driver.findElement(By.tagName("form")).submit();
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("index.html")));
  }

  @Test
  public void canSubmitAFormFromAnInput() {
    driver.get(testServer.page("form.html"));
    driver.findElement(By.name("text")).submit();
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("index.html")));
  }

  @Test
  public void canSubmitAFormFromAnyElementInTheForm() {
    driver.get(testServer.page("form.html"));
    driver.findElement(By.id("div")).submit();
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("index.html")));
  }

  @Test
  public void canGetSize() {
    driver.get(testServer.page("box.html"));
    WebElement redBox = driver.findElement(By.id("red_box"));
    assertThat(redBox.getSize(), equalTo(new Dimension(200, 201)));
  }

  @Test
  public void canGetLocation() {
    driver.get(testServer.page("box.html"));
    WebElement redBox = driver.findElement(By.id("red_box"));
    assertThat(redBox.getLocation(), equalTo(new Point(100, 101)));
  }

  @Test
  public void canGetRectangle() {
    driver.get(testServer.page("box.html"));
    WebElement redBox = driver.findElement(By.id("red_box"));
    assertThat(redBox.getRect(), equalTo(new Rectangle(new Point(100, 101), new Dimension(200, 201))));
  }

  @Test
  public void canGetCoordinatesOnPage() {
    driver.get(testServer.page("box.html"));
    WebElement redBox = driver.findElement(By.id("red_box"));
    assertThat(((Locatable) redBox).getCoordinates().onPage(), equalTo(new Point(100, 101)));
  }

  @Test
  public void canGetCoordinatesInViewport() {
    driver.get(testServer.page("box.html"));
    WebElement redBox = driver.findElement(By.id("red_box"));
    assertThat(((Locatable) redBox).getCoordinates().inViewPort(), equalTo(new Point(100, 101)));
  }

  @Test
  public void elementToStringShouldLookNice() {
    driver.get(testServer.page("link.html"));
    WebElement a = driver.findElement(By.id("link"));
    assertThat(a.toString(), is("<a id=\"link\" href=\"/index.html\">"));
  }

  @Test
  public void elementScreenshotIsNotSupported() {
    thrown.expect(UnsupportedOperationException.class);
    driver.findElement(By.tagName("body")).getScreenshotAs(OutputType.BASE64);
  }

  @Test
  public void coordinatesOnScreenAreNotSupported() {
    thrown.expect(UnsupportedOperationException.class);
    ((Locatable) driver.findElement(By.tagName("body"))).getCoordinates().onScreen();
  }

}
