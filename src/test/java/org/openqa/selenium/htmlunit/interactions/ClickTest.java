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
    public void clickElement() throws Exception {
        String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <button id='tester' onclick='document.title=\"clicked\"'>Test</button>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.id("tester"));

        Action click = new Actions(driver).click(element).build();
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
}
