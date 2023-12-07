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

import java.util.ArrayList;
import java.util.List;

import org.htmlunit.html.DomElement;
import org.openqa.selenium.htmlunit.w3.Action;
import org.openqa.selenium.interactions.Coordinates;

/**
 * A state machine to handle web diver input sequences. We have to analyze the
 * sequences and construct valid HtmlUnit actions out of them.
 *
 * @author Ronald Brill
 */
public class HtmlUnitInputProcessor {
    private final HtmlUnitDriver driver_;
    private final List<HtmlUnitAction> htmlUnitActions_ = new ArrayList<>();

    public HtmlUnitInputProcessor(final HtmlUnitDriver driver) {
        driver_ = driver;
    }

    public void performActions() {
        for (final HtmlUnitAction htmlUnitAction : htmlUnitActions_) {
            htmlUnitAction.process(driver_);
        }
        htmlUnitActions_.clear();
    }

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

    public interface HtmlUnitAction {

        void process(HtmlUnitDriver driver);

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

    public static final class PointerMoveHtmlUnitAction extends DomElementHtmlUnitAction {

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

    public abstract static class PointerHtmlUnitAction extends DomElementHtmlUnitAction {
        private final int button_;

        public PointerHtmlUnitAction(final DomElement domElement, final int button) {
            super(domElement);
            button_ = button;
        }

        public int getButton() {
            return button_;
        }
    }

    public static final class PointerDownHtmlUnitAction extends PointerHtmlUnitAction {

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

    public static final class PointerUpHtmlUnitAction extends PointerHtmlUnitAction {

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

    public static final class KeyDownHtmlUnitAction implements HtmlUnitAction {
        private final String value_;

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

    public static final class KeyUpHtmlUnitAction implements HtmlUnitAction {
        private final String value_;

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

    public static final class KeySendHtmlUnitAction implements HtmlUnitAction {
        private final String value_;

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
