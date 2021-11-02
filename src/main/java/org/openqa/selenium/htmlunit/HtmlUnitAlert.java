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

import static org.openqa.selenium.remote.CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.UnhandledAlertException;

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
    private UnexpectedAlertBehaviour unexpectedAlertBehaviour = UnexpectedAlertBehaviour.DISMISS_AND_NOTIFY;

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
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
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

    public void handleBrowserCapabilities(Capabilities capabilities) {
        UnexpectedAlertBehaviour behaviour = (UnexpectedAlertBehaviour) capabilities.getCapability(UNEXPECTED_ALERT_BEHAVIOUR);
        if (behaviour != null) {
          this.unexpectedAlertBehaviour = behaviour;
        }
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
        String msg = holder_.message;
        msg = msg.replace("\r\n", "\n");
        if (!driver.getBrowserVersion().isIE()) {
            msg = msg.replace('\r', '\n');
        }
        return msg;
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

    public void ensureUnlocked() {
        if (isLocked()) {
            String text = getText();

            switch (unexpectedAlertBehaviour) {
            case ACCEPT:
                accept();
                return;

            case ACCEPT_AND_NOTIFY:
                accept();
                break;

            case DISMISS:
                dismiss();
                return;

            case DISMISS_AND_NOTIFY:
                dismiss();
                break;

            case IGNORE:
                break;
            }
            throw new UnhandledAlertException("Alert found", text);
        }
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
