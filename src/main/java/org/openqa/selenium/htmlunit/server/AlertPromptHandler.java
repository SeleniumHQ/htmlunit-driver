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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.security.Credentials;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.PromptHandler;
import com.gargoylesoftware.htmlunit.WebClient;

class AlertPromptHandler implements AlertHandler, PromptHandler {

  private Map<Page, AlertLock> locks = new HashMap<>();

  AlertPromptHandler(WebClient webClient) {
    webClient.setAlertHandler(this);
    webClient.setPromptHandler(this);
  }

  @Override
  public void handleAlert(Page page, String message) {
    AlertLock lock = new AlertLock();
    lock.message = message;
    locks.put(page, lock);

    synchronized (lock) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  @Override
  public String handlePrompt(Page page, String message) {
    PromptLock lock = new PromptLock();
    lock.message = message;
    locks.put(page, lock);

    synchronized (lock) {
      try {
        lock.wait();
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
    System.out.println("Returning prompt " + lock.value);
    return lock.value;
  }

  private static class AlertLock {
    String message;
  }

  private static class PromptLock extends AlertLock {
    String value;
  }

  boolean isLocked(Page page) {
    return locks.containsKey(page);
  }

  String getMessage(Page page) {
    AlertLock lock = locks.get(page);
    if (lock == null) {
      throw new NoAlertPresentException();
    }
    return lock.message;
  }

  void accept(Page page, String value) {
    AlertLock lock = locks.get(page);
    if (lock == null) {
      throw new NoAlertPresentException();
    }

    if (value != null) {
      if (!(lock instanceof PromptLock)) {
        throw new ElementNotVisibleException("alert is not visible");
      }
      ((PromptLock) lock).value = value;
    }

    synchronized (lock) {
      lock.notify();
    }
  }

  void close(Page page) {
    locks.remove(page);
  }
}
