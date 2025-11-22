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
 * Provides basic mouse interaction support for {@link HtmlUnitDriver}.
 * <p>
 * This class handles element activation, focus changes, and simple event
 * dispatching needed to emulate mouse behavior within HtmlUnit's DOM model.
 * It is typically used internally by the HtmlUnit-backed WebDriver
 * implementation and is not intended for direct user instantiation.
 *
 * @author Simon Stewart
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Martin Bartoš
 */
public class HtmlUnitMouse {

    /**
     * The parent {@link HtmlUnitDriver} that owns this mouse instance.
     * Used to resolve the current page, manage focus, and coordinate
     * interactions with other input devices.
     */
    private final HtmlUnitDriver parent_;

    /**
     * The keyboard device associated with this mouse. Certain actions,
     * such as combined mouse–keyboard interactions, may rely on this
     * instance for modifier key state and related behavior.
     */
    private final HtmlUnitKeyboard keyboard_;

    /**
     * The element that is currently considered "active" for mouse
     * interactions. This may represent the last element clicked or
     * focused. May be {@code null} if no element has been engaged.
     */
    private DomElement currentActiveElement_;

    /**
     * Creates a new {@link HtmlUnitMouse} bound to the given
     * {@link HtmlUnitDriver} and {@link HtmlUnitKeyboard}.
     *
     * @param parent   the owning driver instance; must not be {@code null}
     * @param keyboard the keyboard device used to coordinate input state;
     *                 must not be {@code null}
     */
    public HtmlUnitMouse(final HtmlUnitDriver parent, final HtmlUnitKeyboard keyboard) {
        parent_ = parent;
        keyboard_ = keyboard;
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

    /**
     * Performs a mouse click on the element represented by the given
     * {@link Coordinates}.
     * <p>
     * The coordinates are resolved into the corresponding {@link DomElement},
     * which is then clicked using the parent {@link HtmlUnitDriver}. This
     * method triggers the standard HtmlUnit click behavior, including event
     * dispatch and potential navigation.
     *
     * @param elementCoordinates the coordinates identifying the target element;
     *                           may not be {@code null}
     * @throws NoSuchElementException if no corresponding element can be resolved
     *                                from the provided coordinates
     */
    public void click(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.click(element, false);
    }

    /**
     * Performs a click operation on the given {@link DomElement}, optionally
     * treating the action as a “direct” click. This method emulates HtmlUnit’s
     * mouse interaction model, including hover events, focus updates, and the
     * application of keyboard modifiers.
     * <p>
     * Before clicking, the element is validated for visibility and an appropriate
     * mouse-out/mouse-over sequence is generated as needed. Modifier keys
     * (Shift, Ctrl, Alt) are derived from the associated {@link HtmlUnitKeyboard}.
     *
     * @param element     the target DOM element to be clicked; must not be {@code null}
     * @param directClick whether the click should be treated as a direct action,
     *                    bypassing certain interaction conditions; used primarily
     *                    for {@link HtmlOption} elements:
     *                    <ul>
     *                        <li>{@code true} for {@link WebElement#click()} or</li>
     *                        <li>{@code false} for {@link Actions#click()}</li>
     *                    </ul>
     *
     * @throws ElementNotInteractableException if the element is not displayed
     * @throws TimeoutException if the underlying click triggers a network timeout
     * @throws WebDriverException if an {@link IOException} occurs while
     *                            dispatching the click
     * @throws RuntimeException for other unexpected runtime errors
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

    /**
     * Performs a double-click action on the element identified by the given
     * {@link Coordinates}. The coordinates are resolved to a {@link DomElement},
     * and the click is forwarded to the parent {@link HtmlUnitDriver}.
     *
     * @param elementCoordinates the coordinates representing the target element;
     *                           must not be {@code null}
     * @throws NoSuchElementException if no corresponding element can be resolved
     */
    public void doubleClick(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.doubleClick(element);
    }

    /**
     * Performs a double-click on the specified {@link DomElement}, including
     * hover-out behavior if necessary and dispatch of modifier-key state.
     *
     * @param element the element to be double-clicked; must not be {@code null}
     */
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

    /**
     * Performs a context (right) click on the element identified by the given
     * {@link Coordinates}. The element is resolved and then interacted with
     * using HtmlUnit’s right-click event dispatch.
     *
     * @param elementCoordinates the coordinates of the element to right-click;
     *                           must not be {@code null}
     */
    public void contextClick(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);

        moveOutIfNeeded(element);

        element.rightClick(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed());

        updateActiveElement(element);
    }

    /**
     * Performs a mouse-down (button press) action on the element indicated by
     * the given {@link Coordinates}. The resolved element is forwarded to the
     * driver for processing.
     *
     * @param elementCoordinates the coordinates identifying the target element;
     *                           must not be {@code null}
     */
    public void mouseDown(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.mouseDown(element);
    }

    /**
     * Performs a mouse-down event on the given {@link DomElement}, including
     * necessary mouse-out behavior and modifier-key state propagation.
     *
     * @param element the element on which to dispatch the mouse-down event;
     *                must not be {@code null}
     */
    void mouseDown(final DomElement element) {
        moveOutIfNeeded(element);

        element.mouseDown(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                MouseEvent.BUTTON_LEFT);

        updateActiveElement(element);
    }

    /**
     * Performs a mouse-up (button release) action on the element indicated by
     * the given {@link Coordinates}. The resolved element is delegated to the
     * driver for handling.
     *
     * @param elementCoordinates the coordinates identifying the target element;
     *                           must not be {@code null}
     */
    public void mouseUp(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.mouseUp(element);
    }

    /**
     * Performs a mouse-up event on the specified {@link DomElement}, including
     * hover-out handling and modifier-key propagation.
     *
     * @param element the element to dispatch the mouse-up event to; must not be {@code null}
     */
    void mouseUp(final DomElement element) {
        moveOutIfNeeded(element);

        element.mouseUp(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                MouseEvent.BUTTON_LEFT);

        updateActiveElement(element);
    }

    /**
     * Moves the mouse cursor to the element referenced by the specified
     * {@link Coordinates}. The element is obtained from the coordinates and
     * delegated to the driver.
     *
     * @param elementCoordinates the coordinates whose auxiliary object identifies
     *                           the target element; must not be {@code null}
     */
    public void mouseMove(final Coordinates elementCoordinates) {
        final DomElement element = (DomElement) elementCoordinates.getAuxiliary();
        parent_.mouseMove(element);
    }

    /**
     * Moves the mouse cursor to the specified {@link DomElement}, performing
     * any necessary mouse-out behavior and marking the element as active.
     *
     * @param element the element to move the mouse to; must not be {@code null}
     */
    void mouseMove(final DomElement element) {
        moveOutIfNeeded(element);

        updateActiveElement(element);
    }

    /**
     * Moves the mouse to the specified coordinates with an additional X/Y offset.
     * <p>
     * HtmlUnit does not support arbitrary coordinate-based mouse positioning, so
     * this operation is not available.
     *
     * @param where   the base coordinates; ignored
     * @param xOffset the horizontal offset; ignored
     * @param yOffset the vertical offset; ignored
     * @throws UnsupportedOperationException always, as coordinate-based movement
     *                                       is not supported
     */
    public void mouseMove(final Coordinates where, final long xOffset, final long yOffset) {
        throw new UnsupportedOperationException("Moving to arbitrary X,Y coordinates not supported.");
    }
}
