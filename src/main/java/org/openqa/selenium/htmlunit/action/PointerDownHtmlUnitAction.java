package org.openqa.selenium.htmlunit.action;

import org.htmlunit.html.DomElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Coordinates;

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
public final class PointerDownHtmlUnitAction extends PointerHtmlUnitAction {

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