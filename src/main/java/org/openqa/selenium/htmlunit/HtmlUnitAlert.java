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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openqa.selenium.Alert;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoAlertPresentException;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;

/**
 * Implementation of {@link Alert}.
 */
public class HtmlUnitAlert implements Alert {

  HtmlUnitDriver driver;
  private AlertHolder holder_;
  private boolean quitting_;
  private Lock lock = new ReentrantLock();
  private Condition condition = lock.newCondition();
  private WebWindow webWindow_;

  HtmlUnitAlert(HtmlUnitDriver driver) {
    this.driver = driver;
    WebClient webClient = driver.getWebClient();
    webClient.setAlertHandler(this::alertHandler);
    webClient.setPromptHandler(this::promptHandler);
    webClient.setConfirmHandler(this::confirmHandler);
    webClient.setOnbeforeunloadHandler(this::onbeforeunloadHandler);
  }

  private void alertHandler(Page page, String message) {
    if (quitting_) {
      return;
    }
    webWindow_ = page.getEnclosingWindow();
    holder_ = new AlertHolder(message);
    awaitCondition();
  }

  private boolean confirmHandler(Page page, String message) {
    if (quitting_) {
      return false;
    }
    webWindow_ = page.getEnclosingWindow();
    holder_ = new AlertHolder(message);
    AlertHolder localHolder = holder_;
    awaitCondition();
    return localHolder.isAccepted();
  }

  private void awaitCondition() {
    lock.lock();
    try {
      if (driver.isProcessAlert()) {
        try {
          condition.await(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
    finally {
      lock.unlock();
    }
  }

  private String promptHandler(Page page, String message, String defaultMessage) {
    if (quitting_) {
      return null;
    }
    webWindow_ = page.getEnclosingWindow();
    holder_ = new PromptHolder(message, defaultMessage);
    PromptHolder localHolder = (PromptHolder) holder_;
    awaitCondition();
    return localHolder.value;
  }

  private boolean onbeforeunloadHandler(Page page, String returnValue) {
    if (quitting_) {
      return true;
    }
    webWindow_ = page.getEnclosingWindow();
    holder_ = new AlertHolder(returnValue);
    AlertHolder localHolder = holder_;
    awaitCondition();
    return localHolder.isAccepted();
  }

  WebWindow getWebWindow() {
    return webWindow_;
  }

  public void setAutoAccept(boolean autoAccept) {
    this.quitting_ = autoAccept;
  }

  @Override
  public void dismiss() {
    lock.lock();
    condition.signal();
    holder_ = null;
    lock.unlock();
  }

  @Override
  public void accept() {
    lock.lock();
    holder_.accept();
    condition.signal();
    holder_ = null;
    lock.unlock();
  }

  @Override
  public String getText() {
    if (holder_ == null) {
      throw new NoAlertPresentException();
    }
    return holder_.message;
  }

  @Override
  public void sendKeys(String keysToSend) {
    holder_.sendKeys(keysToSend);
  }

  /**
   * Closes the current window.
   */
  void close() {
    lock.lock();
    condition.signal();
    setAutoAccept(true);
    lock.unlock();
    holder_ = null;
  }

  boolean isLocked() {
    return holder_ != null;
  }

  private static class AlertHolder {
    String message;
    boolean accepted;

    AlertHolder(String message) {
      this.message = message;
    }

    void sendKeys(String keysToSend) {
      if (keysToSend != null) {
        throw new ElementNotInteractableException("alert is not interactable");
      }
      throw new IllegalArgumentException();
    }

    void accept() {
      accepted = true;
    }

    boolean isAccepted() {
      return accepted;
    }
  }

  private static class PromptHolder extends AlertHolder {

    String defaultMessage;
    String value;

    public PromptHolder(String message, String defaultMessage) {
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

    @Override
    void accept() {
      if (value == null) {
        value = defaultMessage;
      }
    }
  }

}
