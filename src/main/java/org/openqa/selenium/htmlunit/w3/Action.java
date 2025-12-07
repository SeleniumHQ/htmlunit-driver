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

package org.openqa.selenium.htmlunit.w3;

import org.htmlunit.html.DomElement;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.HtmlUnitAction;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.KeyDownHtmlUnitAction;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.KeyUpHtmlUnitAction;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.PointerDownHtmlUnitAction;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.PointerMoveHtmlUnitAction;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.PointerUpHtmlUnitAction;

/**
 * An action object constructed with arguments {@code id}, {@code type}, and {@code subtype}
 * represents a single low-level WebDriver action as defined by the
 * <a href="https://www.w3.org/TR/webdriver/#actions">W3C WebDriver Actions specification</a>.
 * <p>
 * This class stores the raw action parameters (such as duration, key value, button, pointer type,
 * and target element) required to later synthesize an HtmlUnit-specific {@link HtmlUnitAction}.
 * <p>
 * The {@code type} field corresponds to a WebDriver input source type (e.g. {@code "pointer"},
 * {@code "key"}, {@code "none"}). The {@code subtype} indicates the specific action to execute
 * for that input source (e.g. {@code "pointerMove"}, {@code "pointerDown"}, {@code "keyUp"}).
 *
 * <p>For example:
 * <ul>
 *   <li>A pointer action with {@code subtype="pointerDown"} requires a pointer button.</li>
 *   <li>A key action with {@code subtype="keyDown"} requires a key value.</li>
 *   <li>Pause actions ({@code subtype="pause"}) contain only duration.</li>
 * </ul>
 *
 * <p>
 * After the action object is configured, {@link #buildHtmlUnitAction()} converts it to an
 * HtmlUnit-specific input action that can be processed by {@code HtmlUnitInputProcessor}.
 *
 * @see <a href="https://www.w3.org/TR/webdriver/#dfn-action-object">W3C Action Object definition</a>
 * @author Ronald Brill
 */
public class Action {
    private final String id_;
    private final String type_;
    private final String subtype_;

    private Integer duration_;
    private String value_;

    private String pointerType_;
    private Integer button_;

    private DomElement domElement_;

    /**
     * Creates a new WebDriver action object.
     *
     * @param id
     *      A unique identifier for the input source that this action belongs to.
     *      For example, the ID for a pointer device, keyboard, or virtual device.
     *
     * @param type
     *      The WebDriver action source type. Common values include:
     *      <ul>
     *        <li>{@code "pointer"} — mouse/touch/pen actions</li>
     *        <li>{@code "key"} — keyboard actions</li>
     *        <li>{@code "none"} — virtual device used only for pause actions</li>
     *      </ul>
     *
     * @param subtype
     *      The specific action subtype. Examples include:
     *      <ul>
     *        <li>{@code "pointerMove"}, {@code "pointerDown"}, {@code "pointerUp"}, {@code "pause"}</li>
     *        <li>{@code "keyDown"}, {@code "keyUp"}, {@code "pause"}</li>
     *      </ul>
     */
    public Action(final String id, final String type, final String subtype) {
        id_ = id;
        type_ = type;
        subtype_ = subtype;
    }

    /**
     * Returns the duration associated with this action.
     * <p>
     * Duration is used for pause actions or timed movements (e.g. {@code pointerMove}).
     *
     * @return duration in milliseconds, or {@code null} if no duration is defined
     */
    public Integer getDuration() {
        return duration_;
    }

    /**
     * Sets the duration associated with this action.
     *
     * @param duration
     *        Duration in milliseconds. Must be non-negative according to the W3C spec.
     */
    public void setDuration(final int duration) {
        duration_ = duration;
    }

    /**
     * Returns the key value associated with this action.
     * <p>
     * This applies only to keyboard actions such as {@code keyDown} or {@code keyUp}.
     * Values must be a single Unicode character or a WebDriver “special key”
     * (e.g. {@code "\uE007"} for Enter).
     *
     * @return the key value, or {@code null} if unset or not applicable
     */
    public String getValue() {
        return value_;
    }

    /**
     * Sets the key value for a keyboard action.
     *
     * @param value
     *      The key value to send. Must be a single Unicode code point or a WebDriver
     *      special key constant. Ignored for non-keyboard actions.
     */
    public void setValue(final String value) {
        value_ = value;
    }

    /**
     * Returns the pointer type for this action.
     * <p>
     * Applicable to pointer actions, this identifies the input device type:
     * <ul>
     *   <li>{@code "mouse"}</li>
     *   <li>{@code "touch"}</li>
     *   <li>{@code "pen"}</li>
     * </ul>
     *
     * @return the pointer type, or {@code null} if not set or not a pointer action
     */
    public String getPointerType() {
        return pointerType_;
    }

    /**
     * Sets the pointer type for this action.
     *
     * @param pointerType
     *        One of {@code "mouse"}, {@code "touch"}, or {@code "pen"}.
     *        Required for pointer actions such as {@code pointerDown}.
     */
    public void setPointerType(final String pointerType) {
        pointerType_ = pointerType;
    }

    /**
     * Returns the pointer button associated with a {@code pointerDown} or {@code pointerUp} action.
     * <p>
     * Button mapping follows the WebDriver spec:
     * <ul>
     *   <li><b>0</b> — left button</li>
     *   <li><b>1</b> — middle button</li>
     *   <li><b>2</b> — right button</li>
     * </ul>
     *
     * @return the pointer button, or {@code null} if unset or not applicable
     */
    public Integer getButton() {
        return button_;
    }

    /**
     * Sets the pointer button for {@code pointerDown} or {@code pointerUp}.
     *
     * @param button
     *        Must be 0, 1, or 2 as defined by the WebDriver Actions specification.
     */
    public void setButton(final int button) {
        button_ = button;
    }

    /**
     * Returns the DOM element associated with this action, if any.
     * <p>
     * Pointer actions such as {@code pointerMove}, {@code pointerDown}, and {@code pointerUp}
     * may target a specific element as the “origin” of the action.
     *
     * @return the target {@link DomElement}, or {@code null} if not applicable
     */
    public DomElement getDomElement() {
        return domElement_;
    }

    /**
     * Sets the DOM element associated with this action.
     * <p>
     * For pointer actions, this element becomes the action’s origin.
     *
     * @param domElement the element associated with this action
     */
    public void setDomElement(final DomElement domElement) {
        domElement_ = domElement;
    }

    /**
     * Converts this WebDriver action object into an HtmlUnit-specific input action.
     * <p>
     * The result depends on the action {@code type} and {@code subtype}:
     *
     * <strong>Pointer actions</strong>
     * <ul>
     *   <li>{@code pointerMove} → {@link PointerMoveHtmlUnitAction}</li>
     *   <li>{@code pointerDown} → {@link PointerDownHtmlUnitAction}</li>
     *   <li>{@code pointerUp} → {@link PointerUpHtmlUnitAction}</li>
     *   <li>{@code pause} → {@code null}</li>
     * </ul>
     *
     * <strong>Keyboard actions</strong>
     * <ul>
     *   <li>{@code keyDown} → {@link KeyDownHtmlUnitAction}</li>
     *   <li>{@code keyUp} → {@link KeyUpHtmlUnitAction}</li>
     *   <li>{@code pause} → {@code null}</li>
     * </ul>
     *
     * <strong>None actions</strong>
     * <ul>
     *   <li>Always return {@code null}</li>
     * </ul>
     *
     * @return a constructed {@link HtmlUnitAction}, or {@code null} if the action represents
     *         a pause or a “none” action
     *
     * @throws RuntimeException
     *         if the type or subtype are unrecognized or unsupported
     */
    public HtmlUnitAction buildHtmlUnitAction() {

        if ("none".equals(type_)) {
            return null;
        }

        if ("pointer".equals(type_)) {
            if ("pause".equals(subtype_)) {
                return null;
            }

            if ("pointerMove".equals(subtype_)) {
                return new PointerMoveHtmlUnitAction(domElement_);
            }

            if ("pointerUp".equals(subtype_)) {
                return new PointerUpHtmlUnitAction(domElement_, getButton());
            }

            if ("pointerDown".equals(subtype_)) {
                return new PointerDownHtmlUnitAction(domElement_, getButton());
            }
        }

        if ("key".equals(type_)) {
            if ("pause".equals(subtype_)) {
                return null;
            }

            if ("keyUp".equals(subtype_)) {
                return new KeyUpHtmlUnitAction(value_);
            }

            if ("keyDown".equals(subtype_)) {
                return new KeyDownHtmlUnitAction(value_);
            }
        }

        throw new RuntimeException("Unsupported action type/subtype combination: "
                                   + type_ + "/" + subtype_);
    }

    /**
     * Returns a human-readable string describing this action, including all
     * parameters that have been set.
     *
     * @return a descriptive string representation of this action
     */
    @Override
    public String toString() {
        String toString = "Action: id=" + id_ + " type=" + type_ + " subtype=" + subtype_;

        if (duration_ != null) {
            toString += " duration=" + duration_;
        }
        if (value_ != null) {
            toString += " value=" + value_;
        }
        if (pointerType_ != null) {
            toString += " pointerType=" + pointerType_;
        }
        if (button_ != null) {
            toString += " button=" + button_;
        }
        return toString;
    }
}
