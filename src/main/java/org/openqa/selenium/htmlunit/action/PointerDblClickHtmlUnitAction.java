package org.openqa.selenium.htmlunit.action;

import org.htmlunit.html.DomElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Coordinates;

/**
 * A synthesised double-click action produced by merging two successive
 * {@link PointerClickHtmlUnitAction}s on the same element.
 *
 * <p>Double-click actions are never merged further.</p>
 */
final class PointerDblClickHtmlUnitAction extends DomElementHtmlUnitAction {

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