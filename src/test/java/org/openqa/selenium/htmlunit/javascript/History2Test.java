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

package org.openqa.selenium.htmlunit.javascript;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.BrowserRunner;
import org.openqa.selenium.htmlunit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.WebDriverTestCase;

@RunWith(BrowserRunner.class)
public class History2Test extends WebDriverTestCase {

  /**
   * @throws Exception if an error occurs
   */
  @Test
  @Alerts({"[object PopStateEvent]", "{\"hi\":\"there\"}",
    "[object PopStateEvent]", "{\"hi\":\"there\"}",
    "[object PopStateEvent]", "null",
    "[object PopStateEvent]", "null",
    "[object PopStateEvent]", "{\"hi\":\"there\"}",
    "[object PopStateEvent]", "{\"hi\":\"there\"}",
    "[object PopStateEvent]", "{\"hi2\":\"there2\"}",
    "[object PopStateEvent]", "{\"hi2\":\"there2\"}"})
  public void pushState() throws Exception {
    final String html = "<html>\n"
        + "<head>\n"
        + "<script>\n"
        + "  function test() {\n"
        + "    if (window.history.pushState) {\n"
        + "      var stateObj = { hi: 'there' };\n"
        + "      window.history.pushState(stateObj, 'page 2', 'bar.html');\n"
        + "    }\n"
        + "  }\n"

        + "  function test2() {\n"
        + "    if (window.history.pushState) {\n"
        + "      var stateObj = { hi2: 'there2' };\n"
        + "      window.history.pushState(stateObj, 'page 3', 'bar2.html');\n"
        + "    }\n"
        + "  }\n"

        + "  function popMe(event) {\n"
        + "    var e = event ? event : window.event;\n"
        + "    alert(e);\n"
        + "    alert(JSON.stringify(e.state));\n"
        + "  }\n"

        + "  function setWindowName() {\n"
        + "    window.name = window.name + 'a';\n"
        + "  }\n"

        + "  window.addEventListener('popstate', popMe);\n"
        + "</script>\n"
        + "</head>\n"
        + "<body onpopstate='popMe(event)' onload='setWindowName()' onbeforeunload='setWindowName()' "
        + "onunload='setWindowName()'>\n"
        + "  <button id=myId onclick='test()'>Click me</button>\n"
        + "  <button id=myId2 onclick='test2()'>Click me</button>\n"
        + "</body></html>";

    final String[] expectedAlerts = getExpectedAlerts();
    int i = 0;
    final WebDriver driver = loadPage2(html);
    assertEquals("a", ((JavascriptExecutor) driver).executeScript("return window.name"));

    final long start = (Long) ((JavascriptExecutor) driver).executeScript("return window.history.length");

    driver.findElement(By.id("myId")).click();
    assertEquals("a", ((JavascriptExecutor) driver).executeScript("return window.name"));
    assertEquals(start + 1, ((JavascriptExecutor) driver).executeScript("return window.history.length"));
    assertEquals(URL_FIRST + "bar.html", driver.getCurrentUrl());

    driver.findElement(By.id("myId2")).click();
    assertEquals("a", ((JavascriptExecutor) driver).executeScript("return window.name"));
    assertEquals(start + 2, ((JavascriptExecutor) driver).executeScript("return window.history.length"));
    assertEquals(URL_FIRST + "bar2.html", driver.getCurrentUrl());

    driver.navigate().back();
    verifyAlerts(driver, expectedAlerts[i++], expectedAlerts[i++], expectedAlerts[i++], expectedAlerts[i++]);
    assertEquals("a", ((JavascriptExecutor) driver).executeScript("return window.name"));
    assertEquals(start + 2, ((JavascriptExecutor) driver).executeScript("return window.history.length"));
    assertEquals(URL_FIRST + "bar.html", driver.getCurrentUrl());

    driver.navigate().back();
    verifyAlerts(driver, expectedAlerts[i++], expectedAlerts[i++], expectedAlerts[i++], expectedAlerts[i++]);
    assertEquals("a", ((JavascriptExecutor) driver).executeScript("return window.name"));
    assertEquals(start + 2, ((JavascriptExecutor) driver).executeScript("return window.history.length"));
    assertEquals(URL_FIRST.toString(), driver.getCurrentUrl());

    driver.navigate().forward();
    verifyAlerts(driver, expectedAlerts[i++], expectedAlerts[i++], expectedAlerts[i++], expectedAlerts[i++]);
    assertEquals("a", ((JavascriptExecutor) driver).executeScript("return window.name"));
    assertEquals(start + 2, ((JavascriptExecutor) driver).executeScript("return window.history.length"));
    assertEquals(URL_FIRST + "bar.html", driver.getCurrentUrl());

    driver.navigate().forward();
    verifyAlerts(driver, expectedAlerts[i++], expectedAlerts[i++], expectedAlerts[i++], expectedAlerts[i++]);
    assertEquals("a", ((JavascriptExecutor) driver).executeScript("return window.name"));
    assertEquals(start + 2, ((JavascriptExecutor) driver).executeScript("return window.history.length"));
    assertEquals(URL_FIRST + "bar2.html", driver.getCurrentUrl());

    assertEquals(1, getMockWebConnection().getRequestCount());

    // because we have changed the window name
    releaseResources();
    shutDownAll();
  }

}
