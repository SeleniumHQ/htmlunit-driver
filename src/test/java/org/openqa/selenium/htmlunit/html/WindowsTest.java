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

package org.openqa.selenium.htmlunit.html;

import static org.junit.Assert.assertThrows;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.htmlunit.MockWebConnection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchFrameException;
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

    /**
     * Test, the correct frame is used for a target, even if some frames
     * have the same name.
     *
     * @throws Exception if the test fails
     */
    @Test
    public void targetResolution() throws Exception {
        final String framesContent =
                "<html><head><title>Top Page</title></head>\n"
                + "<body><div id='content'>Body of top frame</div>\n"
                + "  <iframe src='left.html' id='id-left' name='left'></iframe>\n"
                + "  <iframe src='right.html' id='id-right' name='right'></iframe>\n"
                + "</body>\n"
                + "</html>";

        final String rightFrame =
                "<html><head><title>Right Frame</title></head>\n"
                + "<body><div id='content'>Body of right frame</div></body>\n"
                + "</html>";

        final String leftFrame =
                "<html><head><title>Left Frame</title></head>\n"
                + "<body>\n"
                + "  <div id='content'>Body of left frame</div>\n"
                + "  <a id='link' name='link' href='new_inner.html' target='right'>Click link</a>\n"
                + "  <iframe id='id-inner' name='right' width='100' height='100' src='inner.html'></iframe>\n"
                + "</body>\n"
                + "</html>";

        final String innerFrame =
                "<html><head><title>Inner Frame</title></head>\n"
                + "<body><div id='content'>Body of inner frame</div></body>\n"
                + "</html>";

        final String newInnerFrame =
                "<html><head><title>New inner Frame</title></head>\n"
                + "<body><div id='content'>Body of new inner frame</div></body>\n"
                + "</html>";

        final String baseUrl = URL_FIRST.toString();

        final URL leftFrameUrl = new URL(baseUrl + "left.html");
        final URL rightFrameUrl = new URL(baseUrl + "right.html");
        final URL innerFrameURL = new URL(baseUrl + "inner.html");
        final URL newInnerFrameURL = new URL(baseUrl + "new_inner.html");

        final MockWebConnection webConnection = getMockWebConnection();
        webConnection.setResponse(leftFrameUrl, leftFrame);
        webConnection.setResponse(rightFrameUrl, rightFrame);
        webConnection.setResponse(innerFrameURL, innerFrame);
        webConnection.setResponse(newInnerFrameURL, newInnerFrame);

        final WebDriver driver = loadPage2(framesContent);

        // top frame
        assertTitle(driver, "Top Page");
        assertEquals("Body of top frame", driver.findElement(By.id("content")).getText());

        // left frame
        driver.switchTo().frame("id-left");
        assertEquals("Body of left frame", driver.findElement(By.id("content")).getText());
        // inner frame
        driver.switchTo().frame("id-inner");
        assertEquals("Body of inner frame", driver.findElement(By.id("content")).getText());
        // right frame
        driver.switchTo().defaultContent();
        driver.switchTo().frame("id-right");
        assertEquals("Body of right frame", driver.findElement(By.id("content")).getText());

        // clicking on a link which contains a target 'right'. But this target frame is defined two times.
        driver.switchTo().defaultContent();
        driver.switchTo().frame("id-left");
        driver.findElement(By.id("link")).click();

        // left frame
        driver.switchTo().defaultContent();
        driver.switchTo().frame("id-left");
        assertEquals("Body of left frame", driver.findElement(By.id("content")).getText());
        // inner frame
        driver.switchTo().frame("id-inner");
        assertEquals("Body of new inner frame", driver.findElement(By.id("content")).getText());
        // right frame
        driver.switchTo().defaultContent();
        driver.switchTo().frame("id-right");
        assertEquals("Body of right frame", driver.findElement(By.id("content")).getText());
    }

    /**
     * Proves that switching to a nested frame requires stepping through parent frames sequentially.
     *
     * @throws Exception if the test fails
     */
    @Test
    public void switchToNestedFrameStepByStep() throws Exception {
        final String html = "<html><body>\n"
                + "  <iframe id='level1-frame' src='level1.html'></iframe>\n"
                + "</body></html>";

        final String level1Html = "<html><body>\n"
                + "  <iframe id='level2-frame' src='level2.html'></iframe>\n"
                + "</body></html>";

        final String level2Html = "<html><body>\n"
                + "  <div id='content'>Deep Nested Content</div>\n"
                + "</body></html>";

        final URL baseUrl = URL_FIRST;
        final MockWebConnection webConnection = getMockWebConnection();
        webConnection.setResponse(new URL(baseUrl, "level1.html"), level1Html);
        webConnection.setResponse(new URL(baseUrl, "level2.html"), level2Html);

        final WebDriver driver = loadPage2(html);

        // Step 1: Switch to direct child of top-level
        driver.switchTo().frame("level1-frame");

        // Step 2: Switch to direct child of level1-frame
        driver.switchTo().frame("level2-frame");

        assertEquals("Deep Nested Content", driver.findElement(By.id("content")).getText());
    }

    /**
     * Proves that attempting to switch to a non-direct child frame from top-level
     * throws NoSuchFrameException in W3C WebDriver.
     *
     * @throws Exception if the test fails
     */
    @Test
    public void switchToDeepNestedFrameFailsFromTop() throws Exception {
        final String html = "<html><body>\n"
                + "  <iframe id='level1-frame' src='level1.html'></iframe>\n"
                + "</body></html>";

        final String level1Html = "<html><body>\n"
                + "  <iframe id='level2-frame' src='level2.html'></iframe>\n"
                + "</body></html>";

        final String level2Html = "<html><body>\n"
                + "  <div id='content'>Deep Nested Content</div>\n"
                + "</body></html>";

        final URL baseUrl = URL_FIRST;
        final MockWebConnection webConnection = getMockWebConnection();
        webConnection.setResponse(new URL(baseUrl, "level1.html"), level1Html);
        webConnection.setResponse(new URL(baseUrl, "level2.html"), level2Html);

        final WebDriver driver = loadPage2(html);

        // Expect NoSuchFrameException because level2-frame is not a direct child of top-level
        assertThrows(NoSuchFrameException.class, () -> driver.switchTo().frame("level2-frame"));
    }

    /**
     * Proves that switching to a nested frame by name requires stepping through parent frames sequentially.
     *
     * @throws Exception if the test fails
     */
    @Test
    public void switchToNestedFrameByNameStepByStep() throws Exception {
        final String html = "<html><body>\n"
                + "  <iframe name='level1-frame' src='level1.html'></iframe>\n"
                + "</body></html>";

        final String level1Html = "<html><body>\n"
                + "  <iframe name='level2-frame' src='level2.html'></iframe>\n"
                + "</body></html>";

        final String level2Html = "<html><body>\n"
                + "  <div id='content'>Deep Nested Content</div>\n"
                + "</body></html>";

        final URL baseUrl = URL_FIRST;
        final MockWebConnection webConnection = getMockWebConnection();
        webConnection.setResponse(new URL(baseUrl, "level1.html"), level1Html);
        webConnection.setResponse(new URL(baseUrl, "level2.html"), level2Html);

        final WebDriver driver = loadPage2(html);

        // Step 1: Switch to direct child by frame name
        driver.switchTo().frame("level1-frame");

        // Step 2: Switch to nested child by frame name from level1 context
        driver.switchTo().frame("level2-frame");

        assertEquals("Deep Nested Content", driver.findElement(By.id("content")).getText());
    }

    /**
     * Proves that attempting to switch to a non-direct child frame by name from top-level
     * throws NoSuchFrameException in real browsers.
     *
     * @throws Exception if the test fails
     */
    @Test
    public void switchToDeepNestedFrameByNameFailsFromTop() throws Exception {
        final String html = "<html><body>\n"
                + "  <iframe name='level1-frame' src='level1.html'></iframe>\n"
                + "</body></html>";

        final String level1Html = "<html><body>\n"
                + "  <iframe name='level2-frame' src='level2.html'></iframe>\n"
                + "</body></html>";

        final String level2Html = "<html><body>\n"
                + "  <div id='content'>Deep Nested Content</div>\n"
                + "</body></html>";

        final URL baseUrl = URL_FIRST;
        final MockWebConnection webConnection = getMockWebConnection();
        webConnection.setResponse(new URL(baseUrl, "level1.html"), level1Html);
        webConnection.setResponse(new URL(baseUrl, "level2.html"), level2Html);

        final WebDriver driver = loadPage2(html);

        // Expect NoSuchFrameException because level2-frame is not a direct child of top-level
        assertThrows(NoSuchFrameException.class, () -> driver.switchTo().frame("level2-frame"));
    }
}
