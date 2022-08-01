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

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Coordinates;

import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.javascript.host.event.MouseEvent;

/**
 * Implements mouse operations using the HtmlUnit WebDriver.
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Martin Bartoš
 */
public class HtmlUnitMouse {
    private final HtmlUnitDriver parent_;
    private final HtmlUnitKeyboard keyboard_;
    private DomElement currentActiveElement_;

    public HtmlUnitMouse(HtmlUnitDriver parent, HtmlUnitKeyboard keyboard) {
        this.parent_ = parent;
        this.keyboard_ = keyboard;
    }

    private DomElement getElementForOperation(Coordinates potentialCoordinates) {
        if (potentialCoordinates != null) {
            return (DomElement) potentialCoordinates.getAuxiliary();
        }

        if (currentActiveElement_ == null) {
            throw new NoSuchElementException(
                    "About to perform an interaction that relies" + " on the active element, but there isn't one.");
        }

        return currentActiveElement_;
    }

    public void click(Coordinates elementCoordinates) {
        DomElement element = getElementForOperation(elementCoordinates);
        parent_.click(element, false);
    }

    /**
     * @param directClick {@code true} for {@link WebElement#click()} or
     *                    {@code false} for {@link Actions#click()}
     */
    void click(DomElement element, boolean directClick) {
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
        } catch (IOException e) {
            throw new WebDriverException(e);
        } catch (ScriptException e) {
            // TODO(simon): This isn't good enough.
            System.out.println(e.getMessage());
            // Press on regardless
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SocketTimeoutException) {
                throw new TimeoutException(cause);
            }
            throw e;
        }
    }

    private void moveOutIfNeeded(DomElement element) {
        try {
            if ((currentActiveElement_ != element)) {
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
        } catch (ScriptException ignored) {
            System.out.println(ignored.getMessage());
        }
    }

    private void updateActiveElement(DomElement element) {
        if (element != null) {
            currentActiveElement_ = element;
        }
    }

    public void doubleClick(Coordinates elementCoordinates) {
        DomElement element = getElementForOperation(elementCoordinates);
        parent_.doubleClick(element);
    }

    void doubleClick(DomElement element) {

        moveOutIfNeeded(element);

        // Send the state of modifier keys to the dblClick method.
        try {
            element.dblClick(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed());
            updateActiveElement(element);
        } catch (IOException e) {
            // TODO(eran.mes): What should we do in case of error?
            e.printStackTrace();
        }
    }

    public void contextClick(Coordinates elementCoordinates) {
        DomElement element = getElementForOperation(elementCoordinates);

        moveOutIfNeeded(element);

        element.rightClick(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed());

        updateActiveElement(element);
    }

    public void mouseDown(Coordinates elementCoordinates) {
        DomElement element = getElementForOperation(elementCoordinates);
        parent_.mouseDown(element);
    }

    void mouseDown(DomElement element) {
        moveOutIfNeeded(element);

        element.mouseDown(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                MouseEvent.BUTTON_LEFT);

        updateActiveElement(element);
    }

    public void mouseUp(Coordinates elementCoordinates) {
        DomElement element = getElementForOperation(elementCoordinates);
        parent_.mouseUp(element);
    }

    void mouseUp(DomElement element) {
        moveOutIfNeeded(element);

        element.mouseUp(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                MouseEvent.BUTTON_LEFT);

        updateActiveElement(element);
    }

    public void mouseMove(Coordinates elementCoordinates) {
        DomElement element = (DomElement) elementCoordinates.getAuxiliary();
        parent_.mouseMove(element);
    }

    void mouseMove(DomElement element) {
        moveOutIfNeeded(element);

        updateActiveElement(element);
    }

    public void mouseMove(Coordinates where, long xOffset, long yOffset) {
        throw new UnsupportedOperationException("Moving to arbitrary X,Y coordinates not supported.");
    }
}
