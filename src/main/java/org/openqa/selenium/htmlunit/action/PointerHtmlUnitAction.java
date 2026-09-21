package org.openqa.selenium.htmlunit.action;

import org.htmlunit.html.DomElement;

/**
 * Base class for pointer actions that operate on a specific mouse button.
 * Subclasses represent concrete pointer events such as press or release.
 */
public abstract class PointerHtmlUnitAction extends DomElementHtmlUnitAction {
    private final int button_;

    /**
     * Creates a new pointer action for the specified element and button.
     *
     * @param domElement the target element associated with the pointer event;
     *                   must not be {@code null}
     * @param button     the W3C button index (0 = primary/left, 1 = auxiliary/middle,
     *                   2 = secondary/right)
     */
    public PointerHtmlUnitAction(final DomElement domElement, final int button) {
        super(domElement);
        button_ = button;
    }

    /**
     * Returns the W3C button index associated with this action.
     *
     * @return 0 for primary, 1 for auxiliary, 2 for secondary
     */
    public int getButton() {
        return button_;
    }
}