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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;

/**
 * Tests for By.name.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class FindByNameTest extends WebDriverTestCase {

    @Test
    public void elementByName() throws Exception {
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
        final WebElement element = driver.findElement(By.name("testName"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void elementsByName() throws Exception {
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
        final List<WebElement> elements = driver.findElements(By.name("testName"));
        assertEquals(1, elements.size());
        assertEquals("testId", elements.get(0).getAttribute("id"));
    }

    @Test
    public void relativeElementByName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <form>\n"
                        + "    <div id='ctx'>\n"
                        + "      <input id='testId' name='testName'>\n"
                        + "      <input id='testId2' name='testName2'>\n"
                        + "    </div>\n"
                        + "  <form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.name("testName"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void relativeElementsByName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <form>\n"
                        + "    <div id='ctx'>\n"
                        + "      <input id='testId' name='testName'>\n"
                        + "      <input id='testId2' name='testName2'>\n"
                        + "    </div>\n"
                        + "  <form>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final List<WebElement> elements = ctx.findElements(By.name("testName"));
        assertEquals(1, elements.size());
        assertEquals("testId", elements.get(0).getAttribute("id"));
    }

    /**
     * FindByName within an element context uses XPath internally.
     * A name containing a single quote must not break the expression.
     */
    @Test
    public void findByNameWithSingleQuoteInName() throws Exception {
        final String html = "<html>\n"
                + "<head></head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <input name=\"user's-input\" value='target'/>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.name("user's-input"));
        assertEquals("target", element.getAttribute("value"));
    }

    /**
     * A name containing a double quote should also be handled safely.
     */
    @Test
    public void findByNameWithDoubleQuoteInName() throws Exception {
        final String html = "<html>\n"
                + "<head></head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <input name='say &quot;hello&quot;' value='found'/>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement ctx = driver.findElement(By.id("ctx"));
        final WebElement element = ctx.findElement(By.name("say \"hello\""));
        assertEquals("found", element.getAttribute("value"));
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

    private static String escapeAttribute(final String value) {
        return value.replace("&", "&amp;")
                .replace("'", "&#39;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;");
    }

    // ------------------------------------------------------------------
    // no match
    // ------------------------------------------------------------------

    @Test(expected = NoSuchElementException.class)
    public void elementByNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <input id='a' name='other'>\n"
                + "  <input id='b'>\n"));

        driver.findElement(By.name("testName"));
    }

    @Test
    public void elementsByNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <input id='a' name='other'>\n"
                + "  <input id='b'>\n"));

        assertTrue(driver.findElements(By.name("testName")).isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void relativeElementByNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <input id='a' name='other'>\n"
                + "  </div>\n"
                + "  <input id='outside' name='testName'>\n"));

        driver.findElement(By.id("ctx")).findElement(By.name("testName"));
    }

    @Test
    public void relativeElementsByNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <input id='a' name='other'>\n"
                + "  </div>\n"
                + "  <input id='outside' name='testName'>\n"));

        assertTrue(driver.findElement(By.id("ctx")).findElements(By.name("testName")).isEmpty());
    }

    // ------------------------------------------------------------------
    // matching semantics
    // ------------------------------------------------------------------

    @Test
    public void elementsByNameInDocumentOrder() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <input type='radio' id='r1' name='group'>\n"
                + "    <input id='other' name='other'>\n"
                + "    <div><input type='radio' id='r2' name='group'></div>\n"
                + "    <input type='radio' id='r3' name='group'>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("r1", "r2", "r3"), ids(scope.findElements(By.name("group"))));
            assertEquals("r1", scope.findElement(By.name("group")).getAttribute("id"));
        }
    }

    @Test
    public void anyElementWithNameAttributeMatches() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <input id='input' name='n'>\n"
                + "    <select id='select' name='n'></select>\n"
                + "    <textarea id='textarea' name='n'></textarea>\n"
                + "    <button id='button' name='n'></button>\n"
                + "    <a id='anchor' name='n'>anchor</a>\n"
                + "    <form id='form' name='n'></form>\n"
                + "    <span id='span' name='n'></span>\n"
                + "  </div>\n"));

        final List<String> expected =
                Arrays.asList("input", "select", "textarea", "button", "anchor", "form", "span");
        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(expected, ids(scope.findElements(By.name("n"))));
        }
    }

    @Test
    public void exactNameMatchOnly() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <input id='hit' name='testName'>\n"
                + "    <input id='d1' name='testName2'>\n"
                + "    <input id='d2' name='xtestName'>\n"
                + "    <input id='d3' name='testname'>\n"
                + "    <input id='d4' name='test Name'>\n"
                + "    <input id='d5' name='testName extra'>\n"
                + "    <input id='d6' name=' padded '>\n"
                + "    <div id='testName'></div>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            // no prefix/suffix/token/case matching and no matching of the id attribute
            assertEquals(Arrays.asList("hit"), ids(scope.findElements(By.name("testName"))));

            assertTrue(scope.findElements(By.name("padded")).isEmpty());
            assertTrue(scope.findElements(By.name("extra")).isEmpty());
        }
    }

    @Test
    public void specialCharactersInName() throws Exception {
        final String[] names = {
            "user's-input", "say \"hello\"", "a'b\"c", "a'\"b",
            "a]b", "a[b]", "items[]", "user[name]", "a b",
            "a:b", "a.b", "a#b", "a>b", "a+b", "a~b", "a,b", "a*b", "a=b", "a@b", "a/b", "a\\b",
            "a%b", "a&b", "a$b", "a(b)", "a<b",
            "1abc", "-", "_", "--x"
            // "gr\u00f6\u00dfe", "\u65e5\u672c\u8a9e"
        };

        for (final String name : names) {
            final WebDriver driver = loadPage2(html(
                    "  <div id='ctx'>\n"
                    + "    <input id='decoy1' name='a'>\n"
                    + "    <input id='decoy2' name='b'>\n"
                    + "    <input id='decoy3' name='foo'>\n"
                    + "    <input id='hit' name='" + escapeAttribute(name) + "'>\n"
                    + "  </div>\n"));
            final WebElement ctx = driver.findElement(By.id("ctx"));

            for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
                assertEquals(name, Arrays.asList("hit"), ids(scope.findElements(By.name(name))));
                assertEquals(name, "hit", scope.findElement(By.name(name)).getAttribute("id"));
            }
        }
    }

    @Test
    public void hiddenElementsAreFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <input type='hidden' id='h1' name='csrf' value='token'>\n"
                + "    <input id='h2' name='csrf' style='display: none'>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("h1", "h2"), ids(scope.findElements(By.name("csrf"))));
        }
    }

    // ------------------------------------------------------------------
    // scope
    // ------------------------------------------------------------------

    @Test
    public void relativeSearchIgnoresContextAndOutsideElements() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <input id='before' name='n'>\n"
                + "  <div id='ctx' name='n'>\n"
                + "    <div><input id='deep' name='n'></div>\n"
                + "    <input id='child' name='n'>\n"
                + "  </div>\n"
                + "  <input id='after' name='n'>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(Arrays.asList("deep", "child"), ids(ctx.findElements(By.name("n"))));
        assertEquals("deep", ctx.findElement(By.name("n")).getAttribute("id"));

        assertEquals(Arrays.asList("before", "ctx", "deep", "child", "after"),
                ids(driver.findElements(By.name("n"))));
    }

    // ------------------------------------------------------------------
    // dynamic DOM
    // ------------------------------------------------------------------

    @Test
    public void namesChangedByScript() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <input id='a' name='old'>\n"
                + "    <input id='b' name='keep'>\n"
                + "    <input id='c' name='remove'>\n"
                + "  </div>\n"
                + "  <script>\n"
                + "    var ctx = document.getElementById('ctx');\n"
                + "    document.getElementById('a').name = 'renamed';\n"
                + "    document.getElementById('b').setAttribute('name', 'renamed');\n"
                + "    document.getElementById('c').removeAttribute('name');\n"
                + "    var n = document.createElement('input');\n"
                + "    n.id = 'n';\n"
                + "    n.name = 'created';\n"
                + "    ctx.appendChild(n);\n"
                + "    var x = document.createElement('input');\n"
                + "    x.id = 'x';\n"
                + "    x.setAttribute('name', 'created');\n"
                + "    ctx.appendChild(x);\n"
                + "    ctx.removeChild(x);\n"
                + "  </script>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertTrue("old", scope.findElements(By.name("old")).isEmpty());
            assertTrue("keep", scope.findElements(By.name("keep")).isEmpty());
            assertTrue("remove", scope.findElements(By.name("remove")).isEmpty());

            assertEquals("renamed", Arrays.asList("a", "b"), ids(scope.findElements(By.name("renamed"))));
            assertEquals("created", Arrays.asList("n"), ids(scope.findElements(By.name("created"))));
        }
    }
}
