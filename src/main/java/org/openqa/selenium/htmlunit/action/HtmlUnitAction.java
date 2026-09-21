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

package org.openqa.selenium.htmlunit.action;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Represents a low-level input action that can be executed by an
 * {@link HtmlUnitDriver}.
 *
 * <p>Implementations define how a specific input event (keyboard, mouse,
 * etc.) is dispatched and how it may be merged with a preceding action to
 * produce a higher-level event (e.g. down + up → click).</p>
 */
public interface HtmlUnitAction {

    /**
     * Dispatches this action through the given {@link HtmlUnitDriver}.
     *
     * @param driver the driver used to execute the action; never {@code null}
     */
    void process(HtmlUnitDriver driver);

    /**
     * Attempts to merge this action with the immediately preceding action.
     *
     * <p>If a meaningful merge is possible (e.g. a pointer-up following a
     * pointer-down on the same element with the same button can become a
     * click), implementations return a new {@link HtmlUnitAction} representing
     * the combined event. If no merge is possible, implementations return
     * {@code this}. The caller uses an identity check ({@code joinedAction
     * == this}) to detect the no-merge case.</p>
     *
     * @param previousAction the action immediately before this one in the
     *                       queue; never {@code null}
     * @return a merged {@link HtmlUnitAction} if a merge was possible,
     *         or {@code this} if no merge applies
     */
    HtmlUnitAction join(HtmlUnitAction previousAction);
}