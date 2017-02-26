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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.BrowserRunner;
import org.openqa.selenium.htmlunit.WebDriverTestCase;

@RunWith(BrowserRunner.class)
public class HtmlPage3Test extends WebDriverTestCase {

  /**
   * @exception Exception If the test fails
   */
  @Test
  public void constructor() throws Exception {
      final String html = "<html>\n"
          + "<head><title>foo</title></head>\n"
          + "<body>\n"
          + "<p>hello world</p>\n"
          + "<form id='form1' action='/formSubmit' method='post'>\n"
          + "  <input type='text' NAME='textInput1' value='textInput1'/>\n"
          + "  <input type='text' name='textInput2' value='textInput2'/>\n"
          + "  <input type='hidden' name='hidden1' value='hidden1'/>\n"
          + "  <input type='submit' name='submitInput1' value='push me'/>\n"
          + "</form>\n"
          + "</body></html>";

      final WebDriver driver = loadPage2(html);
      assertEquals("foo", driver.getTitle());
  }

}
