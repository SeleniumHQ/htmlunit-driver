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

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.htmlunit.ScriptException;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.javascript.host.event.MouseEvent;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Coordinates;

/**
 * Implements mouse operations using the HtmlUnit WebDriver.
 *
 * @author Simon Stewart
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Martin Bartoš
 */
public class HtmlUnitMouse {
    private final HtmlUnitDriver parent_;
    private final HtmlUnitKeyboard keyboard_;
    private DomElement currentActiveElement_;

    public HtmlUnitMouse(final HtmlUnitDriver parent, final HtmlUnitKeyboard keyboard) {
        this.parent_ = parent;
        this.keyboard_ = keyboard;
    }

    private DomElement getElementForOperation(final Coordinates potentialCoordinates) {
        if (potentialCoordinates != null) {
            return (DomElement) potentialCoordinates.getAuxiliary();
        }

        if (currentActiveElement_ == null) {
            throw new NoSuchElementException(
                    "About to perform an interaction that relies" + " on the active element, but there isn't one.");
        }

        return currentActiveElement_;
    }

    public void click(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.click(element, false);
    }

    /**
     * @param directClick {@code true} for {@link WebElement#click()} or
     *                    {@code false} for {@link Actions#click()}
     */
    void click(final DomElement element, final boolean directClick) {
        if (!element.isDisplayed()) {
            throw new ElementNotInteractableException("You may only interact with visible elements");
        }

        moveOutIfNeeded(element);

        try {
            element.mouseOver();
            element.mouseMove();

            element.click(keyboard_.isShiftPressed(),
                    keyboard_.isCtrlPressed() || (directClick && element instanceof HtmlOption),
                    keyboard_.isAltPressed());
            updateActiveElement(element);
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }
        catch (final ScriptException e) {
            // TODO(simon): This isn't good enough.
            System.out.println(e.getMessage());
            // Press on regardless
        }
        catch (final RuntimeException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof SocketTimeoutException) {
                throw new TimeoutException(cause);
            }
            throw e;
        }
    }

    private void moveOutIfNeeded(final DomElement element) {
        try {
            if (currentActiveElement_ != element) {
                if (currentActiveElement_ != null) {
                    currentActiveElement_.mouseOver(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(),
                            keyboard_.isAltPressed(), MouseEvent.BUTTON_LEFT);

                    currentActiveElement_.mouseOut(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(),
                            keyboard_.isAltPressed(), MouseEvent.BUTTON_LEFT);
                }

                if (element != null) {
                    element.mouseMove(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                            MouseEvent.BUTTON_LEFT);
                    element.mouseOver(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                            MouseEvent.BUTTON_LEFT);
                }
            }
        }
        catch (final ScriptException ignored) {
            System.out.println(ignored.getMessage());
        }
    }

    private void updateActiveElement(final DomElement element) {
        if (element != null) {
            currentActiveElement_ = element;
        }
    }

    public void doubleClick(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.doubleClick(element);
    }

    void doubleClick(final DomElement element) {

        moveOutIfNeeded(element);

        // Send the state of modifier keys to the dblClick method.
        try {
            element.dblClick(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed());
            updateActiveElement(element);
        }
        catch (final IOException e) {
            // TODO(eran.mes): What should we do in case of error?
            e.printStackTrace();
        }
    }

    public void contextClick(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);

        moveOutIfNeeded(element);

        element.rightClick(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed());

        updateActiveElement(element);
    }

    public void mouseDown(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.mouseDown(element);
    }

    void mouseDown(final DomElement element) {
        moveOutIfNeeded(element);

        element.mouseDown(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                MouseEvent.BUTTON_LEFT);

        updateActiveElement(element);
    }

    public void mouseUp(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.mouseUp(element);
    }

    void mouseUp(final DomElement element) {
        moveOutIfNeeded(element);

        element.mouseUp(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                MouseEvent.BUTTON_LEFT);

        updateActiveElement(element);
    }

    public void mouseMove(final Coordinates elementCoordinates) {
        final DomElement element = (DomElement) elementCoordinates.getAuxiliary();
        parent_.mouseMove(element);
    }

    void mouseMove(final DomElement element) {
        moveOutIfNeeded(element);

        updateActiveElement(element);
    }

    public void mouseMove(final Coordinates where, final long xOffset, final long yOffset) {
        throw new UnsupportedOperationException("Moving to arbitrary X,Y coordinates not supported.");
    }
}
