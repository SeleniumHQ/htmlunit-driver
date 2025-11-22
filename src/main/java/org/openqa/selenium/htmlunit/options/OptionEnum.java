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

package org.openqa.selenium.htmlunit.options;

import java.util.Map;

/**
 * Represents an enumerated option that can be configured on a driver.
 * Provides methods to access the option's key, property name, type, default value,
 * and to apply, encode, or decode its value.
 * 
 * @author Scott Babcock
 */
public interface OptionEnum {

    /**
     * Returns the capability key associated with this option.
     *
     * @return the capability key
     */
    String getCapabilityKey();

    /**
     * Returns the property name for this option.
     *
     * @return the property name
     */
    String getPropertyName();

    /**
     * Returns the expected type of this option's value.
     *
     * @return the option type
     */
    Class<?> getOptionType();

    /**
     * Returns the default value of this option.
     *
     * @return the default value
     */
    Object getDefaultValue();

    /**
     * Determines if the given value matches this option's default value.
     *
     * @param value the value to check
     * @return {@code true} if the value is the default; {@code false} otherwise
     */
    boolean isDefaultValue(Object value);

    /**
     * Applies this option's value to the given options map.
     *
     * @param optionsMap the map of options to update
     */
    void applyPropertyTo(Map<String, Object> optionsMap);

    /**
     * Encodes a value into a format suitable for storing or sending to the driver.
     *
     * @param value the value to encode
     * @return the encoded value
     */
    Object encode(Object value);

    /**
     * Decodes a value from its stored or transmitted form back into the option's native type.
     *
     * @param value the value to decode
     * @return the decoded value
     */
    Object decode(Object value);
}
