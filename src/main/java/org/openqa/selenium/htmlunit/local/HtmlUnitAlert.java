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
import com.gargoylesoftware.htmlunit.WebWindow;

public class HtmlUnitAlert implements Alert {

  HtmlUnitLocalDriver driver;
  private AlertHolder holder_;
  private boolean quitting_;
//  private String message;
//  private String promptKeys;
//  private String promptValue;
//  private boolean accepted;
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
    try {
      holder_ = new AlertHolder(page.getEnclosingWindow(), message);
//    this.message = message;
//    accepted = false;
    l.lock();
    boolean proceed = driver.alert();
    if (proceed) {
      c.awaitUninterruptibly();
    }
    l.unlock();
//    System.out.println("alertHandler 5");
//
//    synchronized (lock_) {
//      try {
//        lock_.wait();
//      } catch (InterruptedException e) {
//        throw new IllegalStateException(e);
//      }
//    }
//    close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  private String promptHandler(Page page, String message, String defaultMessage) {
    if (quitting_) {
      return null;
    }
    holder_ = new PromptHolder(page.getEnclosingWindow(), message, defaultMessage);
    PromptHolder localHolder = (PromptHolder) holder_;
//    this.message = message;
//    this.promptValue = message;
//    accepted = false;
    l.lock();
    driver.alert();
    c.awaitUninterruptibly();
    l.unlock();
    return localHolder.value;
//    lock_ = new PromptLock(page.getEnclosingWindow(), message, defaultMessage);
//
//    synchronized (lock_) {
//      try {
//        lock_.wait();
//      } catch (InterruptedException e) {
//        throw new IllegalStateException(e);
//      }
//    }
//    String value = ((PromptLock) lock_).value;
//    close();
//    return value;
  }

  private boolean onbeforeunloadHandler(Page page, String returnValue) {
    if (quitting_) {
      return true;
    }
    holder_ = new AlertHolder(page.getEnclosingWindow(), returnValue);
    AlertHolder localHolder = holder_;
//    this.message = returnValue;
//    accepted = false;
    l.lock();
    driver.alert();
    c.awaitUninterruptibly();
    l.unlock();
    return localHolder.isAccepted();
//    lock_ = new AlertLock(page.getEnclosingWindow(), returnValue);
//
//    if (!autoAccept_) {
//      synchronized (lock_) {
//        try {
//          lock_.wait();
//        } catch (InterruptedException e) {
//          throw new IllegalStateException(e);
//        }
//      }
//    }
//    boolean accepted = lock_.isAccepted();
//    close();
//    return accepted;
  }

  public void setAutoAccept(boolean autoAccept) {
    this.quitting_ = autoAccept;
  }

  @Override
  public void dismiss() {
    l.lock();
//    message = null;
//    promptValue = null;
//    accepted = false;
    c.signal();
    holder_ = null;
    l.unlock();
//    if (lock_ == null) {
//      throw new NoAlertPresentException();
//    }
//    synchronized (lock_) {
//      lock_.notify();
//    }
  }

  @Override
  public void accept() {
    l.lock();
    holder_.accept();
//    message = null;
//    accepted = true;
    c.signal();
    holder_ = null;
    l.unlock();
//    if (lock_ == null) {
//      throw new NoAlertPresentException();
//    }
//    lock_.accept();
//    synchronized (lock_) {
//      lock_.notify();
//    }
  }

  @Override
  public String getText() {
//    boolean locked = l.tryLock();
//    System.out.println("locked = " + locked);
//    System.out.println("message " + message);
//    if (locked) {
//      l.unlock();
//      throw new NoAlertPresentException();
//    }
//    System.out.println("getText " + message);
    if (holder_ == null) {
      throw new NoAlertPresentException();
    }
    if (holder_.webWindow != driver.getCurrentWindow()) {
      throw new AssertionError();
    }
    return holder_.message;
//    if (message == null)
//    if (lock_ == null) {
//      throw new NoAlertPresentException();
//    }
//    if (lock_.webWindow != driver.getCurrentWindow()) {
//      throw new AssertionError();
//    }
//    return lock_.message;
  }

  @Override
  public void sendKeys(String keysToSend) {
    holder_.sendKeys(keysToSend);
//    if (lock_ == null) {
//      throw new NoAlertPresentException();
//    }
//    lock_.sendKeys(keysToSend);
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
    WebWindow webWindow;
    String message;
    boolean accepted;

    AlertHolder(WebWindow webWindow, String message) {
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

  private static class PromptHolder extends AlertHolder {

    String defaultMessage;
    String value;

    public PromptHolder(WebWindow webWindow, String message, String defaultMessage) {
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
