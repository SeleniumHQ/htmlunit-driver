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
 * Tests for By.partialLinkText.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class FindByPartialLinkTextTest extends WebDriverTestCase {

    @Test
    public void elementByPartialLinkText() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='testId'>TestA</a>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(By.partialLinkText("est"));
        assertEquals("testId", element.getAttribute("id"));
    }

    @Test
    public void elementsByPartialLinkText() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='testId'>TestA</a>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final List<WebElement> elements = driver.findElements(By.partialLinkText("est"));
        assertEquals(1, elements.size());
        assertEquals("testId", elements.get(0).getAttribute("id"));
    }

    @Test
    public void relativeElementByPartialLinkText() throws Exception {
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
        final WebElement element = ctx.findElement(By.partialLinkText("estB"));
        assertEquals("testId2", element.getAttribute("id"));
    }

    @Test
    public void relativeElementsByPartialLinkText() throws Exception {
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
        final List<WebElement> elements = ctx.findElements(By.partialLinkText("es"));
        assertEquals(2, elements.size());
        assertEquals("testId", elements.get(0).getAttribute("id"));
        assertEquals("testId2", elements.get(1).getAttribute("id"));
    }

    @Test
    public void normalization() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='a1' href='about:blank' >Link 1</div>\n"
                        + "  <a id='a2' href='about:blank' >pre Link 2 post </div>\n"
                        + "  <a id='a3' href='about:blank' >pre Link  3    \n \t\t post\n</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        final WebElement body = driver.findElement(By.tagName("body"));

        WebElement elem = body.findElement(By.partialLinkText("Link 1"));
        assertEquals("Link 1", elem.getText());

        elem = body.findElement(By.partialLinkText("Link 2"));
        assertEquals("pre Link 2 post", elem.getText());

        elem = body.findElement(By.partialLinkText("Link 3"));
        assertEquals("pre Link 3 post", elem.getText());
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

        final List<WebElement> elements = body.findElements(By.partialLinkText(" Link 1"));
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

        final List<WebElement> elements = body.findElements(By.partialLinkText("Link 1 "));
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

        final WebElement elem = body.findElement(By.partialLinkText("Link 1"));
        assertEquals("Link 1", elem.getText());
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

    private static String escapeHtml(final String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;");
    }

    // ------------------------------------------------------------------
    // no match
    // ------------------------------------------------------------------

    @Test(expected = NoSuchElementException.class)
    public void elementByPartialLinkTextNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <a id='a'>Other</a>\n"));

        driver.findElement(By.partialLinkText("est"));
    }

    @Test
    public void elementsByPartialLinkTextNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <a id='a'>Other</a>\n"));

        assertTrue(driver.findElements(By.partialLinkText("est")).isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void relativeElementByPartialLinkTextNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='a'>Other</a>\n"
                + "  </div>\n"
                + "  <a id='outside'>TestA</a>\n"));

        driver.findElement(By.id("ctx")).findElement(By.partialLinkText("est"));
    }

    @Test
    public void relativeElementsByPartialLinkTextNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='a'>Other</a>\n"
                + "  </div>\n"
                + "  <a id='outside'>TestA</a>\n"));

        assertTrue(driver.findElement(By.id("ctx")).findElements(By.partialLinkText("est")).isEmpty());
    }

    // ------------------------------------------------------------------
    // matching semantics
    // ------------------------------------------------------------------

    @Test
    public void elementsByPartialLinkTextInDocumentOrder() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='first'>TestA</a>\n"
                + "    <a id='other'>Other</a>\n"
                + "    <ul><li><a id='nested'>my Test</a></li></ul>\n"
                + "    <a id='last'>Test</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("first", "nested", "last"), ids(scope.findElements(By.partialLinkText("Test"))));
            assertEquals("first", scope.findElement(By.partialLinkText("Test")).getAttribute("id"));
        }
    }

    @Test
    public void onlyAnchorsMatch() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <span id='span'>TestA</span>\n"
                + "    <div id='div'>TestA</div>\n"
                + "    <button id='button'>TestA</button>\n"
                + "    <p id='p'><b>TestA</b></p>\n"
                + "    <a id='hit'>TestA</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("hit"), ids(scope.findElements(By.partialLinkText("est"))));
        }
    }

    @Test
    public void substringPositionsAndCase() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='start'>Test one</a>\n"
                + "    <a id='middle'>my Test link</a>\n"
                + "    <a id='end'>last Test</a>\n"
                + "    <a id='whole'>Test</a>\n"
                + "    <a id='inside'>XTestX</a>\n"
                + "    <a id='case'>test lower</a>\n"
                + "    <a id='split'>Te st</a>\n"
                + "    <a id='scattered'>T e s t</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        final List<String> expected = Arrays.asList("start", "middle", "end", "whole", "inside");
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(expected, ids(scope.findElements(By.partialLinkText("Test"))));
        }
    }

    @Test
    public void textIsFoundOnlyOncePerLink() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='rep'>ab ab ab</a>\n"
                + "    <a id='single'>ab</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("rep", "single"), ids(scope.findElements(By.partialLinkText("ab"))));
        }
    }

    @Test
    public void queryIsMatchedAgainstNormalizedText() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='ws'>Link  2    \n \t\t x\n</a>\n"
                + "    <a id='nl'>  pre\n  post  </a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("ws"), ids(scope.findElements(By.partialLinkText("Link 2 x"))));
            assertEquals(Arrays.asList("ws"), ids(scope.findElements(By.partialLinkText("k 2 x"))));
            assertEquals(Arrays.asList("ws"), ids(scope.findElements(By.partialLinkText("2 x"))));
            assertEquals(Arrays.asList("nl"), ids(scope.findElements(By.partialLinkText("pre post"))));

            // the raw whitespace of the source is not part of the text
            assertTrue(scope.findElements(By.partialLinkText("Link  2")).isEmpty());
            assertTrue(scope.findElements(By.partialLinkText("pre\n")).isEmpty());
        }
    }

    @Test
    public void blankInsideQueryMatchesInnerText() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='a1'>Link 1</a>\n"
                + "    <a id='a2'>pre Link 2 post</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            // the text is trimmed, so a leading blank only matches inside of the text
            assertEquals(Arrays.asList("a2"), ids(scope.findElements(By.partialLinkText(" Link"))));
            assertEquals(Arrays.asList("a1", "a2"), ids(scope.findElements(By.partialLinkText("Link "))));
            assertEquals(Arrays.asList("a2"), ids(scope.findElements(By.partialLinkText("2 post"))));

            assertTrue(scope.findElements(By.partialLinkText(" Link 1")).isEmpty());
            assertTrue(scope.findElements(By.partialLinkText("post ")).isEmpty());
        }
    }

    @Test
    public void nonBreakingSpaceInLinkText() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='nbsp'>pre&nbsp;Link&nbsp;two</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("nbsp"), ids(scope.findElements(By.partialLinkText("Link two"))));
        }
    }

    @Test
    public void driverScopeNormalizationAndBlanks() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <a id='a1' href='about:blank' >Link 1</a>\n"
                + "  <a id='a2' href='about:blank' >pre Link 2 post </a>\n"
                + "  <a id='a3' href='about:blank' >pre Link  3    \n \t\t post\n</a>\n"
                + "  <a id='a4'>no href</a>\n"));

        assertEquals("a1", driver.findElement(By.partialLinkText("Link 1")).getAttribute("id"));
        assertEquals("a2", driver.findElement(By.partialLinkText("Link 2")).getAttribute("id"));
        assertEquals("a3", driver.findElement(By.partialLinkText("Link 3")).getAttribute("id"));
        assertEquals("a4", driver.findElement(By.partialLinkText("no href")).getAttribute("id"));

        assertTrue(driver.findElements(By.partialLinkText(" Link 1")).isEmpty());
        assertTrue(driver.findElements(By.partialLinkText("Link 1 ")).isEmpty());
    }

    @Test
    public void nestedMarkupInLinkText() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='n'>Link <b>bold</b> <i>it</i></a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            // a match may span element boundaries
            assertEquals(Arrays.asList("n"), ids(scope.findElements(By.partialLinkText("k bold i"))));
            assertEquals(Arrays.asList("n"), ids(scope.findElements(By.partialLinkText("bold"))));
            assertTrue(scope.findElements(By.partialLinkText("<b>")).isEmpty());
        }
    }

    @Test
    public void hiddenTextIsIgnored() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='partial'>Visible<span style='display: none'> secret</span></a>\n"
                + "    <a id='gone' style='display: none'>Gone</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("partial"), ids(scope.findElements(By.partialLinkText("Visi"))));
            assertTrue(scope.findElements(By.partialLinkText("secret")).isEmpty());
            assertTrue(scope.findElements(By.partialLinkText("Gone")).isEmpty());
        }
    }

    @Test
    public void specialCharactersInPartialLinkText() throws Exception {
        final String[] texts = {
            "it's", "say \"hi\"", "a'b\"c", "a & b", "a < b", "a > b", "100%", "a\\b", "[x]", "a]b"
            // "gr\u00f6\u00dfe", "\u65e5\u672c\u8a9e"
        };

        for (final String text : texts) {
            final WebDriver driver = loadPage2(html(
                    "  <div id='ctx'>\n"
                    + "    <a id='decoy'>Decoy</a>\n"
                    + "    <a id='hit'>pre " + escapeHtml(text) + " post</a>\n"
                    + "  </div>\n"));

            final WebElement ctx = driver.findElement(By.id("ctx"));
            for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
                assertEquals(text, Arrays.asList("hit"), ids(scope.findElements(By.partialLinkText(text))));
                assertEquals(text, "hit", scope.findElement(By.partialLinkText(text)).getAttribute("id"));
            }
        }
    }

    // ------------------------------------------------------------------
    // scope
    // ------------------------------------------------------------------

    @Test
    public void relativeSearchIgnoresOutsideLinks() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <a id='before'>Link</a>\n"
                + "  <div id='ctx'>\n"
                + "    <ul><li><a id='deep'>my Link</a></li></ul>\n"
                + "    <a id='child'>Link</a>\n"
                + "  </div>\n"
                + "  <a id='after'>Link</a>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(Arrays.asList("deep", "child"), ids(ctx.findElements(By.partialLinkText("Lin"))));
        assertEquals("deep", ctx.findElement(By.partialLinkText("Lin")).getAttribute("id"));

        assertEquals(Arrays.asList("before", "deep", "child", "after"),
                ids(driver.findElements(By.partialLinkText("Lin"))));
    }

    @Test
    public void relativeSearchOnAnchorDoesNotFindItself() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <a id='self' href='about:blank'>Self</a>\n"));

        final WebElement self = driver.findElement(By.id("self"));
        assertTrue(self.findElements(By.partialLinkText("Sel")).isEmpty());
    }

    // ------------------------------------------------------------------
    // dynamic DOM
    // ------------------------------------------------------------------

    @Test
    public void linkTextChangedByScript() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='a'>Old text</a>\n"
                + "    <a id='b'>Keep this</a>\n"
                + "    <a id='r'>Remove me</a>\n"
                + "  </div>\n"
                + "  <script>\n"
                + "    var ctx = document.getElementById('ctx');\n"
                + "    document.getElementById('a').textContent = 'New text';\n"
                + "    document.getElementById('b').innerHTML = 'Keep <b>that</b>';\n"
                + "    var r = document.getElementById('r');\n"
                + "    r.parentNode.removeChild(r);\n"
                + "    var n = document.createElement('a');\n"
                + "    n.id = 'n';\n"
                + "    n.appendChild(document.createTextNode('Added text'));\n"
                + "    ctx.appendChild(n);\n"
                + "  </script>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertTrue("Old", scope.findElements(By.partialLinkText("Old")).isEmpty());
            assertTrue("Remove", scope.findElements(By.partialLinkText("Remove")).isEmpty());
            assertTrue("this", scope.findElements(By.partialLinkText("this")).isEmpty());

            assertEquals("text", Arrays.asList("a", "n"), ids(scope.findElements(By.partialLinkText("text"))));
            assertEquals("New", Arrays.asList("a"), ids(scope.findElements(By.partialLinkText("New"))));
            assertEquals("that", Arrays.asList("b"), ids(scope.findElements(By.partialLinkText("that"))));
            assertEquals("Added", Arrays.asList("n"), ids(scope.findElements(By.partialLinkText("Added"))));
        }
    }
}
