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

import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.WebWindow;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.remote.CapabilityType;

/**
 * Implementation of {@link Alert}.
 *
 * @author Ahmed Ashour
 * @author A aftakhov
 * @author Ronald Brill
 */
public class HtmlUnitAlert implements Alert {

    private final HtmlUnitDriver driver_;
    private AlertHolder holder_;
    private boolean quitting_;
    private final Lock lock_ = new ReentrantLock();
    private final Condition condition_ = lock_.newCondition();
    private WebWindow webWindow_;
    private UnexpectedAlertBehaviour unexpectedAlertBehaviour_ = UnexpectedAlertBehaviour.DISMISS_AND_NOTIFY;

    HtmlUnitAlert(final HtmlUnitDriver driver) {
        driver_ = driver;
        final WebClient webClient = driver.getWebClient();
        webClient.setAlertHandler(this::alertHandler);
        webClient.setPromptHandler(this::promptHandler);
        webClient.setConfirmHandler(this::confirmHandler);
        webClient.setOnbeforeunloadHandler(this::onbeforeunloadHandler);
    }

    private void alertHandler(final Page page, final String message) {
        if (quitting_) {
            return;
        }
        webWindow_ = page.getEnclosingWindow();
        holder_ = new AlertHolder(message);
        awaitCondition();
    }

    private boolean confirmHandler(final Page page, final String message) {
        if (quitting_) {
            return false;
        }
        webWindow_ = page.getEnclosingWindow();
        holder_ = new AlertHolder(message);
        final AlertHolder localHolder = holder_;
        awaitCondition();
        return localHolder.isAccepted();
    }

    private void awaitCondition() {
        lock_.lock();
        try {
            if (driver_.isProcessAlert()) {
                try {
                    condition_.await(5, TimeUnit.SECONDS);
                }
                catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        finally {
            lock_.unlock();
        }
    }

    private String promptHandler(final Page page, final String message, final String defaultMessage) {
        if (quitting_) {
            return null;
        }
        webWindow_ = page.getEnclosingWindow();
        holder_ = new PromptHolder(message, defaultMessage);
        final PromptHolder localHolder = (PromptHolder) holder_;
        awaitCondition();
        return localHolder.value_;
    }

    private boolean onbeforeunloadHandler(final Page page, final String returnValue) {
        if (quitting_) {
            return true;
        }
        webWindow_ = page.getEnclosingWindow();
        holder_ = new AlertHolder(returnValue);
        final AlertHolder localHolder = holder_;
        awaitCondition();
        return localHolder.isAccepted();
    }

    WebWindow getWebWindow() {
        return webWindow_;
    }

    public void setAutoAccept(final boolean autoAccept) {
        this.quitting_ = autoAccept;
    }

    public void handleBrowserCapabilities(final Capabilities capabilities) {
        final UnexpectedAlertBehaviour behaviour = (UnexpectedAlertBehaviour) capabilities
                .getCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR);
        if (behaviour != null) {
            this.unexpectedAlertBehaviour_ = behaviour;
        }
    }

    @Override
    public void dismiss() {
        lock_.lock();
        try {
            condition_.signal();
        }
        finally {
            lock_.unlock();
            holder_ = null;
        }
    }

    @Override
    public void accept() {
        lock_.lock();
        try {
            holder_.accept();
            condition_.signal();
        }
        finally {
            lock_.unlock();
            holder_ = null;
        }
    }

    @Override
    public String getText() {
        if (holder_ == null) {
            throw new NoAlertPresentException();
        }
        String msg = holder_.message_;
        msg = msg.replace("\r\n", "\n");
        if (!driver_.getBrowserVersion().isIE()) {
            msg = msg.replace('\r', '\n');
        }
        return msg;
    }

    @Override
    public void sendKeys(final String keysToSend) {
        holder_.sendKeys(keysToSend);
    }

    /**
     * Closes the current window.
     */
    void close() {
        lock_.lock();
        try {
            condition_.signal();
            setAutoAccept(true);
        }
        finally {
            lock_.unlock();
            holder_ = null;
        }
    }

    boolean isLocked() {
        return holder_ != null;
    }

    public void ensureUnlocked() {
        if (isLocked()) {
            final String text = getText();

            switch (unexpectedAlertBehaviour_) {
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

                default:
                    break;
            }
            throw new UnhandledAlertException("Unexpected alert found", text);
        }
    }

    private static class AlertHolder {
        private final String message_;
        private boolean accepted_;

        AlertHolder(final String message) {
            this.message_ = message;
        }

        void sendKeys(final String keysToSend) {
            if (keysToSend != null) {
                throw new ElementNotInteractableException("alert is not interactable");
            }
            throw new IllegalArgumentException();
        }

        void accept() {
            accepted_ = true;
        }

        boolean isAccepted() {
            return accepted_;
        }
    }

    private static class PromptHolder extends AlertHolder {

        private final String defaultMessage_;
        private String value_;

        PromptHolder(final String message, final String defaultMessage) {
            super(message);
            this.defaultMessage_ = defaultMessage;
        }

        @Override
        void sendKeys(String keysToSend) {
            if (keysToSend == null) {
                keysToSend = defaultMessage_;
            }
            this.value_ = keysToSend;
        }

        @Override
        void accept() {
            if (value_ == null) {
                value_ = defaultMessage_;
            }
        }
    }

}
