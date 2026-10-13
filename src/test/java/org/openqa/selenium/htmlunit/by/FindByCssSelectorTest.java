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
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
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

    @Test(expected = InvalidSelectorException.class)
    public void invalidSelectorByCssSelector() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testId' class='testClass'></div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        driver.findElements(By.cssSelector(""));
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

    private static void assertInvalidSelector(final SearchContext context, final String selector) {
        try {
            context.findElement(By.cssSelector(selector));
            fail("findElement: expected InvalidSelectorException for selector '" + selector + "'");
        }
        catch (final InvalidSelectorException e) {
            // expected
        }

        try {
            context.findElements(By.cssSelector(selector));
            fail("findElements: expected InvalidSelectorException for selector '" + selector + "'");
        }
        catch (final InvalidSelectorException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // no match
    // ------------------------------------------------------------------

    @Test(expected = NoSuchElementException.class)
    public void elementByCssSelectorNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a' class='other'></div>\n"));

        driver.findElement(By.cssSelector(".testClass"));
    }

    @Test
    public void elementsByCssSelectorNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a' class='other'></div>\n"));

        assertTrue(driver.findElements(By.cssSelector(".testClass")).isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void relativeElementByCssSelectorNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='a' class='other'></div>\n"
                + "  </div>\n"
                + "  <div id='outside' class='testClass'></div>\n"));

        driver.findElement(By.id("ctx")).findElement(By.cssSelector(".testClass"));
    }

    @Test
    public void relativeElementsByCssSelectorNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='a' class='other'></div>\n"
                + "  </div>\n"
                + "  <div id='outside' class='testClass'></div>\n"));

        assertTrue(driver.findElement(By.id("ctx")).findElements(By.cssSelector(".testClass")).isEmpty());
    }

    // ------------------------------------------------------------------
    // matching semantics
    // ------------------------------------------------------------------

    @Test
    public void elementsByCssSelectorInDocumentOrder() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='first' class='testClass'>\n"
                + "    <span id='nested' class='testClass'></span>\n"
                + "  </div>\n"
                + "  <div id='other' class='other'></div>\n"
                + "  <p id='last' class='testClass'></p>\n"));

        assertEquals(Arrays.asList("first", "nested", "last"), ids(driver.findElements(By.cssSelector(".testClass"))));
        assertEquals("first", driver.findElement(By.cssSelector(".testClass")).getAttribute("id"));
    }

    @Test
    public void selectorListReturnsDocumentOrder() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='first' class='b'></div>\n"
                + "  <div id='second' class='a'></div>\n"
                + "  <div id='third' class='b'></div>\n"));

        // the order of the selector list must not matter, only the document order
        assertEquals(Arrays.asList("first", "second", "third"), ids(driver.findElements(By.cssSelector(".a, .b"))));
        assertEquals(Arrays.asList("first", "second", "third"), ids(driver.findElements(By.cssSelector(".b, .a"))));
        assertEquals("first", driver.findElement(By.cssSelector(".a, .b")).getAttribute("id"));
    }

    @Test
    public void commonSelectors() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='root'>\n"
                + "    <ul id='list'>\n"
                + "      <li id='li1' class='item first'>one</li>\n"
                + "      <li id='li2' class='item' data-x='2'>two</li>\n"
                + "      <li id='li3' class='item last' data-x='3'>three</li>\n"
                + "    </ul>\n"
                + "    <p id='p1'></p>\n"
                + "    <p id='p2' class='note'></p>\n"
                + "    <input id='cb' type='checkbox' checked>\n"
                + "    <input id='txt' type='text' disabled>\n"
                + "  </div>\n"));

        final String[][] cases = {
            {"#li2", "li2"},
            {"li", "li1,li2,li3"},
            {"LI", "li1,li2,li3"},
            {"[data-x]", "li2,li3"},
            {"[data-x='3']", "li3"},
            {"[data-x^='2']", "li2"},
            {"[class~='first']", "li1"},
            {"ul > li", "li1,li2,li3"},
            {"div li", "li1,li2,li3"},
            {"#list > .item.last", "li3"},
            {"li + li", "li2,li3"},
            {"li ~ li", "li2,li3"},
            {"li:first-child", "li1"},
            {"li:last-child", "li3"},
            {"li:nth-child(2)", "li2"},
            {"li:not(.first)", "li2,li3"},
            {"p.note", "p2"},
            {"p:empty", "p1,p2"},
            {"input:checked", "cb"},
            {"input:disabled", "txt"},
            {"input[type=text]", "txt"},
            {"#root > *", "list,p1,p2,cb,txt"}
        };

        for (final String[] c : cases) {
            assertEquals(c[0], Arrays.asList(c[1].split(",")), ids(driver.findElements(By.cssSelector(c[0]))));
            assertEquals(c[0], c[1].split(",")[0], driver.findElement(By.cssSelector(c[0])).getAttribute("id"));
        }
    }

    @Test
    public void selectorWithSurroundingWhitespace() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='testId' class='testClass'></div>\n"));

        assertEquals("testId", driver.findElement(By.cssSelector(" .testClass ")).getAttribute("id"));
        assertEquals("testId", driver.findElement(By.cssSelector("\n.testClass\t")).getAttribute("id"));
    }

    @Test
    public void attributeValuesWithQuotes() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='single' data-x='a&#39;b'></div>\n"
                + "  <div id='double' data-x='a&quot;b'></div>\n"));

        assertEquals("single", driver.findElement(By.cssSelector("[data-x=\"a'b\"]")).getAttribute("id"));
        assertEquals("double", driver.findElement(By.cssSelector("[data-x='a\"b']")).getAttribute("id"));
    }

    @Test
    public void escapedIdentifiers() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a:b'></div>\n"
                + "  <div id='1x'></div>\n"
                + "  <div id='dot' class='foo.bar'></div>\n"
                + "  <div id='num' class='123'></div>\n"
                + "  <div id='both' class='foo bar'></div>\n"));

        final String[][] cases = {
            {"#a\\:b", "a:b"},
            {"#\\31 x", "1x"},
            {".foo\\.bar", "dot"},
            {".\\31 23", "num"}
        };

        for (final String[] c : cases) {
            assertEquals(c[0], Arrays.asList(c[1]), ids(driver.findElements(By.cssSelector(c[0]))));
        }
    }

    @Test
    public void duplicateIds() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='testId' class='one'></div>\n"
                + "  <div id='testId' class='two'></div>\n"));

        final List<WebElement> elements = driver.findElements(By.cssSelector("#testId"));
        assertEquals(2, elements.size());
        assertEquals("one", elements.get(0).getAttribute("class"));
        assertEquals("two", elements.get(1).getAttribute("class"));
        assertEquals("one", driver.findElement(By.cssSelector("#testId")).getAttribute("class"));
    }

    @Test
    public void hiddenElementsAreFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='hidden' class='testClass' style='display: none'></div>\n"));

        assertEquals("hidden", driver.findElement(By.cssSelector(".testClass")).getAttribute("id"));
    }

    // ------------------------------------------------------------------
    // scope
    // ------------------------------------------------------------------

    @Test
    public void relativeSearchIgnoresContextAndOutsideElements() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='before' class='testClass'></div>\n"
                + "  <div id='ctx' class='testClass'>\n"
                + "    <div id='child' class='testClass'>\n"
                + "      <span id='grandchild' class='testClass'></span>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "  <div id='after' class='testClass'></div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(Arrays.asList("child", "grandchild"), ids(ctx.findElements(By.cssSelector(".testClass"))));
        assertEquals("child", ctx.findElement(By.cssSelector(".testClass")).getAttribute("id"));

        // the context element itself is never part of the result
        assertEquals(Arrays.asList("child"), ids(ctx.findElements(By.cssSelector("div"))));
    }

    @Test
    public void relativeSelectorIsEvaluatedAgainstWholeDocument() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <span id='s'></span>\n"
                + "  </div>\n"
                + "  <span id='outside'></span>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));

        // like querySelector(All) on an element: ancestors and the context itself may be part of the selector
        final String[] selectors = {"body > div > span", "#ctx > span", "html span", "div span"};
        for (final String selector : selectors) {
            assertEquals(selector, Arrays.asList("s"), ids(ctx.findElements(By.cssSelector(selector))));
            assertEquals(selector, "s", ctx.findElement(By.cssSelector(selector)).getAttribute("id"));
        }
    }

    @Test
    public void scopePseudoClass() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='child' class='x'>\n"
                + "      <div id='grandchild' class='x'></div>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "  <div id='outside' class='x'></div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(Arrays.asList("child"), ids(ctx.findElements(By.cssSelector(":scope > .x"))));
    }

    // ------------------------------------------------------------------
    // dynamic DOM
    // ------------------------------------------------------------------

    @Test
    public void elementsChangedByScript() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a' class='x'></div>\n"
                + "  <script>\n"
                + "    var c = document.createElement('div');\n"
                + "    c.id = 'c';\n"
                + "    c.className = 'x';\n"
                + "    document.body.appendChild(c);\n"
                + "    var d = document.createElement('div');\n"
                + "    d.id = 'd';\n"
                + "    d.className = 'x';\n"
                + "    document.body.appendChild(d);\n"
                + "    d.parentNode.removeChild(d);\n"
                + "    document.getElementById('a').setAttribute('data-state', 'on');\n"
                + "  </script>\n"));

        assertEquals(Arrays.asList("a", "c"), ids(driver.findElements(By.cssSelector(".x"))));
        assertEquals(Arrays.asList("a"), ids(driver.findElements(By.cssSelector("[data-state=on]"))));
    }

    @Test
    public void liveStateSelectors() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <input type='checkbox' id='a' checked>\n"
                + "  <input type='checkbox' id='b'>\n"
                + "  <script>\n"
                + "    document.getElementById('a').checked = false;\n"
                + "    document.getElementById('b').checked = true;\n"
                + "  </script>\n"));

        assertEquals(Arrays.asList("b"), ids(driver.findElements(By.cssSelector("input:checked"))));
    }

    // ------------------------------------------------------------------
    // invalid selectors
    // ------------------------------------------------------------------

    @Test
    public void invalidSelectorsByCssSelector() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx' class='testClass'></div>\n"));
        final WebElement ctx = driver.findElement(By.id("ctx"));

        final String[] selectors = {"", " ", "div[", "..foo", "#", "!!", "a:not(", "div:unknownpseudo"};
        for (final String selector : selectors) {
            assertInvalidSelector(driver, selector);
            assertInvalidSelector(ctx, selector);
        }
    }
}
