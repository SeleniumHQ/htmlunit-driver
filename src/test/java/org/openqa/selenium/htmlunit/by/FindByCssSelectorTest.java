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

package org.openqa.selenium.htmlunit.by;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;

/**
 * Tests for By.cssSelector.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class FindByCssSelectorTest extends WebDriverTestCase {

    @Test
    public void elementByCssSelector() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testId' class='testClass'></div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.cssSelector(".testClass"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void elementsByCssSelector() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testId' class='testClass other'></div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final List<WebElement> elements = driver.findElements(By.cssSelector(".testClass"));
        assertEquals(1, elements.size());
        assertEquals("testId", elements.get(0).getAttribute("id"));
    }

    @Test
    public void relativeElementByCssSelector() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='ctx'>\n"
                        + "    <div id='testId' class='other testClass'></div>\n"
                        + "    <div id='testId' class='other'></div>\n"
                        + "  </div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.cssSelector(".testClass"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void relativeElementsByCssSelector() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='ctx'>\n"
                        + "    <div id='testId' class='other'></div>\n"
                        + "    <div id='testId2' class='testClass'></div>\n"
                        + "  </div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final List<WebElement> elements = ctx.findElements(By.cssSelector(".testClass"));
        assertEquals(1, elements.size());
        assertEquals("testId2", elements.get(0).getAttribute("id"));
    }
}
