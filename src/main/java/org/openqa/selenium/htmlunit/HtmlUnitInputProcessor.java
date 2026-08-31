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

import org.htmlunit.html.DomElement;
import org.openqa.selenium.htmlunit.w3.Action;
import org.openqa.selenium.interactions.Coordinates;

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
    private static final int BUTTON_PRIMARY = 0;

    /**
     * W3C WebDriver button index for the auxiliary (middle) mouse button.
     */
    private static final int BUTTON_AUXILIARY = 1;

    /**
     * W3C WebDriver button index for the secondary (right) mouse button.
     */
    private static final int BUTTON_SECONDARY = 2;

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

    /**
     * Represents a low-level input action that can be executed by an
     * {@link HtmlUnitDriver}.
     *
     * <p>Implementations define how a specific input event (keyboard, mouse,
     * etc.) is dispatched and how it may be merged with a preceding action to
     * produce a higher-level event (e.g. down + up → click).</p>
     */
    public interface HtmlUnitAction {

        /**
         * Dispatches this action through the given {@link HtmlUnitDriver}.
         *
         * @param driver the driver used to execute the action; never {@code null}
         */
        void process(HtmlUnitDriver driver);

        /**
         * Attempts to merge this action with the immediately preceding action.
         *
         * <p>If a meaningful merge is possible (e.g. a pointer-up following a
         * pointer-down on the same element with the same button can become a
         * click), implementations return a new {@link HtmlUnitAction} representing
         * the combined event. If no merge is possible, implementations return
         * {@code this}. The caller uses an identity check ({@code joinedAction
         * == this}) to detect the no-merge case.</p>
         *
         * @param previousAction the action immediately before this one in the
         *                       queue; never {@code null}
         * @return a merged {@link HtmlUnitAction} if a merge was possible,
         *         or {@code this} if no merge applies
         */
        HtmlUnitAction join(HtmlUnitAction previousAction);
    }

    /**
     * Base class for actions that target a specific {@link DomElement}.
     */
    private abstract static class DomElementHtmlUnitAction implements HtmlUnitAction {
        private final DomElement domElement_;

        DomElementHtmlUnitAction(final DomElement domElement) {
            domElement_ = domElement;
        }

        /**
         * Returns the {@link DomElement} targeted by this action.
         *
         * @return the target element; may be {@code null} if constructed without one
         */
        protected DomElement getDomElement() {
            return domElement_;
        }
    }

    /**
     * An {@link HtmlUnitAction} that moves the pointer to the specified
     * {@link DomElement}. Updates the driver's simulated mouse position without
     * performing a click.
     *
     * <p>Mouse-move actions are never merged with adjacent actions.</p>
     */
    public static final class PointerMoveHtmlUnitAction extends DomElementHtmlUnitAction {

        /**
         * Creates a new pointer-move action targeting the given DOM element.
         *
         * @param domElement the element to move the pointer to; must not be {@code null}
         */
        public PointerMoveHtmlUnitAction(final DomElement domElement) {
            super(domElement);
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getMouse().mouseMove(getDomElement());
        }

        /** Mouse-move actions are never merged; always returns {@code this}. */
        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    /**
     * Base class for pointer actions that operate on a specific mouse button.
     * Subclasses represent concrete pointer events such as press or release.
     */
    public abstract static class PointerHtmlUnitAction extends DomElementHtmlUnitAction {
        private final int button_;

        /**
         * Creates a new pointer action for the specified element and button.
         *
         * @param domElement the target element associated with the pointer event;
         *                   must not be {@code null}
         * @param button     the W3C button index (0 = primary/left, 1 = auxiliary/middle,
         *                   2 = secondary/right)
         */
        public PointerHtmlUnitAction(final DomElement domElement, final int button) {
            super(domElement);
            button_ = button;
        }

        /**
         * Returns the W3C button index associated with this action.
         *
         * @return 0 for primary, 1 for auxiliary, 2 for secondary
         */
        public int getButton() {
            return button_;
        }
    }

    /**
     * A pointer action that presses a specific mouse button (mouse-down event).
     *
     * <p><b>Note:</b> HtmlUnit's {@code Mouse} API does not accept explicit
     * coordinates or a target element for {@code mouseDown}; the element and
     * button stored in this action are used only for merge decisions in
     * {@link PointerUpHtmlUnitAction#join(HtmlUnitAction)} and are not passed
     * to the underlying driver call.</p>
     *
     * <p>Pointer-down actions are never merged with a preceding action.</p>
     */
    public static final class PointerDownHtmlUnitAction extends PointerHtmlUnitAction {

        /**
         * Creates a new pointer-down action for the given element and button.
         *
         * @param domElement the element at which the button is pressed
         * @param button     the W3C button index
         */
        public PointerDownHtmlUnitAction(final DomElement domElement, final int button) {
            super(domElement, button);
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            // HtmlUnit's Mouse API does not expose per-element / per-button mouseDown;
            // the element and button are used only for merge decisions.
            driver.getMouse().mouseDown((Coordinates) null);
        }

        /** Pointer-down actions are never merged; always returns {@code this}. */
        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    /**
     * A pointer action that releases a specific mouse button (mouse-up event).
     *
     * <p>When preceded by a {@link PointerDownHtmlUnitAction} targeting the
     * same element with the same button, this action merges into a
     * {@link PointerClickHtmlUnitAction}.</p>
     *
     * <p><b>Note:</b> like {@link PointerDownHtmlUnitAction}, HtmlUnit's API
     * does not support per-element / per-button {@code mouseUp}; the stored
     * element and button are used only for merge decisions.</p>
     */
    public static final class PointerUpHtmlUnitAction extends PointerHtmlUnitAction {

        /**
         * Creates a new pointer-up action for the given element and button.
         *
         * @param domElement the element at which the button is released
         * @param button     the W3C button index
         */
        public PointerUpHtmlUnitAction(final DomElement domElement, final int button) {
            super(domElement, button);
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            // HtmlUnit's Mouse API does not expose per-element / per-button mouseUp;
            // the element and button are used only for merge decisions.
            driver.getMouse().mouseUp((Coordinates) null);
        }

        /**
         * Merges with a preceding {@link PointerDownHtmlUnitAction} on the same
         * element and button into a {@link PointerClickHtmlUnitAction}.
         */
        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            if (previousAction instanceof PointerDownHtmlUnitAction) {
                final PointerDownHtmlUnitAction pointerDownAction =
                        (PointerDownHtmlUnitAction) previousAction;
                if (pointerDownAction.getDomElement() == getDomElement()
                        && pointerDownAction.getButton() == getButton()) {
                    return new PointerClickHtmlUnitAction(getDomElement(), getButton());
                }
            }
            return this;
        }
    }

    /**
     * A synthesised click action produced by merging a pointer-down and a
     * pointer-up on the same element with the same button.
     *
     * <p>Two successive clicks on the same element (regardless of button) are
     * further merged into a {@link PointerDblClickHtmlUnitAction}.</p>
     */
    private static final class PointerClickHtmlUnitAction extends PointerHtmlUnitAction {

        PointerClickHtmlUnitAction(final DomElement domElement, final int button) {
            super(domElement, button);
        }

        /**
         * Dispatches the click. Primary button (0) triggers a standard click;
         * secondary button (2) triggers a context-click. The auxiliary (middle)
         * button and any other button fall back to a standard click because
         * HtmlUnit's {@code Mouse} API does not expose a dedicated middle-click.
         */
        @Override
        public void process(final HtmlUnitDriver driver) {
            if (BUTTON_SECONDARY == getButton()) {
                driver.getMouse().contextClick(null);
                return;
            }
            // BUTTON_PRIMARY (0) and BUTTON_AUXILIARY (1) both map to click();
            // HtmlUnit has no dedicated middle-click API.
            driver.getMouse().click(null);
        }

        /**
         * Merges two successive clicks on the same element <em>with the same
         * button</em> into a double-click. The button must match to avoid
         * combining e.g. a right-click and a left-click into a left double-click.
         */
        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            if (previousAction instanceof PointerClickHtmlUnitAction) {
                final PointerClickHtmlUnitAction pointerClickAction =
                        (PointerClickHtmlUnitAction) previousAction;
                if (pointerClickAction.getDomElement() == getDomElement()
                        && pointerClickAction.getButton() == getButton()) {
                    return new PointerDblClickHtmlUnitAction(getDomElement());
                }
            }
            return this;
        }
    }

    /**
     * A synthesised double-click action produced by merging two successive
     * {@link PointerClickHtmlUnitAction}s on the same element.
     *
     * <p>Double-click actions are never merged further.</p>
     */
    private static final class PointerDblClickHtmlUnitAction extends DomElementHtmlUnitAction {

        PointerDblClickHtmlUnitAction(final DomElement domElement) {
            super(domElement);
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getMouse().doubleClick((Coordinates) null);
        }

        /** Double-click actions are never merged further; always returns {@code this}. */
        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    /**
     * An {@link HtmlUnitAction} that represents a key-down (key-press) event.
     * Presses the specified key through the driver's keyboard.
     *
     * <p>Key-down actions are never merged with a preceding action.</p>
     */
    public static final class KeyDownHtmlUnitAction implements HtmlUnitAction {
        private final String value_;

        /**
         * Creates a new key-down action for the given key value.
         *
         * @param value the key to press; must not be {@code null}
         */
        public KeyDownHtmlUnitAction(final String value) {
            value_ = value;
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getKeyboard().pressKey(value_);
        }

        /** Key-down actions are never merged; always returns {@code this}. */
        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    /**
     * An {@link HtmlUnitAction} that represents a key-up (key-release) event.
     * Releases the specified key through the driver's keyboard.
     *
     * <p>When preceded by a {@link KeyDownHtmlUnitAction} for the same key, this
     * action merges into a {@link KeySendHtmlUnitAction}, which sends the key as
     * a complete keystroke rather than as separate press/release events.</p>
     */
    public static final class KeyUpHtmlUnitAction implements HtmlUnitAction {
        private final String value_;

        /**
         * Creates a new key-up action for the given key value.
         *
         * @param value the key to release; must not be {@code null}
         */
        public KeyUpHtmlUnitAction(final String value) {
            value_ = value;
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getKeyboard().releaseKey(value_);
        }

        /**
         * Merges with a preceding {@link KeyDownHtmlUnitAction} for the same key
         * into a {@link KeySendHtmlUnitAction}.
         *
         * <p>{@code value_} is guaranteed non-null by the constructor; no null
         * check is required here because {@code previousAction} is never
         * {@code null} (the merge loop only calls {@code join} when there are at
         * least two entries in the queue).</p>
         */
        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            if (previousAction instanceof KeyDownHtmlUnitAction) {
                final KeyDownHtmlUnitAction keyDownAction = (KeyDownHtmlUnitAction) previousAction;
                if (value_.equals(keyDownAction.value_)) {
                    return new KeySendHtmlUnitAction(value_);
                }
            }
            return this;
        }
    }

    /**
     * An {@link HtmlUnitAction} representing a complete keystroke (press +
     * release). Sends the key value directly through the driver's keyboard.
     *
     * <p>Consecutive {@link KeySendHtmlUnitAction}s are merged by concatenating
     * their values, allowing multi-character type sequences to be dispatched in
     * a single {@code sendKeys} call.</p>
     */
    public static final class KeySendHtmlUnitAction implements HtmlUnitAction {
        private final String value_;

        /**
         * Creates a new key-send action for the given key value.
         *
         * @param value the key value to send; must not be {@code null}
         */
        public KeySendHtmlUnitAction(final String value) {
            value_ = value;
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getKeyboard().sendKeys(value_);
        }

        /**
         * Merges with a preceding {@link KeySendHtmlUnitAction} by concatenating
         * the two key values into a single action.
         */
        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            if (previousAction instanceof KeySendHtmlUnitAction) {
                final KeySendHtmlUnitAction keySendAction = (KeySendHtmlUnitAction) previousAction;
                return new KeySendHtmlUnitAction(keySendAction.value_ + value_);
            }
            return this;
        }
    }
}