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

package org.openqa.selenium.htmlunit.server;

import org.openqa.selenium.Alert;
import org.openqa.selenium.security.Credentials;

import com.gargoylesoftware.htmlunit.Page;

class HtmlUnitAlert implements Alert {

  private HtmlUnitLocalDriver driver;
  private AlertPromptHandler handler;

  HtmlUnitAlert(HtmlUnitLocalDriver driver) {
    this.driver = driver;
    handler = new AlertPromptHandler(driver.getWebClient());
  }

  @Override
  public void dismiss() {
    handler.accept(getPage(), null);
  }

  @Override
  public void accept() {
    handler.accept(getPage(), null);
  }

  @Override
  public String getText() {
    return handler.getMessage(getPage());
  }

  @Override
  public void sendKeys(String keysToSend) {
    handler.accept(getPage(), keysToSend);
  }

  @Override
  public void authenticateUsing(Credentials credentials) {
  }

  @Override
  public void setCredentials(Credentials credentials) {
  }

  private Page getPage() {
    return driver.getCurrentWindow().getEnclosedPage();
  }

  /**
   * Closes the current window.
   */
  void close() {
    handler.close(getPage());
  }

  boolean isLocked() {
    return handler.isLocked(getPage());
  }
}
