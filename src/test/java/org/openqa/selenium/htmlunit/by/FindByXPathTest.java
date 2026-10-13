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

    private static String literal(final String value) {
        return value.contains("'") ? "\"" + value + "\"" : "'" + value + "'";
    }

    private static void assertIds(final SearchContext scope, final String xpath, final String expectedIds) {
        final List<String> expected = Arrays.asList(expectedIds.split(","));
        assertEquals(xpath, expected, ids(scope.findElements(By.xpath(xpath))));
        assertEquals(xpath, expected.get(0), scope.findElement(By.xpath(xpath)).getAttribute("id"));
    }

    private static void assertNotFound(final SearchContext scope, final String xpath) {
        assertEquals(xpath, 0, scope.findElements(By.xpath(xpath)).size());
        try {
            scope.findElement(By.xpath(xpath));
            fail("findElement: expected NoSuchElementException for xpath '" + xpath + "'");
        }
        catch (final NoSuchElementException e) {
            // expected
        }
    }

    private static void assertInvalidSelector(final SearchContext scope, final String xpath) {
        try {
            scope.findElement(By.xpath(xpath));
            fail("findElement: expected InvalidSelectorException for xpath '" + xpath + "'");
        }
        catch (final InvalidSelectorException e) {
            // expected
        }

        try {
            scope.findElements(By.xpath(xpath));
            fail("findElements: expected InvalidSelectorException for xpath '" + xpath + "'");
        }
        catch (final InvalidSelectorException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // no match
    // ------------------------------------------------------------------

    @Test(expected = NoSuchElementException.class)
    public void elementByXPathNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a' class='other'></div>\n"));

        driver.findElement(By.xpath(".//*[@class = 'testClass']"));
    }

    @Test
    public void elementsByXPathNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a' class='other'></div>\n"));

        assertTrue(driver.findElements(By.xpath(".//*[@class = 'testClass']")).isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void relativeElementByXPathNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='a' class='other'></div>\n"
                + "  </div>\n"
                + "  <div id='outside' class='testClass'></div>\n"));

        driver.findElement(By.id("ctx")).findElement(By.xpath(".//*[@class = 'testClass']"));
    }

    @Test
    public void relativeElementsByXPathNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div id='a' class='other'></div>\n"
                + "  </div>\n"
                + "  <div id='outside' class='testClass'></div>\n"));

        assertTrue(driver.findElement(By.id("ctx")).findElements(By.xpath(".//*[@class = 'testClass']")).isEmpty());
    }

    // ------------------------------------------------------------------
    // expressions
    // ------------------------------------------------------------------

    @Test
    public void commonExpressions() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='root'>\n"
                + "    <ul id='list'>\n"
                + "      <li id='li1' class='item first'>one</li>\n"
                + "      <li id='li2' class='item' data-x='2'>two</li>\n"
                + "      <li id='li3' class='item last' data-x='3'> three </li>\n"
                + "    </ul>\n"
                + "    <p id='p1'></p>\n"
                + "    <p id='p2' class='note'>note</p>\n"
                + "    <a id='link' href='about:blank'>Link</a>\n"
                + "  </div>\n"));

        final String[][] cases = {
            {"//*[@id='li2']", "li2"},
            {"//li", "li1,li2,li3"},
            {"(//li)[2]", "li2"},
            {"//li[1]", "li1"},
            {"//li[last()]", "li3"},
            {"//li[position() > 1]", "li2,li3"},
            {"//li[@data-x]", "li2,li3"},
            {"//li[@data-x='3']", "li3"},
            {"//li[contains(@class, 'last')]", "li3"},
            {"//li[starts-with(@class, 'item')]", "li1,li2,li3"},
            {"//li[text()='two']", "li2"},
            {"//li[normalize-space(text())='three']", "li3"},
            {"//li[not(@data-x)]", "li1"},
            {"//li[@data-x and contains(@class, 'last')]", "li3"},
            {"//li[@id='li1' or @id='li3']", "li1,li3"},
            {"//li[translate(@class, 'ITEM', 'item')='item']", "li2"},
            {"//ul/li", "li1,li2,li3"},
            {"//ul[@id='list']/li[2]/following-sibling::li", "li3"},
            {"//li[@id='li3']/preceding-sibling::li", "li1,li2"},
            {"//li[@id='li2']/..", "list"},
            {"//li[@id='li2']/ancestor::div", "root"},
            {"//p[not(node())]", "p1"},
            {"//a[@href]", "link"},
            {"//li | //p", "li1,li2,li3,p1,p2"},
            {"//p | //li", "li1,li2,li3,p1,p2"},
            {"(//li | //p)[last()]", "p2"},
            {"/html/body/div[@id='root']/*", "list,p1,p2,link"},
            {"//div[@id='root']/ul/li[position()=2 or position()=3]", "li2,li3"}
        };

        // absolute expressions are evaluated against the whole document, regardless of the scope
        final WebElement root = driver.findElement(By.id("root"));
        for (final SearchContext scope : new SearchContext[] {driver, root}) {
            for (final String[] c : cases) {
                assertIds(scope, c[0], c[1]);
            }
        }
    }

    @Test
    public void elementsByXPathInDocumentOrder() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <b id='b1'></b>\n"
                + "    <i id='i1'></i>\n"
                + "    <b id='b2'></b>\n"
                + "    <i id='i2'></i>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertIds(scope, ".//i | .//b", "b1,i1,b2,i2");
            assertIds(scope, "//i | //b", "b1,i1,b2,i2");
            // a union never contains an element twice
            assertIds(scope, "//b | //b[1]", "b1,b2");
        }
    }

    @Test
    public void surroundingWhitespaceIsAllowed() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <span id='s'></span>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertIds(scope, "  .//span[@id='s']\n", "s");
        }
    }

    @Test
    public void textFunctionsUseRawText() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <span id='s'> padded </span>\n"
                + "  </div>\n"));

        assertIds(driver, "//span[text()=' padded ']", "s");
        assertIds(driver, "//span[normalize-space(text())='padded']", "s");
        assertNotFound(driver, "//span[text()='padded']");
    }

    @Test
    public void elementNamesAreCaseInsensitive() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <span id='s'></span>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertIds(scope, ".//SPAN", "s");
            assertIds(scope, "//DIV[@id='ctx']", "ctx");
        }
    }

    @Test
    public void specialCharactersInLiterals() throws Exception {
        final String[] values = {
            "a]b", "a[b", "a/b", "a|b", "a&b", "a<b", "a=b", "a@b", "a b", "a'b", "a\\b", "a:b", "a(b)"
            // "gr\u00f6\u00dfe", "\u65e5\u672c\u8a9e"
        };

        for (final String value : values) {
            final WebDriver driver = loadPage2(html(
                    "  <div id='ctx'>\n"
                    + "    <span id='decoy1' class='a'></span>\n"
                    + "    <span id='decoy2' class='b'></span>\n"
                    + "    <span id='hit' class='" + escapeAttribute(value) + "'></span>\n"
                    + "  </div>\n"));

            final WebElement ctx = driver.findElement(By.id("ctx"));
            for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
                assertIds(scope, ".//*[@class = " + literal(value) + "]", "hit");
            }
        }
    }

    @Test
    public void hiddenAndNonRenderedElementsAreFound() throws Exception {
        final String html = "<html>\n"
                + "<head>\n"
                + "  <title>Title</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div id='ctx'>\n"
                + "    <div id='hidden' style='display: none'></div>\n"
                + "    <input type='hidden' id='hiddenInput'>\n"
                + "    <script>var x = 1;</script>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        assertIds(driver, "//div[@id='hidden']", "hidden");
        assertIds(driver, "//input[@type='hidden']", "hiddenInput");
        assertEquals("title", driver.findElement(By.xpath("//title")).getTagName());
        assertEquals("head", driver.findElement(By.xpath("//head")).getTagName());
        assertEquals("script", driver.findElement(By.xpath("//div[@id='ctx']/script")).getTagName());
    }

    // ------------------------------------------------------------------
    // scope
    // ------------------------------------------------------------------

    @Test
    public void relativePathsFromContextElement() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='top'>\n"
                + "    <div id='ctx'>\n"
                + "      <span id='s1'>1</span>\n"
                + "      <div id='inner'><span id='s2'>2</span></div>\n"
                + "    </div>\n"
                + "    <span id='sibling'>sib</span>\n"
                + "  </div>\n"
                + "  <span id='outside'>out</span>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        final String[][] cases = {
            {".", "ctx"},
            {"self::div", "ctx"},
            {".//span", "s1,s2"},
            {"descendant::span", "s1,s2"},
            {"span", "s1"},
            {"./span", "s1"},
            {"*", "s1,inner"},
            {"./*/span", "s2"},
            {"..", "top"},
            {"ancestor::div", "top"},
            {"following-sibling::span", "sibling"},
            // absolute expressions ignore the context element
            {"//span", "s1,s2,sibling,outside"},
            {"/html/body/span", "outside"}
        };

        for (final String[] c : cases) {
            assertIds(ctx, c[0], c[1]);
        }
    }

    @Test
    public void documentRootExpressions() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'></div>\n"));

        assertEquals("html", driver.findElement(By.xpath("/html")).getTagName());
        assertEquals("html", driver.findElement(By.xpath("/*")).getTagName());
        assertEquals("html", driver.findElement(By.xpath("//html")).getTagName());
        assertEquals("head", driver.findElement(By.xpath("/html/head")).getTagName());
        assertEquals("body", driver.findElement(By.xpath("/html/body")).getTagName());
        assertEquals(1, driver.findElements(By.xpath("/html")).size());

        // the context element itself and its ancestors can be selected by an element search
        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals("body", ctx.findElement(By.xpath("..")).getTagName());
        assertEquals("html", ctx.findElement(By.xpath("ancestor::html")).getTagName());
    }

    // ------------------------------------------------------------------
    // invalid expressions and results
    // ------------------------------------------------------------------

    @Test
    public void emptyAndBlankXPathIsInvalid() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'></div>\n"));
        final WebElement ctx = driver.findElement(By.id("ctx"));

        for (final String xpath : new String[] {"", " ", "\t", "\n", "  \t \r\n"}) {
            assertInvalidSelector(driver, xpath);
            assertInvalidSelector(ctx, xpath);
        }
    }

    @Test
    public void invalidXPathExpressions() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'></div>\n"));
        final WebElement ctx = driver.findElement(By.id("ctx"));

        final String[] expressions = {
            "//div[", "//", "///", "//div[@id='x'", "div[@class=", "not(", "//div[foo()]", "$var", "//x:y"
        };
        for (final String xpath : expressions) {
            assertInvalidSelector(driver, xpath);
            assertInvalidSelector(ctx, xpath);
        }
    }

    @Test
    public void nonElementResultsAreInvalid() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx' class='c'>text<!-- comment --></div>\n"));
        final WebElement ctx = driver.findElement(By.id("ctx"));

        final String[] expressions = {
            "//div/@id", "//div/text()", "//div/comment()", "count(//div)", "string(//div)", "true()"
        };
        for (final String xpath : expressions) {
            assertInvalidSelector(driver, xpath);
            assertInvalidSelector(ctx, xpath);
        }
    }

    @Test
    public void emptyResultOfNonElementExpression() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'></div>\n"));
        final WebElement ctx = driver.findElement(By.id("ctx"));

        assertNotFound(driver, "//div/@missing");
        assertNotFound(ctx, "//div/@missing");
        assertNotFound(driver, "//div/text()");
        assertNotFound(ctx, "//div/text()");
    }

    // ------------------------------------------------------------------
    // dynamic DOM
    // ------------------------------------------------------------------

    @Test
    public void elementsChangedByScript() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <p id='p1' class='old'></p>\n"
                + "  </div>\n"
                + "  <script>\n"
                + "    var ctx = document.getElementById('ctx');\n"
                + "    document.getElementById('p1').className = 'new';\n"
                + "    var p2 = document.createElement('p');\n"
                + "    p2.id = 'p2';\n"
                + "    p2.className = 'new';\n"
                + "    ctx.appendChild(p2);\n"
                + "    var p3 = document.createElement('p');\n"
                + "    p3.id = 'p3';\n"
                + "    p3.className = 'new';\n"
                + "    ctx.appendChild(p3);\n"
                + "    ctx.removeChild(p3);\n"
                + "    var p4 = document.createElement('p');\n"
                + "    p4.id = 'p4';\n"
                + "    p4.className = 'new';\n"
                + "    document.body.appendChild(p4);\n"
                + "  </script>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertIds(ctx, ".//p[@class='new']", "p1,p2");
        assertIds(driver, ".//p[@class='new']", "p1,p2,p4");
        assertNotFound(driver, ".//p[@class='old']");
        assertNotFound(ctx, ".//p[@class='old']");
    }
}
