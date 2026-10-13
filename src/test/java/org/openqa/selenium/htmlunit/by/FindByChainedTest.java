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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.support.pagefactory.ByChained;

/**
 * Tests for ByChained.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class FindByChainedTest extends WebDriverTestCase {

    @Test
    public void byByChainedOne() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId'>TestDiv</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(new ByChained(By.id("testDivId")));
        assertEquals("TestDiv", element.getText());
    }

    @Test
    public void byByChained() throws Exception {
        final String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <div id='testDivId'>\n"
                        + "    <div id='testDivId2'>TestDiv2</div>\n"
                        + "  </div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);
        final WebElement element = driver.findElement(new ByChained(By.id("testDivId"), By.id("testDivId2")));
        assertEquals("TestDiv2", element.getText());
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

    private static By chain(final By... bys) {
        return new ByChained(bys);
    }

    // ------------------------------------------------------------------
    // no match
    // ------------------------------------------------------------------

    @Test
    public void notFoundInFirstStep() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a'><div id='b'></div></div>\n"));

        assertTrue(driver.findElements(chain(By.id("unknown"), By.id("b"))).isEmpty());
        try {
            driver.findElement(chain(By.id("unknown"), By.id("b")));
            fail("expected NoSuchElementException");
        }
        catch (final NoSuchElementException e) {
            assertTrue(e.getMessage().contains("Cannot locate an element using"));
        }
    }

    @Test
    public void notFoundInLaterStep() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a'><div id='b'></div></div>\n"
                + "  <div id='c'></div>\n"));

        // c exists, but not inside of a
        assertTrue(driver.findElements(chain(By.id("a"), By.id("c"))).isEmpty());
        assertTrue(driver.findElements(chain(By.id("a"), By.id("b"), By.id("c"))).isEmpty());
        try {
            driver.findElement(chain(By.id("a"), By.id("c")));
            fail("expected NoSuchElementException");
        }
        catch (final NoSuchElementException e) {
            assertTrue(e.getMessage().contains("Cannot locate an element using"));
        }
    }

    @Test
    public void emptyChain() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a'></div>\n"));

        assertTrue(driver.findElements(chain()).isEmpty());
        try {
            driver.findElement(chain());
            fail("expected NoSuchElementException");
        }
        catch (final NoSuchElementException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // locator types
    // ------------------------------------------------------------------

    @Test
    public void mixedLocatorTypes() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='l1'>\n"
                + "    <div class='l2'>\n"
                + "      <ul>\n"
                + "        <li><a id='one' name='n1'>One</a></li>\n"
                + "        <li><a id='two' name='n2' class='target'>Two</a></li>\n"
                + "      </ul>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "  <div class='l2'><ul><li><a id='outside' name='n2' class='target'>Two</a></li></ul></div>\n"));

        final By[] lastSteps = {
            By.linkText("Two"), By.partialLinkText("Tw"), By.className("target"), By.name("n2"),
            By.cssSelector("a.target"), By.xpath(".//a[@id='two']"), By.id("two")
        };
        for (final By last : lastSteps) {
            assertEquals(last.toString(), Arrays.asList("two"),
                    ids(driver.findElements(chain(By.id("l1"), By.cssSelector(".l2"), By.tagName("ul"),
                            By.xpath(".//li[2]"), last))));
        }

        assertEquals("two", driver.findElement(chain(By.id("l1"), By.className("l2"), By.tagName("li"),
                By.linkText("Two"))).getAttribute("id"));
    }

    // ------------------------------------------------------------------
    // results
    // ------------------------------------------------------------------

    @Test
    public void multipleResultsAreCollectedStepByStep() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div class='row'>\n"
                + "    <span id='r1c1' class='cell'></span>\n"
                + "    <span id='r1c2' class='cell'></span>\n"
                + "  </div>\n"
                + "  <div class='row'>\n"
                + "    <span id='r2c1' class='cell'></span>\n"
                + "    <span id='r2c2' class='cell'></span>\n"
                + "  </div>\n"
                + "  <span id='stray' class='cell'></span>\n"));

        assertEquals(Arrays.asList("r1c1", "r1c2", "r2c1", "r2c2"),
                ids(driver.findElements(chain(By.className("row"), By.className("cell")))));
        assertEquals("r1c1", driver.findElement(chain(By.className("row"), By.className("cell"))).getAttribute("id"));
    }

    @Test
    public void branchesWithoutMatchAreIgnored() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div class='box' id='b1'><i class='item' id='i1'></i></div>\n"
                + "  <div class='box' id='b2'></div>\n"
                + "  <div class='box' id='b3'><i class='item' id='i3'></i><i class='item' id='i3b'></i></div>\n"
                + "  <i class='item' id='stray'></i>\n"));

        assertEquals(Arrays.asList("i1", "i3", "i3b"),
                ids(driver.findElements(chain(By.className("box"), By.className("item")))));
    }

    @Test
    public void absoluteXPathInLaterStepIgnoresContext() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a'><span id='s1'></span></div>\n"
                + "  <span id='s2'></span>\n"));

        assertEquals(Arrays.asList("s1"), ids(driver.findElements(chain(By.id("a"), By.xpath(".//span")))));
        // an absolute path is evaluated against the whole document
        assertEquals(Arrays.asList("s1", "s2"), ids(driver.findElements(chain(By.id("a"), By.xpath("//span")))));
    }

    @Test
    public void nestedChains() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='a'><div id='b'><div id='c'>text</div></div></div>\n"));

        assertEquals("c", driver.findElement(chain(chain(By.id("a"), By.id("b")), By.id("c"))).getAttribute("id"));
        assertEquals("c", driver.findElement(chain(By.id("a"), chain(By.id("b"), By.id("c")))).getAttribute("id"));
        assertEquals("c", driver.findElement(chain(chain(By.id("a")), chain(By.id("b")), chain(By.id("c"))))
                .getAttribute("id"));

        assertTrue(driver.findElements(chain(chain(By.id("a"), By.id("unknown")), By.id("c"))).isEmpty());
    }

    @Test
    public void hiddenElementsAreFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div class='a' style='display: none'>\n"
                + "      <input type='hidden' id='h' class='b'>\n"
                + "    </div>\n"
                + "  </div>\n"));

        assertEquals(Arrays.asList("h"),
                ids(driver.findElements(chain(By.id("ctx"), By.className("a"), By.className("b")))));
    }

    // ------------------------------------------------------------------
    // scope
    // ------------------------------------------------------------------

    @Test
    public void chainFromElementContext() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'>\n"
                + "    <div class='a'><span class='b' id='inner'></span></div>\n"
                + "  </div>\n"
                + "  <div class='a'><span class='b' id='outside'></span></div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertEquals(Arrays.asList("inner"), ids(ctx.findElements(chain(By.className("a"), By.className("b")))));
        assertEquals("inner", ctx.findElement(chain(By.className("a"), By.className("b"))).getAttribute("id"));

        assertEquals(Arrays.asList("inner", "outside"),
                ids(driver.findElements(chain(By.className("a"), By.className("b")))));
    }

    @Test
    public void chainFromElementContextNotFound() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'></div>\n"
                + "  <div class='a'><span class='b' id='outside'></span></div>\n"));

        final WebElement ctx = driver.findElement(By.id("ctx"));
        assertTrue(ctx.findElements(chain(By.className("a"), By.className("b"))).isEmpty());
        try {
            ctx.findElement(chain(By.className("a"), By.className("b")));
            fail("expected NoSuchElementException");
        }
        catch (final NoSuchElementException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // invalid steps
    // ------------------------------------------------------------------

    @Test
    public void invalidStepsAreReported() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'><div class='a'></div></div>\n"));

        final By[] invalidSteps = {
            By.className("a b"), By.cssSelector("div["), By.xpath("//div["), By.tagName("")
        };
        for (final By invalid : invalidSteps) {
            // the invalid step is the first one
            assertInvalid(driver, chain(invalid, By.id("ctx")));
            // the invalid step is reached
            assertInvalid(driver, chain(By.id("ctx"), invalid));
            assertInvalid(driver, chain(By.id("ctx"), By.className("a"), invalid));
        }
    }

    private static void assertInvalid(final WebDriver driver, final By by) {
        try {
            driver.findElement(by);
            fail("findElement: expected InvalidSelectorException for " + by);
        }
        catch (final InvalidSelectorException e) {
            // expected
        }

        try {
            driver.findElements(by);
            fail("findElements: expected InvalidSelectorException for " + by);
        }
        catch (final InvalidSelectorException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // dynamic DOM
    // ------------------------------------------------------------------

    @Test
    public void elementsAddedByScript() throws Exception {
        final WebDriver driver = loadPage2(html(
                "  <div id='ctx'><div class='a'></div></div>\n"
                + "  <script>\n"
                + "    var d = document.createElement('span');\n"
                + "    d.className = 'b';\n"
                + "    d.id = 'added';\n"
                + "    document.querySelector('#ctx .a').appendChild(d);\n"
                + "  </script>\n"));

        assertEquals(Arrays.asList("added"),
                ids(driver.findElements(chain(By.id("ctx"), By.className("a"), By.className("b")))));
    }
}
