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

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;

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

        String windowHandle = driver.getWindowHandle();
        assertTrue("invalid windowHandle + '" + windowHandle + "'", windowHandle.length() > 4);

        Set<String> windowHandles = driver.getWindowHandles();
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

        String windowHandle = driver.getWindowHandle();
        assertTrue("invalid windowHandle + '" + windowHandle + "'", windowHandle.length() > 4);

        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(1, windowHandles.size());

        assertTrue(windowHandles.contains(windowHandle));
    }
}
