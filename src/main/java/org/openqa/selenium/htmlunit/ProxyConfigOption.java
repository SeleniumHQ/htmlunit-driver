package org.openqa.selenium.htmlunit;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.htmlunit.ProxyConfig;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.TypeToken;

public enum ProxyConfigOption {
    PROXY_HOST("proxyHost", String.class) {
        @Override
        public void insert(final ProxyConfig config, final Object value) {
            config.setProxyHost(decodeString(value));
        }
        
        @Override
        public Object obtain(final ProxyConfig config) {
            return config.getProxyHost();
        }        
    },
    
    PROXY_PORT("proxyPort", int.class) {
        @Override
        public void insert(final ProxyConfig config, final Object value) {
            config.setProxyPort(decodeInt(value));
        }
        
        @Override
        public Object obtain(final ProxyConfig config) {
            return config.getProxyPort();
        }        
    },
    
    PROXY_SCHEME("proxyScheme", String.class) {
        @Override
        public void insert(final ProxyConfig config, final Object value) {
            // NOTE: Target method is mis-named
            config.setProxyPort(decodeString(value));
        }
        
        @Override
        public Object obtain(final ProxyConfig config) {
            return config.getProxyScheme();
        }        
    },
    
    IS_SOCKS_PROXY("isSocksProxy", boolean.class) {
        @Override
        public void insert(final ProxyConfig config, final Object value) {
            config.setSocksProxy(decodeBoolean(value));
        }
        
        @Override
        public Object obtain(final ProxyConfig config) {
            return config.isSocksProxy();
        }        
    },
    
    PROXY_BYPASS_HOSTS("proxyBypassHosts", List.class) {
        @Override
        public void insert(final ProxyConfig config, final Object value) {
            decodeList(value).stream().forEach(host -> config.addHostsToProxyBypass(host));
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public Object obtain(final ProxyConfig config) {
            try {
                Field proxyBypassHosts_ = ProxyConfig.class.getDeclaredField("proxyBypassHosts_");
                proxyBypassHosts_.setAccessible(true);
                Map<String, Pattern> proxyBypassHosts = (Map<String, Pattern>) proxyBypassHosts_.get(config);
                return new ArrayList<String>(proxyBypassHosts.keySet());
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                return new ArrayList<String>();
            }
        }        
    },
    
    PROXY_AUTO_CONFIG_URL("proxyAutoConfigUrl", String.class) {
        @Override
        public void insert(final ProxyConfig config, final Object value) {
            config.setProxyAutoConfigUrl(decodeString(value));
        }
        
        @Override
        public Object obtain(final ProxyConfig config) {
            return config.getProxyAutoConfigUrl();
        }        
    };
    
    public final String key;
    public final String name;
    public final Class<?> type;
    
    /** Specifier for {@code List<String>} input/output type */
    private static final Type LIST_TYPE = new TypeToken<List<String>>() {}.getType();
    
    ProxyConfigOption(String key, Class<?> type) {
        this.key = key;
        this.name = "webdriver.htmlunit." + key;
        this.type = type;
    }
    
    public Object encode(final Object value) {
        switch (this.type.getName()) {
        case "boolean":
        case "int":
        case "java.lang.String":
            return value;
        case "java.util.List":
            return encodeList(value);
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
        case "java.util.List":
            return decodeList(value);
        }
        throw new IllegalStateException(
                String.format("Unsupported type '%s' specified for option [%s]; value is of type: %s",
                this.type.getName(), this.toString(), getClassName(value)));
    }
    
    public void insert(final ProxyConfig config, final Object value) {
        throw new UnsupportedOperationException(
                String.format("Option '%s' does not support value insertion", this.toString()));
    }
    
    public Object obtain(final ProxyConfig config) {
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
    
    @SuppressWarnings("unchecked")
    private static String encodeList(final Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof List) {
            List<String> list = (List<String>) value;
            return new Json().toJson(list);
        }
        throw new IllegalStateException("Specified value must be 'List' or 'String'; was " + getClassName(value));
    }
    
    @SuppressWarnings("unchecked")
    private static List<String> decodeList(final Object value) {
        if (value instanceof List) {
            return (List<String>) value;
        }
        if (value instanceof String) {
            return new Json().toType((String) value, LIST_TYPE);
        }
        throw new IllegalStateException("Specified value must be 'List' or 'String'; was " + getClassName(value));
    }

    private static String getClassName(final Object value) {
        return (value != null) ? value.getClass().getName() : "'null'";
    }
}
