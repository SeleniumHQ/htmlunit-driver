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
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.security.Credentials;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;

class HtmlUnitAlert implements Alert {

  HtmlUnitLocalDriver driver;
  private AlertLock lock_;
  private boolean autoAccept_;

  HtmlUnitAlert(HtmlUnitLocalDriver driver) {
    this.driver = driver;
    WebClient webClient = driver.getWebClient();
    webClient.setAlertHandler(this::alertHandler);
    webClient.setPromptHandler(this::promptHandler);
    webClient.setOnbeforeunloadHandler(this::onbeforeunloadHandler);
  }

  private void alertHandler(Page page, String message) {
    lock_ = new AlertLock(page.getEnclosingWindow(), message);

    synchronized (lock_) {
      try {
        lock_.wait();
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
    close();
  }

  private String promptHandler(Page page, String message, String defaultMessage) {
    lock_ = new PromptLock(page.getEnclosingWindow(), message, defaultMessage);

    synchronized (lock_) {
      try {
        lock_.wait();
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
    String value = ((PromptLock) lock_).value;
    close();
    return value;
  }

  private boolean onbeforeunloadHandler(Page page, String returnValue) {
    lock_ = new AlertLock(page.getEnclosingWindow(), returnValue);

    if (!autoAccept_) {
      synchronized (lock_) {
        try {
          lock_.wait();
        } catch (InterruptedException e) {
          throw new IllegalStateException(e);
        }
      }
    }
    boolean accepted = lock_.isAccepted();
    close();
    return accepted;
  }

  void setAutoAccept(boolean autoAccept) {
    this.autoAccept_ = autoAccept;
  }

  @Override
  public void dismiss() {
    if (lock_ == null) {
      throw new NoAlertPresentException();
    }
    synchronized (lock_) {
      lock_.notify();
    }
  }

  @Override
  public void accept() {
    if (lock_ == null) {
      throw new NoAlertPresentException();
    }
    lock_.accept();
    synchronized (lock_) {
      lock_.notify();
    }
  }

  @Override
  public String getText() {
    if (lock_ == null) {
      throw new NoAlertPresentException();
    }
    if (lock_.webWindow != driver.getCurrentWindow()) {
      throw new AssertionError();
    }
    return lock_.message;
  }

  @Override
  public void sendKeys(String keysToSend) {
    if (lock_ == null) {
      throw new NoAlertPresentException();
    }
    lock_.sendKeys(keysToSend);
  }

  @Override
  public void authenticateUsing(Credentials credentials) {
  }

  @Override
  public void setCredentials(Credentials credentials) {
  }

  /**
   * Closes the current window.
   */
  void close() {
    lock_ = null;
  }

  boolean isLocked() {
    return lock_ != null;
  }

  private static class AlertLock {
    WebWindow webWindow;
    String message;
    boolean accepted;

    AlertLock(WebWindow webWindow, String message) {
      this.webWindow = webWindow;
      this.message = message;
    }

    void sendKeys(String keysToSend) {
      if (keysToSend != null) {
          throw new ElementNotVisibleException("alert is not visible");
      }
    }

    void accept() {
      accepted = true;
    }

    boolean isAccepted() {
      return accepted;
    }
  }

  private static class PromptLock extends AlertLock {

    String defaultMessage;
    String value;

    public PromptLock(WebWindow webWindow, String message, String defaultMessage) {
      super(webWindow, message);
      this.defaultMessage = defaultMessage;
    }
  
    @Override
    void sendKeys(String keysToSend) {
      if (keysToSend == null) {
        keysToSend = defaultMessage;
      }
      this.value = keysToSend;
    }

    void accept() {
      if (value == null) {
        value = defaultMessage;
      }
    }
  }

}
