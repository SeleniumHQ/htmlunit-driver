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

package org.openqa.selenium.htmlunit.interactions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

/**
 * Tests for click action.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class ClickTest extends WebDriverTestCase {

    /**
     * @throws Exception on test failure
     */
    @Test
    public void click() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <button id='tester' onclick='document.title=\"clicked\"'>Test</button>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.id("tester"));

        final Action click = new Actions(driver).click(element).build();
        click.perform();

        assertEquals("clicked", driver.getTitle());
    }

    /**
     * @throws Exception on test failure
     */
    @Test
    @Alerts({"focus-start", "blur-start", "focus-target"})
    public void clickTriggerBlurEvent() throws Exception {
        final String html = "<html>\n"
                + "  <head>\n"
                + "    <script>\n"
                + "      function log(x) {\n"
                + "        document.title += x + ';';\n"
                + "      }\n"
                + "    </script>\n"
                + "  </head>\n"
                + "<body>\n"
                + "  <input id='start' onfocus='log(\"focus-start\");' onblur='log(\"blur-start\");'>\n"
                + "  <input id='target' onfocus='log(\"focus-target\");' onblur='log(\"blur-target\");'>\n"
                + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement start = driver.findElement(By.id("start"));
        final WebElement target = driver.findElement(By.id("target"));

        final Actions actions = new Actions(driver);
        actions.click(start);
        actions.click(target);
        actions.perform();

        assertEquals(String.join(";", getExpectedAlerts()) + ";", driver.getTitle());
    }

    /**
     * @throws Exception on test failure
     */
    @Test
    @Alerts("focus-start")
    public void mouseMoveDoesNotTriggerBlurEvent() throws Exception {
        final String html = "<html>\n"
                + "  <head>\n"
                + "    <script>\n"
                + "      function log(x) {\n"
                + "        document.title += x + ';';\n"
                + "      }\n"
                + "    </script>\n"
                + "  </head>\n"
                + "<body>\n"
                  + "  <input id='start' onfocus='log(\"focus-start\");' onblur='log(\"blur-start\");'>\n"
                + "  <input id='target' onfocus='log(\"focus-target\");' onblur='log(\"blur-target\");'>\n"
                + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement start = driver.findElement(By.id("start"));
        final WebElement target = driver.findElement(By.id("target"));

        final Actions actions = new Actions(driver);
        actions.click(start);
        actions.moveToElement(target);
        actions.perform();

        assertEquals(String.join(";", getExpectedAlerts()) + ";", driver.getTitle());
    }

    /**
     * @exception Exception If the test fails
     */
    @Test
    @Alerts({"1", "First"})
    public void shiftClick() throws Exception {
        final String html = "<html><head><title>First</title></head><body>\n"
            + "<a href='" + URL_SECOND + "'>Click Me</a>\n"
            + "</form></body></html>";

        getMockWebConnection().setResponse(URL_SECOND, "<head><title>Second</title>");
        final WebDriver driver = loadPage2(html);

        final WebElement link = driver.findElement(By.linkText("Click Me"));

        final int windowsSize = driver.getWindowHandles().size();

        new Actions(driver)
            .moveToElement(link)
            .keyDown(Keys.SHIFT)
            .click()
            .keyUp(Keys.SHIFT)
            .perform();

        Thread.sleep(100);
        assertEquals("Should have opened a new window",
                windowsSize + Integer.parseInt(getExpectedAlerts()[0]), driver.getWindowHandles().size());
        assertEquals("Should not have navigated away", getExpectedAlerts()[1], driver.getTitle());
    }

    /**
     * @exception Exception If the test fails
     */
    @Test
    @Alerts({"1", "First"})
    public void ctrlClick() throws Exception {
        final String html = "<html><head><title>First</title></head><body>\n"
            + "<a href='" + URL_SECOND + "'>Click Me</a>\n"
            + "</form></body></html>";

        getMockWebConnection().setResponse(URL_SECOND, "<head><title>Second</title>");
        final WebDriver driver = loadPage2(html);

        final WebElement link = driver.findElement(By.linkText("Click Me"));

        final int windowsSize = driver.getWindowHandles().size();

        new Actions(driver)
                .moveToElement(link)
                .keyDown(Keys.CONTROL)
                .click()
                .keyUp(Keys.CONTROL)
                .perform();

        Thread.sleep(DEFAULT_WAIT_TIME);
        assertEquals("Should have opened a new window",
                windowsSize + Integer.parseInt(getExpectedAlerts()[0]), driver.getWindowHandles().size());
        assertEquals("Should not have navigated away", getExpectedAlerts()[1], driver.getTitle());
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = {"[object Event]", "undefined", "[object MouseEvent]", "1",
                       "[object MouseEvent]", "2", "[object MouseEvent]", "2"},
            CHROME = {"[object Event]", "undefined", "[object PointerEvent]", "1",
                      "[object MouseEvent]", "2", "[object PointerEvent]", "0"},
            EDGE = {"[object Event]", "undefined", "[object PointerEvent]", "1",
                    "[object MouseEvent]", "2", "[object PointerEvent]", "0"},
            IE = {"[object Event]", "undefined", "[object PointerEvent]", "0",
                  "[object PointerEvent]", "0", "[object PointerEvent]", "0"})
    public void detail() throws Exception {
        final String html =
              "<html><head><script>\n"
            + LOG_TITLE_FUNCTION
            + "  function alertDetail(e) {\n"
            + "    log(e);\n"
            + "    log(e.detail);\n"
            + "  }\n"
            + "</script></head>\n"
            + "<body onload='alertDetail(event)'>\n"
            + "  <div id='a' onclick='alertDetail(event)'>abc</div>\n"
            + "  <div id='b' ondblclick='alertDetail(event)'>xyz</div>\n"
            + "  <div id='c' oncontextmenu='alertDetail(event)'>xyz</div>\n"
            + "</body></html>";

        final String[] alerts = getExpectedAlerts();
        int i = 0;

        final WebDriver driver = loadPage2(html);
        verifyTitle2(driver, alerts[i++], alerts[i++]);

        i = 0;
        driver.findElement(By.id("a")).click();
        verifyTitle2(driver, alerts[i++], alerts[i++], alerts[i++], alerts[i++]);

        i = 0;
        Actions action = new Actions(driver);
        action.doubleClick(driver.findElement(By.id("b")));
        action.perform();
        verifyTitle2(driver, alerts[i++], alerts[i++], alerts[i++], alerts[i++], alerts[i++], alerts[i++]);

        action = new Actions(driver);
        action.contextClick(driver.findElement(By.id("c")));
        action.perform();
        verifyTitle2(driver, alerts);
    }
}
