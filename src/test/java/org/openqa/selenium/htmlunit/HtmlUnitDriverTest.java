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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.SessionNotFoundException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test the proxy setting.
 */
public class HtmlUnitDriverTest extends TestBase {

  private HtmlUnitDriver driver;

  @Before
  public void initDriver() {
    driver = new HtmlUnitDriver();
  }

  @After
  public void stopDriver() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  @Test
  public void canOpenAPage() {
    driver.get(testServer.page("/"));
    assertThat(driver.getCurrentUrl(), equalTo(testServer.page("/")));
  }

  @Test
  public void throwsOnMalformedUrl() {
    thrown.expect(WebDriverException.class);
    driver.get("www.test.com");
  }

  @Test
  public void doesNotThrowsOnUnknownHost() {
    driver.get("http://www.thisurldoesnotexist.comx/");
    assertThat(driver.getCurrentUrl(), equalTo("http://www.thisurldoesnotexist.comx/"));
  }

  @Test
  public void throwsOnAnyOperationAfterQuit() {
    driver.quit();
    thrown.expect(SessionNotFoundException.class);
    driver.get(testServer.page("/"));
  }

  @Test
  public void canGetPageTitle() {
    driver.get(testServer.page("/"));
    assertThat(driver.getTitle(), equalTo("Hello, world!"));
  }

}
