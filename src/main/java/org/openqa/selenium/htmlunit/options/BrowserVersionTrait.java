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

package org.openqa.selenium.htmlunit.options;

import java.util.Map;
import java.util.TimeZone;
import org.htmlunit.BrowserVersion;
import org.htmlunit.BrowserVersion.BrowserVersionBuilder;

public enum BrowserVersionTrait implements BrowserVersionTraitNames, OptionEnum {
    /**
     * Returns the numeric code for the browser represented by this <b>BrowserVersion</b>.
     * <p>
     * property: <b>webdriver.htmlunit.numericCode</b><br>
     * type: {@code int}<br>
     * default: {@code 0}
     */
    NUMERIC_CODE(optNumericCode, int.class, 0) {
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getBrowserVersionNumeric();
        }
    },
    
    /**
     * Returns the nickname for the browser represented by this <b>BrowserVersion</b>.
     * <p>
     * property: <b>webdriver.htmlunit.nickname</b><br>
     * type: {@code String}<br>
     * default: {@code null}
     */
    NICKNAME(optNickname, String.class, null) {
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getNickname();
        }
    },
    
    /**
     * Returns the application version, for example "4.0 (compatible; MSIE 6.0b; Windows 98)".
     * <p>
     * property: <b>webdriver.htmlunit.applicationVersion</b><br>
     * type: {@code String}<br>
     * default: {@code null}<br>
     * see: <a href="http://msdn.microsoft.com/en-us/library/ms533080.aspx">MSDN documentation</a>
     */
    APPLICATION_VERSION(optApplicationVersion, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setApplicationVersion(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getApplicationVersion();
        }
    },
    
    /**
     * Returns the user agent string, for example "Mozilla/4.0 (compatible; MSIE 6.0b; Windows 98)".
     * <p>
     * property: <b>webdriver.htmlunit.userAgent</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    USER_AGENT(optUserAgent, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setUserAgent(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getUserAgent();
        }
    },

    /**
     * Returns the application name, for example "Microsoft Internet Explorer".
     * <p>
     * property: <b>webdriver.htmlunit.applicationName</b><br>
     * type: {@code String}<br>
     * default: {@code null}<br>
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms533079.aspx">MSDN documentation</a>
     */
    APPLICATION_NAME(optApplicationName, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setApplicationName(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getApplicationName();
        }
    },
    
    /**
     * Returns the application code name, for example "Mozilla".
     * <p>
     * property: <b>webdriver.htmlunit.applicationCodeName</b><br>
     * type: {@code String}<br>
     * default: "Mozilla"<br>
     * see: <a href="http://msdn.microsoft.com/en-us/library/ms533077.aspx">MSDN documentation</a>
     */
    APPLICATION_CODE_NAME(optApplicationCodeName, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setApplicationCodeName(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getApplicationCodeName();
        }
    },
    
    /**
     * Returns the application minor version, for example "0".
     * <p>
     * property: <b>webdriver.htmlunit.applicationMinorVersion</b><br>
     * type: {@code String}<br>
     * default: "0"<br>
     * see: <a href="http://msdn.microsoft.com/en-us/library/ms533078.aspx">MSDN documentation</a>
     */
    APPLICATION_MINOR_VERSION(optApplicationMinorVersion, String.class, "0") {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setApplicationMinorVersion(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getApplicationMinorVersion();
        }
    },
    
    /**
     * Returns the browser vendor, for example "Google Inc.".
     * <p>
     * property: <b>webdriver.htmlunit.vendor</b><br>
     * type: {@code String}<br>
     * default: ""
     */
    VENDOR(optVendor, String.class, "") {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setVendor(TypeCodec.decodeString(value));            
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getVendor();
        }
    },

    /**
     * Returns the browser language, for example "en-US".
     * <p>
     * property: <b>webdriver.htmlunit.browserLanguage</b><br>
     * type: {@link String}<br>
     * default: "en_US"
     */
    BROWSER_LANGUAGE(optBrowserLanguage, String.class, "en-US") {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setBrowserLanguage(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getBrowserLanguage();
        }
    },

    /**
     * Returns {@code true} if the browser is currently online.
     * <p>
     * property: <b>webdriver.htmlunit.isOnline</b><br>
     * type: {@code boolean}<br>
     * default: {@code true}<br>
     * see: <a href="http://msdn.microsoft.com/en-us/library/ms534307.aspx">MSDN documentation</a>
     */
    IS_ONLINE(optIsOnline, boolean.class, true) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setOnLine(TypeCodec.decodeBoolean(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.isOnLine();
        }
    },

    /**
     * Returns the platform on which the application is running, for example "Win32".
     * <p>
     * property: <b>webdriver.htmlunit.platform</b><br>
     * type: {@code String}<br>
     * default: "Win32"<br>
     * see: <a href="http://msdn.microsoft.com/en-us/library/ms534340.aspx">MSDN documentation</a>
     */
    PLATFORM(optPlatform, String.class, "Win32") {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setPlatform(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getPlatform();
        }
    },

    /**
     * Returns the system {@link TimeZone}.
     * <p>
     * property: <b>webdriver.htmlunit.systemTimezone</b><br>
     * type: {@link TimeZone}<br>
     * default: TimeZone.getTimeZone("America/New_York")
     */
    SYSTEM_TIMEZONE(optSystemTimezone, TimeZone.class, TimeZone.getTimeZone("America/New_York")) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setSystemTimezone(TypeCodec.decodeTimeZone(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getSystemTimezone();
        }
    },

    /**
     * Returns the value used by the browser for the {@code Accept-Encoding} header.
     * <p>
     * property: <b>webdriver.htmlunit.acceptEncodingHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    ACCEPT_ENCODING_HEADER(optAcceptEncodingHeader, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setAcceptEncodingHeader(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getAcceptEncodingHeader();
        }
    },

    /**
     * Returns the value used by the browser for the {@code Accept-Language} header.
     * <p>
     * property: <b>webdriver.htmlunit.acceptLanguageHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    ACCEPT_LANGUAGE_HEADER(optAcceptLanguageHeader, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setAcceptLanguageHeader(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getAcceptLanguageHeader();
        }
    },

    /**
     * Returns the value used by the browser for the {@code Accept} header if requesting a page.
     * <p>
     * property: <b>webdriver.htmlunit.htmlAcceptHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    HTML_ACCEPT_HEADER(optHtmlAcceptHeader, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setHtmlAcceptHeader(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getHtmlAcceptHeader();
        }
    },

    /**
     * Returns the value used by the browser for the {@code Accept} header
     * if requesting an image.
     * <p>
     * property: <b>webdriver.htmlunit.imgAcceptHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    IMG_ACCEPT_HEADER(optImgAcceptHeader, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setImgAcceptHeader(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getImgAcceptHeader();
        }
    },

    /**
     * Returns the value used by the browser for the {@code Accept} header
     * if requesting a CSS declaration.
     * <p>
     * property: <b>webdriver.htmlunit.cssAcceptHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    CSS_ACCEPT_HEADER(optCssAcceptHeader, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setCssAcceptHeader(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getCssAcceptHeader();
        }
    },

    /**
     * Returns the value used by the browser for the {@code Accept} header
     * if requesting a script.
     * <p>
     * property: <b>webdriver.htmlunit.scriptAcceptHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    SCRIPT_ACCEPT_HEADER(optScriptAcceptHeader, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setScriptAcceptHeader(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getScriptAcceptHeader();
        }
    },

    /**
     * Returns the value used by the browser for the {@code Accept} header
     * if performing an XMLHttpRequest.
     * <p>
     * property: <b>webdriver.htmlunit.xmlHttpRequestAcceptHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    XML_HTTP_REQUEST_ACCEPT_HEADER(optXmlHttpRequestAcceptHeader, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setXmlHttpRequestAcceptHeader(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getXmlHttpRequestAcceptHeader();
        }
    },

    /**
     * Returns the value used by the browser for the {@code Sec-CH-UA} header.
     * <p>
     * property: <b>webdriver.htmlunit.secClientHintUserAgentHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    SEC_CLIENT_HINT_USER_AGENT_HEADER(optSecClientHintUserAgentHeader, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setSecClientHintUserAgentHeader(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getSecClientHintUserAgentHeader();
        }
    },

    /**
     * Returns the value used by the browser for the {@code Sec-CH-UA-Platform} header.
     * <p>
     * property: <b>webdriver.htmlunit.secClientHintUserAgentPlatformHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    SEC_CLIENT_HINT_USER_AGENT_PLATFORM_HEADER(optSecClientHintUserAgentPlatformHeader, String.class, null) {
        @Override
        public void apply(final Object value, final BrowserVersionBuilder builder) {
            builder.setSecClientHintUserAgentPlatformHeader(TypeCodec.decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getSecClientHintUserAgentPlatformHeader();
        }
    };

    public final String key;
    public final String name;
    public final Class<?> type;
    public final Object initial;
    
    BrowserVersionTrait(final String key, final Class<?> type, final Object initial) {
        this.key = key;
        this.name = "webdriver.htmlunit.browserVersionTrait." + key;
        this.type = type;
        this.initial = initial;
    }
    
    @Override
    public String getCapabilityKey() {
        return key;
    }
    
    @Override
    public String getPropertyName() {
        return name;
    }
    
    @Override
    public Class<?> getOptionType() {
        return type;
    }
    
    @Override
    public Object getDefaultValue() {
        return initial;
    }
    
    /**
     * Determine if the specified value matches the default for this trait.
     * 
     * @param value value to be evaluated
     * @return {@code true} if specified value matches the default value; otherwise {@code false}
     */
    @Override
    public boolean isDefaultValue(final Object value) {
        if (initial == null) return value == null;
        if (value == null) return false;
        return value.equals(initial);
    }
    
    /**
     * Apply the value of the system property associated with this trait to the specified options map.
     * 
     * @param optionsMap browser version options map
     */
    @Override
    public
    void applyPropertyTo(Map<String, Object> optionsMap) {
        String value = System.getProperty(name);
        if (value != null) {
            optionsMap.put(key, decode(value));
            System.clearProperty(key);
        }
    }
    
    /**
     * Encode the specified value according to the type of this trait.
     * 
     * @param value value to be encoded
     * @return trait-specific encoding for specified value
     */
    @Override
    public Object encode(final Object value) {
        switch (this.type.getName()) {
        case "boolean":
        case "int":
        case "java.lang.String":
            return value;
        case "java.util.TimeZone":
            return TypeCodec.encodeTimeZone(value);
        }
        throw new IllegalStateException(
                String.format("Unsupported type '%s' specified for option [%s]; value is of type: %s",
                this.type.getName(), this.toString(), TypeCodec.getClassName(value)));
    }
    
    /**
     * Decode the specified value according to the type of this trait.
     * 
     * @param value value to be decoded
     * @return trait-specific decoding for specified value
     */
    @Override
    public Object decode(final Object value) {
        switch (this.type.getName()) {
        case "boolean":
            return TypeCodec.decodeBoolean(value);
        case "int":
            return TypeCodec.decodeInt(value);
        case "java.lang.String":
            return TypeCodec.decodeString(value);
        case "java.util.TimeZone":
            return TypeCodec.decodeTimeZone(value);
        }
        throw new IllegalStateException(
                String.format("Unsupported type '%s' specified for option [%s]; value is of type: %s",
                this.type.getName(), this.toString(), TypeCodec.getClassName(value)));
    }
    
    /**
     * Apply the specified value for this trait into the provided browser version builder.
     * 
     * @param value value to be inserted
     * @param builder {@link BrowserVersionBuilder} object
     */
    public void apply(final Object value, final BrowserVersionBuilder builder) {
        throw new UnsupportedOperationException(
                String.format("Trait '%s' does not support value insertion", this.toString()));
    }
    
    /**
     * Obtain the value for this trait from the specified browser version object.
     * 
     * @param version {@link BrowserVersion} object
     * @return value for this trait
     */
    public Object obtain(final BrowserVersion version) {
        return null;
    }
    
    public static BrowserVersionTrait fromCapabilityKey(final String key) {
        for (BrowserVersionTrait trait : BrowserVersionTrait.values()) {
            if (trait.key.equals(key)) {
                return trait;
            }
        }
        return null;
    }
    
    public static BrowserVersionTrait fromPropertyName(final String name) {
        for (BrowserVersionTrait trait : BrowserVersionTrait.values()) {
            if (trait.name.equals(name)) {
                return trait;
            }
        }
        return null;
    }
}
