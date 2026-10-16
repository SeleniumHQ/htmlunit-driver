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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.support.pagefactory.ByAll;
import org.openqa.selenium.support.pagefactory.ByChained;

/**
 * Tests for ByAll.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class FindByAllTest extends WebDriverTestCase {

    @Test
    public void byAllOneId() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId'>TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(new ByAll(By.id("testDivId")));
        assertEquals("TestDiv", element.getText());
    }

    @Test
    public void byAllTwoIds() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId'>TestDiv</div>\n"
                        + "  <div id='testDivId2'>TestDiv2</div>\n"
                        + "  <div id='testDivId3'>TestDiv3</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(new ByAll(By.id("testDivId"), By.id("testDivId3")));
        assertEquals("TestDiv", element.getText());
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static String html(final String body) {
        return "<html>\n"
                + "<head>\n"
                + "</head>\n"
                + "<body>\n"
                + body
                + "</body>\n"
                + "</html>\n";
    }

    private static List<String> ids(final List<WebElement> elements) {
        final List<String> ids = new ArrayList<>();
        for (final WebElement element : elements) {
            ids.add(element.getAttribute("id"));
        }
        return ids;
    }

    // ------------------------------------------------------------------
    // no match
    // ------------------------------------------------------------------

    @Test
    public void nothingFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a'></div>\n"));

        assertTrue(driver.findElements(new ByAll(By.id("unknown"), By.className("unknown"), By.tagName("p"))).isEmpty());
        try {
            driver.findElement(new ByAll(By.id("unknown"), By.className("unknown"), By.tagName("p")));
            fail("expected NoSuchElementException");
        }
        catch (final NoSuchElementException e) {
            assertTrue(e.getMessage().contains("Cannot locate an element using"));
        }
    }

    @Test
    public void someLocatorsWithoutMatch() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a'></div>\n"
                + "  <div id='b'></div>\n"));

        assertEquals(Arrays.asList("a"), ids(driver.findElements(new ByAll(By.id("unknown"), By.id("a")))));
        assertEquals(Arrays.asList("b"), ids(driver.findElements(new ByAll(By.id("b"), By.id("unknown")))));
        assertEquals("a", driver.findElement(new ByAll(By.id("unknown"), By.id("a"))).getAttribute("id"));
    }

    @Test
    public void emptyByAll() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a'></div>\n"));

        assertTrue(driver.findElements(new ByAll()).isEmpty());
        try {
            driver.findElement(new ByAll());
            fail("expected NoSuchElementException");
        }
        catch (final NoSuchElementException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // union semantics
    // ------------------------------------------------------------------

    @Test
    public void resultsAreInLocatorOrder() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a' class='x'></div>\n"
                + "  <div id='b'></div>\n"
                + "  <div id='c' class='x'></div>\n"));

        // all matches of the first locator, then all matches of the second one; not the document order
        assertEquals(Arrays.asList("c", "a"), ids(driver.findElements(new ByAll(By.id("c"), By.id("a")))));
        assertEquals(Arrays.asList("a", "c", "b"), ids(driver.findElements(new ByAll(By.className("x"), By.id("b")))));
        assertEquals(Arrays.asList("b", "a", "c"), ids(driver.findElements(new ByAll(By.id("b"), By.className("x")))));

        // the first element found is returned
        assertEquals("c", driver.findElement(new ByAll(By.id("c"), By.id("a"))).getAttribute("id"));
        assertEquals("b", driver.findElement(new ByAll(By.id("b"), By.className("x"))).getAttribute("id"));
    }

    @Test
    public void mixedLocatorTypes() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='byId'></div>\n"
                + "    <span id='byClass' class='cls'></span>\n"
                + "    <input id='byName' name='nm'>\n"
                + "    <b id='byTag'></b>\n"
                + "    <i id='byCss' class='css'></i>\n"
                + "    <u id='byXPath'></u>\n"
                + "    <a id='byLink'>link text</a>\n"
                + "    <a id='byPartial'>partial text</a>\n"
                + "  </div>\n"));

        final By by = new ByAll(By.id("byId"), By.className("cls"), By.name("nm"), By.tagName("b"),
                By.cssSelector("i.css"), By.xpath("//u"), By.linkText("link text"), By.partialLinkText("partial"));

        assertEquals(Arrays.asList("byId", "byClass", "byName", "byTag", "byCss", "byXPath", "byLink", "byPartial"),
                ids(driver.findElements(by)));
        assertEquals("byId", driver.findElement(by).getAttribute("id"));
    }

    @Test
    public void elementMatchedByMultipleLocators() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='x' class='c' name='n'></div>\n"));

        // depending on the Selenium version the element is listed once or once per matching locator
        final List<String> ids = ids(driver.findElements(new ByAll(By.id("x"), By.className("c"), By.name("n"))));
        assertFalse(ids.isEmpty());
        assertEquals(new HashSet<>(Arrays.asList("x")), new HashSet<>(ids));
        assertEquals("x", driver.findElement(new ByAll(By.className("c"), By.name("n"))).getAttribute("id"));
    }

    @Test
    public void hiddenElementsAreFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='h1' style='display: none'></div>\n"
                + "  <input type='hidden' id='h2' name='csrf'>\n"));

        assertEquals(Arrays.asList("h1", "h2"), ids(driver.findElements(new ByAll(By.id("h1"), By.name("csrf")))));
    }

    // ------------------------------------------------------------------
    // combinations
    // ------------------------------------------------------------------

    @Test
    public void nestedByAll() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a'></div>\n"
                + "  <div id='b'></div>\n"
                + "  <div id='c'></div>\n"));

        assertEquals(Arrays.asList("a", "b", "c"),
                ids(driver.findElements(new ByAll(new ByAll(By.id("a"), By.id("b")), By.id("c")))));
        assertEquals(Arrays.asList("c", "a"),
                ids(driver.findElements(new ByAll(new ByAll(By.id("unknown"), By.id("c")), new ByAll(By.id("a"))))));
    }

    @Test
    public void combinedWithChained() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <span id='in1' class='x'></span>\n"
                + "    <b id='in2'></b>\n"
                + "  </div>\n"
                + "  <span id='out1' class='x'></span>\n"
                + "  <b id='out2'></b>\n"));

        // a chain with an alternative as last step
        assertEquals(Arrays.asList("in1", "in2"),
                ids(driver.findElements(new ByChained(By.id("ctx"), new ByAll(By.className("x"), By.tagName("b"))))));

        // alternatives, one of them is a chain
        assertEquals(Arrays.asList("in1", "out2"),
                ids(driver.findElements(new ByAll(new ByChained(By.id("ctx"), By.className("x")), By.id("out2")))));
    }

    // ------------------------------------------------------------------
    // scope
    // ------------------------------------------------------------------

    @Test
    public void fromElementContext() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <span id='in1' class='x'></span>\n"
                + "    <b id='in2'></b>\n"
                + "  </div>\n"
                + "  <span id='out1' class='x'></span>\n"
                + "  <b id='out2'></b>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        final By by = new ByAll(By.className("x"), By.tagName("b"));

        assertEquals(Arrays.asList("in1", "in2"), ids(ctx.findElements(by)));
        assertEquals("in1", ctx.findElement(by).getAttribute("id"));

        assertEquals(Arrays.asList("in1", "out1", "in2", "out2"), ids(driver.findElements(by)));
    }

    @Test
    public void fromElementContextNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'></div>\n"
                + "  <span id='out1' class='x'></span>\n"
                + "  <b id='out2'></b>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        final By by = new ByAll(By.className("x"), By.tagName("b"));

        assertTrue(ctx.findElements(by).isEmpty());
        try {
            ctx.findElement(by);
            fail("expected NoSuchElementException");
        }
        catch (final NoSuchElementException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // dynamic DOM
    // ------------------------------------------------------------------

    @Test
    public void elementsChangedByScript() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <span id='s1' class='x'></span>\n"
                + "    <b id='gone'></b>\n"
                + "  </div>\n"
                + "  <script>\n"
                + "    var ctx = document.getElementById('ctx');\n"
                + "    var b = document.createElement('b');\n"
                + "    b.id = 'added';\n"
                + "    ctx.appendChild(b);\n"
                + "    var s2 = document.createElement('span');\n"
                + "    s2.id = 's2';\n"
                + "    s2.className = 'x';\n"
                + "    ctx.appendChild(s2);\n"
                + "    ctx.removeChild(document.getElementById('gone'));\n"
                + "  </script>\n"));

        assertEquals(Arrays.asList("s1", "s2", "added"),
                ids(driver.findElements(new ByAll(By.className("x"), By.tagName("b")))));
    }
}
