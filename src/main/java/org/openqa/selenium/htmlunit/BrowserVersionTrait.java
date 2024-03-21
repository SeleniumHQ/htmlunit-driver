package org.openqa.selenium.htmlunit;

import java.util.TimeZone;
import org.htmlunit.BrowserVersion;
import org.htmlunit.BrowserVersion.BrowserVersionBuilder;

public enum BrowserVersionTrait {
    /**
     * Returns the numeric code for the browser represented by this <b>BrowserVersion</b>.
     * <p>
     * property: <b>webdriver.htmlunit.numericCode</b><br>
     * type: {@code int}<br>
     * default: {@code 0}
     */
    NUMERIC_CODE("numericCode", int.class, 0) {
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
    NICKNAME("nickname", String.class, null) {
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
    APPLICATION_VERSION("applicationVersion", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setApplicationVersion(decodeString(value));
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
    USER_AGENT("userAgent", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setUserAgent(decodeString(value));
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
    APPLICATION_NAME("applicationName", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setApplicationName(decodeString(value));
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
    APPLICATION_CODE_NAME("applicationCodeName", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setApplicationCodeName(decodeString(value));
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
    APPLICATION_MINOR_VERSION("applicationMinorVersion", String.class, "0") {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setApplicationMinorVersion(decodeString(value));
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
    VENDOR("vendor", String.class, "") {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setVendor(decodeString(value));            
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
    BROWSER_LANGUAGE("browserLanguage", String.class, "en-US") {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setBrowserLanguage(decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getBrowserLanguage();
        }
    },

    /**
     * Returns the type of CPU in the machine, for example "x86".
     * <p>
     * property: <b>webdriver.htmlunit.cpuClass</b><br>
     * type: {@code String}<br>
     * default: "x86"<br>
     * see: <a href="http://msdn.microsoft.com/en-us/library/ms533697.aspx">MSDN documentation</a>
     */
    CPU_CLASS("cpuClass", String.class, "x86") {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setCpuClass(decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getCpuClass();
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
    IS_ONLINE("isOnline", boolean.class, true) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setOnLine(decodeBoolean(value));
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
    PLATFORM("platform", String.class, "Win32") {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setPlatform(decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getPlatform();
        }
    },

    /**
     * Returns the system language, for example "en-US".
     * <p>
     * property: <b>webdriver.htmlunit.systemLanguage</b><br>
     * type: {@code String}<br>
     * default: "en-US"<br>
     * see: <a href="http://msdn.microsoft.com/en-us/library/ms534653.aspx">MSDN documentation</a>
     */
    SYSTEM_LANGUAGE("systemLanguage", String.class, "en-US") {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setSystemLanguage(decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getSystemLanguage();
        }
    },

    /**
     * Returns the system {@link TimeZone}.
     * <p>
     * property: <b>webdriver.htmlunit.systemTimezone</b><br>
     * type: {@link TimeZone}<br>
     * default: TimeZone.getTimeZone("America/New_York")
     */
    SYSTEM_TIMEZONE("systemTimezone", TimeZone.class, TimeZone.getTimeZone("America/New_York")) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setSystemTimezone(decodeTimeZone(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getSystemTimezone();
        }
    },

    /**
     * Returns the user language, for example "en-US".
     * <p>
     * property: <b>webdriver.htmlunit.userLanguage</b><br>
     * type: {@link String}<br>
     * default: "en-US"<br>
     * @see <a href="http://msdn.microsoft.com/en-us/library/ms534713.aspx">MSDN documentation</a>
     */
    USER_LANGUAGE("userLanguage", String.class, "en-US") {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setUserLanguage(decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getUserLanguage();
        }
    },

    /**
     * Returns the value used by the browser for the {@code Accept_Encoding} header.
     * <p>
     * property: <b>webdriver.htmlunit.acceptEncodingHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    ACCEPT_ENCODING_HEADER("acceptEncodingHeader", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setAcceptEncodingHeader(decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getAcceptEncodingHeader();
        }
    },

    /**
     * Returns the value used by the browser for the {@code Accept_Language} header.
     * <p>
     * property: <b>webdriver.htmlunit.acceptLanguageHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    ACCEPT_LANGUAGE_HEADER("acceptLanguageHeader", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setAcceptLanguageHeader(decodeString(value));
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
    HTML_ACCEPT_HEADER("htmlAcceptHeader", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setHtmlAcceptHeader(decodeString(value));
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
    IMG_ACCEPT_HEADER("imgAcceptHeader", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setImgAcceptHeader(decodeString(value));
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
    CSS_ACCEPT_HEADER("cssAcceptHeader", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setCssAcceptHeader(decodeString(value));
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
    SCRIPT_ACCEPT_HEADER("scriptAcceptHeader", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setScriptAcceptHeader(decodeString(value));
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
    XML_HTTP_REQUEST_ACCEPT_HEADER("xmlHttpRequestAcceptHeader", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setXmlHttpRequestAcceptHeader(decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getXmlHttpRequestAcceptHeader();
        }
    },

    /**
     * Returns the value used by the browser for the {@code sec-ch-ua} header.
     * <p>
     * property: <b>webdriver.htmlunit.secClientHintUserAgentHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    SEC_CLIENT_HINT_USER_AGENT_HEADER("secClientHintUserAgentHeader", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setSecClientHintUserAgentHeader(decodeString(value));
        }
        
        @Override
        public Object obtain(final BrowserVersion version) {
            return version.getSecClientHintUserAgentHeader();
        }
    },

    /**
     * Returns the value used by the browser for the {@code sec-ch-ua-platform} header.
     * <p>
     * property: <b>webdriver.htmlunit.secClientHintUserAgentPlatformHeader</b><br>
     * type: {@link String}<br>
     * default: {@code null}
     */
    SEC_CLIENT_HINT_USER_AGENT_PLATFORM_HEADER("secClientHintUserAgentPlatformHeader", String.class, null) {
        @Override
        public void insert(final BrowserVersionBuilder builder, final Object value) {
            builder.setSecClientHintUserAgentPlatformHeader(decodeString(value));
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
        this.name = "webdriver.htmlunit." + key;
        this.type = type;
        this.initial = initial;
    }
    
    public boolean isDefault(final Object value) {
        if (initial == null) return value == null;
        if (value == null) return false;
        return value.equals(initial);
    }
    
    public Object encode(final Object value) {
        switch (this.type.getName()) {
        case "boolean":
        case "int":
        case "java.lang.String":
            return value;
        case "java.util.TimeZone":
            return encodeTimeZone(value);
        }
        throw new IllegalStateException(
                String.format("Unsupported type '%s' specified for option [%s]; value is of type: %s",
                this.type.getName(), this.toString(), getClassName(value)));
    }
    
    public Object decode(final Object value) {
        switch (this.type.getName()) {
        case "boolean":
            return decodeBoolean(value);
        case "int":
            return decodeInt(value);
        case "java.lang.String":
            return decodeString(value);
        case "java.util.TimeZone":
            return decodeTimeZone(value);
        }
        throw new IllegalStateException(
                String.format("Unsupported type '%s' specified for option [%s]; value is of type: %s",
                this.type.getName(), this.toString(), getClassName(value)));
    }
    
    public void insert(final BrowserVersionBuilder builder, final Object value) {
        throw new UnsupportedOperationException(
                String.format("Trait '%s' does not support value insertion", this.toString()));
    }
    
    public Object obtain(final BrowserVersion version) {
        return null;
    }
    
    private static boolean decodeBoolean(final Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        throw new IllegalStateException("Specified value must be 'Boolean' or 'String'; was " + getClassName(value));
    }
    
    private static int decodeInt(final Object value) {
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        throw new IllegalStateException("Specified value must be 'Long', 'Integer', or 'String'; was " + getClassName(value));
    }
    
    private static String decodeString(final Object value) {
        if (value == null) return null;
        if (value instanceof String) {
            return (String) value;
        }
        throw new IllegalStateException("Specified value must be 'String'; was " + getClassName(value));
    }
    
    private static String encodeTimeZone(final Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof TimeZone) {
            return ((TimeZone) value).getID();
        }
        throw new IllegalStateException("Specified value must be 'TimeZone' or 'String'; was " + getClassName(value));
    }
    
    private static TimeZone decodeTimeZone(final Object value) {
        if (value instanceof TimeZone) {
            return (TimeZone) value;
        }
        if (value instanceof String) {
            return TimeZone.getTimeZone((String) value);
        }
        throw new IllegalStateException("Specified value must be 'TimeZone' or 'String'; was " + getClassName(value));
    }
    
    private static String getClassName(final Object value) {
        return (value != null) ? value.getClass().getName() : "'null'";
    }
}
