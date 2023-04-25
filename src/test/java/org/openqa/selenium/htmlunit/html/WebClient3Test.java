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

import org.htmlunit.MockWebConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;

@RunWith(BrowserRunner.class)
public class WebClient3Test extends WebDriverTestCase {

    /**
     * @throws Exception if something goes wrong
     */
    @Test
    @Alerts("executed")
    public void javascriptContentDetectorWithoutContentMixedCase() throws Exception {
        final MockWebConnection conn = getMockWebConnection();
        conn.setDefaultResponse("<scRIPt>alert('executed')</scRIPt>", 200, "OK", null);
        loadPageWithAlerts2(URL_FIRST);
    }

    /**
     * @throws Exception if test fails
     */
    @Test
    public void getPageAboutProtocol() throws Exception {
        final WebDriver driver = getWebDriver();
        driver.get("about:blank");

        final WebElement body = driver.findElement(By.tagName("body"));
        assertEquals("", body.getText());
    }

    /**
     * @throws Exception if test fails
     */
    @Test
    public void getPageDataProtocol() throws Exception {
        final String html = "<html><body>DataUrl Test</body></html>";

        final WebDriver driver = getWebDriver();
        driver.get("data:text/html;charset=utf-8," + html);

        final WebElement body = driver.findElement(By.tagName("body"));
        assertEquals("DataUrl Test", body.getText());
    }

    /**
     * @throws Exception if test fails
     */
    @Test
    public void getPageJavascriptProtocol() throws Exception {
        final WebDriver driver = getWebDriver();
        driver.get("javascript:void(document.title='Hello')");

        assertEquals("Hello", driver.getTitle());
    }
}
