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

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.WebDriverTestCase;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;
import org.openqa.selenium.htmlunit.junit.BrowserRunner.Alerts;

@RunWith(BrowserRunner.class)
public class HtmlInputElementTest extends WebDriverTestCase {

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({"abcx", "", "abcx", "abcx"})
    public void clipboard() throws Exception {
        final String html =
                "<html><head>\n"
                + "  <script>\n"
                + LOG_TITLE_FUNCTION
                + "    function test() {\n"
                + "      log(document.getElementById('i1').value);\n"
                + "      log(document.getElementById('i2').value);\n"
                + "    }\n"
                + "  </script>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <form>\n"
                + "    <input type='text' id='i1' value='abcx'>\n"
                + "    <input type='text' id='i2'>\n"
                + "  </form>\n"
                + "  <button id='check' onclick='test()'>Test</button>\n"
                + "</body></html>";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("check")).click();
        verifyTitle2(driver, Arrays.copyOfRange(getExpectedAlerts(), 0, 2));

        driver.findElement(By.id("i1")).sendKeys(Keys.CONTROL + "a");
        driver.findElement(By.id("i1")).sendKeys(Keys.CONTROL + "c");

        driver.findElement(By.id("i2")).sendKeys(Keys.CONTROL + "v");
        driver.findElement(By.id("check")).click();
        verifyTitle2(driver, getExpectedAlerts());
    }
}
