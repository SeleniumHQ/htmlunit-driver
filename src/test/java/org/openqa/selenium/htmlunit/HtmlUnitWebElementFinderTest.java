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
import org.openqa.selenium.WebElement;

/**
 * Separate test class for the HtmlUnitWebElement.getAttribute(String) method.
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitWebElementFinderTest extends WebDriverTestCase {

    @Test
    public void linkText() throws Exception {
        String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='a1' href='about:blank' >Link 1</div>\n"
                        + "  <a id='a2' href='about:blank' >Link  2    \n \t\t x\n</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement body = driver.findElement(By.tagName("body"));

        WebElement elem = body.findElement(By.linkText("Link 1"));
        assertEquals("Link 1", elem.getText());

        elem = body.findElement(By.linkText("Link 2 x"));
        assertEquals("Link 2 x", elem.getText());
    }

    @Test
    public void partialLinkText() throws Exception {
        String html = "<html>\n"
                        + "<head>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "  <a id='a1' href='about:blank' >Link 1</div>\n"
                        + "  <a id='a2' href='about:blank' >pre Link 2 post </div>\n"
                        + "  <a id='a3' href='about:blank' >pre Link  3    \n \t\t post\n</div>\n"
                        + "</body>\n"
                        + "</html>\n";

        final WebDriver driver = loadPage2(html);

        WebElement body = driver.findElement(By.tagName("body"));

        WebElement elem = body.findElement(By.partialLinkText("Link 1"));
        assertEquals("Link 1", elem.getText());

        elem = body.findElement(By.partialLinkText("Link 2"));
        assertEquals("pre Link 2 post", elem.getText());

        elem = body.findElement(By.partialLinkText("Link 3"));
        assertEquals("pre Link 3 post", elem.getText());
    }
}
