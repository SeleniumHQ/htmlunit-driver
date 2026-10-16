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

    @Test(expected = NoSuchElementException.class)
    public void elementByIdNotFound() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId'>TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("unknown"));
    }

    @Test
    public void elementsByIdNothingFound() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId'>TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final List<WebElement> elements = driver.findElements(By.id("unknown"));
        assertEquals(0, elements.size());
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
                        + "  <div id='testDivId'>TestDiv Clone</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.id("testDivId"));
        assertEquals("TestDiv", element.getText());
    }

    @Test(expected = NoSuchElementException.class)
    public void relativeElementByIdNotFound() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='ctx'>\n"
                        + "    <div id='testDivId2'>TestDiv2</div>\n"
                        + "  </div>\n"
                        + "  <div id='testDivId'>TestDiv Clone</div>\n"
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
                        + "    <div id='testDivId2'>TestDiv2</div>\n"
                        + "  </div>\n"
                        + "  <div id='testDivId'>TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final List<WebElement> elements = ctx.findElements(By.id("testDivId"));
        assertEquals(0, elements.size());
    }

    /**
     * FindByID within an element context uses XPath internally.
     * An id containing a single quote must not break the expression.
     */
    @Test
    public void findByIdWithSingleQuoteInId() throws Exception {
        final String html = "<html>\n"
                + "<head></head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <span id=\"it's-fine\">target</span>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        // Must find the element rather than throw InvalidSelectorException
        final WebElement element = ctx.findElement(By.id("it's-fine"));
        assertEquals("it's-fine", element.getAttribute("id"));
    }

    /**
     * findElements by id within an element context with a single quote in the id.
     */
    @Test
    public void findElementsByIdWithSingleQuoteInId() throws Exception {
        final String html = "<html>\n"
                + "<head></head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <span id=\"it's-fine\">first</span>\n"
                + "    <span id=\"it's-fine\">second</span>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final List<WebElement> elements = ctx.findElements(By.id("it's-fine"));
        assertEquals(2, elements.size());
    }


    /**
     * An id containing both single and double quotes — the pathological case
     * that defeats simple quoting strategies.
     */
    @Test
    public void findByIdWithBothQuoteTypesInId() throws Exception {
        // id = it's a "test"
        final String html = "<html>\n"
                + "<head></head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <span id='it&apos;s a &quot;test&quot;'>target</span>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.id("it's a \"test\""));
        assertEquals("it's a \"test\"", element.getAttribute("id"));
    }

    /**
     * Closing-bracket in an id value must not terminate the XPath predicate early.
     */
    @Test
    public void findByIdWithClosingBracketInId() throws Exception {
        final String html = "<html>\n"
                + "<head></head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <span id='tricky]id'>target</span>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.id("tricky]id"));
        assertEquals("tricky]id", element.getAttribute("id"));
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

    private static List<String> classes(final List<WebElement> elements) {
        final List<String> classes = new ArrayList<>();
        for (final WebElement element : elements) {
            classes.add(element.getAttribute("class"));
        }
        return classes;
    }

    /**
     * Escapes the value for the usage inside of a html attribute. All non ASCII characters are written as
     * numeric character reference; this way the result does not depend on the charset the page is served with
     * (a real browser would decode a page without charset declaration as ISO-8859-1 and the name in the page
     * would not match the name searched for).
     */
    private static String escapeAttribute(final String value) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length();) {
            final int codePoint = value.codePointAt(i);
            i += Character.charCount(codePoint);

            if (codePoint == '&') {
                result.append("&amp;");
            }
            else if (codePoint == '\'') {
                result.append("&#39;");
            }
            else if (codePoint == '"') {
                result.append("&quot;");
            }
            else if (codePoint == '<') {
                result.append("&lt;");
            }
            else if (codePoint > 127) {
                result.append("&#").append(codePoint).append(';');
            }
            else {
                result.append((char) codePoint);
            }
        }
        return result.toString();
    }

    private static boolean found(final SearchContext context, final String id) {
        return !context.findElements(By.id(id)).isEmpty();
    }

    private static void assertEmptyIdNeverMatches(final SearchContext context) {
        // depending on the implementation an empty id is either simply not found or an invalid selector
        try {
            assertEquals(0, context.findElements(By.id("")).size());
        }
        catch (final InvalidSelectorException e) {
            // acceptable
        }

        try {
            context.findElement(By.id(""));
            fail("an empty id must never match an element");
        }
        catch (final NoSuchElementException | InvalidSelectorException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // duplicates and ordering
    // ------------------------------------------------------------------

    @Test
    public void elementsByIdWithDuplicates() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='testDivId' class='one'></div>\n"
                + "  <div id='other'></div>\n"
                + "  <div id='testDivId' class='two'></div>\n"));

        assertEquals(Arrays.asList("one", "two"), classes(driver.findElements(By.id("testDivId"))));
        assertEquals("one", driver.findElement(By.id("testDivId")).getAttribute("class"));
    }

    @Test
    public void relativeElementsByIdInDocumentOrder() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='before' class='outside-before'></div>\n"
                + "  <div id='ctx'>\n"
                + "    <div id='testDivId' class='one'>\n"
                + "      <span id='testDivId' class='nested'></span>\n"
                + "    </div>\n"
                + "    <div id='testDivId' class='three'></div>\n"
                + "  </div>\n"
                + "  <div id='testDivId' class='outside-after'></div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(Arrays.asList("one", "nested", "three"), classes(ctx.findElements(By.id("testDivId"))));
        assertEquals("one", ctx.findElement(By.id("testDivId")).getAttribute("class"));
    }

    // ------------------------------------------------------------------
    // scope
    // ------------------------------------------------------------------

    @Test(expected = NoSuchElementException.class)
    public void relativeElementByIdDoesNotFindContext() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='child'></div>\n"
                + "  </div>\n"));

        driver.findElement(By.id("ctx")).findElement(By.id("ctx"));
    }

    @Test
    public void relativeElementsByIdDoesNotFindContext() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='child'></div>\n"
                + "  </div>\n"));

        assertTrue(driver.findElement(By.id("ctx")).findElements(By.id("ctx")).isEmpty());
    }

    @Test
    public void relativeElementByIdFindsDeepDescendant() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='level1'>\n"
                + "      <div id='level2'>\n"
                + "        <span id='deep'>deep</span>\n"
                + "      </div>\n"
                + "    </div>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals("deep", ctx.findElement(By.id("deep")).getText());
        assertEquals(1, ctx.findElements(By.id("deep")).size());

        // chained lookups
        assertEquals("deep", ctx.findElement(By.id("level1")).findElement(By.id("deep")).getText());
    }

    // ------------------------------------------------------------------
    // matching semantics
    // ------------------------------------------------------------------

    @Test
    public void exactIdMatchOnly() throws Exception {
        final String html = "<!DOCTYPE html>\n"
                + html("  <div id='ctx'>\n"
                    + "    <div id='testDivId' class='hit'></div>\n"
                    + "    <div id='testDivId2'></div>\n"
                    + "    <div id='xtestDivId'></div>\n"
                    + "    <div id='testdivid'></div>\n"
                    + "    <div id=' padded '></div>\n"
                    + "  </div>\n");

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));

        assertEquals(Arrays.asList("hit"), classes(driver.findElements(By.id("testDivId"))));
        assertEquals(Arrays.asList("hit"), classes(ctx.findElements(By.id("testDivId"))));

        // no case folding in standards mode, no trimming of the attribute value
        assertFalse(found(driver, "TESTDIVID"));
        assertFalse(found(ctx, "TESTDIVID"));
        assertFalse(found(driver, "padded"));
        assertFalse(found(ctx, "padded"));
    }

    @Test
    public void specialCharactersInId() throws Exception {
        final String[] ids = {
            "it's-fine", "it's a \"test\"", "a\"b", "tricky]id", "a[b]", "a b",
            "a:b", "a.b", "a#b", "a>b", "a+b", "a~b", "a,b", "a*b", "a=b", "a@b", "a/b", "a\\b",
            "a%b", "a&b", "a$b", "a(b)", "a<b",
            "1abc", "-", "_", "--x", "[id]",
            "gr\u00f6\u00dfe", "\u65e5\u672c\u8a9e"
        };

        for (final String id : ids) {
            final WebDriver driver = loadPage2(html(
                    "  <div id='ctx'>\n"
                    + "    <div id='a'></div>\n"
                    + "    <div id='b'></div>\n"
                    + "    <div id='foo'></div>\n"
                    + "    <div id='bar'></div>\n"
                    + "    <div id='" + escapeAttribute(id) + "'></div>\n"
                    + "  </div>\n"));
            final WebElement ctx = driver.findElement(By.id("ctx"));

            // driver scope
            assertEquals(id, id, driver.findElement(By.id(id)).getAttribute("id"));
            assertEquals(id, 1, driver.findElements(By.id(id)).size());

            // element scope
            assertEquals(id, id, ctx.findElement(By.id(id)).getAttribute("id"));
            assertEquals(id, 1, ctx.findElements(By.id(id)).size());
        }
    }

    @Test
    public void emptyId() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <span id=''>empty</span>\n"
                + "    <span>no id</span>\n"
                + "  </div>\n"));

        assertEmptyIdNeverMatches(driver);
        assertEmptyIdNeverMatches(driver.findElement(By.id("ctx")));
    }

    @Test
    public void hiddenElementsAreFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='hidden' style='display: none'>hidden</div>\n"
                + "  </div>\n"));

        assertEquals("hidden", driver.findElement(By.id("hidden")).getAttribute("id"));
        assertEquals("hidden", driver.findElement(By.id("ctx")).findElement(By.id("hidden")).getAttribute("id"));
    }

    // ------------------------------------------------------------------
    // dynamic DOM
    // ------------------------------------------------------------------

    @Test
    public void idsChangedByScript() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='a'></div>\n"
                + "    <div id='b'></div>\n"
                + "    <div id='x'></div>\n"
                + "    <div id='out'></div>\n"
                + "  </div>\n"
                + "  <div id='m'></div>\n"
                + "  <script>\n"
                + "    var ctx = document.getElementById('ctx');\n"
                + "    document.getElementById('a').id = 'a2';\n"
                + "    document.getElementById('b').setAttribute('id', 'b2');\n"
                + "    document.getElementById('x').removeAttribute('id');\n"
                + "    var c = document.createElement('div');\n"
                + "    c.id = 'c';\n"
                + "    ctx.appendChild(c);\n"
                + "    var d = document.createElement('div');\n"
                + "    d.id = 'd';\n"
                + "    ctx.appendChild(d);\n"
                + "    d.parentNode.removeChild(d);\n"
                + "    ctx.appendChild(document.getElementById('m'));\n"
                + "    document.body.appendChild(document.getElementById('out'));\n"
                + "  </script>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));

        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            // old / removed ids are gone
            assertFalse("a", found(scope, "a"));
            assertFalse("b", found(scope, "b"));
            assertFalse("x", found(scope, "x"));
            assertFalse("d", found(scope, "d"));

            // new ids are found
            assertTrue("a2", found(scope, "a2"));
            assertTrue("b2", found(scope, "b2"));
            assertTrue("c", found(scope, "c"));
            assertTrue("m", found(scope, "m"));
        }

        // 'out' was moved out of the context element
        assertTrue(found(driver, "out"));
        assertFalse(found(ctx, "out"));
    }
}
