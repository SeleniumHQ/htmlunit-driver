// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   https://www.apache.org/licenses/LICENSE-2.0
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
 * Provides an implementation of {@link Alert} for {@link HtmlUnitDriver}.
 * Handles JavaScript alert, confirm, prompt, and beforeunload dialogs.
 *
 * @author Ahmed Ashour
 * @author A aftakhov
 * @author Ronald Brill
 */
public class HtmlUnitAlert implements Alert {

    /** The {@link HtmlUnitDriver} that owns this alert handler. */
    private final HtmlUnitDriver driver_;

    /** Holds the current alert state and message. */
    private AlertHolder holder_;

    /** Indicates whether the driver is quitting. */
    private boolean quitting_;

    /** Lock used to coordinate access to alert state. */
    private final Lock lock_ = new ReentrantLock();

    /** Condition used to signal when an alert is available. */
    private final Condition condition_ = lock_.newCondition();

    /** The {@link WebWindow} currently associated with alert events. */
    private WebWindow webWindow_;

    /** The configured behavior for unexpected alerts. */
    private UnexpectedAlertBehaviour unexpectedAlertBehaviour_ = UnexpectedAlertBehaviour.DISMISS_AND_NOTIFY;

    /**
     * Constructs a new {@link HtmlUnitAlert} for the given driver.
     * Registers handlers for alert, prompt, confirm, and beforeunload events.
     *
     * @param driver the driver that owns this alert handler
     */
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

    /**
     * Returns the {@link WebWindow} associated with this alert.
     *
     * @return the current web window
     */
    WebWindow getWebWindow() {
        return webWindow_;
    }

    /**
     * Sets whether alerts should be automatically accepted when the driver is quitting.
     *
     * @param autoAccept {@code true} to automatically accept alerts; {@code false} otherwise
     */
    public void setAutoAccept(final boolean autoAccept) {
        quitting_ = autoAccept;
    }

    /**
     * Configures the behavior for unexpected alerts based on the given capabilities.
     *
     * @param capabilities the browser capabilities to inspect for unhandled prompt behavior
     */
    public void handleBrowserCapabilities(final Capabilities capabilities) {
        final UnexpectedAlertBehaviour behaviour = (UnexpectedAlertBehaviour) capabilities
                .getCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR);
        if (behaviour != null) {
            unexpectedAlertBehaviour_ = behaviour;
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
        msg = msg.replace("\r\n", "\n").replace('\r', '\n');
        return msg;
    }

    @Override
    public void sendKeys(final String keysToSend) {
        holder_.sendKeys(keysToSend);
    }

    /**
     * Closes the current alert, signals any waiting threads, and sets auto-accept to {@code true}.
     * Clears the current alert holder.
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

    /**
     * Returns whether an alert is currently active and locking the driver.
     *
     * @return {@code true} if an alert is active; {@code false} otherwise
     */
    boolean isLocked() {
        return holder_ != null;
    }

    /**
     * Ensures that any active alert is handled according to the configured {@link UnexpectedAlertBehaviour}.
     * If an alert is present and the behavior is not {@code IGNORE}, it will be accepted or dismissed.
     * If the behavior indicates notification, an {@link UnhandledAlertException} is thrown.
     *
     * @throws UnhandledAlertException if an unexpected alert is found and notification is required
     */
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
            message_ = message;
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
            defaultMessage_ = defaultMessage;
        }

        @Override
        void sendKeys(String keysToSend) {
            if (keysToSend == null) {
                keysToSend = defaultMessage_;
            }
            value_ = keysToSend;
        }

        @Override
        void accept() {
            if (value_ == null) {
                value_ = defaultMessage_;
            }
        }
    }

}
