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
    public void elementByLinkTextNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <a id='a'>Other</a>\n"));

        driver.findElement(By.linkText("TestA"));
    }

    @Test
    public void elementsByLinkTextNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <a id='a'>Other</a>\n"));

        assertTrue(driver.findElements(By.linkText("TestA")).isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void relativeElementByLinkTextNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='a'>Other</a>\n"
                + "  </div>\n"
                + "  <a id='outside'>TestA</a>\n"));

        driver.findElement(By.id("ctx")).findElement(By.linkText("TestA"));
    }

    @Test
    public void relativeElementsByLinkTextNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='a'>Other</a>\n"
                + "  </div>\n"
                + "  <a id='outside'>TestA</a>\n"));

        assertTrue(driver.findElement(By.id("ctx")).findElements(By.linkText("TestA")).isEmpty());
    }

    // ------------------------------------------------------------------
    // matching semantics
    // ------------------------------------------------------------------

    @Test
    public void elementsByLinkTextInDocumentOrder() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='first'>TestA</a>\n"
                + "    <a id='other'>TestB</a>\n"
                + "    <ul><li><a id='nested'>TestA</a></li></ul>\n"
                + "    <a id='last'>TestA</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("first", "nested", "last"), ids(scope.findElements(By.linkText("TestA"))));
            assertEquals("first", scope.findElement(By.linkText("TestA")).getAttribute("id"));
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
            assertEquals(Arrays.asList("hit"), ids(scope.findElements(By.linkText("TestA"))));
        }
    }

    @Test
    public void exactTextMatchOnly() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='hit'>TestA</a>\n"
                + "    <a id='suffix'>TestAB</a>\n"
                + "    <a id='prefix'>XTestA</a>\n"
                + "    <a id='case'>testa</a>\n"
                + "    <a id='inner'>Test A</a>\n"
                + "    <a id='sub'>my TestA link</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("hit"), ids(scope.findElements(By.linkText("TestA"))));
        }
    }

    @Test
    public void whitespaceVariantsInLinkText() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='newlines'>  Link\n   one  \n</a>\n"
                + "    <a id='mixed'>Link \t\r\n  three</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("newlines"), ids(scope.findElements(By.linkText("Link one"))));
            assertEquals(Arrays.asList("mixed"), ids(scope.findElements(By.linkText("Link three"))));
            assertTrue(scope.findElements(By.linkText("Link  one")).isEmpty());
        }
    }

    @Test
    public void nonBreakingSpaceInLinkText() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='nbsp'>Link&nbsp;two</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("nbsp"), ids(scope.findElements(By.linkText("Link two"))));
        }
    }

    @Test
    public void driverScopeNormalizationAndBlanks() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <a id='a1' href='about:blank' >Link 1</a>\n"
                + "  <a id='a2' href='about:blank' >Link  2    \n \t\t x\n</a>\n"
                + "  <a id='a3'>Link 3</a>\n"));

        assertEquals("a1", driver.findElement(By.linkText("Link 1")).getAttribute("id"));
        assertEquals("a2", driver.findElement(By.linkText("Link 2 x")).getAttribute("id"));
        assertEquals("a3", driver.findElement(By.linkText("Link 3")).getAttribute("id"));

        assertTrue(driver.findElements(By.linkText(" Link 1")).isEmpty());
        assertTrue(driver.findElements(By.linkText("Link 1 ")).isEmpty());
    }

    @Test
    public void nestedMarkupInLinkText() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='n'>Link <b>bold</b> <i>it</i></a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("n"), ids(scope.findElements(By.linkText("Link bold it"))));
            assertTrue(scope.findElements(By.linkText("Link")).isEmpty());
        }
    }

    @Test
    public void hiddenTextIsIgnored() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='partial'>Visible<span style='display: none'> hidden</span></a>\n"
                + "    <a id='gone' style='display: none'>Gone</a>\n"
                + "  </div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertEquals(Arrays.asList("partial"), ids(scope.findElements(By.linkText("Visible"))));
            assertTrue(scope.findElements(By.linkText("Visible hidden")).isEmpty());
            assertTrue(scope.findElements(By.linkText("Gone")).isEmpty());
        }
    }

    @Test
    public void specialCharactersInLinkText() throws Exception {
        final String[] texts = {
            "it's", "say \"hi\"", "a'b\"c", "a & b", "a < b", "a > b", "100%", "a\\b", "[x]", "a]b"
            // "gr\u00f6\u00dfe", "\u65e5\u672c\u8a9e"
        };

        for (final String text : texts) {
            final WebDriver driver = loadPage2(html(
                    "  <div id='ctx'>\n"
                    + "    <a id='decoy'>Decoy</a>\n"
                    + "    <a id='hit'>" + escapeHtml(text) + "</a>\n"
                    + "  </div>\n"));

            final WebElement ctx = driver.findElement(By.id("ctx"));
            for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
                assertEquals(text, Arrays.asList("hit"), ids(scope.findElements(By.linkText(text))));
                assertEquals(text, "hit", scope.findElement(By.linkText(text)).getAttribute("id"));
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
                + "    <ul><li><a id='deep'>Link</a></li></ul>\n"
                + "    <a id='child'>Link</a>\n"
                + "  </div>\n"
                + "  <a id='after'>Link</a>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(Arrays.asList("deep", "child"), ids(ctx.findElements(By.linkText("Link"))));
        assertEquals("deep", ctx.findElement(By.linkText("Link")).getAttribute("id"));

        assertEquals(Arrays.asList("before", "deep", "child", "after"), ids(driver.findElements(By.linkText("Link"))));
    }

    @Test
    public void relativeSearchOnAnchorDoesNotFindItself() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <a id='self' href='about:blank'>Self</a>\n"));

        final WebElement self = driver.findElement(By.id("self"));
        assertTrue(self.findElements(By.linkText("Self")).isEmpty());
    }

    // ------------------------------------------------------------------
    // dynamic DOM
    // ------------------------------------------------------------------

    @Test
    public void linkTextChangedByScript() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <a id='a'>Old</a>\n"
                + "    <a id='b'>Keep</a>\n"
                + "    <a id='r'>Remove</a>\n"
                + "  </div>\n"
                + "  <script>\n"
                + "    var ctx = document.getElementById('ctx');\n"
                + "    document.getElementById('a').textContent = 'New';\n"
                + "    document.getElementById('b').innerHTML = 'Keep <b>it</b>';\n"
                + "    var r = document.getElementById('r');\n"
                + "    r.parentNode.removeChild(r);\n"
                + "    var n = document.createElement('a');\n"
                + "    n.id = 'n';\n"
                + "    n.appendChild(document.createTextNode('Added'));\n"
                + "    ctx.appendChild(n);\n"
                + "  </script>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        for (final SearchContext scope : new SearchContext[] {driver, ctx}) {
            assertTrue("Old", scope.findElements(By.linkText("Old")).isEmpty());
            assertTrue("Remove", scope.findElements(By.linkText("Remove")).isEmpty());
            assertTrue("Keep", scope.findElements(By.linkText("Keep")).isEmpty());

            assertEquals("New", Arrays.asList("a"), ids(scope.findElements(By.linkText("New"))));
            assertEquals("Keep it", Arrays.asList("b"), ids(scope.findElements(By.linkText("Keep it"))));
            assertEquals("Added", Arrays.asList("n"), ids(scope.findElements(By.linkText("Added"))));
        }
    }
}
