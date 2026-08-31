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
 * <p>This class is thread-safe. Alert handlers are invoked from HtmlUnit's
 * JavaScript thread, while {@link Alert} methods ({@link #accept()},
 * {@link #dismiss()}, {@link #getText()}, etc.) are called from the
 * WebDriver/test thread. All shared mutable state is either guarded by
 * {@link #lock_} or declared {@code volatile} to ensure correct visibility
 * across threads.</p>
 *
 * <p>The alert lifecycle is as follows:</p>
 * <ol>
 *   <li>HtmlUnit's JS thread invokes a handler (e.g. {@code alertHandler}),
 *       sets {@link #holder_} and {@link #webWindow_}, then blocks inside
 *       {@link #awaitCondition()} waiting for the dialog to be resolved.</li>
 *   <li>The WebDriver thread calls {@link #accept()} or {@link #dismiss()},
 *       which signals {@link #condition_} and clears {@link #holder_}, allowing
 *       the JS thread to resume.</li>
 * </ol>
 *
 * @author Ahmed Ashour
 * @author A aftakhov
 * @author Ronald Brill
 */
public class HtmlUnitAlert implements Alert {

    /** The {@link HtmlUnitDriver} that owns this alert handler. */
    private final HtmlUnitDriver driver_;

    /**
     * Holds the current alert state and message.
     *
     * <p>Written and read from two different threads (JS thread and WebDriver
     * thread). Declared {@code volatile} so that writes on one thread are
     * immediately visible to the other without requiring the full lock.
     * Any compound check-then-act sequence (e.g. null-check followed by
     * field access) must still be performed while holding {@link #lock_}.</p>
     */
    private volatile AlertHolder holder_;

    /**
     * Indicates whether the driver is quitting.
     *
     * <p>Declared {@code volatile} because it is written by the WebDriver
     * thread (via {@link #setAutoAccept(boolean)}) and read by HtmlUnit's
     * JS thread inside the various dialog handlers.</p>
     */
    private volatile boolean quitting_;

    /** Lock used to coordinate access to alert state between threads. */
    private final Lock lock_ = new ReentrantLock();

    /**
     * Condition used to signal the JS thread when an alert has been resolved.
     * Must only be awaited/signalled while holding {@link #lock_}.
     */
    private final Condition condition_ = lock_.newCondition();

    /**
     * The {@link WebWindow} most recently associated with an alert event.
     *
     * <p>Declared {@code volatile} because it is written by HtmlUnit's JS
     * thread and read by the WebDriver thread via {@link #getWebWindow()},
     * with no lock held on the read path.</p>
     */
    private volatile WebWindow webWindow_;

    /** The configured behavior for unexpected (unhandled) alerts. */
    private UnexpectedAlertBehaviour unexpectedAlertBehaviour_ = UnexpectedAlertBehaviour.DISMISS_AND_NOTIFY;

    /**
     * Constructs a new {@link HtmlUnitAlert} for the given driver.
     * Registers handlers for alert, prompt, confirm, and beforeunload events
     * on the underlying {@link WebClient}.
     *
     * @param driver the {@link HtmlUnitDriver} that owns this alert handler;
     *               must not be {@code null}
     */
    HtmlUnitAlert(final HtmlUnitDriver driver) {
        driver_ = driver;
        final WebClient webClient = driver.getWebClient();
        webClient.setAlertHandler(this::alertHandler);
        webClient.setPromptHandler(this::promptHandler);
        webClient.setConfirmHandler(this::confirmHandler);
        webClient.setOnbeforeunloadHandler(this::onbeforeunloadHandler);
    }

    /**
     * Invoked by HtmlUnit on the JS thread when a {@code window.alert()} call
     * is made. Blocks until the alert is dismissed or a 5-second timeout elapses.
     *
     * @param page    the page that triggered the alert
     * @param message the alert message text
     */
    private void alertHandler(final Page page, final String message) {
        if (quitting_) {
            return;
        }
        webWindow_ = page.getEnclosingWindow();
        holder_ = new AlertHolder(message);
        awaitCondition();
    }

    /**
     * Invoked by HtmlUnit on the JS thread when a {@code window.confirm()} call
     * is made. Blocks until the dialog is accepted or dismissed (or timeout).
     *
     * @param page    the page that triggered the confirm dialog
     * @param message the confirm message text
     * @return {@code true} if the dialog was accepted; {@code false} if dismissed
     */
    private boolean confirmHandler(final Page page, final String message) {
        if (quitting_) {
            return false;
        }
        webWindow_ = page.getEnclosingWindow();
        // Capture holder in a local variable before blocking so that we can
        // safely read isAccepted() after the WebDriver thread has cleared holder_.
        final AlertHolder localHolder = new AlertHolder(message);
        holder_ = localHolder;
        awaitCondition();
        return localHolder.isAccepted();
    }

    /**
     * Blocks the calling (JS) thread until the alert condition is signalled by
     * the WebDriver thread, or until the 5-second timeout expires.
     *
     * <p>The loop guards against spurious wakeups: the thread re-checks
     * {@code holder_ != null} after each wakeup and continues waiting if the
     * alert has not yet been resolved. Spurious wakeups are explicitly permitted
     * by the {@link Condition} contract and must be handled.</p>
     *
     * @throws RuntimeException wrapping {@link InterruptedException} if the
     *                          thread is interrupted while waiting
     */
    private void awaitCondition() {
        lock_.lock();
        try {
            if (driver_.isProcessAlert()) {
                // Guard against spurious wakeups by re-checking the predicate.
                while (holder_ != null) {
                    try {
                        final boolean signalled = condition_.await(5, TimeUnit.SECONDS);
                        if (!signalled) {
                            // Timed out — treat as a dismiss and unblock.
                            break;
                        }
                    }
                    catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        finally {
            lock_.unlock();
        }
    }

    /**
     * Invoked by HtmlUnit on the JS thread when a {@code window.prompt()} call
     * is made. Blocks until the dialog is resolved (or timeout).
     *
     * @param page           the page that triggered the prompt
     * @param message        the prompt message text
     * @param defaultMessage the default value pre-filled in the prompt input
     * @return the string entered by the user, or {@code null} if dismissed
     */
    private String promptHandler(final Page page, final String message, final String defaultMessage) {
        if (quitting_) {
            return null;
        }
        webWindow_ = page.getEnclosingWindow();
        // Capture in a local variable before blocking (holder_ may be cleared
        // by the WebDriver thread before we read value_ below).
        final PromptHolder localHolder = new PromptHolder(message, defaultMessage);
        holder_ = localHolder;
        awaitCondition();
        return localHolder.value_;
    }

    /**
     * Invoked by HtmlUnit on the JS thread when a {@code window.onbeforeunload}
     * handler returns a non-empty string. Blocks until the dialog is resolved.
     *
     * @param page        the page that triggered the beforeunload dialog
     * @param returnValue the return value from the {@code onbeforeunload} handler
     * @return {@code true} to allow navigation; {@code false} to cancel it
     */
    private boolean onbeforeunloadHandler(final Page page, final String returnValue) {
        if (quitting_) {
            return true;
        }
        webWindow_ = page.getEnclosingWindow();
        // Capture in a local variable before blocking so we can read isAccepted()
        // after the WebDriver thread has cleared holder_.
        final AlertHolder localHolder = new AlertHolder(returnValue);
        holder_ = localHolder;
        awaitCondition();
        return localHolder.isAccepted();
    }

    /**
     * Returns the {@link WebWindow} associated with the most recently triggered alert.
     *
     * @return the current web window, or {@code null} if no alert has been triggered yet
     */
    WebWindow getWebWindow() {
        return webWindow_;
    }

    /**
     * Sets whether alerts should be automatically accepted (i.e. the driver is quitting).
     * When {@code true}, all subsequent alert handlers return immediately without blocking.
     *
     * @param autoAccept {@code true} to skip alert handling; {@code false} to resume normal handling
     */
    public void setAutoAccept(final boolean autoAccept) {
        quitting_ = autoAccept;
    }

    /**
     * Configures the behavior for unexpected alerts based on the given capabilities.
     * Reads the {@link CapabilityType#UNHANDLED_PROMPT_BEHAVIOUR} capability and
     * updates {@link #unexpectedAlertBehaviour_} if it is present.
     *
     * @param capabilities the browser capabilities to inspect; must not be {@code null}
     */
    public void handleBrowserCapabilities(final Capabilities capabilities) {
        final UnexpectedAlertBehaviour behaviour = (UnexpectedAlertBehaviour) capabilities
                .getCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR);
        if (behaviour != null) {
            unexpectedAlertBehaviour_ = behaviour;
        }
    }

    /**
     * Dismisses the currently active alert dialog (equivalent to clicking "Cancel").
     *
     * <p>{@code holder_} is set to {@code null} inside the locked region, before
     * the lock is released, so that other threads waiting on {@link #lock_} never
     * observe a stale non-null {@code holder_} after the signal.</p>
     *
     * @throws NoAlertPresentException if no alert is currently active
     */
    @Override
    public void dismiss() {
        lock_.lock();
        try {
            if (holder_ == null) {
                throw new NoAlertPresentException();
            }
            // Clear holder_ before signalling so the JS thread sees null
            // as soon as it reacquires the lock after wakeup.
            holder_ = null;
            condition_.signal();
        }
        finally {
            lock_.unlock();
        }
    }

    /**
     * Accepts the currently active alert dialog (equivalent to clicking "OK").
     *
     * <p>{@code holder_} is set to {@code null} inside the locked region, before
     * the lock is released, so that other threads waiting on {@link #lock_} never
     * observe a stale non-null {@code holder_} after the signal.</p>
     *
     * @throws NoAlertPresentException if no alert is currently active
     */
    @Override
    public void accept() {
        lock_.lock();
        try {
            if (holder_ == null) {
                throw new NoAlertPresentException();
            }
            holder_.accept();
            // Clear holder_ before signalling (same reasoning as dismiss()).
            holder_ = null;
            condition_.signal();
        }
        finally {
            lock_.unlock();
        }
    }

    /**
     * Returns the text of the currently active alert dialog.
     *
     * <p>Line endings are normalised: {@code \r\n} and bare {@code \r} are
     * both converted to {@code \n}.</p>
     *
     * @return the alert message text, never {@code null}
     * @throws NoAlertPresentException if no alert is currently active
     */
    @Override
    public String getText() {
        final AlertHolder localHolder = holder_;
        if (localHolder == null) {
            throw new NoAlertPresentException();
        }
        String msg = localHolder.message_;
        msg = msg.replace("\r\n", "\n").replace('\r', '\n');
        return msg;
    }

    /**
     * Sends keystrokes to the currently active dialog.
     * Only meaningful for {@code window.prompt()} dialogs; calling this on a
     * plain alert or confirm dialog throws {@link ElementNotInteractableException}.
     *
     * @param keysToSend the text to type into the prompt input field; may be
     *                   {@code null} for a prompt (treated as the default value)
     * @throws NoAlertPresentException if no alert is currently active
     */
    @Override
    public void sendKeys(final String keysToSend) {
        final AlertHolder localHolder = holder_;
        if (localHolder == null) {
            throw new NoAlertPresentException();
        }
        localHolder.sendKeys(keysToSend);
    }

    /**
     * Signals any JS thread waiting on the alert condition, sets auto-accept to
     * {@code true}, and clears the current alert holder.
     *
     * <p>Called when the driver is being closed. Setting {@link #quitting_} to
     * {@code true} prevents subsequent alert handlers from blocking.
     * {@code holder_} is cleared inside the locked region before the lock is
     * released.</p>
     */
    void close() {
        lock_.lock();
        try {
            holder_ = null;
            condition_.signal();
            setAutoAccept(true);
        }
        finally {
            lock_.unlock();
        }
    }

    /**
     * Returns whether an alert dialog is currently active.
     *
     * @return {@code true} if an alert is present (i.e. the JS thread is blocked
     *         waiting for the dialog to be resolved); {@code false} otherwise
     */
    boolean isLocked() {
        return holder_ != null;
    }

    /**
     * Ensures that any active alert is handled according to the configured
     * {@link UnexpectedAlertBehaviour}, then throws {@link UnhandledAlertException}
     * if the behaviour requires notification.
     *
     * <p>The entire check-then-act sequence (read text, accept/dismiss, throw) is
     * performed while holding {@link #lock_} to prevent a TOCTOU race where
     * another thread could resolve the alert between the {@link #isLocked()} check
     * and the subsequent {@link #getText()} / {@link #accept()} calls.</p>
     *
     * <ul>
     *   <li>{@link UnexpectedAlertBehaviour#ACCEPT} — accepts the alert and returns.</li>
     *   <li>{@link UnexpectedAlertBehaviour#ACCEPT_AND_NOTIFY} — accepts the alert,
     *       then throws {@link UnhandledAlertException}.</li>
     *   <li>{@link UnexpectedAlertBehaviour#DISMISS} — dismisses the alert and returns.</li>
     *   <li>{@link UnexpectedAlertBehaviour#DISMISS_AND_NOTIFY} — dismisses the alert,
     *       then throws {@link UnhandledAlertException}.</li>
     *   <li>{@link UnexpectedAlertBehaviour#IGNORE} — does nothing.</li>
     * </ul>
     *
     * @throws UnhandledAlertException if an unexpected alert is found and the
     *                                 configured behaviour requires notification
     */
    public void ensureUnlocked() {
        lock_.lock();
        try {
            // Re-read holder_ under the lock to avoid TOCTOU between isLocked()
            // and the subsequent getText() / accept() / dismiss() calls.
            final AlertHolder localHolder = holder_;
            if (localHolder == null) {
                return;
            }

            // Capture text before the holder is cleared by accept()/dismiss().
            String text = localHolder.message_;
            text = text.replace("\r\n", "\n").replace('\r', '\n');

            switch (unexpectedAlertBehaviour_) {
                case ACCEPT:
                    localHolder.accept();
                    holder_ = null;
                    condition_.signal();
                    return;

                case ACCEPT_AND_NOTIFY:
                    localHolder.accept();
                    holder_ = null;
                    condition_.signal();
                    break;

                case DISMISS:
                    holder_ = null;
                    condition_.signal();
                    return;

                case DISMISS_AND_NOTIFY:
                    holder_ = null;
                    condition_.signal();
                    break;

                case IGNORE:
                    return;

                default:
                    return;
            }
            throw new UnhandledAlertException("Unexpected alert found", text);
        }
        finally {
            lock_.unlock();
        }
    }

    /**
     * Holds the state for an active alert or confirm dialog.
     *
     * <p>Instances are created on the JS thread and may be read or mutated by
     * the WebDriver thread. The containing class's {@link HtmlUnitAlert#lock_}
     * must be held for any compound operations on the same {@code AlertHolder}
     * instance, but individual field reads performed via a locally captured
     * reference (after the holder has already been published via the
     * {@code volatile} {@link HtmlUnitAlert#holder_}) are safe.</p>
     */
    private static class AlertHolder {

        /** The dialog message text set at construction time; never modified after that. */
        private final String message_;

        /** Whether this dialog has been accepted. Mutated only via {@link #accept()}. */
        private boolean accepted_;

        /**
         * Constructs an {@link AlertHolder} with the given message.
         *
         * @param message the alert message text; must not be {@code null}
         */
        AlertHolder(final String message) {
            message_ = message;
        }

        /**
         * Handles an attempt to type into this dialog.
         * Plain alert and confirm dialogs are not interactable, so this always throws.
         *
         * <p>Note: {@code null} is an invalid argument here because alert dialogs
         * have no input field to receive it. Only {@link PromptHolder} overrides this
         * to accept {@code null} (treating it as the default value).</p>
         *
         * @param keysToSend the keys to send; always causes an exception for this type
         * @throws ElementNotInteractableException if {@code keysToSend} is non-null
         *         (the typical case — the dialog has no input field)
         * @throws IllegalArgumentException        if {@code keysToSend} is {@code null}
         *         (invalid argument for a non-prompt dialog)
         */
        void sendKeys(final String keysToSend) {
            if (keysToSend != null) {
                throw new ElementNotInteractableException("alert is not interactable");
            }
            throw new IllegalArgumentException("keysToSend must not be null for a non-prompt dialog");
        }

        /**
         * Marks this dialog as accepted.
         */
        void accept() {
            accepted_ = true;
        }

        /**
         * Returns whether this dialog has been accepted.
         *
         * @return {@code true} if {@link #accept()} has been called; {@code false} otherwise
         */
        boolean isAccepted() {
            return accepted_;
        }
    }

    /**
     * Extends {@link AlertHolder} with prompt-specific behaviour: it accepts
     * optional user input via {@link #sendKeys(String)} and returns that input
     * (or the default message) when {@link #accept()} is called.
     */
    private static class PromptHolder extends AlertHolder {

        /** The default value pre-filled in the prompt input field; may be {@code null}. */
        private final String defaultMessage_;

        /**
         * The value to be returned by the prompt. Set by {@link #sendKeys(String)} or
         * defaulted to {@link #defaultMessage_} on {@link #accept()}.
         */
        private String value_;

        /**
         * Constructs a {@link PromptHolder} with the given prompt message and default value.
         *
         * @param message        the prompt message text; must not be {@code null}
         * @param defaultMessage the pre-filled default value; may be {@code null}
         */
        PromptHolder(final String message, final String defaultMessage) {
            super(message);
            defaultMessage_ = defaultMessage;
        }

        /**
         * Sets the value that will be returned by the prompt dialog.
         * Passing {@code null} is treated as using the default message.
         *
         * @param keysToSend the text entered by the user; {@code null} selects the default value
         */
        @Override
        void sendKeys(String keysToSend) {
            if (keysToSend == null) {
                keysToSend = defaultMessage_;
            }
            value_ = keysToSend;
        }

        /**
         * Accepts the prompt dialog. If no keys were sent, the returned value
         * falls back to {@link #defaultMessage_}.
         */
        @Override
        void accept() {
            if (value_ == null) {
                value_ = defaultMessage_;
            }
        }
    }
}
