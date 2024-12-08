# HtmlUnitDriverOptions Class

The **HtmlUnitDriverOptions** class provides methods to manage options specific to **HtmlUnitDriver**. This browser-specific
options object is assigned to the custom capability name **`garg:htmlunitOptions`**.

### HtmlUnitDriverOptions example usage:

```java
HtmlUnitDriverOptions options = new HtmlUnitDriverOptions()
    .setWebClientVersion(BrowserVersion.FIREFOX_ESR)
    .setJavaScriptEnabled(true);

// For use with HtmlUnitDriver:
HtmlUnitDriver driver = new HtmlUnitDriver(options);

// For use with RemoteWebDriver:
RemoteWebDriver driver = new RemoteWebDriver(
    new URL("http://localhost:4444/"),
    new HtmlUnitDriverOptions());
```

## Getting/setting HtmlUnitDriver options:

In addition to methods for reading and writing specific **HtmlUnitDriver** options, you can use
the standard **Capabilities** API:

* `is(String)`
* `getCapability(String)`
* `setCapability(String, Object)`

### Getting individual browser version traits:

**HtmlUnitDriverOptions** contains a **BrowserVersion** capability which can be read and written directly:

* `getWebClientVersion()`
* `setWebClientVersion(BrowserVersion)`

The individual traits of the **BrowserVersion** object can be read directly as well via the standard
**Capabilities** API. For example:

```java
HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.EDGE);
// System time zone accessed via BrowserVersion API
TimeZone viaBrowserVersion = options.getWebClientVersion.getSystemTimezone();
// System time zone accessed via standard Capabilities API
TimeZone viaCapabilityName = (TimeZone) options.getCapability(BrowserVersionTrait.optSystemTimezone);
```

**NOTE**: Although **HtmlUnitDriverOptions** objects are mutable (their capabilities can be altered),
the individual traits of the **BrowserVersion** object within these objects cannot be altered:

```java
HtmlUnitDriverOptions options = new HtmlUnitDriverOptions(BrowserVersion.CHROME);
options.setCapability(BrowserVersionTrait.optUserAgent, "HtmlUnitDriver emulating Google Chrome");
// => UnsupporterOperationException: Individual browser version traits are immutable; 'optUserAgent' cannot be set
```

For more details, see [The BrowserVersionTraits Enumeration](#the-browserversiontrait-enumeration) below.

## The HtmlUnitOption Enumeration

The **HtmlUnitDriverOptions** class provides a few targeted methods for manipulating frequently-used or complex capabilities
(e.g. - `isJavaScriptEnabled()`). However, the majority of the capabilities of **HtmlUnitDriver** are manipulated via the
standard **Capabilities** API as shown above. All of the capabilities defined by **HtmlUnitDriver** are represented
in the **HtmlUnitOption** enumeration, which provides the capability names, accessors, and mutators for the browser options. 

### HtmlUnitOption/BrowserVersionTrait example usage:

```java
HtmlUnitDriverOptions options = new HtmlUnitDriverOptions();
boolean popupBlockerEnabled = options.is(HtmlUnitOption.optPopupBlockerEnabled);
// NOTE: See subsection "Getting individual browser version traits" above
String  browserLanguage = (String) options.getCapability(BrowserVersionTrait.optBrowserLanguage);
options.setCapability(HtmlUnitOption.optGeolocationEnabled, true);
```

| property name | value type | default | description |
|--|--|--|--|
| webClientVersion | **BrowserVersion** | BrowserVersion.BEST_SUPPORTED | Browser version of this **HtmlUnitDriver**. |
| javascriptEnabled | boolean | `true` | Enables/disables JavaScript support. |
| cssEnabled | boolean | `true` | <details><summary>Enables/disables CSS support. </summary>If disabled, **HtmlUnit** will not download linked CSS files and also not trigger the associated `onload`/`onerror` events.</details> |
| printContentOnFailingStatusCode | boolean | `true` | <details><summary>Print document on failing status code. </summary>Specifies whether or not the content of the resulting document will be printed to the console in the event of a failing response code. Successful response codes are in the range **200-299**.</details> |
| throwExceptionOnFailingStatusCode | boolean | `true` | <details><summary>Throw exception on failing status code. </summary>Specifies whether or not an exception will be thrown in the event of a failing status code. Successful status codes are in the range **200-299**.</details> |
| throwExceptionOnScriptError | boolean | `true` | <details><summary>Throw exception on script error. </summary>Indicates if an exception should be thrown when a script execution fails or if it should be caught and just logged to allow page execution to continue.</details> |
| popupBlockerEnabled | boolean | `false` | <details><summary>Enable/disable the popup window blocker. </summary>By default, the popup blocker is disabled, and popup windows are allowed. When set to `true`, the `window.open()` function has no effect and returns `null`.</details> |
| isRedirectEnabled | boolean | `true` | <details><summary>Enables/disables automatic redirection. </summary>Sets whether or not redirections will be followed automatically on receipt of a redirect status code from the server.</details> |
| tempFileDirectory | **File** | _(none)_ (use system default) | <details><summary>Directory for response content temporary files. </summary>Path to the directory to be used for storing the response content in a temporary file. The specified directory is created if it doesn't exist.</details> |
| sslClientCertificateStore | **KeyStore** | _(none)_ | <details><summary>The SSL client certificate **KeyStore** to use. </summary>**NOTE**: This option is omitted when serializing session settings.</details> |
| sslClientCertificateType | char[] | _(none)_ | Type of the specified SSL client certificate **KeyStore** (e.g. - `jks` or `pkcs12`). |
| sslClientCertificatePassword | char[] | _(none)_ | Password for the specified SSL client certificate **KeyStore**. |
| sslTrustStore | KeyStore | _(none)_ | <details><summary>The SSL server certificate trust store. </summary>All server certificates will be validated against this trust store.</details> |
| sslTrustStoreType | char[] | _(none)_ | Type of the specified SSL trust **KeyStore** (e.g. - `jks` or `pkcs12`). |
| sslTrustStorePassword | char[] | _(none)_ | Password for the specified SSL trust **KeyStore**. |
| sslClientProtocols | **String[]** | _(none)_ (use default protocols) | Protocol versions enabled for use on SSL connections. |
| sslClientCipherSuites | **String[]** | _(none)_ (use default suites) | Cipher suites enabled for use on SSL connections. |
| geolocationEnabled | boolean | `false` | Enables/disables geo-location support. |
| doNotTrackEnabled | boolean | `false` | Enables/disables "Do Not Track" support. |
| homePage | **String** | "https://www.htmlunit.org/" | Home page for this client. |
| proxyConfig | **ProxyConfig** | _(none)_ | Proxy configuration for this client. |
| timeout | int | 90,000 | <details><summary>**WebConnection** timeout for this client. </summary>Set to zero (0) for an infinite wait.</details> |
| connectionTimeToLive | long | -1L (use HTTP default) | <details><summary>**HttpClient** connection pool `connTimeToLive` (in milliseconds). </summary>Use this if you are working with web pages behind a DNS based load balancer.</details> |
| useInsecureSSL | boolean | `false` | <details><summary>Accept/reject connection to servers with expired or corrupt certificates. </summary>If set to `true`, the client will accept connections to any host, regardless of whether they have valid certificates or not. This is especially useful when you are trying to connect to a server with expired or corrupt certificates.</details> |
| sslInsecureProtocol | **String** | _(none)_ (use SSL) | SSL protocol to use when `USE_INSECURE_SSL` is set to `true`. |
| maxInMemory | int | 500 * 1024 | <details><summary>Maximum bytes to have in memory. </summary>Content that exceeds the specified maximum size is stored in a temporary file. Specifying **0** or **-1** deactivates this storage feature.</details> |
| historySizeLimit | int | 50 | <details><summary>Maximum number of pages to cache in history. </summary>**HtmlUnit** uses `SoftReference<Page>` for storing the pages that are part of the history. If you like to fine tune this, you can use **HISTORY_PAGE_CACHE_LIMIT** to limit the number of page references stored by the history.</details> |
| historyPageCacheLimit | int | Integer.MAX_VALUE | <details><summary>Maximum number of pages to cache in history. </summary>If this value is smaller than **HISTORY_SIZE_LIMIT**, **HtmlUnit** will only use soft references for the first **HISTORY_PAGE_CACHE_LIMIT** entries in the history. For older entries, only the URL is saved; the page will be (re)retrieved on demand.</details> |
| localAddress | **InetAddress** | _(none)_ (use `localhost`) | <details><summary>Local address to be used for request execution. </summary>On machines with multiple network interfaces, this parameter can be used to select the network interface from which the connection originates.</details> |
| downloadImages | boolean | `false` | Enables/disables automatic image downloading. |
| screenWidth | int | 1920 | Screen width. |
| screenHeight | int | 1080 | Screen height. |
| webSocketEnabled | boolean | `true` | Enables/disables WebSocket support. |
| webSocketMaxTextMessageSize | int | -1 (use default size) | WebSocket `maxTextMessageSize` parameter. |
| webSocketMaxTextMessageBufferSize | int | -1 (use default size) | WebSocket `maxTextMessageBufferSize` parameter. |
| webSocketMaxBinaryMessageSize | int | -1 (use default size) | WebSocket `maxBinaryMessageSize` parameter. |
| webSocketMaxBinaryMessageBufferSize | int | -1 (use default size) | WebSocket `maxBinaryMessageBufferSize` parameter. |
| fetchPolyfillEnabled | boolean | `false` | Enables/disables fetch polyfill |
| fileProtocolForXMLHttpRequestsAllowed | boolean | `false` | <details><summary>Allows/blocks file protocol for **XMLHttpRequests**. </summary>If set to `true`, the client will accept **XMLHttpRequests** to URLs using the `file` protocol. Allowing this introduces security problems and is therefore not allowed by current browsers. But some browsers have special settings to open this door; therefore, we have this option.</details> |
 
## The BrowserVersionTrait Enumeration
 
 The **BrowserVersion** capability of **HtmlUnitDriverOptions** is comprised of many individual traits, and each of these can be read discretely via the standard **Capabiltiies** API. Examples of this are shown above (e.g. - [SYSTEM_TIME_ZONE](#getting-individual-browser-version-traits)). Note that these traits are immutable.
    
 > **NOTE**: Technically, the traits of **BrowserVersion** objects can be revised, but alterations made to existing instances aren't propagated to the associated **HtmlUnitDriver**; browser version traits are only imported en masse when a new browser version is specified. To avoid confusion, setting of individual traits is only supported via the **BrowserVersionBuilder** class.

For additional information, see [Getting individual browser version traits](#getting-individual-browser-version-traits) above.

| trait name | value type | default | description |
|--|--|--|--|
| numericCode | int | **0** | Returns the numeric code for the browser represented by this **BrowserVersion**. |
| nickname | **String** | _(none)_ | Returns the nickname for the browser represented by this **BrowserVersion**. |
| applicationVersion | **String** | _(none)_ | <details><summary>Returns the application version. </summary>e.g. - "4.0 (compatible; MSIE 6.0b; Windows 98)".</details> |
| userAgent | **String** | _(none)_ | <details><summary>Returns the user agent string. </summary>e.g. - "Mozilla/4.0 (compatible; MSIE 6.0b; Windows 98)"</details> |
| applicationName | **String** | _(none)_ | <details><summary>Returns the application name. </summary>e.g. - "Microsoft Internet Explorer"</details> |
| applicationCodeName | **String** | "Mozilla" | Returns the application code name. |
| applicationMinorVersion | **String** | "0" | Returns the application minor version. |
| vendor | **String** | "" | <details><summary>Returns the browser vendor. </summary>e.g. - "Google Inc."</details> |
| browserLanguage | **String** | "en-US" | Returns the browser language. |
| isOnline | boolean | `true` | Returns `true` if the browser is currently online. |
| platform | **String** | "Win32" | Returns the platform on which the application is running. |
| systemTimezone | **TimeZone** | <code>TimeZone.getTimeZone(&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;"America/New_York")</code> | Returns the system **TimeZone**. |
| acceptEncodingHeader | **String** | _(none)_ | Returns the value used by the browser for the `Accept-Encoding` header. |
| acceptLanguageHeader | **String** | _(none)_ | Returns the value used by the browser for the `Accept-Language` header. |
| htmlAcceptHeader | **String** | _(none)_ | Returns the value used by the browser for the `Accept` header if requesting a page. |
| imgAcceptHeader | **String** | _(none)_ | Returns the value used by the browser for the `Accept` header if requesting an image. |
| cssAcceptHeader | **String** | _(none)_ | Returns the value used by the browser for the `Accept` header if requesting a CSS declaration. |
| scriptAcceptHeader | **String** | _(none)_ | Returns the value used by the browser for the `Accept` header if requesting a script. |
| xmlHttpRequestAcceptHeader | **String** | _(none)_ | Returns the value used by the browser for the `Accept` header if performing an **XMLHttpRequest**. |
| secClientHintUserAgentHeader | **String** | _(none)_ | Returns the value used by the browser for the `Sec-CH-UA` header. |
| secClientHintUserAgentPlatformHeader | **String** | _(none)_ | Returns the value used by the browser for the `Sec-CH-UA-Platform` header. |

### HtmlUnitOption and BrowserVersionTrait System Property Definitions
  
Each **HtmlUnitOption** property can be overridden by a corresponding Java system property whose name matches the pattern
**webdriver.htmlunit._&lt;property-or-trait-name&gt;_** (e.g. - `webdriver.htmlunit.javascriptEnabled`). If defined, these
system properties are applied as default values when **HtmlUnitDriver** is instantiated. Subsequent requests to update
corresponding capabilities on existing drivers are honored as expected.

> Written with [StackEdit](https://stackedit.io/).