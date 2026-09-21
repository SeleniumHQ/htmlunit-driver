package org.openqa.selenium.htmlunit.action;

import org.htmlunit.html.DomElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * An {@link HtmlUnitAction} that moves the pointer to the specified
 * {@link DomElement}. Updates the driver's simulated mouse position without
 * performing a click.
 *
 * <p>Mouse-move actions are never merged with adjacent actions.</p>
 */
public final class PointerMoveHtmlUnitAction extends DomElementHtmlUnitAction {

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