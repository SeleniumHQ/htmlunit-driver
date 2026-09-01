// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   https://www.apache.org/licenses/LICENSE-2.0
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
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;

/**
 * Tests for By.xpath.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class FindByXPathTest extends WebDriverTestCase {

    @Test
    public void elementByXPath() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testId' class='testClass'></div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.xpath(".//*[@class = 'testClass']"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void elementsByXPath() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testId' class='testClass'></div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final List<WebElement> elements = driver.findElements(By.xpath(".//*[@class = 'testClass']"));
        assertEquals(1, elements.size());
        assertEquals("testId", elements.get(0).getAttribute("id"));
    }

    @Test
    public void relativeElementByXPath() throws Exception {
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
        final WebElement element = ctx.findElement(By.xpath(".//*[@class = 'other']"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void relativeElementsByXPath() throws Exception {
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
        final List<WebElement> elements = ctx.findElements(By.xpath(".//*[@class = 'testClass']"));
        assertEquals(1, elements.size());
        assertEquals("testId2", elements.get(0).getAttribute("id"));
    }

    @Test(expected = InvalidSelectorException.class)
    public void elementsByXPathEmpty() throws Exception {
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
        ctx.findElements(By.xpath(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void elementsByXPathNull() throws Exception {
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
        ctx.findElements(By.xpath(null));
    }

    @Test(expected = InvalidSelectorException.class)
    public void elementsByXPathBlank() throws Exception {
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
        ctx.findElements(By.xpath("  \t "));
    }

    @Test(expected = InvalidSelectorException.class)
    public void elementByXPathEmpty() throws Exception {
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
        driver.findElement(By.xpath(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void elementByXPathNull() throws Exception {
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
        driver.findElement(By.xpath(null));
    }

    @Test(expected = InvalidSelectorException.class)
    public void elementByXPathBlank() throws Exception {
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
        driver.findElement(By.xpath("  \t "));
    }

    /**
     * XPath with a string literal containing a single quote.
     * Without proper handling this breaks the XPath expression.
     */
    @Test
    public void elementByXPathWithSingleQuoteInValue() throws Exception {
        final String html = "<html>\n"
                + "<head></head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <span class=\"it's-fine\">target</span>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.xpath(".//*[@class = \"it's-fine\"]"));
        assertEquals("target", element.getText());
    }

    /**
     * XPath with a string literal containing a double quote.
     */
    @Test
    public void elementByXPathWithDoubleQuoteInValue() throws Exception {
        final String html = "<html>\n"
                + "<head></head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <span class='say &quot;hello&quot;'>target</span>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.xpath(".//*[@class = 'say \"hello\"']"));
        assertEquals("target", element.getText());
    }

    /**
     * XPath using concat() to match a value containing both quote types —
     * the only pure XPath 1.0 way to express such a literal.
     */
    @Test
    public void elementByXPathWithBothQuoteTypesUsingConcat() throws Exception {
        final String html = "<html>\n"
                + "<head></head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <span class='it&apos;s a &quot;test&quot;'>target</span>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        // XPath 1.0 has no escape sequences — concat() is the only solution
        final WebElement element = ctx.findElement(
                By.xpath(".//*[@class = concat('it', \"'\", 's a \"test\"')]"));
        assertEquals("target", element.getText());
    }
}
