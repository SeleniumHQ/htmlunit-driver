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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import org.htmlunit.BrowserVersion;
import org.htmlunit.BrowserVersion.BrowserVersionBuilder;
import org.htmlunit.ProxyConfig;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.TypeToken;

/**
 * @author Scott Babcock
 * @author Ronald Brill
 */
final class TypeCodec {

    private TypeCodec() {
        throw new AssertionError("TypeCodec is a static utility class that cannot be instantiated");
    }

    /** Specifier for {@code Map<String, Object>} input/output type */
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() { }.getType();

    /** Specifier for {@code List<String>} input/output type */
    private static final Type LIST_TYPE = new TypeToken<List<String>>() { }.getType();

    /**
     * Decode the specified value as a {@code boolean}.
     *
     * @param value value to be decoded
     * @return specified value decoded as {@code boolean}
     */
    static boolean decodeBoolean(final Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        throw new IllegalStateException("Specified value must be 'Boolean' or 'String'; was " + getClassName(value));
    }

    /**
     * Decode the specified value as an {@code int}.
     *
     * @param value value to be decoded
     * @return specified value decoded as {@code int}
     */
    static int decodeInt(final Object value) {
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        throw new IllegalStateException(
                "Specified value must be 'Long', 'Integer', or 'String'; was " + getClassName(value));
    }

    /**
     * Decode the specified value as a {@code long}.
     *
     * @param value value to be decoded
     * @return specified value decoded as {@code long}
     */
    static long decodeLong(final Object value) {
        if (value instanceof Long) {
            return ((Long) value).longValue();
        }
        if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        throw new IllegalStateException("Specified value must be 'Long' or 'String'; was " + getClassName(value));
    }

    /**
     * Decode the specified value as a {@code String}.
     *
     * @param value value to be decoded
     * @return specified value decoded as {@link String}
     */
    static String decodeString(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        throw new IllegalStateException("Specified value must be 'String'; was " + getClassName(value));
    }

    /**
     * Decode the specified value as a char[].
     *
     * @param value value to be decoded
     * @return specified value decoded as char[]
     */
    static char[] decodeCharArray(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof char[]) {
            return (char[]) value;
        }
        if (value instanceof String) {
            return ((String) value).toCharArray();
        }
        throw new IllegalStateException("Specified value must be 'char[]' or 'String'; was " + getClassName(value));
    }

    /**
     * Decode the specified value as a String[].
     *
     * @param value value to be decoded
     * @return specified value decoded as String[]
     */
    static String[] decodeStringArray(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String[]) {
            return (String[]) value;
        }
        if (value instanceof String) {
            final List<String> listOfStrings = new Json().toType((String) value, LIST_TYPE);
            return listOfStrings.toArray(new String[0]);
        }
        throw new IllegalStateException("Specified value must be 'String[]' or 'String'; was " + getClassName(value));
    }

    /**
     * Encode the specified {@code File} object.
     *
     * @param value {@link File} object to be encoded
     * @return encoded {@code File} object
     */
    static String encodeFile(final Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof File) {
            try {
                return ((File) value).getCanonicalPath();
            }
            catch (final IOException e) {
                throw new IllegalStateException("Failed encoding 'File' to canonical path", e);
            }
        }
        throw new IllegalStateException("Specified value must be 'File' or 'String'; was " + getClassName(value));
    }

    /**
     * Decode the specified value as a {@code File}.
     *
     * @param value value to be decoded
     * @return specified value decoded as {@link File}
     */
    static File decodeFile(final Object value) {
        if (value instanceof File) {
            return (File) value;
        }
        if (value instanceof String) {
            return new File((String) value);
        }
        throw new IllegalStateException("Specified value must be 'File' or 'String'; was " + getClassName(value));
    }

    /**
     * Encode the specified {@code InetAddress} object.
     *
     * @param value {@link InetAddress} object to be encoded
     * @return encoded {@code InetAddress} object
     */
    static String encodeInetAddress(final Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof InetAddress) {
            return ((InetAddress) value).getHostAddress();
        }
        throw new IllegalStateException(
                "Specified value must be 'InetAddress' or 'String'; was " + getClassName(value));
    }

    /**
     * Decode the specified value as an {@code InetAddress}.
     *
     * @param value value to be decoded
     * @return specified value decoded as {@link InetAddress}
     */
    static InetAddress decodeInetAddress(final Object value) {
        if (value instanceof InetAddress) {
            return (InetAddress) value;
        }
        if (value instanceof String) {
            try {
                return InetAddress.getByName((String) value);
            }
            catch (final UnknownHostException e) {
                throw new IllegalArgumentException("Failed decoding address: " + ((String) value), e);
            }
        }
        throw new IllegalStateException(
                "Specified value must be 'InetAddress' or 'String'; was " + getClassName(value));
    }

    static KeyStoreBean decodeKeyStore(final Object value) {
        if (value instanceof String) {
            final KeyStoreBean bean = new Json().toType((String) value, KeyStoreBean.class);
            Objects.requireNonNull(bean.getUrl(), "Client certificate store object omits [url] property");
            Objects.requireNonNull(bean.getType(), "Client certificate store object omits [type] property");
            return bean;
        }

        throw new IllegalStateException("Specified value must be 'String'; was " + getClassName(value));
    }

    /**
     * Encode the specified {@code ProxyConfig} object.
     *
     * @param value {@link ProxyConfig} object to be encoded
     * @return encoded {@code ProxyConfig} object
     */
    static Map<String, Object> encodeProxyConfig(final Object value) {
        if (value instanceof ProxyConfig) {
            return ProxyConfigBean.encodeProxyConfig((ProxyConfig) value);
        }
        throw new IllegalStateException("Specified value must be 'ProxyConfig'; was " + getClassName(value));
    }

    /**
     * Decode the specified value as a {@code ProxyConfig}.
     *
     * @param value value to be decoded
     * @return specified value decoded as {@link ProxyConfig}
     */
    static ProxyConfig decodeProxyConfig(final Object value) {
        final String json;

        if (value instanceof ProxyConfig) {
            return (ProxyConfig) value;
        }
        else if (value instanceof Map) {
            json = new Json().toJson(value);
        }
        else if (value instanceof String) {
            json = (String) value;
        }
        else {
            throw new IllegalStateException(
                    "Specified value must be 'ProxyConfig', 'Map', or 'String'; was " + getClassName(value));
        }

        final ProxyConfigBean bean = new Json().toType(json, ProxyConfigBean.class);
        return bean.build();
    }

    /**
     * Encode the specified {@code BrowserVersion} object.
     *
     * @param value {@link BrowserVersion} object to be encoded
     * @return encoded {@code BrowserVersion} object
     */
    static Map<String, Object> encodeBrowserVersion(final Object value) {
        if (value instanceof BrowserVersion) {
            final Map<String, Object> optionsMap = new HashMap<>();
            final BrowserVersion browserVersion = (BrowserVersion) value;
            for (final BrowserVersionTrait trait : BrowserVersionTrait.values()) {
                final Object traitValue = trait.obtain(browserVersion);
                if (!trait.isDefaultValue(traitValue)) {
                    optionsMap.put(trait.getCapabilityKey(), trait.encode(traitValue));
                }
            }
            return optionsMap;
        }
        throw new IllegalStateException("Specified value must be 'BrowserVersion'; was " + getClassName(value));
    }

    /**
     * Decode the specified value as a {@code BrowserVersion}.
     *
     * @param value value to be decoded
     * @return specified value decoded as {@link BrowserVersion}
     */
    @SuppressWarnings("unchecked")
    static BrowserVersion decodeBrowserVersion(final Object value) {
        final int code;
        final String name;
        final BrowserVersion seed;
        Map<String, Object> optionsMap = new HashMap<>();

        // if value spec'd
        if (value != null) {
            if (value instanceof BrowserVersion) {
                // encode BrowserVersion to options map
                optionsMap = encodeBrowserVersion(value);
            }
            else if (value instanceof Map) {
                // adopt specified options map
                optionsMap = (Map<String, Object>) value;
            }
            else if (value instanceof String) {
                // decode JSON value to options map
                optionsMap = new Json().toType((String) value, MAP_TYPE);
            }
            else {
                throw new IllegalStateException(
                        "Specified value must be 'BrowserVersion', 'Map', or 'String'; was " + getClassName(value));
            }
        }

        // apply specified system properties to options map
        for (final BrowserVersionTrait option : BrowserVersionTrait.values()) {
            option.applyPropertyTo(optionsMap);
        }

        // browser version numeric code is required
        final Object numericCode =  Objects.requireNonNull(
                optionsMap.get(BrowserVersionTrait.NUMERIC_CODE.getCapabilityKey()),
                "Required browser version trait [numericCode] is unspecified");

        if (numericCode instanceof Long) {
            code = ((Long) numericCode).intValue();
        }
        else if (numericCode instanceof Integer) {
            code = ((Integer) numericCode).intValue();
        }
        else {
            throw new IllegalStateException(
                    "Browser numeric code must be 'Long' or 'Integer'; was " + getClassName(numericCode));
        }

        // browser version nickname is required
        final Object nickname = Objects.requireNonNull(
                optionsMap.get(BrowserVersionTrait.NICKNAME.getCapabilityKey()),
                "Required browser version trait [nickname] is unspecified");

        if (nickname instanceof String) {
            name = (String) nickname;
        }
        else {
            throw new IllegalStateException("Browser nickname must be 'String'; was " + getClassName(nickname));
        }

        // create seed from spec'd name
        if (name.startsWith("Chrome")) {
            seed = BrowserVersion.CHROME;
        }
        else if (name.startsWith("Edge")) {
            seed = BrowserVersion.EDGE;
        }
        else if (name.startsWith("FF")) {
            if (code == BrowserVersion.FIREFOX_ESR.getBrowserVersionNumeric()) {
                seed = BrowserVersion.FIREFOX_ESR;
            }
            else {
                seed = BrowserVersion.FIREFOX;
            }
        }
        else {
            throw new IllegalArgumentException(
                    "Browser nickname must start with 'Chrome', 'Edge', or 'FF'; was: " + name);
        }

        // if spec'd numeric code overrides seed
        if (seed.getBrowserVersionNumeric() != code) {
            try {
                final Field browserVersionNumericField = BrowserVersion.class.getField("browserVersionNumeric_");
                browserVersionNumericField.setAccessible(true);
                browserVersionNumericField.set(seed, code);
            }
            catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // nothing to do here
            }
        }

        // create browser version builder from seed
        final BrowserVersionBuilder builder = new BrowserVersionBuilder(seed);

        // apply defined options map values to browser version builder
        for (final BrowserVersionTrait trait : BrowserVersionTrait.values()) {
            switch (trait) {
                case NUMERIC_CODE:
                case NICKNAME:
                    continue;
                default:
                    if (optionsMap.containsKey(trait.getCapabilityKey())) {
                        trait.apply(optionsMap.get(trait.getCapabilityKey()), builder);
                    }
            }
        }

        return builder.build();
    }

    /**
     * Encode the specified time zone object.
     *
     * @param value {@link TimeZone} object to be encoded
     * @return encoded {@code TimeZone} object
     */
    static String encodeTimeZone(final Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof TimeZone) {
            return ((TimeZone) value).getID();
        }
        throw new IllegalStateException("Specified value must be 'TimeZone' or 'String'; was " + getClassName(value));
    }

    /**
     * Decode the specified value as a {@code TimeZone}.
     *
     * @param value value to be decoded
     * @return specified value decoded as {@link TimeZone}
     */
    static TimeZone decodeTimeZone(final Object value) {
        if (value instanceof TimeZone) {
            return (TimeZone) value;
        }
        if (value instanceof String) {
            return TimeZone.getTimeZone((String) value);
        }
        throw new IllegalStateException("Specified value must be 'TimeZone' or 'String'; was " + getClassName(value));
    }

    /**
     * Get class name for the specified value.
     *
     * @param value value from which to get class name
     * @return class name for specified value; 'null' if value is {@code null}
     */
    static String getClassName(final Object value) {
        return (value != null) ? value.getClass().getName() : "'null'";
    }
}
