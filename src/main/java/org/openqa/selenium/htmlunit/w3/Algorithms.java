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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor;
import org.openqa.selenium.htmlunit.HtmlUnitWebElement;
import org.openqa.selenium.interactions.Sequence;

/**
 * To follow the spec as close as possible we have this collection of mehtods
 * and no object oriented design.
 *
 * @author Ronald Brill
 */
public final class Algorithms {

    /**
     * Private ctor because this class offers only static functions.
     */
    private Algorithms() {
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/webdriver/#dfn-extract-an-action-sequence">extract
     *      an action sequence</a>
     *
     * @param sequences the sequences
     * @return actions by tick
     */
    public static List<List<Action>> extractActionSequence(
            final Collection<Sequence> sequences /* InputState inputState, paramters */) {

        // Let actions by tick be an empty List.
        final List<List<Action>> actionsByTick = new ArrayList<>();

        // For each value action sequence corresponding to an indexed property in
        // actions:
        for (final Sequence sequence : sequences) {
            final Map<String, Object> actionSequence = sequence.encode();

            // Let source actions be the result of trying to process an input source action
            // sequence given input state and action sequence.
            final ArrayList<Action> sourceActions = processInputSourceActionSequence(actionSequence);

            // For each action in source actions:
            // Let i be the zero-based index of action in source actions.
            for (int i = 0; i < sourceActions.size(); i++) {
                final Action action = sourceActions.get(i);

                // If the length of actions by tick is less than i + 1, append a new List to
                // actions by tick.
                if (actionsByTick.size() < i + 1) {
                    actionsByTick.add(new ArrayList<>());
                }

                // Append action to the List at index i in actions by tick.
                actionsByTick.get(i).add(action);
            }
        }

        // Return success with data actions by tick.
        return actionsByTick;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/webdriver/#dfn-process-an-input-source-action-sequence">process
     *      an input source action sequence</a>
     */
    private static ArrayList<Action> processInputSourceActionSequence(final Map<String, Object> actionSequence) {
        // Let type be the result of getting a property named "type" from action
        // sequence.
        final String type = actionSequence.get("type").toString();

        // If type is not "key", "pointer", "wheel", or "none", return an error with
        // error code invalid argument.

        // Let id be the result of getting the property "id" from action sequence.
        final Object id = actionSequence.get("id");

        // If id is undefined or is not a String, return error with error code invalid
        // argument.

        // If type is equal to "pointer", let parameters data be the result of getting
        // the
        // property "parameters" from action sequence.
        // Then let parameters be the result of trying to process pointer parameters
        // with argument parameters data.
        Map<String, Object> parameters = null;
        if ("pointer".equals(type)) {
            final Map<String, Object> parametersData = (Map<String, Object>) actionSequence.get("parameters");
            parameters = processPointerParameters(parametersData);
        }

        // Let source be the result of trying to get or create an input source given
        // input state, type and id.
        // InputSource source = new InputSource(inputState, type, id);

        // If parameters is not undefined, then if its pointerType property is not equal
        // to
        // source’s subtype property, return an error with error code invalid argument.

        // Let action items be the result of getting a property named actions from
        // action sequence.
        final List<Map<String, Object>> actionItems = (List<Map<String, Object>>) actionSequence.get("actions");

        // If action items is not an Array, return error with error code invalid
        // argument.

        // Let actions be a new list.
        final ArrayList<Action> actions = new ArrayList<>();

        // For each action item in action items:
        for (final Map<String, Object> actionItem : actionItems) {
            // If action item is not an Object return error with error code invalid
            // argument.

            Action action = null;
            // If type is "none" let action be the result of trying to process a null action
            // with parameters id, and action item.
            if ("none".equals(type)) {
                action = processNullAction(id.toString(), actionItem);
            }

            // Otherwise, if type is "key" let action be the result of trying to process a
            // key action with parameters id, and action item.
            else if ("key".equals(type)) {
                action = processKeyAction(id.toString(), actionItem);
            }

            // Otherwise, if type is "pointer" let action be the result of trying to process
            // a pointer action with parameters id, parameters, and action item.
            else if ("pointer".equals(type)) {
                action = processPointerAction(id.toString(), parameters, actionItem);
            }

            // Otherwise, if type is "wheel" let action be the result of trying to process a
            // wheel action with parameters id, and action item.
            else if ("wheel".equals(type)) {
                action = processWheelAction(id.toString(), actionItem);
            }

            // Append action to actions.
            actions.add(action);
        }

        // Return success with data actions.
        return actions;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/webdriver/#dfn-process-pointer-parameters">process
     *      pointer parameters</a>
     */
    private static Map<String, Object> processPointerParameters(final Map<String, Object> parametersData) {
        // Let parameters be the default pointer parameters.
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("pointerType", "mouse");

        // If parameters data is undefined, return success with data parameters.
        if (parametersData == null) {
            return parameters;
        }

        // If parameters data is not an Object, return error with error code invalid
        // argument.

        // Let pointer type be the result of getting a property named pointerType from
        // parameters data.
        final Object pointerType = parametersData.get("pointerType");

        // If pointer type is not undefined:
        if (pointerType != null) {
            // If pointer type does not have one of the values "mouse", "pen", or "touch",
            // return error with error code invalid argument.

            // Set the pointerType property of parameters to pointer type.
            parameters.put("pointerType", pointerType);
        }

        // Return success with data parameters.
        return parameters;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/webdriver/#dfn-process-a-null-action">process a
     *      null action</a>
     */
    private static Action processNullAction(final String id, final Map<String, Object> actionItem) {
        // Let subtype be the result of getting a property named "type" from action
        // item.
        final String subtype = actionItem.get("type").toString();

        // If subtype is not "pause", return error with error code invalid argument.

        // Let action be an action object constructed with arguments id, "none", and
        // subtype.
        final Action action = new Action(id, "none", subtype);

        // Let result be the result of trying to process a pause action with arguments
        // action item and action.
        final Action result = processPauseAction(actionItem, action);

        // Return result.
        return result;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/webdriver/#dfn-process-a-key-action">process a
     *      key action</a>
     */
    private static Action processKeyAction(final String id, final Map<String, Object> actionItem) {
        // Let subtype be the result of getting a property named "type" from action
        // item.
        final String subtype = actionItem.get("type").toString();

        // If subtype is not one of the values "keyUp", "keyDown", or "pause",
        // return an error with error code invalid argument.

        // Let action be an action object constructed with arguments id, "key", and
        // subtype.
        final Action action = new Action(id, "key", subtype);

        // If subtype is "pause", let result be the result of trying to
        // process a pause action with arguments action item and action, and return
        // result.
        if ("pause".equals(subtype)) {
            final Action result = processPauseAction(actionItem, action);
            return result;
        }

        // Let key be the result of getting a property named value from action item.
        final Object key = actionItem.get("value");

        // If key is not a String containing a single unicode code point or grapheme
        // cluster? return error with error code invalid argument.

        // Set the value property on action to key.
        action.setValue(key.toString());

        // Return success with data action.
        return action;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/webdriver/#dfn-process-a-pointer-action">process
     *      a pointer action</a>
     */
    private static Action processPointerAction(final String id, final Map<String, Object> parameters,
            final Map<String, Object> actionItem) {
        // Let subtype be the result of getting a property named "type" from action
        // item.
        final String subtype = actionItem.get("type").toString();

        // If subtype is not one of the values "pause", "pointerUp", "pointerDown",
        // "pointerMove", or "pointerCancel",
        // return an error with error code invalid argument.

        // Let action be an action object constructed with arguments id, "pointer", and
        // subtype.
        final Action action = new Action(id, "pointer", subtype);

        // If subtype is "pause", let result be the result of trying to
        // process a pause action with arguments action item and action, and return
        // result.
        if ("pause".equals(subtype)) {
            final Action result = processPauseAction(actionItem, action);
            return result;
        }

        final Object origin = actionItem.get("origin");
        if (origin instanceof HtmlUnitWebElement) {
            final HtmlUnitWebElement webElement = (HtmlUnitWebElement) origin;
            action.setDomElement(webElement.getElement());
        }

        // Set the pointerType property of action equal to the pointerType property of
        // parameters.
        action.setPointerType(parameters.get("pointerType").toString());

        // If subtype is "pointerUp" or "pointerDown", process a pointer up or pointer
        // down action
        // with arguments action item and action.
        // If doing so results in an error, return that error.
        if ("pointerUp".equals(subtype) || "pointerDown".equals(subtype)) {
            processPointerUpOrPointerDownAction(actionItem, action);
        }

        // If subtype is "pointerMove" process a pointer move action with
        // arguments action item and action.
        // If doing so results in an error, return that error.
        if ("pointerMove".equals(subtype)) {
            processPointerMoveAction(actionItem, action);
        }

        // If subtype is "pointerCancel" process a pointer cancel action. If doing so
        // results in an error, return that error.
        if ("pointerCancel".equals(subtype)) {
            processPointerCancelAction(actionItem);
        }

        // Return success with data action.
        return action;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/webdriver/#dfn-process-a-wheel-action">process a
     *      wheel action</a>
     */
    private static Action processWheelAction(final String id, final Map<String, Object> actionItem) {
        return null;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/webdriver/#dfn-process-a-pointer-up-or-pointer-down-action">process
     *      a pointer up or pointer down action</a>
     */
    private static void processPointerUpOrPointerDownAction(final Map<String, Object> actionItem, final Action action) {
        // Let button be the result of getting the property button from action item.
        final Object button = actionItem.get("button");

        // If button is not an Integer greater than or equal to 0 return error with
        // error code invalid argument.
        if (button == null) {
            // TODO
        }
        try {
            final int butt = Integer.parseInt(button.toString());
            if (butt < 0) {
                // TODO
            }

            // Set the button property of action to button.
            action.setButton(butt);
        }
        catch (final NumberFormatException e) {
            // TODO
        }

        // Let width be the result of getting the property width from action item.
        //
        // If width is not undefined and width is not a Number greater than or equal to
        // 0 return error with error code invalid argument.
        //
        // Set the width property of action to width.
        //
        // Let height be the result of getting the property height from action item.
        //
        // If height is not undefined and height is not a Number greater than or equal
        // to 0 return error with error code invalid argument.
        //
        // Set the height property of action to height.
        //
        // Let pressure be the result of getting the property pressure from action item.
        //
        // If pressure is not undefined and pressure is not a Number greater than or
        // equal to 0 and less than or equal to 1 return error with error code invalid
        // argument.
        //
        // Set the pressure property of action to pressure.
        //
        // Let tangentialPressure be the result of getting the property
        // tangentialPressure from action item.
        //
        // If tangentialPressure is not undefined and tangentialPressure is not a Number
        // greater than or equal to -1 and less than or equal to 1 return error with
        // error code invalid argument.
        //
        // Set the tangentialPressure property of action to tangentialPressure.
        //
        // Let tiltX be the result of getting the property tiltX from action item.
        //
        // If tiltX is not undefined and tiltX is not an Integer greater than or equal
        // to -90 and less than or equal to 90 return error with error code invalid
        // argument.
        //
        // Set the tiltX property of action to tiltX.
        //
        // Let tiltY be the result of getting the property tiltY from action item.
        //
        // If tiltY is not undefined and tiltY is not an Integer greater than or equal
        // to -90 and less than or equal to 90 return error with error code invalid
        // argument.
        //
        // Set the tiltY property of action to tiltY.
        //
        // Let twist be the result of getting the property twist from action item.
        //
        // If twist is not undefined and twist is not an Integer greater than or equal
        // to 0 and less than or equal to 359 return error with error code invalid
        // argument.
        //
        // Set the twist property of action to twist.
        //
        // Let altitudeAngle be the result of getting the property altitudeAngle from
        // action item.
        //
        // If altitudeAngle is not undefined and altitudeAngle is not a Number greater
        // than or equal to 0 and less than or equal to π/2 return error with error code
        // invalid argument.
        //
        // Set the altitudeAngle property of action to altitudeAngle.
        //
        // Let azimuthAngle be the result of getting the property azimuthAngle from
        // action item.
        //
        // If azimuthAngle is not undefined and azimuthAngle is not a Number greater
        // than or equal to 0 and less than or equal to 2π return error with error code
        // invalid argument.
        //
        // Set the azimuthAngle property of action to azimuthAngle.
        //
        // Return success with data null.
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/webdriver/#dfn-process-a-pointer-move-action">process
     *      a pointer move action</a>
     */
    private static void processPointerMoveAction(final Map<String, Object> actionItem, final Action action) {
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/webdriver/#dfn-process-a-pointer-cancel-action">process
     *      a pointer cancel action</a>
     */
    private static void processPointerCancelAction(final Map<String, Object> actionItem) {
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/webdriver/#dfn-process-a-pause-action">process a
     *      pause action</a>
     */
    private static Action processPauseAction(final Map<String, Object> actionItem, final Action action) {
        // Let duration be the result of getting the property "duration" from action
        // item.
        final Object duration = actionItem.get("duration");

        // If duration is not undefined and duration is not an Integer greater than or
        // equal to 0,
        // return error with error code invalid argument.
        if (duration == null) {
            // TODO
        }
        try {
            final int dur = Integer.parseInt(duration.toString());
            if (dur < 0) {
                // TODO
            }

            // Set the duration property of action to duration.
            action.setDuration(dur);
        }
        catch (final NumberFormatException e) {
            // TODO
        }

        // Return success with data action.
        return action;
    }

    /**
     * @see <a href="https://www.w3.org/TR/webdriver/#dfn-dispatch-actions">dispatch
     *      actions</a>
     * @param actionsByTick actions by tick
     * @param inputProcessor the HtmlUnitInputProcessor
     */
    public static void dispatchActions(final List<List<Action>> actionsByTick,
            final HtmlUnitInputProcessor inputProcessor) {
        // Let token be a new unique identifier.
        // Enqueue token in session's actions queue.
        // Wait for token to be the first item in input state's actions queue.
        // Note
        // This ensures that only one set of actions can be run at a time, and therefore
        // different actions
        // commands using the same underlying state don't race. In a session that is
        // only a HTTP session
        // only one command can run at a time, so this will never block. But other
        // session types
        // can allow running multiple commands in parallel, in which case this is
        // necessary to ensure sequential access.
        // Let actions result be the result of dispatch actions inner with input state,
        // actions by tick, and browsing context
        // Dequeue input state's actions queue.
        // Assert: this returns token
        // Return actions result.

        for (final List<Action> actions : actionsByTick) {
            for (final Action action : actions) {
                inputProcessor.enqueuAction(action);
            }
        }

        inputProcessor.performActions();
    }
}
