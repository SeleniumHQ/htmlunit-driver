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

package org.openqa.selenium.htmlunit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.junit.BrowserRunner;

@RunWith(BrowserRunner.class)
public class HtmlUnitAlertTest extends WebDriverTestCase {

    @Test
    public void confirm() throws Exception {
        final String message = "Are you sure?";

        final String html = "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "    confirm('" + message + "');\n"
            + "</script>\n"
            + "</head>\n"
            + "<body>\n"
            + "</body>\n"
            + "</html>\n";

        final WebDriver driver = loadPage2(html);
        assertEquals(message, driver.switchTo().alert().getText());
        driver.switchTo().alert().accept();
    }

    @Test
    public void confirmWithRedirect() throws Exception {
        final String message = "Are you sure?";

        final String html = "<html>\n"
                + "<a id='confirm' href='http://htmlunit.sourceforge.net/' "
                        + "onclick='return confirm(\"" + message + "\");'>Confirm</a>\n"
                + "<div id='message'>Default</div>"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("confirm")).click();

        assertEquals(message, driver.switchTo().alert().getText());
        driver.switchTo().alert().accept();

        Thread.sleep(2000);

        assertTrue("Title was '" + driver.getTitle() + "'",
                driver.getTitle().contains("Welcome to HtmlUnit"));
    }

    @Test
    public void confirmWithoutRedirect() throws Exception {
        final String message = "Are you sure?";

        final String html = "<html>\n"
                + "<head>\n"
                + "<title>ConfirmWithoutRedirect</title>\n"
                + "<script>\n"
                + "function runConfirm() {\n"
                + "if (!confirm('" + message + "')) {"
                + "document.getElementById('message').innerHTML = 'False';\n"
                + "return false;\n"
                + "}\n"
                + "}\n"
                + "</script>\n"
                + "</head>\n"
                + "<body>\n"
                + "<a id='confirm' href='http://htmlunit.sourceforge.net/' onclick='return runConfirm();'>Confirm</a>\n"
                + "<div id='message'>Default</div>"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("confirm")).click();

        assertEquals(message, driver.switchTo().alert().getText());
        driver.switchTo().alert().dismiss();

        Thread.sleep(1000);

        assertEquals("False", driver.findElement(By.id("message")).getText());
        assertEquals("ConfirmWithoutRedirect", driver.getTitle());
    }

    @Test
    public void alertWithLineBreak() throws Exception {
        final String html = "<html>\n"
                + "<head>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <button id='clickMe' onClick='alert(\"1\\n2\\r\\n3\\t4\\r5\\n\\r6\")'>do it</button>\n"
                + "</body>\n"
                + "</html>\n";

        final WebDriver driver = loadPage2(html);
        driver.findElement(By.id("clickMe")).click();

        // selenium seems to normalize this
        if (getBrowserVersion().isIE()) {
            assertEquals("1\n2\n3\t4\r5\n\r6", driver.switchTo().alert().getText());
        }
        else {
            assertEquals("1\n2\n3\t4\n5\n\n6", driver.switchTo().alert().getText());
        }
        driver.switchTo().alert().dismiss();
    }
}
