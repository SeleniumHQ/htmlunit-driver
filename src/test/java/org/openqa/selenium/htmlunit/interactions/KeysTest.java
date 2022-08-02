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
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

/**
 * Tests for key action.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class KeysTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void sendKeys() throws Exception {
        final String html = "<html>\n"
                + "<body><input type='text' id='t'/>\n"
                + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        final Action action = new Actions(driver)
                .moveToElement(t)
                .click()
                .sendKeys("abc")
                .build();
        action.perform();

        assertEquals("abc", t.getAttribute("value"));
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void sendKeysAndShift() throws Exception {
        final String html = "<html>\n"
                + "<body><input type='text' id='t'/>\n"
                + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final WebElement t = driver.findElement(By.id("t"));

        final Action action = new Actions(driver)
                .moveToElement(t)
                .click()
                .sendKeys("Hell")
                .keyDown(Keys.SHIFT)
                .sendKeys("o")
                .keyUp(Keys.SHIFT)
                .build();
        action.perform();

        assertEquals("HellO", t.getAttribute("value"));
    }
}
