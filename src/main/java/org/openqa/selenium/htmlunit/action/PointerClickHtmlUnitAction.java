package org.openqa.selenium.htmlunit.action;

import org.htmlunit.html.DomElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor;

/**
 * A synthesised click action produced by merging a pointer-down and a
 * pointer-up on the same element with the same button.
 *
 * <p>Two successive clicks on the same element (regardless of button) are
 * further merged into a {@link PointerDblClickHtmlUnitAction}.</p>
 */
final class PointerClickHtmlUnitAction extends PointerHtmlUnitAction {

    PointerClickHtmlUnitAction(final DomElement domElement, final int button) {
        super(domElement, button);
    }

    /**
     * Dispatches the click. Primary button (0) triggers a standard click;
     * secondary button (2) triggers a context-click. The auxiliary (middle)
     * button and any other button fall back to a standard click because
     * HtmlUnit's {@code Mouse} API does not expose a dedicated middle-click.
     */
    @Override
    public void process(final HtmlUnitDriver driver) {
        if (HtmlUnitInputProcessor.BUTTON_SECONDARY == getButton()) {
            driver.getMouse().contextClick(null);
            return;
        }
        // BUTTON_PRIMARY (0) and BUTTON_AUXILIARY (1) both map to click();
        // HtmlUnit has no dedicated middle-click API.
        driver.getMouse().click(null);
    }

    /**
     * Merges two successive clicks on the same element <em>with the same
     * button</em> into a double-click. The button must match to avoid
     * combining e.g. a right-click and a left-click into a left double-click.
     */
    @Override
    public HtmlUnitAction join(final HtmlUnitAction previousAction) {
        if (previousAction instanceof PointerClickHtmlUnitAction) {
            final PointerClickHtmlUnitAction pointerClickAction =
                    (PointerClickHtmlUnitAction) previousAction;
            if (pointerClickAction.getDomElement() == getDomElement()
                    && pointerClickAction.getButton() == getButton()) {
                return new PointerDblClickHtmlUnitAction(getDomElement());
            }
        }
        return this;
    }
}