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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.Point;
import org.openqa.selenium.ScriptTimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.testing.JUnit4TestBase;

//we migrated to 'remote' WebDriver
@Ignore
public class HtmlUnitDriverTest extends JUnit4TestBase {

  @Test
  public void canGetAPage() {
    driver.get(appServer.whereIs(""));
    assertThat(driver.getCurrentUrl(), equalTo(appServer.whereIs("")));
  }

  @Test
  public void canGetAPageByUrl() throws MalformedURLException {
    driver.get(appServer.whereIs(""));
    assertThat(driver.getCurrentUrl(), equalTo(appServer.whereIs("")));
  }

  @Test
  public void canGetPageSource() {
    driver.get(appServer.whereIs(""));
    assertThat(driver.getPageSource(), containsString("Hello"));
  }

  @Test
  public void canSetImplicitWaitTimeout() {
    driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
  }

  @Test
  public void canNavigateToAPage() {
    driver.navigate().to(appServer.whereIs(""));
    assertThat(driver.getCurrentUrl(), equalTo(appServer.whereIs("")));
  }

  @Test
  public void canNavigateToAnUrl() throws MalformedURLException {
    driver.navigate().to(new URL(appServer.whereIs("")));
    assertThat(driver.getCurrentUrl(), equalTo(appServer.whereIs("")));
  }

  @Test
  public void canRefreshAPage() {
    driver.get(appServer.whereIs(""));
    driver.navigate().refresh();
    assertThat(driver.getCurrentUrl(), equalTo(appServer.whereIs("")));
  }

  @Test
  public void canNavigateBackAndForward() {
    driver.get(appServer.whereIs("link.html"));
    driver.findElement(By.id("link")).click();
    assertThat(driver.getCurrentUrl(), equalTo(appServer.whereIs("index.html")));
    driver.navigate().back();
    assertThat(driver.getCurrentUrl(), equalTo(appServer.whereIs("link.html")));
    driver.navigate().forward();
    assertThat(driver.getCurrentUrl(), equalTo(appServer.whereIs("index.html")));
  }

  @Test(expected = WebDriverException.class)
  public void throwsOnMalformedUrl() {
    driver.get("www.test.com");
  }

  @Test
  public void doesNotThrowsOnUnknownHost() {
    driver.get("http://www.thisurldoesnotexist.comx/");
    assertThat(driver.getCurrentUrl(), equalTo("http://www.thisurldoesnotexist.comx/"));
  }

  @Test(expected = NoSuchSessionException.class)
  public void throwsOnAnyOperationAfterQuit() {
    driver.quit();
    driver.get(appServer.whereIs(""));
  }

  @Test
  public void canGetPageTitle() {
    driver.get(appServer.whereIs(""));
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
    driver.get(appServer.whereIs("frame.html"));
    driver.switchTo().frame(driver.findElement(By.id("iframe")));
    driver.switchTo().parentFrame();
    driver.switchTo().frame("iframe");
    driver.switchTo().parentFrame();
    driver.switchTo().frame(0);
    driver.switchTo().defaultContent();
  }

  @Test(expected = NoAlertPresentException.class)
  public void throwsOnMissingAlertAcceptAnAlert() {
    driver.switchTo().alert();
  }

  @Test
  public void canAcceptAnAlert() {
    driver.get(appServer.whereIs("alert.html"));
    driver.findElement(By.id("link")).click();
    Alert alert = driver.switchTo().alert();
    assertThat(alert.getText(), equalTo("An alert"));
    alert.accept();
  }

  @Test
  public void canDismissAnAlert() {
    driver.get(appServer.whereIs("alert.html"));
    driver.findElement(By.id("link")).click();
    Alert alert = driver.switchTo().alert();
    assertThat(alert.getText(), equalTo("An alert"));
    alert.dismiss();
  }

  @Test
  public void canManageWindowSize() {
    Dimension origSize = driver.manage().window().getSize();
    driver.manage().window().setSize(new Dimension(200, 300));
    assertThat(driver.manage().window().getSize(), equalTo(new Dimension(200, 300)));
    driver.manage().window().maximize();
    assertThat(driver.manage().window().getSize(), equalTo(origSize));
    driver.manage().window().setSize(new Dimension(200, 300));
    assertThat(driver.manage().window().getSize(), equalTo(new Dimension(200, 300)));
    driver.manage().window().fullscreen();
    assertThat(driver.manage().window().getSize(), equalTo(origSize));
  }

  @Test
  public void canManageWindowPosition() {
    Point origPosition = driver.manage().window().getPosition();
    driver.manage().window().setPosition(new Point(200, 300));
    assertThat(driver.manage().window().getPosition(), equalTo(new Point(200, 300)));
    driver.manage().window().maximize();
    assertThat(driver.manage().window().getPosition(), equalTo(origPosition));
    driver.manage().window().setPosition(new Point(200, 300));
    assertThat(driver.manage().window().getPosition(), equalTo(new Point(200, 300)));
    driver.manage().window().fullscreen();
    assertThat(driver.manage().window().getPosition(), equalTo(origPosition));
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
    driver.manage().deleteCookie(new Cookie("xxx", "yyy", appServer.getHostName(), "/", null));
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
    driver.get(appServer.whereIs(""));
    Object result = getWebDriver().executeScript("return window.location.href;");
    assertThat(result, instanceOf(String.class));
    assertThat(((String) result), equalTo(appServer.whereIs("")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void canExecuteScriptThatReturnsAnArray() {
    driver.get(appServer.whereIs(""));
    Object result = getWebDriver().executeScript("return [window.location.href];");
    assertThat(result, instanceOf(List.class));
    assertThat(((List<String>) result), equalTo(Arrays.asList(appServer.whereIs(""))));
  }

  @Test
  public void canExecuteScriptThatReturnsAnElement() {
    driver.get(appServer.whereIs(""));
    Object result = getWebDriver().executeScript("return document.body;");
    assertThat(result, instanceOf(WebElement.class));
    assertThat(((WebElement) result).getTagName(), equalTo("body"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void canExecuteScriptThatReturnsAListOfElements() {
    driver.get(appServer.whereIs("form.html"));
    Object result = getWebDriver().executeScript("return document.getElementsByTagName('input');");
    assertThat(result, instanceOf(List.class));
    List<WebElement> elements = (List<WebElement>) result;
    assertThat(elements.size(), equalTo(3));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void canExecuteScriptThatReturnsLocation() {
    driver.get(appServer.whereIs(""));
    Object result = getWebDriver().executeScript("return window.location;");
    assertThat(result, instanceOf(Map.class));
    assertThat(((Map<String, Object>) result).get("href"), equalTo((Object) appServer.whereIs("")));
  }

  @Test
  public void canExecuteAsyncScript() {
    Object result = getWebDriver().executeAsyncScript("arguments[arguments.length - 1](123);");
    assertThat(result, instanceOf(Number.class));
    assertThat(((Number) result).intValue(), equalTo(123));
  }

  @Test(expected = ScriptTimeoutException.class)
  public void shouldTimeoutIfScriptDoesNotInvokeCallbackWithAZeroTimeout() {
    driver.get(appServer.whereIs("ajaxy_page.html"));
    getWebDriver().executeAsyncScript("window.setTimeout(function() {}, 0);");
  }

  @Test
  public void shouldNotReturnSourceOfOldPageWhenLoadFailsDueToABadHost() {
    driver.get(appServer.whereIs(""));
    String originalSource = driver.getPageSource();

    driver.get("http://thishostdoesnotexist.norshallitever");
    String currentSource = driver.getPageSource();

    assertThat(currentSource, not(equalTo(originalSource)));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void imeIsNotSupported() {
    driver.manage().ime();
  }

  private HtmlUnitDriver getWebDriver() {
    return ((HtmlUnitDriver) driver);
  }

  private void openNewWindow(WebDriver driver) {
    ((HtmlUnitDriver) driver).executeScript("window.open('new')");
  }
}
