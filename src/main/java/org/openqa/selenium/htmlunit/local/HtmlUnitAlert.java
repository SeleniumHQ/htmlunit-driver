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

package org.openqa.selenium.htmlunit.local;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openqa.selenium.Alert;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.security.Credentials;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

public class HtmlUnitAlert implements Alert {

  HtmlUnitLocalDriver driver;
  private AlertHolder holder_;
  private boolean quitting_;
  private Lock l = new ReentrantLock();
  private Condition c = l.newCondition();

  HtmlUnitAlert(HtmlUnitLocalDriver driver) {
    this.driver = driver;
    WebClient webClient = driver.getWebClient();
    webClient.setAlertHandler(this::alertHandler);
    webClient.setPromptHandler(this::promptHandler);
    webClient.setOnbeforeunloadHandler(this::onbeforeunloadHandler);
  }

  private void alertHandler(Page page, String message) {
    if (quitting_) {
      return;
    }
    l.lock();
    holder_ = new AlertHolder(message);
    boolean proceed = driver.alert();
    if (proceed) {
      c.awaitUninterruptibly();
    }
    l.unlock();
  }

  private String promptHandler(Page page, String message, String defaultMessage) {
    if (quitting_) {
      return null;
    }
    holder_ = new PromptHolder(message, defaultMessage);
    PromptHolder localHolder = (PromptHolder) holder_;
    l.lock();
    driver.alert();
    c.awaitUninterruptibly();
    l.unlock();
    return localHolder.value;
  }

  private boolean onbeforeunloadHandler(Page page, String returnValue) {
    if (quitting_) {
      return true;
    }
    holder_ = new AlertHolder(returnValue);
    AlertHolder localHolder = holder_;
    l.lock();
    driver.alert();
    c.awaitUninterruptibly();
    l.unlock();
    return localHolder.isAccepted();
  }

  public void setAutoAccept(boolean autoAccept) {
    this.quitting_ = autoAccept;
  }

  @Override
  public void dismiss() {
    l.lock();
    c.signal();
    holder_ = null;
    l.unlock();
  }

  @Override
  public void accept() {
    l.lock();
    holder_.accept();
    c.signal();
    holder_ = null;
    l.unlock();
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
    l.lock();
    c.signal();
    setAutoAccept(true);
    l.unlock();
    holder_ = null;
  }

  public boolean isLocked() {
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

    void accept() {
      if (value == null) {
        value = defaultMessage;
      }
    }
  }

}
