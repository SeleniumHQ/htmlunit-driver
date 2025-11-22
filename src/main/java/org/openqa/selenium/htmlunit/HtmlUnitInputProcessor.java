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
 * Processes and executes input-related {@link HtmlUnitAction} instances for a {@link HtmlUnitDriver}.
 * <p>
 * This class maintains an internal queue of actions that represent low-level input
 * operations (keyboard, pointer, wheel, etc.). Actions are collected and executed
 * in sequence when {@link #performActions()} is called. After execution, the queue
 * is cleared.
 *
 * @author Ronald Brill
 */
public class HtmlUnitInputProcessor {

    /**
     * The {@link HtmlUnitDriver} instance used to execute input actions.
     */
    private final HtmlUnitDriver driver_;

    /**
     * The queue of {@link HtmlUnitAction} instances to be performed.
     * Actions are executed in order when {@link #performActions()} is called.
     */
    private final List<HtmlUnitAction> htmlUnitActions_ = new ArrayList<>();

    /**
     * Creates a new input processor for the given driver.
     *
     * @param driver the {@link HtmlUnitDriver} used to process input actions
     */
    public HtmlUnitInputProcessor(final HtmlUnitDriver driver) {
        driver_ = driver;
    }

    /**
     * Executes all queued {@link HtmlUnitAction} instances in the order they were added.
     * <p>
     * Each action's {@link HtmlUnitAction#process(HtmlUnitDriver)} method is invoked
     * with the associated driver. After all actions have been processed, the internal
     * queue is cleared.
     */
    public void performActions() {
        for (final HtmlUnitAction htmlUnitAction : htmlUnitActions_) {
            htmlUnitAction.process(driver_);
        }
        htmlUnitActions_.clear();
    }

    /**
     * Builds and enqueues an {@link HtmlUnitAction} created from the provided
     * W3C WebDriver {@link Action}.
     * <p>
     * If the action does not produce an {@link HtmlUnitAction} (i.e., if
     * {@link Action#buildHtmlUnitAction()} returns {@code null}), nothing is enqueued.
     *
     * @param action the high-level WebDriver action to translate and queue
     */
    public void enqueuAction(final Action action) {
        final HtmlUnitAction htmlUnitAction = action.buildHtmlUnitAction();
        if (htmlUnitAction != null) {
            enqueuHtmlUnitAction(htmlUnitAction);
        }
    }

    private void enqueuHtmlUnitAction(final HtmlUnitAction action) {
        htmlUnitActions_.add(action);

        while (htmlUnitActions_.size() > 1) {
            final int lastPos = htmlUnitActions_.size() - 1;
            final int secondLastPos = lastPos - 1;

            final HtmlUnitAction lastAction = htmlUnitActions_.get(lastPos);
            final HtmlUnitAction joinedAction = lastAction.join(htmlUnitActions_.get(secondLastPos));
            if (joinedAction == lastAction) {
                return;
            }

            htmlUnitActions_.set(secondLastPos, joinedAction);
            htmlUnitActions_.remove(lastPos);
        }

    }

    /**
     * Represents a low-level input action that can be executed by an {@link HtmlUnitDriver}.
     * Implementations define how a specific action (keyboard, mouse, etc.) is processed.
     */
    public interface HtmlUnitAction {

        /**
         * Processes this action using the given {@link HtmlUnitDriver}.
         * @param driver the driver used to execute the action; must not be {@code null}
         */
        void process(HtmlUnitDriver driver);

        /**
         * Combines this action with a previous action, returning a new {@link HtmlUnitAction}
         * that represents the joined sequence. This can be used to optimize or merge actions.
         * @param previousAction the action to join with; may be {@code null}
         * @return a new {@link HtmlUnitAction} representing the combined actions
         */
        HtmlUnitAction join(HtmlUnitAction previousAction);
    }

    private abstract static class DomElementHtmlUnitAction implements HtmlUnitAction {
        private final DomElement domElement_;

        DomElementHtmlUnitAction(final DomElement domElement) {
            domElement_ = domElement;
        }

        protected DomElement getDomElement() {
            return domElement_;
        }
    }

    /**
     * An {@link HtmlUnitAction} that moves the pointer to the specified {@link DomElement}.
     * This action updates the driver's mouse location without performing a click.
     */
    public static final class PointerMoveHtmlUnitAction extends DomElementHtmlUnitAction {

        /**
         * Creates a new pointer-move action targeting the given DOM element.
         *
         * @param domElement the element to move the pointer to
         */
        public PointerMoveHtmlUnitAction(final DomElement domElement) {
            super(domElement);
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getMouse().mouseMove(getDomElement());
        }

        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    /**
     * Base class for pointer actions that operate on a specific button.
     * Subclasses represent concrete pointer events such as press or release.
     */
    public abstract static class PointerHtmlUnitAction extends DomElementHtmlUnitAction {
        private final int button_;

        /**
         * Creates a new pointer action for the specified element and button.
         *
         * @param domElement the target element associated with the pointer event
         * @param button the mouse button involved in the action
         */
        public PointerHtmlUnitAction(final DomElement domElement, final int button) {
            super(domElement);
            button_ = button;
        }

        /**
         * Returns the mouse button associated with this action.
         *
         * @return the button value
         */
        public int getButton() {
            return button_;
        }
    }

    /**
     * A pointer action that presses a specific mouse button.
     * The action triggers a mouse-down event through the driver's mouse.
     */
    public static final class PointerDownHtmlUnitAction extends PointerHtmlUnitAction {

        /**
         * Creates a new pointer-down action for the element and button.
         *
         * @param domElement the element associated with the press
         * @param button the button to press
         */
        public PointerDownHtmlUnitAction(final DomElement domElement, final int button) {
            super(domElement, button);
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getMouse().mouseDown((Coordinates) null);
        }

        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    /**
     * A pointer action that releases a specific mouse button.
     * When paired with a preceding matching press event, this may combine to form a click action.
     */
    public static final class PointerUpHtmlUnitAction extends PointerHtmlUnitAction {

        /**
         * Creates a new pointer-up action for the element and button.
         *
         * @param domElement the element associated with the release
         * @param button the button to release
         */
        public PointerUpHtmlUnitAction(final DomElement domElement, final int button) {
            super(domElement, button);
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getMouse().mouseUp((Coordinates) null);
        }

        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            if (previousAction instanceof PointerDownHtmlUnitAction) {
                final PointerDownHtmlUnitAction pointerDownAction = (PointerDownHtmlUnitAction) previousAction;
                if (pointerDownAction.getDomElement() == getDomElement()
                        && pointerDownAction.getButton() == getButton()) {
                    return new PointerClickHtmlUnitAction(getDomElement(), getButton());
                }
            }

            return this;
        }
    }

    private static final class PointerClickHtmlUnitAction extends PointerHtmlUnitAction {

        PointerClickHtmlUnitAction(final DomElement domElement, final int button) {
            super(domElement, button);
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            if (2 == getButton()) {
                driver.getMouse().contextClick(null);
                return;
            }

            driver.getMouse().click(null);
        }

        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            if (previousAction instanceof PointerClickHtmlUnitAction) {
                final PointerClickHtmlUnitAction pointerClickAction = (PointerClickHtmlUnitAction) previousAction;
                if (pointerClickAction.getDomElement() == getDomElement()) {
                    return new PointerDblClickHtmlUnitAction(getDomElement());
                }
            }

            return this;
        }
    }

    private static final class PointerDblClickHtmlUnitAction extends DomElementHtmlUnitAction {

        PointerDblClickHtmlUnitAction(final DomElement domElement) {
            super(domElement);
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getMouse().doubleClick((Coordinates) null);
        }

        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    /**
     * An {@link HtmlUnitAction} that represents a key-down event.
     * The action presses the specified key using the driver's keyboard.
     */
    public static final class KeyDownHtmlUnitAction implements HtmlUnitAction {
        private final String value_;

        /**
         * Creates a new key-down action for the given key value.
         *
         * @param value the key to press
         */
        public KeyDownHtmlUnitAction(final String value) {
            value_ = value;
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getKeyboard().pressKey(value_);
        }

        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    /**
     * An {@link HtmlUnitAction} that represents a key-up event.
     * The action releases the specified key using the driver's keyboard.
     */
    public static final class KeyUpHtmlUnitAction implements HtmlUnitAction {
        private final String value_;

        /**
         * Creates a new key-up action for the given key value.
         *
         * @param value the key to release
         */
        public KeyUpHtmlUnitAction(final String value) {
            value_ = value;
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getKeyboard().releaseKey(value_);
        }

        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            if (previousAction instanceof KeyDownHtmlUnitAction) {
                final KeyDownHtmlUnitAction keyDownHtmlUnitAction = (KeyDownHtmlUnitAction) previousAction;
                // todo null check
                if (value_.equals(keyDownHtmlUnitAction.value_)) {
                    return new KeySendHtmlUnitAction(value_);
                }
            }

            return this;
        }
    }

    /**
     * An {@link HtmlUnitAction} representing a complete key press sequence.
     * The action sends the key value directly through the driver's keyboard.
     */
    public static final class KeySendHtmlUnitAction implements HtmlUnitAction {
        private final String value_;

        /**
         * Creates a new key-send action for the given key value.
         *
         * @param value the key value to send
         */
        public KeySendHtmlUnitAction(final String value) {
            value_ = value;
        }

        @Override
        public void process(final HtmlUnitDriver driver) {
            driver.getKeyboard().sendKeys(value_);
        }

        @Override
        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            if (previousAction instanceof KeySendHtmlUnitAction) {
                final KeySendHtmlUnitAction keySendHtmlUnitAction = (KeySendHtmlUnitAction) previousAction;
                return new KeySendHtmlUnitAction(keySendHtmlUnitAction.value_ + value_);
            }

            return this;
        }
    }
}
