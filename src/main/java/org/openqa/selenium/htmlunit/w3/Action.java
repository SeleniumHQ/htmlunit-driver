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

package org.openqa.selenium.htmlunit.w3;

import org.htmlunit.html.DomElement;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.HtmlUnitAction;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.KeyDownHtmlUnitAction;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.KeyUpHtmlUnitAction;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.PointerDownHtmlUnitAction;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.PointerMoveHtmlUnitAction;
import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor.PointerUpHtmlUnitAction;

/**
 * An action object constructed with arguments id, type, and subtype is an
 * object with property id set to id, type set to type and subtype set to
 * subtype. Specific action objects have further properties added by other
 * algorithms in this specification.
 *
 * @see <a href="https://www.w3.org/TR/webdriver/#dfn-action-object">action object</a>
 *
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
     * Ctor.
     *
     * @param id unique id
     * @param type the type
     * @param subtype the subtype
     */
    public Action(final String id, final String type, final String subtype) {
        id_ = id;
        type_ = type;
        subtype_ = subtype;
    }

    public Integer getDuration() {
        return duration_;
    }

    public void setDuration(final int duration) {
        duration_ = duration;
    }

    public String getValue() {
        return value_;
    }

    public void setValue(final String value) {
        value_ = value;
    }

    public String getPointerType() {
        return pointerType_;
    }

    public void setPointerType(final String pointerType) {
        pointerType_ = pointerType;
    }

    public Integer getButton() {
        return button_;
    }

    public void setButton(final int button) {
        button_ = button;
    }

    public DomElement getDomElement() {
        return domElement_;
    }

    public void setDomElement(final DomElement domElement) {
        domElement_ = domElement;
    }

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

        throw new RuntimeException("ups");
    }

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
