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

import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.SessionNotFoundException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class HtmlUnitDriverTest extends TestBase {

  @Test
  public void canGetAPage() {
    driver.get(testServer.page(""));
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("")));
  }

  @Test
  public void canGetAPageByUrl() throws MalformedURLException {
    driver.get(new URL(testServer.page("")));
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
    driver.navigate().to(testServer.page(""));
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("")));
  }

  @Test
  public void canNavigateToAnUrl() throws MalformedURLException {
    driver.navigate().to(new URL(testServer.page("")));
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("")));
  }

  @Test
  public void canRefreshAPage() {
    driver.get(testServer.page(""));
    driver.navigate().refresh();
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("")));
  }

  @Test
  public void canNavigateBackAndForward() {
    driver.get(testServer.page("link.html"));
    driver.findElement(By.id("link")).click();
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("index.html")));
    driver.navigate().back();
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("link.html")));
    driver.navigate().forward();
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("index.html")));
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
    driver.switchTo().frame("iframe");
    driver.switchTo().parentFrame();
    driver.switchTo().frame(0);
    driver.switchTo().defaultContent();
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
  public void shouldNotReturnSourceOfOldPageWhenLoadFailsDueToABadHost() {
    driver.get(testServer.page(""));
    String originalSource = driver.getPageSource();

    driver.get("http://thishostdoesnotexist.norshallitever");
    String currentSource = driver.getPageSource();

    assertThat(currentSource, not(equalTo(originalSource)));
  }

  @Test
  public void imeIsNotSupported() {
    thrown.expect(UnsupportedOperationException.class);
    driver.manage().ime();
  }

  private void openNewWindow(HtmlUnitDriver driver) {
    driver.executeScript("window.open('new')");
  }

}
