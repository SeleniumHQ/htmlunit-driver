package org.openqa.selenium.htmlunit.action;

import org.htmlunit.html.DomElement;

/**
 * Base class for actions that target a specific {@link DomElement}.
 */
abstract class DomElementHtmlUnitAction implements HtmlUnitAction {
    private final DomElement domElement_;

    DomElementHtmlUnitAction(final DomElement domElement) {
        domElement_ = domElement;
    }

    /**
     * Returns the {@link DomElement} targeted by this action.
     *
     * @return the target element; may be {@code null} if constructed without one
     */
    protected DomElement getDomElement() {
        return domElement_;
    }
}