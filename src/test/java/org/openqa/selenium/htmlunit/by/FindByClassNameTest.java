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
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.HtmlUnitNYI;

/**
 * Tests for By.className.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class FindByClassNameTest extends WebDriverTestCase {

    @Test
    public void elementByClassName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testId' class='testClass'></div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.className("testClass"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void elementsByClassName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testId' class='testClass other'></div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final List<WebElement> elements = driver.findElements(By.className("testClass"));
        assertEquals(1, elements.size());
        assertEquals("testId", elements.get(0).getAttribute("id"));
    }

    @Test
    public void relativeElementByClassName() throws Exception {
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
        final WebElement element = ctx.findElement(By.className("testClass"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void relativeElementsByClassName() throws Exception {
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
        final List<WebElement> elements = ctx.findElements(By.className("testClass"));
        assertEquals(1, elements.size());
        assertEquals("testId2", elements.get(0).getAttribute("id"));
    }

    @Test
    @Alerts("testId")
    @HtmlUnitNYI(CHROME = "", EDGE = "", FF = "", FF_ESR = "")
    public void caseInsensitiveByClassName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testId' class='testClass'></div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        try {
            final WebElement element = driver.findElement(By.className("Testclass"));
            assertEquals(getExpectedAlerts()[0], element.getAttribute("id"));
        }
        catch (final NoSuchElementException e) {
            assertEquals(getExpectedAlerts()[0], "");
        }
    }

    @Test(expected = InvalidSelectorException.class)
    public void invalidSelectorByClassName() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testId' class='testClass'></div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.className("selector with blank"));
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

    private static void assertInvalidSelector(final SearchContext context, final String name) {
        try {
            context.findElement(By.className(name));
            fail("findElement: expected InvalidSelectorException for class name '" + name + "'");
        }
        catch (final InvalidSelectorException e) {
            // expected
        }

        try {
            context.findElements(By.className(name));
            fail("findElements: expected InvalidSelectorException for class name '" + name + "'");
        }
        catch (final InvalidSelectorException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // no match
    // ------------------------------------------------------------------

    @Test(expected = NoSuchElementException.class)
    public void elementByClassNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a' class='other'></div>\n"
                + "  <div id='b'></div>\n"
                + "  <div id='c' class=''></div>\n"));

        driver.findElement(By.className("testClass"));
    }

    @Test
    public void elementsByClassNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a' class='other'></div>\n"
                + "  <div id='b'></div>\n"
                + "  <div id='c' class=''></div>\n"));

        assertTrue(driver.findElements(By.className("testClass")).isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void relativeElementByClassNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='a' class='other'></div>\n"
                + "  </div>\n"
                + "  <div id='outside' class='testClass'></div>\n"));

        driver.findElement(By.id("ctx")).findElement(By.className("testClass"));
    }

    @Test
    public void relativeElementsByClassNameNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='a' class='other'></div>\n"
                + "  </div>\n"
                + "  <div id='outside' class='testClass'></div>\n"));

        assertTrue(driver.findElement(By.id("ctx")).findElements(By.className("testClass")).isEmpty());
    }

    // ------------------------------------------------------------------
    // matching semantics
    // ------------------------------------------------------------------

    @Test
    public void elementsByClassNameInDocumentOrder() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='first' class='testClass'>\n"
                + "    <span id='nested' class='x testClass y'></span>\n"
                + "  </div>\n"
                + "  <div id='other' class='other'></div>\n"
                + "  <p id='last' class='testClass'></p>\n"));

        assertEquals(Arrays.asList("first", "nested", "last"), ids(driver.findElements(By.className("testClass"))));
        assertEquals("first", driver.findElement(By.className("testClass")).getAttribute("id"));
    }

    @Test
    public void classNameMustMatchWholeToken() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='prefix' class='testClassX'></div>\n"
                + "  <div id='suffix' class='XtestClass'></div>\n"
                + "  <div id='hyphen' class='testClass-x test-testClass'></div>\n"
                + "  <div id='hit' class='testClass'></div>\n"));

        assertEquals(Arrays.asList("hit"), ids(driver.findElements(By.className("testClass"))));
    }

    @Test
    public void classAttributeWithIrregularWhitespace() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='testId' class='  other \t\n  testClass \r\n '></div>\n"
                + "  <div id='dup' class='testClass testClass'></div>\n"));

        assertEquals(Arrays.asList("testId", "dup"), ids(driver.findElements(By.className("testClass"))));
    }

    @Test
    public void specialCharactersInClassName() throws Exception {
        final String[] names = {
            "foo.bar", "a:b", "a#b", "a[b]", "a(b)", "a>b", "a+b", "a~b", "a,b", "a*b", "a=b",
            "a@b", "a/b", "a\\b", "a%b", "a&b", "a$b",
            "1abc", "-", "_", "--x", "test-class_1",
            "a'b", "a\"b", "a'\"b"
            // "gr\u00f6\u00dfe", "\u65e5\u672c\u8a9e"
        };

        for (final String name : names) {
            final WebDriver driver = loadPage2(html(
                    "  <div id='ctx'>\n"
                    + "    <div id='decoy1' class='foo bar'></div>\n"
                    + "    <div id='decoy2' class='a b'></div>\n"
                    + "    <div id='hit' class='" + escapeAttribute(name) + "'></div>\n"
                    + "  </div>\n"));
            final WebElement ctx = driver.findElement(By.id("ctx"));

            assertEquals(name, "hit", driver.findElement(By.className(name)).getAttribute("id"));
            assertEquals(name, Arrays.asList("hit"), ids(driver.findElements(By.className(name))));
            assertEquals(name, "hit", ctx.findElement(By.className(name)).getAttribute("id"));
            assertEquals(name, Arrays.asList("hit"), ids(ctx.findElements(By.className(name))));
        }
    }

    @Test
    public void caseSensitiveInStandardsMode() throws Exception {
        final String html = "<!DOCTYPE html>\n"
                + html("  <div id='testId' class='testClass'></div>\n");

        final WebDriver driver = loadPage2(html);
        assertTrue(driver.findElements(By.className("Testclass")).isEmpty());
        assertEquals("testId", driver.findElement(By.className("testClass")).getAttribute("id"));
    }

    @Test
    public void hiddenElementsAreFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='hidden' class='testClass' style='display: none'></div>\n"));

        assertEquals("hidden", driver.findElement(By.className("testClass")).getAttribute("id"));
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
        assertEquals(Arrays.asList("child", "grandchild"), ids(ctx.findElements(By.className("testClass"))));
        assertEquals("child", ctx.findElement(By.className("testClass")).getAttribute("id"));
    }

    // ------------------------------------------------------------------
    // dynamic DOM
    // ------------------------------------------------------------------

    @Test
    public void classNameChangedByScript() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a' class='before'></div>\n"
                + "  <div id='b' class='other'></div>\n"
                + "  <script>\n"
                + "    document.getElementById('a').className = 'after';\n"
                + "    document.getElementById('b').classList.add('after');\n"
                + "    var c = document.createElement('div');\n"
                + "    c.id = 'c';\n"
                + "    c.className = 'after';\n"
                + "    document.body.appendChild(c);\n"
                + "  </script>\n"));

        assertTrue(driver.findElements(By.className("before")).isEmpty());
        assertEquals(Arrays.asList("a", "b", "c"), ids(driver.findElements(By.className("after"))));
    }

    // ------------------------------------------------------------------
    // invalid class names
    // ------------------------------------------------------------------

    @Test
    public void invalidSelectorEmptyOrWhitespaceByClassName() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx' class='testClass'></div>\n"));
        final WebElement ctx = driver.findElement(By.id("ctx"));

        final String[] names = {"", " ", "a b", "a\tb", "a\nb", "a\r\nb", "a\fb", " a", "a ", "\ta"};
        for (final String name : names) {
            assertInvalidSelector(driver, name);
            assertInvalidSelector(ctx, name);
        }
    }
}
