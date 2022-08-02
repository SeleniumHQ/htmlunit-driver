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
 * Tests for By.linkText.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class FindByLinkTextTest extends WebDriverTestCase {

    @Test
    public void elementByLinkText() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='testId'>TestA</a>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.linkText("TestA"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void elementsByLinkText() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='testId'>TestA</a>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final List<WebElement> elements = driver.findElements(By.linkText("TestA"));
        assertEquals(1, elements.size());
        assertEquals("testId", elements.get(0).getAttribute("id"));
    }

    @Test
    public void relativeElementByLinkText() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='ctx'>\n"
                        + "    <a id='testId'>TestA</a>\n"
                        + "    <a id='testId2'>TestB</a>\n"
                        + "  </div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.linkText("TestB"));
        assertEquals("testId2", element.getAttribute("id"));
    }

    @Test
    public void relativeElementsByLinkText() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='ctx'>\n"
                        + "    <a id='testId'>TestA</a>\n"
                        + "    <a id='testId2'>TestB</a>\n"
                        + "  </div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final List<WebElement> elements = ctx.findElements(By.linkText("TestB"));
        assertEquals(1, elements.size());
        assertEquals("testId2", elements.get(0).getAttribute("id"));
    }

    @Test
    public void normalization() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='a1' href='about:blank' >Link 1</div>\n"
                        + "  <a id='a2' href='about:blank' >Link  2    \n \t\t x\n</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        final WebElement body = driver.findElement(By.tagName("body"));

        WebElement elem = body.findElement(By.linkText("Link 1"));
        assertEquals("Link 1", elem.getText());

        elem = body.findElement(By.linkText("Link 2 x"));
        assertEquals("Link 2 x", elem.getText());
    }

    @Test
    public void queryWithLeadingBlank() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='a1' href='about:blank' >Link 1</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        final WebElement body = driver.findElement(By.tagName("body"));

        final List<WebElement> elements = body.findElements(By.linkText(" Link 1"));
        assertEquals(0, elements.size());
    }

    @Test
    public void queryWithTrailingBlank() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='a1' href='about:blank' >Link 1</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        final WebElement body = driver.findElement(By.tagName("body"));

        final List<WebElement> elements = body.findElements(By.linkText("Link 1 "));
        assertEquals(0, elements.size());
    }

    @Test
    public void missingHref() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='a1'>Link 1</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        final WebElement body = driver.findElement(By.tagName("body"));

        final WebElement elem = body.findElement(By.linkText("Link 1"));
        assertEquals("Link 1", elem.getText());
    }
}
