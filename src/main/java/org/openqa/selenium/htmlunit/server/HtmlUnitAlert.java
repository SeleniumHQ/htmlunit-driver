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

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.PromptHandler;
import com.gargoylesoftware.htmlunit.WebClient;

class HtmlUnitAlert implements Alert {

  private AlertLock lock_;

  HtmlUnitAlert(HtmlUnitLocalDriver driver) {
    WebClient webClient = driver.getWebClient();
    webClient.setAlertHandler(this::alertHandler);
    webClient.setPromptHandler(this::promptHandler);
    webClient.setOnbeforeunloadHandler(this::onbeforeunloadHandler);
  }

  private void alertHandler(Page page, String message) {
    lock_ = new AlertLock(message);

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
    lock_ = new PromptLock(message, defaultMessage);

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
    lock_ = new AlertLock(returnValue);

    synchronized (lock_) {
      try {
        lock_.wait();
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
    close();
    return false;
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
    synchronized (lock_) {
      lock_.notify();
    }
  }

  @Override
  public String getText() {
    if (lock_ == null) {
      throw new NoAlertPresentException();
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
    String message;

    AlertLock(final String message) {
      this.message = message;
    }

    void sendKeys(String keysToSend) {
      if (keysToSend != null) {
          throw new ElementNotVisibleException("alert is not visible");
      }
    }
  }

  private static class PromptLock extends AlertLock {

    String defaultMessage;
    String value;

    public PromptLock(String message, String defaultMessage) {
      super(message);
      this.defaultMessage = defaultMessage;
    }
  
    @Override
    void sendKeys(String keysToSend) {
      if (keysToSend == null) {
        keysToSend = defaultMessage;
      }
      this.value = keysToSend;
    }
  }

}
