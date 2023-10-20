// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit.html;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.interactions.Actions;

/**
 * Test for window handling.
 *
 * @see <a href=
 *      "https://www.selenium.dev/documentation/webdriver/browser/windows/">Working
 *      with windows and tabs</a>
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class WindowsTest extends WebDriverTestCase {

    /**
     * @throws Exception if something goes wrong
     */
    @Test
    public void getWindowHandleWithoutContent() throws Exception {
        final WebDriver driver = getWebDriver();

        final String windowHandle = driver.getWindowHandle();
        assertTrue("invalid windowHandle + '" + windowHandle + "'", windowHandle.length() > 4);

        final Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(1, windowHandles.size());

        assertTrue(windowHandles.contains(windowHandle));
    }

    /**
     * @throws Exception if something goes wrong
     */
    @Test
    public void getWindowHandleWithContent() throws Exception {
        final String html = "<html><body>DataUrl Test</body></html>";

        final WebDriver driver = loadPage2(html);

        final String windowHandle = driver.getWindowHandle();
        assertTrue("invalid windowHandle + '" + windowHandle + "'", windowHandle.length() > 4);

        final Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(1, windowHandles.size());

        assertTrue(windowHandles.contains(windowHandle));
    }

    /**
     * @throws Exception if something goes wrong
     */
    @Test
    public void newWindowFromLinkWithTarget() throws Exception {
        final String html =
                "<html>\n"
                + "<head><title>First</title></head>\n"
                + "<body>\n"
                + "<a id='a' target='_blank' href='" + URL_SECOND + "'>Foo</a>\n"
                + "</body></html>\n";

        final String secondHtml = "<html>\n"
                + "<head><title>Second</title></head>\n"
                + "<body></body></html>\n";
        getMockWebConnection().setResponse(URL_SECOND, secondHtml);

        final WebDriver driver = loadPage2(html);

        assertEquals("First", driver.getTitle());

        final String windowHandle = driver.getWindowHandle();
        assertTrue("invalid windowHandle + '" + windowHandle + "'", windowHandle.length() > 4);

        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(1, windowHandles.size());

        driver.findElement(By.id("a")).click();
        Thread.sleep(100);

        assertEquals("First", driver.getTitle());

        final String windowHandleAfterClick = driver.getWindowHandle();
        assertEquals(windowHandle, windowHandleAfterClick);

        windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());

        assertTrue(windowHandles.contains(windowHandle));
    }

    /**
     * @throws Exception if something goes wrong
     */
    @Test
    // shift click seems not working with real IE
    public void newWindowFromShiftClick() throws Exception {
        final String html =
                "<html>\n"
                + "<head><title>foo</title></head>\n"
                + "<body>\n"
                + "<a id='a' href='" + URL_SECOND + "'>Foo</a>\n"
                + "</body></html>\n";

        final String secondHtml = "<html>\n"
                + "<head><title>Second</title></head>\n"
                + "<body></body></html>\n";
        getMockWebConnection().setResponse(URL_SECOND, secondHtml);

        final WebDriver driver = loadPage2(html);

        assertEquals("foo", driver.getTitle());

        final String windowHandle = driver.getWindowHandle();
        assertTrue("invalid windowHandle + '" + windowHandle + "'", windowHandle.length() > 4);

        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(1, windowHandles.size());

        final WebElement link = driver.findElement(By.id("a"));
        new Actions(driver)
            .moveToElement(link)
            .keyDown(Keys.SHIFT)
            .click()
            .keyUp(Keys.SHIFT)
            .perform();
        Thread.sleep(100);

        assertEquals("foo", driver.getTitle());

        final String windowHandleAfterClick = driver.getWindowHandle();
        assertEquals(windowHandle, windowHandleAfterClick);

        windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());

        assertTrue(windowHandles.contains(windowHandle));
    }

    /**
     * @throws Exception if something goes wrong
     */
    @Test
    public void switchWindow() throws Exception {
        final String html =
                "<html>\n"
                + "<head><title>First</title></head>\n"
                + "<body>\n"
                + "<a id='a' target='_blank' href='" + URL_SECOND + "'>Foo</a>\n"
                + "</body></html>\n";

        final String secondHtml = "<html>\n"
                + "<head><title>Second</title></head>\n"
                + "<body></body></html>\n";
        getMockWebConnection().setResponse(URL_SECOND, secondHtml);

        final WebDriver driver = loadPage2(html);

        assertEquals("First", driver.getTitle());

        final String windowHandle = driver.getWindowHandle();
        assertTrue("invalid windowHandle + '" + windowHandle + "'", windowHandle.length() > 4);

        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(1, windowHandles.size());

        driver.findElement(By.id("a")).click();
        Thread.sleep(100);

        assertEquals("First", driver.getTitle());

        windowHandles = new HashSet<>(driver.getWindowHandles());
        assertEquals(2, windowHandles.size());

        windowHandles.remove(windowHandle);
        driver.switchTo().window(windowHandles.iterator().next());

        assertEquals("Second", driver.getTitle());
    }

    /**
     * @throws Exception if something goes wrong
     */
    @Test
    public void switchToNewWindow() throws Exception {
        final String html =
                "<html>\n"
                + "<head><title>First</title></head>\n"
                + "<body>\n"
                + "</body></html>\n";

        final WebDriver driver = loadPage2(html);

        assertEquals("First", driver.getTitle());

        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(1, windowHandles.size());

        driver.switchTo().newWindow(WindowType.WINDOW);

        windowHandles = new HashSet<>(driver.getWindowHandles());
        assertEquals(2, windowHandles.size());

        assertEquals("", driver.getTitle());
        assertEquals("about:blank", driver.getCurrentUrl());
    }

    /**
     * @throws Exception if something goes wrong
     */
    @Test
    public void switchToNewTab() throws Exception {
        final String html =
                "<html>\n"
                + "<head><title>First</title></head>\n"
                + "<body>\n"
                + "</body></html>\n";

        final WebDriver driver = loadPage2(html);

        assertEquals("First", driver.getTitle());

        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(1, windowHandles.size());

        driver.switchTo().newWindow(WindowType.TAB);

        windowHandles = new HashSet<>(driver.getWindowHandles());
        assertEquals(2, windowHandles.size());

        assertEquals("", driver.getTitle());
        assertEquals("about:blank", driver.getCurrentUrl());
    }

    /**
     * @throws Exception if something goes wrong
     */
    @Test
    public void closeCurrentWindow() throws Exception {
        final String htmlFirst =
                "<html>\n"
                + "<head><title>First</title></head>\n"
                + "<body>\n"
                + "</body></html>\n";

        final WebDriver driver = loadPage2(htmlFirst);

        assertEquals("First", driver.getTitle());

        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(1, windowHandles.size());

        driver.switchTo().newWindow(WindowType.TAB);

        windowHandles = new HashSet<>(driver.getWindowHandles());
        assertEquals(2, windowHandles.size());

        assertEquals("", driver.getTitle());
        assertEquals("about:blank", driver.getCurrentUrl());

        final String htmlSecond =
                "<html>\n"
                + "<head><title>Second</title></head>\n"
                + "<body>\n"
                + "</body></html>\n";
        loadPage2(htmlSecond);

        windowHandles = new HashSet<>(driver.getWindowHandles());
        assertEquals(2, windowHandles.size());

        assertEquals("Second", driver.getTitle());

        driver.close();

        windowHandles = new HashSet<>(driver.getWindowHandles());
        assertEquals(1, windowHandles.size());

        try {
            driver.getTitle();
            Assert.fail("NoSuchWindowException expected");
        }
        catch (final NoSuchWindowException e) {
            // expected
        }
    }

    /**
     * @throws Exception if something goes wrong
     */
    @Test
    public void closeLastWindow() throws Exception {
        final String htmlFirst =
                "<html>\n"
                + "<head><title>First</title></head>\n"
                + "<body>\n"
                + "</body></html>\n";

        final WebDriver driver = loadPage2(htmlFirst);

        assertEquals("First", driver.getTitle());

        final Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(1, windowHandles.size());

        driver.close();

        try {
            driver.getWindowHandle();
            Assert.fail("NoSuchSessionException expected");
        }
        catch (final NoSuchSessionException e) {
            // expected
        }
    }
}
