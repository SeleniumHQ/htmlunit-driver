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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.htmlunit.w3.Action;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.interactions.Sequence;

import com.gargoylesoftware.htmlunit.html.DomElement;

/**
 * A state machine to handle web diver input sequences. We have to analyze the
 * sequences and construct valid HtmlUnit actions out of them.
 */
public class HtmlUnitInputProcessor {
    private final HtmlUnitDriver driver_;
    private final List<HtmlUnitAction> htmlUnitActions = new ArrayList<>();

    public HtmlUnitInputProcessor(HtmlUnitDriver driver) {
        driver_ = driver;
    }

    public void performActions() {
        for (HtmlUnitAction htmlUnitAction : htmlUnitActions) {
            htmlUnitAction.process(driver_);
        }
        htmlUnitActions.clear();
    }

    public void enqueuAction(Action action) {
        HtmlUnitAction htmlUnitAction = action.buildHtmlUnitAction();
        if (htmlUnitAction != null) {
            enqueuHtmlUnitAction(htmlUnitAction);
        }
    }

    private void enqueuHtmlUnitAction(HtmlUnitAction action) {
        htmlUnitActions.add(action);

        while (htmlUnitActions.size() > 1) {
            int lastPos = htmlUnitActions.size() - 1;
            int secondLastPos = lastPos - 1;

            HtmlUnitAction lastAction = htmlUnitActions.get(lastPos);
            HtmlUnitAction joinedAction = lastAction.join(htmlUnitActions.get(secondLastPos));
            if (joinedAction == lastAction) {
                return;
            }

            htmlUnitActions.set(secondLastPos, joinedAction);
            htmlUnitActions.remove(lastPos);
        }

    }

    public static interface HtmlUnitAction {

        void process(final HtmlUnitDriver driver);

        HtmlUnitAction join(final HtmlUnitAction previousAction);
    }

    private static abstract class DomElementHtmlUnitAction implements HtmlUnitAction {
        private DomElement domElement_;

        public DomElementHtmlUnitAction(DomElement domElement) {
            domElement_ = domElement;
        }

        protected DomElement getDomElement() {
            return domElement_;
        }
    }

    public static final class PointerMoveHtmlUnitAction extends DomElementHtmlUnitAction {

        public PointerMoveHtmlUnitAction(DomElement domElement) {
            super(domElement);
        }

        public void process(final HtmlUnitDriver driver) {
            driver.getMouse().mouseMove(getDomElement());
        }

        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    public static final class PointerDownHtmlUnitAction extends DomElementHtmlUnitAction {

        public PointerDownHtmlUnitAction(DomElement domElement) {
            super(domElement);
        }

        public void process(final HtmlUnitDriver driver) {
            driver.getMouse().mouseDown((Coordinates) null);
        }

        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    public static final class PointerUpHtmlUnitAction extends DomElementHtmlUnitAction {

        public PointerUpHtmlUnitAction(DomElement domElement) {
            super(domElement);
        }

        public void process(final HtmlUnitDriver driver) {
            driver.getMouse().mouseUp((Coordinates) null);
        }

        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            if (previousAction instanceof PointerDownHtmlUnitAction) {
                PointerDownHtmlUnitAction pointerDownAction = (PointerDownHtmlUnitAction) previousAction;
                if (pointerDownAction.getDomElement() == getDomElement()) {
                    return new PointerClickHtmlUnitAction(getDomElement());
                }
            }

            return this;
        }
    }

    private static final class PointerClickHtmlUnitAction extends DomElementHtmlUnitAction {

        public PointerClickHtmlUnitAction(DomElement domElement) {
            super(domElement);
        }

        public void process(final HtmlUnitDriver driver) {
            driver.getMouse().click(null);
        }

        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            if (previousAction instanceof PointerClickHtmlUnitAction) {
                PointerClickHtmlUnitAction pointerClickAction = (PointerClickHtmlUnitAction) previousAction;
                if (pointerClickAction.getDomElement() == getDomElement()) {
                    return new PointerDblClickHtmlUnitAction(getDomElement());
                }
            }

            return this;
        }
    }

    private static final class PointerDblClickHtmlUnitAction extends DomElementHtmlUnitAction {

        public PointerDblClickHtmlUnitAction(DomElement domElement) {
            super(domElement);
        }

        public void process(final HtmlUnitDriver driver) {
            driver.getMouse().doubleClick((Coordinates) null);
        }

        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    public static final class KeyDownHtmlUnitAction implements HtmlUnitAction {
        private final String value_;

        public KeyDownHtmlUnitAction(String value) {
            value_ = value;
        }

        public void process(final HtmlUnitDriver driver) {
            driver.getKeyboard().pressKey(value_);
        }

        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }

    public static final class KeyUpHtmlUnitAction implements HtmlUnitAction {
        private final String value_;

        public KeyUpHtmlUnitAction(String value) {
            value_ = value;
        }

        public void process(final HtmlUnitDriver driver) {
            driver.getKeyboard().releaseKey(value_);
        }

        public HtmlUnitAction join(final HtmlUnitAction previousAction) {
            return this;
        }
    }
}
