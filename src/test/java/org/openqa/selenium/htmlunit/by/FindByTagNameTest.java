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
 * Tests for By.tagName.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class FindByTagNameTest extends WebDriverTestCase {

    @Test
    public void elementByTagName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <form>\n"
                        + "    <input id='testId' name='testName'>\n"
                        + "  <form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.tagName("input"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void elementsByTagName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <form>\n"
                        + "    <input id='testId' name='testName'>\n"
                        + "  <form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final List<WebElement> elements = driver.findElements(By.tagName("input"));
        assertEquals(1, elements.size());
        assertEquals("testId", elements.get(0).getAttribute("id"));
    }

    @Test
    public void relativeElementByTagName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <form>\n"
                        + "    <div id='ctx'>\n"
                        + "      <div id='testId' name='testName'></div>\n"
                        + "      <input id='testId2' name='testName2'>\n"
                        + "    </div>\n"
                        + "  <form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.tagName("input"));
        assertEquals("testId2", element.getAttribute("id"));
    }

    @Test
    public void relativeElementsByName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <form>\n"
                        + "    <div id='ctx'>\n"
                        + "      <div id='testId' name='testName'></div>\n"
                        + "      <input id='testId2' name='testName2'>\n"
                        + "    </div>\n"
                        + "  <form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final List<WebElement> elements = ctx.findElements(By.tagName("input"));
        assertEquals(1, elements.size());
        assertEquals("testId2", elements.get(0).getAttribute("id"));
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

    private static List<String> tagNames(final List<WebElement> elements) {
        final List<String> tagNames = new ArrayList<>();
        for (final WebElement element : elements) {
            tagNames.add(element.getTagName());
        }
        return tagNames;
    }

    private static void assertNotFound(final SearchContext scope, final String tagName) {
        assertEquals(tagName, 0, scope.findElements(By.tagName(tagName)).size());
        try {
            scope.findElement(By.tagName(tagName));
            fail("findElement: expected NoSuchElementException for tag name '" + tagName + "'");
        }
        catch (final NoSuchElementException e) {
            // expected
        }
    }

    private static void assertInvalidSelector(final SearchContext scope, final String tagName) {
        try {
            scope.findElement(By.tagName(tagName));
            fail("findElement: expected InvalidSelectorException for tag name '" + tagName + "'");
        }
        catch (final InvalidSelectorException e) {
            // expected
        }

        try {
            scope.findElements(By.tagName(tagName));
            fail("findElements: expected InvalidSelectorException for tag name '" + tagName + "'");
        }
        catch (final InvalidSelectorException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // no match
    // ------------------------------------------------------------------

    @Test(expected = NoSuchElementException.class)
    public void elementByTagNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a'></div>\n"));

        driver.findElement(By.tagName("input"));
    }

    @Test
    public void elementsByTagNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a'></div>\n"));

        assertTrue(driver.findElements(By.tagName("input")).isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void relativeElementByTagNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='a'></div>\n"
                + "  </div>\n"
                + "  <input id='outside'>\n"));

        driver.findElement(By.id("ctx")).findElement(By.tagName("input"));
    }

    @Test
    public void relativeElementsByTagNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='a'></div>\n"
                + "  </div>\n"
                + "  <input id='outside'>\n"));

        assertTrue(driver.findElement(By.id("ctx")).findElements(By.tagName("input")).isEmpty());
    }

    // ------------------------------------------------------------------
    // matching semantics
    // ------------------------------------------------------------------

    @Test
    public void elementsByTagNameInDocumentOrder() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='a'><div id='b'></div></div>\n"
                + "    <span id='s'></span>\n"
                + "    <div id='c'></div>\n"
                + "  </div>\n"
                + "  <div id='d'></div>\n"));

        assertEquals(Arrays.asList("ctx", "a", "b", "c", "d"), ids(driver.findElements(By.tagName("div"))));
        assertEquals("ctx", driver.findElement(By.tagName("div")).getAttribute("id"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(Arrays.asList("a", "b", "c"), ids(ctx.findElements(By.tagName("div"))));
        assertEquals("a", ctx.findElement(By.tagName("div")).getAttribute("id"));
    }

    @Test
    public void tagNameIsCaseInsensitive() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <input id='i1'>\n"
                + "    <INPUT id='i2'>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            for (final String name : new String[] {"input", "INPUT", "Input", "iNpUt"}) {
                assertEquals(name, Arrays.asList("i1", "i2"), ids(scope.findElements(By.tagName(name))));
                assertEquals(name, "i1", scope.findElement(By.tagName(name)).getAttribute("id"));
            }
        }
    }

    @Test
    public void wildcardTagName() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <span id='s'></span>\n"
                + "    <b id='b'><i id='i'></i></b>\n"
                + "  </div>\n"
                + "  <p id='p'></p>\n"));

        assertEquals(Arrays.asList("html", "head", "body", "div", "span", "b", "i", "p"),
                tagNames(driver.findElements(By.tagName("*"))));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(Arrays.asList("s", "b", "i"), ids(ctx.findElements(By.tagName("*"))));
        assertEquals("s", ctx.findElement(By.tagName("*")).getAttribute("id"));
    }

    @Test
    public void documentStructureElements() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'></div>\n"));

        assertEquals("html", driver.findElement(By.tagName("html")).getTagName());
        assertEquals("head", driver.findElement(By.tagName("head")).getTagName());
        assertEquals("body", driver.findElement(By.tagName("body")).getTagName());
        assertEquals(1, driver.findElements(By.tagName("body")).size());

        // ancestors are not part of an element search
        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertNotFound(ctx, "body");
        assertNotFound(ctx, "html");
    }

    @Test
    public void customAndUnknownElements() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <my-element id='custom'></my-element>\n"
                + "    <foo id='unknown'></foo>\n"
                + "    <h2 id='heading'></h2>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("custom"), ids(scope.findElements(By.tagName("my-element"))));
            assertEquals(Arrays.asList("unknown"), ids(scope.findElements(By.tagName("foo"))));
            assertEquals(Arrays.asList("heading"), ids(scope.findElements(By.tagName("h2"))));
            assertEquals("custom", scope.findElement(By.tagName("my-element")).getAttribute("id"));
        }
    }

    @Test
    public void nonRenderedElementsAreFound() throws Exception {
        final String html = "<html>\n"
                + "<head>\n"
                + "  <title>Title</title>\n"
                + "  <style>.x { color: red; }</style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <script>var x = 1;</script>\n"
                + "    <div id='hidden' style='display: none'></div>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        assertEquals(1, driver.findElements(By.tagName("title")).size());
        assertEquals(1, driver.findElements(By.tagName("style")).size());
        assertEquals(1, driver.findElements(By.tagName("script")).size());

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(1, ctx.findElements(By.tagName("script")).size());
        assertEquals(Arrays.asList("hidden"), ids(ctx.findElements(By.tagName("div"))));
    }

    @Test
    public void svgElements() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <svg id='svg' width='10' height='10'><circle id='circle' r='4'></circle></svg>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("svg"), ids(scope.findElements(By.tagName("svg"))));
            assertEquals(Arrays.asList("circle"), ids(scope.findElements(By.tagName("circle"))));
        }
    }

    @Test
    public void specialCharactersInTagNameMatchNothing() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='a' class='x'><p id='p'></p></div>\n"
                + "  </div>\n"));

        // a tag name is never interpreted as a selector
        final String[] names = {
            "div p", "div>p", "p,div", ".x", "#a", "[id]", "div:first-child", "a'b", "a\"b", "a\\b", "1x", "-", "*x"
        };

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final String name : names) {
            assertNotFound(driver, name);
            assertNotFound(ctx, name);
        }
    }

    // ------------------------------------------------------------------
    // invalid tag names
    // ------------------------------------------------------------------

    @Test
    public void emptyTagNameIsInvalid() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'></div>\n"));

        assertInvalidSelector(driver, "");
        assertInvalidSelector(driver.findElement(By.id("ctx")), "");
    }

    // ------------------------------------------------------------------
    // scope
    // ------------------------------------------------------------------

    @Test
    public void relativeSearchIgnoresContextAndOutsideElements() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='before'></div>\n"
                + "  <div id='ctx'>\n"
                + "    <div id='child'>\n"
                + "      <div id='grandchild'></div>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "  <div id='after'></div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(Arrays.asList("child", "grandchild"), ids(ctx.findElements(By.tagName("div"))));
        assertEquals("child", ctx.findElement(By.tagName("div")).getAttribute("id"));

        // chained
        assertEquals(Arrays.asList("grandchild"),
                ids(ctx.findElement(By.id("child")).findElements(By.tagName("div"))));
    }

    // ------------------------------------------------------------------
    // dynamic DOM
    // ------------------------------------------------------------------

    @Test
    public void elementsChangedByScript() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <p id='p1'></p>\n"
                + "  </div>\n"
                + "  <script>\n"
                + "    var ctx = document.getElementById('ctx');\n"
                + "    var p2 = document.createElement('p');\n"
                + "    p2.id = 'p2';\n"
                + "    ctx.appendChild(p2);\n"
                + "    var p3 = document.createElement('p');\n"
                + "    p3.id = 'p3';\n"
                + "    ctx.appendChild(p3);\n"
                + "    ctx.removeChild(p3);\n"
                + "    ctx.innerHTML += '<p id=\"p4\"></p>';\n"
                + "    var p5 = document.createElement('p');\n"
                + "    p5.id = 'p5';\n"
                + "    document.body.appendChild(p5);\n"
                + "  </script>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(Arrays.asList("p1", "p2", "p4"), ids(ctx.findElements(By.tagName("p"))));
        assertEquals(Arrays.asList("p1", "p2", "p4", "p5"), ids(driver.findElements(By.tagName("p"))));
    }
}
