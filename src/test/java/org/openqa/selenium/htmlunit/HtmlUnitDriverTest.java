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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.SessionNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test the proxy setting.
 */
public class HtmlUnitDriverTest extends TestBase {

  private HtmlUnitDriver driver;

  @Before
  public void initDriver() {
    driver = new HtmlUnitDriver(true);
    driver.get(testServer.page(""));
  }

  @After
  public void stopDriver() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  @Test
  public void canGetAPage() {
    driver.get(testServer.page(""));
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("")));
  }

  @Test
  public void canGetPageSource() {
    driver.get(testServer.page(""));
    assertThat(driver.getPageSource(), containsString("Hello"));
  }

  @Test
  public void canSetImplicitWaitTimeout() {
    driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
  }

  @Test
  public void canNavigateToAPage() {
    driver.get(testServer.page(""));
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("")));
  }

  @Test
  public void canRefreshAPage() {
    driver.get(testServer.page(""));
    driver.navigate().refresh();
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("")));
  }

  @Test
  public void throwsOnMalformedUrl() {
    thrown.expect(WebDriverException.class);
    driver.get("www.test.com");
  }

  @Test
  public void doesNotThrowsOnUnknownHost() {
    driver.get("http://www.thisurldoesnotexist.comx/");
    assertThat(driver.getCurrentUrl(), equalTo("http://www.thisurldoesnotexist.comx/"));
  }

  @Test
  public void throwsOnAnyOperationAfterQuit() {
    driver.quit();
    thrown.expect(SessionNotFoundException.class);
    driver.get(testServer.page(""));
  }

  @Test
  public void canGetPageTitle() {
    driver.get(testServer.page(""));
    assertThat(driver.getTitle(), equalTo("Hello, world!"));
  }

  @Test
  public void canOpenNewWindow() {
    String mainWindow = driver.getWindowHandle();
    openNewWindow(driver);
    assertThat(driver.getWindowHandle(), equalTo(mainWindow));
  }

  @Test
  public void canGetWindowHandles() {
    openNewWindow(driver);
    assertThat(driver.getWindowHandles().size(), equalTo(2));
  }

  @Test
  public void canSwitchToAnotherWindow() {
    String mainWindow = driver.getWindowHandle();
    openNewWindow(driver);
    Set<String> windowHandles = driver.getWindowHandles();
    windowHandles.remove(mainWindow);
    driver.switchTo().window(windowHandles.iterator().next());
    assertThat(driver.getWindowHandle(), not(equalTo(mainWindow)));
  }

  @Test
  public void canCloseWindow() {
    String mainWindow = driver.getWindowHandle();
    openNewWindow(driver);
    Set<String> windowHandles = driver.getWindowHandles();
    windowHandles.remove(mainWindow);
    driver.switchTo().window(windowHandles.iterator().next());
    driver.close();
    driver.switchTo().window(mainWindow);
    assertThat(driver.getWindowHandles().size(), equalTo(1));
  }

  @Test
  public void canSwitchToFrame() {
    driver.get(testServer.page("frame.html"));
    driver.switchTo().frame(driver.findElement(By.id("iframe")));
    driver.switchTo().parentFrame();
    driver.switchTo().frame(driver.findElement(By.id("iframe")));
    driver.switchTo().defaultContent();
    driver.switchTo().frame(driver.findElement(By.id("iframe")));
  }

  @Test
  public void canFindElementByTagName() {
    driver.get(testServer.page("form.html"));
    WebElement input = driver.findElement(By.tagName("form"))
        .findElement(By.tagName("input"));
    assertThat(input.getTagName(), equalTo("input"));
  }

  @Test
  public void canFindElementsByTagName() {
    driver.get(testServer.page("form.html"));
    List<WebElement> forms = driver.findElements(By.tagName("form"));
    assertThat(forms.size(), equalTo(1));
    List<WebElement> inputs = forms.get(0).findElements(By.tagName("input"));
    assertThat(inputs.size(), equalTo(3));
  }

  @Test
  public void canFindElementByCssSelector() {
    driver.get(testServer.page("form.html"));
    WebElement input = driver.findElement(By.cssSelector("#form_id"))
        .findElement(By.cssSelector("input"));
    assertThat(input.getTagName(), equalTo("input"));
  }

  @Test
  public void canFindElementsByCssSelector() {
    driver.get(testServer.page("form.html"));
    List<WebElement> forms = driver.findElements(By.cssSelector("#form_id"));
    assertThat(forms.size(), equalTo(1));
    List<WebElement> inputs = forms.get(0).findElements(By.cssSelector("input"));
    assertThat(inputs.size(), equalTo(3));
  }

  @Test
  public void canFindElementByXpath() {
    driver.get(testServer.page("form.html"));
    WebElement input = driver.findElement(By.xpath("//form"))
        .findElement(By.xpath("./input"));
    assertThat(input.getTagName(), equalTo("input"));
  }

  @Test
  public void canFindElementsByXpath() {
    driver.get(testServer.page("form.html"));
    List<WebElement> forms = driver.findElements(By.xpath("//form"));
    assertThat(forms.size(), equalTo(1));
    List<WebElement> inputs = forms.get(0).findElements(By.xpath("./input"));
    assertThat(inputs.size(), equalTo(3));
  }

  @Test
  public void canFindElementByName() {
    driver.get(testServer.page("form.html"));
    WebElement input = driver.findElement(By.name("form_name"))
        .findElement(By.name("text"));
    assertThat(input.getTagName(), equalTo("input"));
  }

  @Test
  public void canFindElementsByName() {
    driver.get(testServer.page("form.html"));
    List<WebElement> forms = driver.findElements(By.name("form_name"));
    assertThat(forms.size(), equalTo(1));
    List<WebElement> inputs = forms.get(0).findElements(By.name("text"));
    assertThat(inputs.size(), equalTo(1));
  }

  @Test
  public void canFindElementById() {
    driver.get(testServer.page("form.html"));
    WebElement input = driver.findElement(By.id("form_id"))
        .findElement(By.id("text"));
    assertThat(input.getTagName(), equalTo("input"));
  }

  @Test
  public void canFindElementsById() {
    driver.get(testServer.page("form.html"));
    List<WebElement> forms = driver.findElements(By.id("form_id"));
    assertThat(forms.size(), equalTo(1));
    List<WebElement> inputs = forms.get(0).findElements(By.id("text"));
    assertThat(inputs.size(), equalTo(1));
  }

  @Test
  public void canFindElementByLinkText() {
    driver.get(testServer.page("alert.html"));
    WebElement link = driver.findElement(By.linkText("Click me to get an alert"));
    assertThat(link.getTagName(), equalTo("a"));
    assertThat(link.getText(), equalTo("Click me to get an alert"));

    link = driver.findElement(By.tagName("body")).findElement(By.linkText("Click me to get an alert"));
    assertThat(link.getTagName(), equalTo("a"));
    assertThat(link.getText(), equalTo("Click me to get an alert"));
  }

  @Test
  public void canFindElementByPartialLinkText() {
    driver.get(testServer.page("alert.html"));
    WebElement link = driver.findElement(By.partialLinkText("Click me"));
    assertThat(link.getTagName(), equalTo("a"));
    assertThat(link.getText(), equalTo("Click me to get an alert"));

    link = driver.findElement(By.tagName("body")).findElement(By.partialLinkText("Click me"));
    assertThat(link.getTagName(), equalTo("a"));
    assertThat(link.getText(), equalTo("Click me to get an alert"));
  }

  @Test
  public void canFindElementsByPartialLinkText() {
    driver.get(testServer.page("alert.html"));
    List<WebElement> links = driver.findElements(By.partialLinkText("Click me"));
    assertThat(links.size(), equalTo(1));

    links = driver.findElement(By.tagName("body")).findElements(By.partialLinkText("Click me"));
    assertThat(links.size(), equalTo(1));
  }

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
  public void textOfPreformattedIsPreformatted() {
    driver.get(testServer.page("preformatted.html"));
    assertThat(driver.findElement(By.id("pre")).getText(), equalTo("Preformatted\n    text"));
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
  public void canUseActions() {
    driver.get(testServer.page("form.html"));
    WebElement text = driver.findElement(By.name("text"));
    WebElement checkbox = driver.findElement(By.name("checkbox"));
    new Actions(driver)
        .click(text).keyDown(Keys.SHIFT).sendKeys("changed ").keyUp(Keys.SHIFT)
        .click(checkbox)
        .perform();
    assertThat(text.getAttribute("value"), equalTo("default textCHANGED "));
    assertThat(checkbox.isSelected(), is(true));
  }

  @Test
  public void throwsOnMissingAlertAcceptAnAlert() {
    thrown.expect(NoAlertPresentException.class);
    driver.switchTo().alert();
  }

  @Test
  public void canAcceptAnAlert() {
    driver.get(testServer.page("alert.html"));
    driver.findElement(By.id("link")).click();
    Alert alert = driver.switchTo().alert();
    assertThat(alert.getText(), equalTo("An alert"));
    alert.accept();
  }

  @Test
  public void canDismissAnAlert() {
    driver.get(testServer.page("alert.html"));
    driver.findElement(By.id("link")).click();
    Alert alert = driver.switchTo().alert();
    assertThat(alert.getText(), equalTo("An alert"));
    alert.dismiss();
  }

  @Test
  public void canSetAndGetWindowSize() {
    driver.manage().window().setSize(new Dimension(200, 300));
    assertThat(driver.manage().window().getSize(), equalTo(new Dimension(200, 300)));
  }

  @Test
  public void canSetAndGetWindowPosition() {
    driver.manage().window().setPosition(new Point(200, 300));
    assertThat(driver.manage().window().getPosition(), equalTo(new Point(200, 300)));
  }

  @Test
  public void canSetGetAndDeleteCookie() {
    driver.manage().addCookie(new Cookie("xxx", "yyy"));
    assertThat(driver.manage().getCookieNamed("xxx"), equalTo(new Cookie("xxx", "yyy")));
    assertThat(driver.manage().getCookies().size(), equalTo(1));
    assertThat(driver.manage().getCookies().iterator().next(), equalTo(new Cookie("xxx", "yyy")));
    driver.manage().deleteCookieNamed("xxx");
    assertThat(driver.manage().getCookieNamed("xxx"), is(nullValue()));
  }

  @Test
  public void canDeleteCookieObject() {
    driver.manage().addCookie(new Cookie("xxx", "yyy"));
    driver.manage().deleteCookie(new Cookie("xxx", "yyy", testServer.domain(), "/", null));
    assertThat(driver.manage().getCookieNamed("xxx"), is(nullValue()));
  }

  @Test
  public void canSetGetAndDeleteMultipleCookies() {
    driver.manage().addCookie(new Cookie("xxx", "yyy"));
    driver.manage().addCookie(new Cookie("yyy", "xxx"));
    assertThat(driver.manage().getCookies().size(), equalTo(2));
    driver.manage().deleteAllCookies();
    assertThat(driver.manage().getCookies().size(), equalTo(0));
  }

  @Test
  public void canExecuteScriptThatReturnsAString() {
    driver.get(testServer.page(""));
    Object result = driver.executeScript("return window.location.href;");
    assertThat(result, instanceOf(String.class));
    assertThat(((String) result), equalTo(testServer.page("")));
  }

  @Test
  public void canExecuteScriptThatReturnsAnArray() {
    driver.get(testServer.page(""));
    Object result = driver.executeScript("return [window.location.href];");
    assertThat(result, instanceOf(List.class));
    assertThat(((List<String>) result), equalTo(Arrays.asList(testServer.page(""))));
  }

  @Test
  public void canExecuteScriptThatReturnsAnElement() {
    driver.get(testServer.page(""));
    Object result = driver.executeScript("return document.body;");
    assertThat(result, instanceOf(WebElement.class));
    assertThat(((WebElement) result).getTagName(), equalTo("body"));
  }

  @Test
  public void canExecuteScriptThatReturnsAListOfElements() {
    driver.get(testServer.page("form.html"));
    Object result = driver.executeScript("return document.getElementsByTagName('input');");
    assertThat(result, instanceOf(List.class));
    List<WebElement> elements = (List<WebElement>) result;
    assertThat(elements.size(), equalTo(3));
  }

  @Test
  public void canExecuteScriptThatReturnsLocation() {
    driver.get(testServer.page(""));
    Object result = driver.executeScript("return window.location;");
    assertThat(result, instanceOf(Map.class));
    assertThat(((Map<String, Object>) result).get("href"), equalTo((Object) testServer.page("")));
  }

  @Test
  public void canExecuteAsyncScript() {
    Object result = driver.executeAsyncScript("arguments[arguments.length - 1](123);");
    assertThat(result, instanceOf(Number.class));
    assertThat(((Number) result).intValue(), equalTo(123));
  }

  @Test
  public void elementToStringShouldLookNice() {
    driver.get(testServer.page("link.html"));
    WebElement a = driver.findElement(By.id("link"));
    assertThat(a.toString(), is("<a id=\"link\" href=\"/index.html\">"));
  }

  @Test
  public void shouldNotReturnSourceOfOldPageWhenLoadFailsDueToABadHost() {
    driver.get(testServer.page(""));
    String originalSource = driver.getPageSource();

    driver.get("http://thishostdoesnotexist.norshallitever");
    String currentSource = driver.getPageSource();

    assertThat(currentSource, not(equalTo(originalSource)));
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

  private void openNewWindow(HtmlUnitDriver driver) {
    driver.executeScript("window.open('new')");
  }

}
