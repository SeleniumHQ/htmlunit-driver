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

@RunWith(BrowserRunner.class)
public class HtmlUnitWebElementTest extends WebDriverTestCase {

  /**
   * @throws Exception if the test fails
   */
  @Test
  public void outerHTML() throws Exception {
      final String html = "<html><head></head>\n"
          + "</html>";
      final WebDriver webDriver = loadPage2(html);
      final WebElement head = webDriver.findElement(By.xpath("//head"));
      assertEquals("<head></head>", head.getAttribute("outerHTML"));
  }

  /**
   * @throws Exception if the test fails
   */
  @Test
  public void outerhtml() throws Exception {
      final String html = "<html><head></head>\n"
          + "</html>";
      final WebDriver webDriver = loadPage2(html);
      final WebElement head = webDriver.findElement(By.xpath("//head"));
      assertNull(head.getAttribute("outerhtml"));
  }

  /**
   * @throws Exception if the test fails
   */
  @Test
  public void innterHTML() throws Exception {
      final String html = "<html><head><title>abc</title></head>\n"
          + "</html>";
      final WebDriver webDriver = loadPage2(html);
      final WebElement head = webDriver.findElement(By.xpath("//head"));
      assertEquals("<title>abc</title>", head.getAttribute("innerHTML"));
  }

  /**
   * @throws Exception if the test fails
   */
  @Test
  public void innterhtml() throws Exception {
      final String html = "<html><head><title>abc</title></head>\n"
          + "</html>";
      final WebDriver webDriver = loadPage2(html);
      final WebElement head = webDriver.findElement(By.xpath("//head"));
      assertNull(head.getAttribute("innerhtml"));
  }

  /**
   * @throws Exception if the test fails
   */
  @Test
  public void title() throws Exception {
      final String html = "<html><head><title>abc</title></head>\n"
          + "<body></body>\n"
          + "</html>";
      final WebDriver webDriver = loadPage2(html);
      final WebElement body = webDriver.findElement(By.tagName("body"));
      assertEquals("", body.getAttribute("title"));
  }

  // @Test
  public void memoryLeak() throws Exception {
    final int elements = 1000;
    String html = "<html><head><title>abc</title></head>\n"
            + "<body>\n";
    for (int i = 0; i < elements; i++) {
        html += "<div id='id" + i + "'>abcd</div>\n";
    }
    html += "</body>\n"
            + "</html>";

    for (int i = 0; i < 10000; i++) {
      final WebDriver webDriver = loadPage2(html);
      for (int j = 0; j < elements; j++) {
          webDriver.findElement(By.id("id" + j));
      }
    }
  }
}
