package org.openqa.selenium.htmlunit.action;

import org.htmlunit.html.DomElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Coordinates;

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
public final class PointerUpHtmlUnitAction extends PointerHtmlUnitAction {

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