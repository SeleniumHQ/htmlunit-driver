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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;

/**
 * Tests for By.id.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class FindByIdTest extends WebDriverTestCase {

    @Test
    public void elementById() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId'>TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.id("testDivId"));
        assertEquals("TestDiv", element.getText());
    }

    @Test
    public void elementsById() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId'>TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final List<WebElement> elements = driver.findElements(By.id("testDivId"));
        assertEquals(1, elements.size());
        assertEquals("TestDiv", elements.get(0).getText());
    }

    @Test
    public void relativeElementById() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='ctx'>\n"
                        + "    <div id='testDivId'>TestDiv</div>\n"
                        + "    <div id='testDivId2'>TestDiv2</div>\n"
                        + "  </div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.id("testDivId"));
        assertEquals("TestDiv", element.getText());
    }

    @Test
    public void relativeElementsById() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='ctx'>\n"
                        + "    <div id='testDivId'>TestDiv</div>\n"
                        + "    <div id='testDivId2'>TestDiv2</div>\n"
                        + "  </div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final List<WebElement> elements = ctx.findElements(By.id("testDivId"));
        assertEquals(1, elements.size());
        assertEquals("TestDiv", elements.get(0).getText());
    }

    @Test
    public void elementsByIdNotFound() throws Exception {
        final String html = "<html>\n"
                + "<head>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div id='testDivId'>TestDiv</div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        List<WebElement> elements = driver.findElements(By.id("nonExistentId"));

        Assert.assertNotNull(elements);
        Assert.assertEquals(0, elements.size());
    }

    @Test
    public void relativeElementsByIdNotFound() throws Exception {
        final String html = "<html>\n"
                + "<head>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <div id='testDivId'>TestDiv</div>\n"
                + "    <div id='testDivId2'>TestDiv2</div>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        WebElement ctx = driver.findElement(By.id("ctx"));
        List<WebElement> elements = ctx.findElements(By.id("nonExistentId"));

        Assert.assertNotNull(elements);
        Assert.assertEquals(0, elements.size());
    }
}
