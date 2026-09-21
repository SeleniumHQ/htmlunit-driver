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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.htmlunit.action.HtmlUnitAction;
import org.openqa.selenium.htmlunit.w3.Action;

/**
 * Processes and executes input-related {@link HtmlUnitAction} instances for a
 * {@link HtmlUnitDriver}.
 *
 * <p>This class maintains an internal queue of actions that represent low-level
 * input operations (keyboard, pointer, wheel, etc.). Actions are collected via
 * {@link #enqueueAction(Action)} and executed in sequence when
 * {@link #performActions()} is called. Adjacent compatible actions are
 * opportunistically merged (e.g. pointer-down + pointer-up → click, click +
 * click → double-click, key-down + key-up → key-send) to reduce the number of
 * individual driver calls.</p>
 *
 * <p>This class is not thread-safe. All enqueue and perform calls are expected
 * to originate from the WebDriver/test thread.</p>
 *
 * @author Ronald Brill
 */
public class HtmlUnitInputProcessor {

    /**
     * W3C WebDriver button index for the primary (left) mouse button.
     */
    public static final int BUTTON_PRIMARY = 0;

    /**
     * W3C WebDriver button index for the auxiliary (middle) mouse button.
     */
    public static final int BUTTON_AUXILIARY = 1;

    /**
     * W3C WebDriver button index for the secondary (right) mouse button.
     */
    public static final int BUTTON_SECONDARY = 2;

    /**
     * The {@link HtmlUnitDriver} instance used to execute input actions.
     */
    private final HtmlUnitDriver driver_;

    /**
     * The queue of {@link HtmlUnitAction} instances to be performed.
     * Actions are executed in order when {@link #performActions()} is called.
     * Adjacent actions may be merged by {@link #enqueueHtmlUnitAction(HtmlUnitAction)}.
     */
    private final List<HtmlUnitAction> htmlUnitActions_ = new ArrayList<>();

    /**
     * Creates a new input processor for the given driver.
     *
     * @param driver the {@link HtmlUnitDriver} used to process input actions;
     *               must not be {@code null}
     */
    public HtmlUnitInputProcessor(final HtmlUnitDriver driver) {
        driver_ = driver;
    }

    /**
     * Executes all queued {@link HtmlUnitAction} instances in the order they
     * were added (after merging).
     *
     * <p>The internal queue is always cleared after this method returns, even
     * if an action throws a runtime exception. This prevents stale or
     * partially-executed actions from being re-run on the next call.</p>
     *
     * @throws RuntimeException if any action's
     *         {@link HtmlUnitAction#process(HtmlUnitDriver)} throws; the queue
     *         is still cleared before the exception propagates
     */
    public void performActions() {
        try {
            for (final HtmlUnitAction htmlUnitAction : htmlUnitActions_) {
                htmlUnitAction.process(driver_);
            }
        }
        finally {
            htmlUnitActions_.clear();
        }
    }

    /**
     * Translates the provided W3C WebDriver {@link Action} into an
     * {@link HtmlUnitAction} and enqueues it for later execution.
     *
     * <p>If {@link Action#buildHtmlUnitAction()} returns {@code null}, the
     * action is silently ignored and nothing is enqueued.</p>
     *
     * @param action the high-level WebDriver action to translate and queue;
     *               must not be {@code null}
     */
    public void enqueueAction(final Action action) {
        final HtmlUnitAction htmlUnitAction = action.buildHtmlUnitAction();
        if (htmlUnitAction != null) {
            enqueueHtmlUnitAction(htmlUnitAction);
        }
    }

    /**
    /**
     * Adds the given action to the queue, attempting to merge it with the
     * immediately preceding action.
     * @param action the action to enqueue; must not be {@code null}
     *
     * @deprecated Use {@link #enqueueAction(Action)} instead. This method
     *             exists only for binary compatibility and will be removed in a
     *             future release.
     */
    @Deprecated
    public void enqueuAction(final Action action) {
        enqueueAction(action);
    }

    /**
     * Adds the given action to the queue, attempting to merge it with the
     * immediately preceding action.
     *
     * <p>The merge loop works backwards from the tail of the queue:
     * <ol>
     *   <li>The new action is appended.</li>
     *   <li>It is asked to {@link HtmlUnitAction#join(HtmlUnitAction) join}
     *       the action before it.</li>
     *   <li>If a merge produces a new combined action, the two original entries
     *       are replaced by the combined one, and the loop repeats — giving the
     *       combined action a chance to merge further (e.g. click + click →
     *       double-click).</li>
     *   <li>If {@code join} returns the same object as the action that was
     *       asked to join (identity check), no merge occurred and the loop
     *       stops.</li>
     * </ol>
     * </p>
     *
     * @param action the action to enqueue; must not be {@code null}
     */
    private void enqueueHtmlUnitAction(final HtmlUnitAction action) {
        htmlUnitActions_.add(action);

        while (htmlUnitActions_.size() > 1) {
            final int lastPos = htmlUnitActions_.size() - 1;
            final int secondLastPos = lastPos - 1;

            final HtmlUnitAction lastAction = htmlUnitActions_.get(lastPos);
            final HtmlUnitAction joinedAction = lastAction.join(htmlUnitActions_.get(secondLastPos));
            if (joinedAction == lastAction) {
                // No merge occurred; the new action stays as a separate entry.
                return;
            }

            // Replace the two tail entries with the merged action and try again.
            htmlUnitActions_.set(secondLastPos, joinedAction);
            htmlUnitActions_.remove(lastPos);
        }
    }
}