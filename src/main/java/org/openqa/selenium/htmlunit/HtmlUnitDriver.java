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

package org.openqa.selenium.htmlunit;

import static org.openqa.selenium.remote.Browser.HTMLUNIT;
import static org.openqa.selenium.remote.CapabilityType.ACCEPT_SSL_CERTS;
import static org.openqa.selenium.remote.CapabilityType.PAGE_LOAD_STRATEGY;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLHandshakeException;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.Version;
import com.gargoylesoftware.htmlunit.WaitingRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.Element;
import com.gargoylesoftware.htmlunit.javascript.host.Location;
import com.gargoylesoftware.htmlunit.javascript.host.html.DocumentProxy;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLCollection;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import com.gargoylesoftware.htmlunit.platform.AwtClipboardHandler;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.IdScriptableObject;
import net.sourceforge.htmlunit.corejs.javascript.NativeArray;
import net.sourceforge.htmlunit.corejs.javascript.NativeObject;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.Undefined;

/**
 * An implementation of {@link WebDriver} that drives
 * <a href="http://htmlunit.sourceforge.net/">HtmlUnit</a>, which is a headless
 * (GUI-less) browser simulator.
 * <p>
 * The main supported browsers are Chrome, Edge, Firefox and Internet Explorer.
 */
public class HtmlUnitDriver implements WebDriver, JavascriptExecutor, HasCapabilities, Interactive {

    private static final int sleepTime = 200;

    private WebClient webClient;
    private final HtmlUnitAlert alert;
    private final HtmlUnitWindow windowManager;
    private HtmlUnitKeyboard keyboard;
    private HtmlUnitMouse mouse;
    private boolean gotPage;
    private final TargetLocator targetLocator;
    private AsyncScriptExecutor asyncScriptExecutor;
    private PageLoadStrategy pageLoadStrategy = PageLoadStrategy.NORMAL;
    private final ElementsMap elementsMap = new ElementsMap();
    private final Options options;

    private final HtmlUnitElementFinder elementFinder;

    public static final String INVALIDXPATHERROR = "The xpath expression '%s' cannot be evaluated";
    public static final String INVALIDSELECTIONERROR = "The xpath expression '%s' selected an object of type '%s' instead of a WebElement";

    public static final String BROWSER_LANGUAGE_CAPABILITY = "browserLanguage";
    public static final String DOWNLOAD_IMAGES_CAPABILITY = "downloadImages";
    public static final String JAVASCRIPT_ENABLED = "javascriptEnabled";

    /**
     * The Lock for the {@link #mainCondition}, which waits at the end of
     * {@link #runAsync(Runnable)} till either and alert is triggered, or
     * {@link Runnable} finishes.
     */
    private final Lock conditionLock = new ReentrantLock();
    private final Condition mainCondition = conditionLock.newCondition();
    private boolean runAsyncRunning;
    private RuntimeException exception;
    private final ExecutorService defaultExecutor;
    private Executor executor;

    /**
     * Constructs a new instance with JavaScript disabled, and the
     * {@link BrowserVersion#getDefault() default} BrowserVersion.
     */
    public HtmlUnitDriver() {
        this(BrowserVersion.getDefault(), false);
    }

    /**
     * Constructs a new instance with the specified {@link BrowserVersion}.
     *
     * @param version the browser version to use
     */
    public HtmlUnitDriver(BrowserVersion version) {
        this(version, false);
    }

    /**
     * Constructs a new instance, specify JavaScript support and using the
     * {@link BrowserVersion#getDefault() default} BrowserVersion.
     *
     * @param enableJavascript whether to enable JavaScript support or not
     */
    public HtmlUnitDriver(boolean enableJavascript) {
        this(BrowserVersion.getDefault(), enableJavascript);
    }

    /**
     * Constructs a new instance with the specified {@link BrowserVersion} and the
     * JavaScript support.
     *
     * @param version          the browser version to use
     * @param enableJavascript whether to enable JavaScript support or not
     */
    public HtmlUnitDriver(BrowserVersion version, boolean enableJavascript) {
        this(version, enableJavascript, null);

        modifyWebClient(webClient);
    }

    /**
     * The browserName is {@link BrowserType#HTMLUNIT} "htmlunit" and the
     * browserVersion denotes the required browser AND its version. For example
     * "chrome" for Chrome, "firefox-45" for Firefox 45 or "internet explorer" for
     * IE.
     *
     * @param capabilities desired capabilities requested for the htmlunit driver
     *                     session
     */
    public HtmlUnitDriver(Capabilities capabilities) {
        this(BrowserVersionDeterminer.determine(capabilities),
                capabilities.getCapability(JAVASCRIPT_ENABLED) == null || capabilities.is(JAVASCRIPT_ENABLED),
                Proxy.extractFrom(capabilities));

        setDownloadImages(capabilities.is(DOWNLOAD_IMAGES_CAPABILITY));

        if (alert != null) {
            alert.handleBrowserCapabilities(capabilities);
        }

        Boolean acceptSslCerts = (Boolean) capabilities.getCapability(ACCEPT_SSL_CERTS);
        if (acceptSslCerts == null) {
            acceptSslCerts = true;
        }
        setAcceptSslCertificates(acceptSslCerts);

        String pageLoadStrategyString = (String) capabilities.getCapability(PAGE_LOAD_STRATEGY);
        if ("none".equals(pageLoadStrategyString)) {
            pageLoadStrategy = PageLoadStrategy.NONE;
        } else if ("eager".equals(pageLoadStrategyString)) {
            pageLoadStrategy = PageLoadStrategy.EAGER;
        }

        modifyWebClient(webClient);
    }

    public HtmlUnitDriver(Capabilities desiredCapabilities, Capabilities requiredCapabilities) {
        this(new DesiredCapabilities(desiredCapabilities, requiredCapabilities));
    }

    private HtmlUnitDriver(BrowserVersion version, boolean enableJavascript, Proxy proxy) {
        webClient = newWebClient(version);

        final WebClientOptions clientOptions = webClient.getOptions();
        clientOptions.setHomePage(UrlUtils.URL_ABOUT_BLANK.toString());
        clientOptions.setThrowExceptionOnFailingStatusCode(false);
        clientOptions.setPrintContentOnFailingStatusCode(false);
        clientOptions.setRedirectEnabled(true);
        clientOptions.setUseInsecureSSL(true);

        setJavascriptEnabled(enableJavascript);
        setProxySettings(proxy);

        webClient.setRefreshHandler(new WaitingRefreshHandler());
        webClient.setClipboardHandler(new AwtClipboardHandler());

        elementFinder = new HtmlUnitElementFinder();

        alert = new HtmlUnitAlert(this);
        windowManager = new HtmlUnitWindow(this);

        defaultExecutor = Executors.newCachedThreadPool();
        executor = defaultExecutor;

        // Now put us on the home page, like a real browser
        get(clientOptions.getHomePage());
        gotPage = false;

        options = new HtmlUnitOptions(this);
        targetLocator = new HtmlUnitTargetLocator(this);
        resetKeyboardAndMouseState();
    }

    /**
     * @return to process or not to proceed
     */
    boolean isProcessAlert() {
        if (asyncScriptExecutor != null) {
            String text = alert.getText();
            alert.dismiss();
            asyncScriptExecutor.alertTriggered(text);
            return false;
        }
        conditionLock.lock();
        mainCondition.signal();
        conditionLock.unlock();
        return true;
    }

    protected void runAsync(Runnable r) {
        boolean loadStrategyWait = pageLoadStrategy != PageLoadStrategy.NONE;

        if (loadStrategyWait) {
            while (runAsyncRunning) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            conditionLock.lock();
            runAsyncRunning = true;
        }

        exception = null;
        Runnable wrapped = () -> {
            try {
                r.run();
            } catch (RuntimeException e) {
                exception = e;
            } finally {
                conditionLock.lock();
                runAsyncRunning = false;
                mainCondition.signal();
                conditionLock.unlock();
            }
        };
        executor.execute(wrapped);

        if (loadStrategyWait && this.runAsyncRunning) {
            mainCondition.awaitUninterruptibly();
            conditionLock.unlock();
        }

        if (exception != null) {
            throw exception;
        }
    }

    public void click(DomElement element, boolean directClick) {
        runAsync(() -> mouse.click(element, directClick));
    }

    public void doubleClick(DomElement element) {
        runAsync(() -> mouse.doubleClick(element));
    }

    public void mouseUp(DomElement element) {
        runAsync(() -> mouse.mouseUp(element));
    }

    public void mouseMove(DomElement element) {
        runAsync(() -> mouse.mouseMove(element));
    }

    public void mouseDown(DomElement element) {
        runAsync(() -> mouse.mouseDown(element));
    }

    public void submit(HtmlUnitWebElement element) {
        runAsync(element::submitImpl);
    }

    public void sendKeys(HtmlUnitWebElement element, CharSequence... value) {
        runAsync(() -> keyboard.sendKeys(element, true, value));
    }

    /**
     * Get the simulated {@code BrowserVersion}.
     *
     * @return the used {@code BrowserVersion}
     */
    public BrowserVersion getBrowserVersion() {
        return webClient.getBrowserVersion();
    }

    /**
     * Create the underlying WebClient, but don't set any fields on it.
     *
     * @param version Which browser to emulate
     * @return a new instance of WebClient.
     */
    protected WebClient newWebClient(BrowserVersion version) {
        return new WebClient(version);
    }

    /**
     * Child classes can override this method to customize the WebClient that the
     * HtmlUnit driver uses.
     *
     * @param client The client to modify
     * @return The modified client
     */
    protected WebClient modifyWebClient(WebClient client) {
        // Does nothing here to be overridden.
        return client;
    }

    public HtmlUnitWindow getWindowManager() {
        return windowManager;
    }

    public HtmlUnitAlert getAlert() {
        return alert;
    }

    public ElementsMap getElementsMap() {
        return elementsMap;
    }

    public void setCurrentWindow(WebWindow window) {
        getWebClient().setCurrentWindow(window);
    }

    /**
     * Set proxy for WebClient using Proxy.
     *
     * @param proxy The proxy preferences.
     */
    public void setProxySettings(Proxy proxy) {
        if (proxy == null || proxy.getProxyType() == Proxy.ProxyType.UNSPECIFIED) {
            return;
        }

        switch (proxy.getProxyType()) {
        case MANUAL:
            List<String> noProxyHosts = new ArrayList<>();
            String noProxy = proxy.getNoProxy();
            if (noProxy != null && !noProxy.isEmpty()) {
                String[] hosts = noProxy.split(",");
                for (String host : hosts) {
                    if (host.trim().length() > 0) {
                        noProxyHosts.add(host.trim());
                    }
                }
            }

            String httpProxy = proxy.getHttpProxy();
            if (httpProxy != null && !httpProxy.isEmpty()) {
                String host = httpProxy;
                int port = 0;

                int index = httpProxy.indexOf(":");
                if (index != -1) {
                    host = httpProxy.substring(0, index);
                    port = Integer.parseInt(httpProxy.substring(index + 1));
                }

                setHTTPProxy(host, port, noProxyHosts);
            }

            String socksProxy = proxy.getSocksProxy();
            if (socksProxy != null && !socksProxy.isEmpty()) {
                String host = socksProxy;
                int port = 0;

                int index = socksProxy.indexOf(":");
                if (index != -1) {
                    host = socksProxy.substring(0, index);
                    port = Integer.parseInt(socksProxy.substring(index + 1));
                }

                setSocksProxy(host, port, noProxyHosts);
            }

            // sslProxy is not supported/implemented
            // ftpProxy is not supported/implemented

            break;

        case PAC:
            String pac = proxy.getProxyAutoconfigUrl();
            if (pac != null && !pac.isEmpty()) {
                setAutoProxy(pac);
            }
            break;

        default:
            break;
        }
    }

    /**
     * Sets HTTP proxy for WebClient
     *
     * @param host The hostname of HTTP proxy
     * @param port The port of HTTP proxy, 0 means HTTP proxy w/o port
     */
    public void setProxy(String host, int port) {
        setHTTPProxy(host, port, null);
    }

    /**
     * Sets HTTP proxy for WebClient with bypass proxy hosts
     *
     * @param host         The hostname of HTTP proxy
     * @param port         The port of HTTP proxy, 0 means HTTP proxy w/o port
     * @param noProxyHosts The list of hosts which need to bypass HTTP proxy
     */
    public void setHTTPProxy(String host, int port, List<String> noProxyHosts) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setProxyHost(host);
        proxyConfig.setProxyPort(port);
        if (noProxyHosts != null && noProxyHosts.size() > 0) {
            for (String noProxyHost : noProxyHosts) {
                proxyConfig.addHostsToProxyBypass(noProxyHost);
            }
        }
        getWebClient().getOptions().setProxyConfig(proxyConfig);
    }

    /**
     * Sets SOCKS proxy for WebClient
     *
     * @param host The hostname of SOCKS proxy
     * @param port The port of SOCKS proxy, 0 means HTTP proxy w/o port
     */
    public void setSocksProxy(String host, int port) {
        setSocksProxy(host, port, null);
    }

    /**
     * Sets SOCKS proxy for WebClient with bypass proxy hosts
     *
     * @param host         The hostname of SOCKS proxy
     * @param port         The port of SOCKS proxy, 0 means HTTP proxy w/o port
     * @param noProxyHosts The list of hosts which need to bypass SOCKS proxy
     */
    public void setSocksProxy(String host, int port, List<String> noProxyHosts) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setProxyHost(host);
        proxyConfig.setProxyPort(port);
        proxyConfig.setSocksProxy(true);
        if (noProxyHosts != null && noProxyHosts.size() > 0) {
            for (String noProxyHost : noProxyHosts) {
                proxyConfig.addHostsToProxyBypass(noProxyHost);
            }
        }
        getWebClient().getOptions().setProxyConfig(proxyConfig);
    }

    /**
     * Sets the {@link Executor} to be used for submitting async tasks to. You have
     * to close this manually on {@link #quit()}
     *
     * @param executor the {@link Executor} to use
     */
    public void setExecutor(Executor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor cannot be null");
        }
        this.executor = executor;
    }

    /**
     * Sets Proxy Autoconfiguration URL for WebClient
     *
     * @param autoProxyUrl The Proxy Autoconfiguration URL
     */
    public void setAutoProxy(String autoProxyUrl) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setProxyAutoConfigUrl(autoProxyUrl);
        getWebClient().getOptions().setProxyConfig(proxyConfig);
    }

    @Override
    public Capabilities getCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities(HTMLUNIT.browserName(), "", Platform.ANY);

        capabilities.setPlatform(Platform.getCurrent());
        capabilities.setJavascriptEnabled(isJavascriptEnabled());
        capabilities.setVersion(Version.getProductVersion());
        return capabilities;
    }

    @Override
    public void get(String url) {
        URL fullUrl;
        try {
            // this takes care of data: and about:
            fullUrl = UrlUtils.toUrlUnsafe(url);
        } catch (Exception e) {
            throw new WebDriverException(e);
        }

        runAsync(() -> get(fullUrl));
    }

    /**
     * Allows HtmlUnit's about:blank to be loaded in the constructor, and may be
     * useful for other tests?
     *
     * @param fullUrl The URL to visit
     */
    protected void get(URL fullUrl) {
        getAlert().close();
        getAlert().setAutoAccept(false);
        try {
            // we can't use webClient.getPage(url) here because selenium has a different
            // idea
            // of the current window and we like to load into to selenium current one
            final BrowserVersion browser = getBrowserVersion();
            final WebRequest request = new WebRequest(fullUrl, browser.getHtmlAcceptHeader(),
                    browser.getAcceptEncodingHeader());
            request.setCharset(StandardCharsets.UTF_8);
            getWebClient().getPage(getCurrentWindow().getTopWindow(), request);

            // A "get" works over the entire page
            setCurrentWindow(getCurrentWindow().getTopWindow());
        } catch (UnknownHostException e) {
            getCurrentWindow().getTopWindow().setEnclosedPage(new UnexpectedPage(
                    new StringWebResponse("Unknown host", fullUrl), getCurrentWindow().getTopWindow()));
        } catch (ConnectException e) {
            // This might be expected
        } catch (SocketTimeoutException e) {
            throw new TimeoutException(e);
        } catch (NoSuchSessionException e) {
            throw e;
        } catch (SSLHandshakeException e) {
            return;
        } catch (Exception e) {
            throw new WebDriverException(e);
        }

        gotPage = true;
        resetKeyboardAndMouseState();
    }

    private void resetKeyboardAndMouseState() {
        keyboard = new HtmlUnitKeyboard(this);
        mouse = new HtmlUnitMouse(this, keyboard);
    }

    @Override
    public String getCurrentUrl() {
        getWebClient(); // check that session is active
        Page page = getCurrentWindow().getTopWindow().getEnclosedPage();
        if (page == null) {
            return null;
        }
        URL url = page.getUrl();
        if (url == null) {
            return null;
        }
        return url.toString();
    }

    @Override
    public String getTitle() {
        alert.ensureUnlocked();
        Page page = getWindowManager().lastPage();
        if (!(page instanceof HtmlPage)) {
            return null; // no page so there is no title
        }
        if (getCurrentWindow() instanceof FrameWindow) {
            page = getCurrentWindow().getTopWindow().getEnclosedPage();
        }

        return ((HtmlPage) page).getTitleText();
    }

    @Override
    public WebElement findElement(By by) {
        alert.ensureUnlocked();
        return implicitlyWaitFor(() -> elementFinder.findElement(this, by));
    }

    @Override
    public List<WebElement> findElements(By by) {
        long implicitWait = options.timeouts().getImplicitWaitTimeout().toMillis();
        if (implicitWait < sleepTime) {
            return elementFinder.findElements(this, by);
        }

        long end = System.currentTimeMillis() + implicitWait;
        List<WebElement> found;
        do {
            found = elementFinder.findElements(this, by);
            if (!found.isEmpty()) {
                return found;
            }
            sleepQuietly(sleepTime);
        } while (System.currentTimeMillis() < end);

        return found;
    }

    public WebElement findElement(HtmlUnitWebElement element, By by) {
        alert.ensureUnlocked();
        return implicitlyWaitFor(() -> elementFinder.findElement(element, by));
    }

    public List<WebElement> findElements(HtmlUnitWebElement element, By by) {
        long implicitWait = options.timeouts().getImplicitWaitTimeout().toMillis();
        if (implicitWait < sleepTime) {
            return elementFinder.findElements(element, by);
        }

        long end = System.currentTimeMillis() + implicitWait;
        List<WebElement> found;
        do {
            found = elementFinder.findElements(element, by);
            if (!found.isEmpty()) {
                return found;
            }
            sleepQuietly(sleepTime);
        } while (System.currentTimeMillis() < end);

        return found;
    }

    @Override
    public String getPageSource() {
        Page page = windowManager.lastPage();
        if (page == null) {
            return null;
        }

        if (page instanceof SgmlPage) {
            return ((SgmlPage) page).asXml();
        }
        WebResponse response = page.getWebResponse();
        return response.getContentAsString();
    }

    @Override
    public void close() {
        getWebClient(); // check that session is active
        if (getWebClient().getWebWindows().size() == 1) {
            // closing the last window is equivalent to quit
            quit();
        } else {
            WebWindow thisWindow = getCurrentWindow(); // check that the current window is active
            if (thisWindow != null) {
                alert.close();
                ((TopLevelWindow) thisWindow.getTopWindow()).close();
            }
            if (getWebClient().getWebWindows().size() == 0) {
                quit();
            }
        }
    }

    @Override
    public void quit() {
        if (webClient != null) {
            alert.close();
            webClient.close();
            webClient = null;
        }
        defaultExecutor.shutdown();
    }

    @Override
    public Set<String> getWindowHandles() {
        final Set<String> allHandles = Sets.newHashSet();
        for (final WebWindow window : getWebClient().getTopLevelWindows()) {
            allHandles.add(String.valueOf(System.identityHashCode(window)));
        }

        return allHandles;
    }

    @Override
    public String getWindowHandle() {
        WebWindow topWindow = getCurrentWindow().getTopWindow();
        if (topWindow.isClosed()) {
            throw new NoSuchWindowException("Window is closed");
        }
        return String.valueOf(System.identityHashCode(topWindow));
    }

    @Override
    public Object executeScript(String script, final Object... args) {
        HtmlPage page = getPageToInjectScriptInto();

        script = "function() {" + script + "\n};";
        ScriptResult result = page.executeJavaScript(script);
        Object function = result.getJavaScriptResult();

        Object[] parameters = convertScriptArgs(page, args);

        try {
            result = page.executeJavaScriptFunction(function, getCurrentWindow().getScriptableObject(), parameters,
                    page.getDocumentElement());

            return parseNativeJavascriptResult(result);
        } catch (Throwable ex) {
            throw new WebDriverException(ex);
        }
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        HtmlPage page = getPageToInjectScriptInto();
        args = convertScriptArgs(page, args);

        asyncScriptExecutor = new AsyncScriptExecutor(page, options.timeouts().getScriptTimeout().toMillis());
        try {
            Object result = asyncScriptExecutor.execute(script, args);

            alert.ensureUnlocked();
            return parseNativeJavascriptResult(result);
        } finally {
            asyncScriptExecutor = null;
        }
    }

    private Object[] convertScriptArgs(HtmlPage page, final Object[] args) {
        final Object scope = page.getEnclosingWindow().getScriptableObject();

        if (!(scope instanceof Scriptable)) {
            return args;
        }

        final Object[] parameters = new Object[args.length];
        Context.enter();
        try {
            for (int i = 0; i < args.length; i++) {
                parameters[i] = parseArgumentIntoJavascriptParameter((Scriptable) scope, args[i]);
            }
        } finally {
            Context.exit();
        }
        return parameters;
    }

    private HtmlPage getPageToInjectScriptInto() {
        if (!isJavascriptEnabled()) {
            throw new UnsupportedOperationException("Javascript is not enabled for this HtmlUnitDriver instance");
        }

        final Page lastPage = windowManager.lastPage();
        if (!(lastPage instanceof HtmlPage)) {
            throw new UnsupportedOperationException("Cannot execute JS against a plain text page");
        } else if (!gotPage) {
            // just to make
            // ExecutingJavascriptTest.testShouldThrowExceptionIfExecutingOnNoPage happy
            // but does this limitation make sense?
            throw new WebDriverException("Can't execute JavaScript before a page has been loaded!");
        }

        return (HtmlPage) lastPage;
    }

    private Object parseArgumentIntoJavascriptParameter(Scriptable scope, Object arg) {
        while (arg instanceof WrapsElement) {
            arg = ((WrapsElement) arg).getWrappedElement();
        }

        if (!(arg instanceof HtmlUnitWebElement || arg instanceof HtmlElement || // special case the underlying type
                arg instanceof Number || arg instanceof String || arg instanceof Boolean || arg.getClass().isArray()
                || arg instanceof Collection<?> || arg instanceof Map<?, ?>)) {
            throw new IllegalArgumentException(
                    "Argument must be a string, number, boolean or WebElement: " + arg + " (" + arg.getClass() + ")");
        }

        if (arg instanceof HtmlUnitWebElement) {
            HtmlUnitWebElement webElement = (HtmlUnitWebElement) arg;
            assertElementNotStale(webElement.getElement());
            return webElement.getElement().getScriptableObject();

        } else if (arg instanceof HtmlElement) {
            HtmlElement element = (HtmlElement) arg;
            assertElementNotStale(element);
            return element.getScriptableObject();

        } else if (arg instanceof Collection<?>) {
            List<Object> list = new ArrayList<>();
            for (Object o : (Collection<?>) arg) {
                list.add(parseArgumentIntoJavascriptParameter(scope, o));
            }
            return Context.getCurrentContext().newArray(scope, list.toArray());

        } else if (arg.getClass().isArray()) {
            List<Object> list = new ArrayList<>();
            for (Object o : (Object[]) arg) {
                list.add(parseArgumentIntoJavascriptParameter(scope, o));
            }
            return Context.getCurrentContext().newArray(scope, list.toArray());

        } else if (arg instanceof Map<?, ?>) {
            Map<?, ?> argmap = (Map<?, ?>) arg;
            Scriptable map = Context.getCurrentContext().newObject(scope);
            for (Object key : argmap.keySet()) {
                map.put((String) key, map, parseArgumentIntoJavascriptParameter(scope, argmap.get(key)));
            }
            return map;

        } else {
            return arg;
        }
    }

    protected void assertElementNotStale(DomElement element) {
        SgmlPage elementPage = element.getPage();
        Page lastPage = windowManager.lastPage();

        if (!lastPage.equals(elementPage)) {
            throw new StaleElementReferenceException(
                    "Element appears to be stale. Did you navigate away from the page that contained it? "
                            + " And is the current window focussed the same as the one holding this element?");
        }

        // We need to walk the DOM to determine if the element is actually attached
        DomNode parentElement = element;
        while (parentElement != null && !(parentElement instanceof SgmlPage)) {
            parentElement = parentElement.getParentNode();
        }

        if (parentElement == null) {
            throw new StaleElementReferenceException("The element seems to be disconnected from the DOM. "
                    + " This means that a user cannot interact with it.");
        }
    }

    public HtmlUnitKeyboard getKeyboard() {
        return keyboard;
    }

    public HtmlUnitMouse getMouse() {
        return mouse;
    }

    protected interface JavaScriptResultsCollection {
        int getLength();

        Object item(int index);
    }

    private Object parseNativeJavascriptResult(Object result) {
        Object value;
        if (result instanceof ScriptResult) {
            value = ((ScriptResult) result).getJavaScriptResult();
        } else {
            value = result;
        }
        if (value instanceof HTMLElement) {
            return toWebElement(((HTMLElement) value).getDomNodeOrDie());
        }

        if (value instanceof DocumentProxy) {
            Element element = ((DocumentProxy) value).getDelegee().getDocumentElement();
            if (element instanceof HTMLElement) {
                return toWebElement(((HTMLElement) element).getDomNodeOrDie());
            }
            throw new WebDriverException("Do not know how to coerce to an HTMLElement: " + element);
        }

        if (value instanceof Number) {
            final Number n = (Number) value;
            final String s = n.toString();
            if (!s.contains(".") || s.endsWith(".0")) { // how safe it is? enough for the unit tests!
                return n.longValue();
            }
            return n.doubleValue();
        }

        if (value instanceof NativeObject) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> map = Maps.newHashMap((NativeObject) value);
            for (final Entry<String, Object> e : map.entrySet()) {
                e.setValue(parseNativeJavascriptResult(e.getValue()));
            }
            return map;
        }

        if (value instanceof Location) {
            return convertLocationToMap((Location) value);
        }

        if (value instanceof NativeArray) {
            final NativeArray array = (NativeArray) value;

            JavaScriptResultsCollection collection = new JavaScriptResultsCollection() {
                @Override
                public int getLength() {
                    return (int) array.getLength();
                }

                @Override
                public Object item(int index) {
                    return array.get(index);
                }
            };

            return parseJavascriptResultsList(collection);
        }

        if (value instanceof HTMLCollection) {
            final HTMLCollection array = (HTMLCollection) value;

            JavaScriptResultsCollection collection = new JavaScriptResultsCollection() {
                @Override
                public int getLength() {
                    return array.getLength();
                }

                @Override
                public Object item(int index) {
                    return array.get(index);
                }
            };

            return parseJavascriptResultsList(collection);
        }

        if (value instanceof IdScriptableObject && value.getClass().getSimpleName().equals("NativeDate")) {
            long l = ((Number) getPrivateField(value, "date")).longValue();
            return Instant.ofEpochMilli(l).toString();
        }

        if (Undefined.isUndefined(value)) {
            return null;
        }

        return value;
    }

    private static Object getPrivateField(Object o, String fieldName) {
        try {
            final Field field = o.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Object> convertLocationToMap(Location location) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("href", location.getHref());
        map.put("protocol", location.getProtocol());
        map.put("host", location.getHost());
        map.put("hostname", location.getHostname());
        map.put("port", location.getPort());
        map.put("pathname", location.getPathname());
        map.put("search", location.getSearch());
        map.put("hash", location.getHash());
        map.put("href", location.getHref());
        return map;
    }

    private List<Object> parseJavascriptResultsList(JavaScriptResultsCollection array) {
        List<Object> list = new ArrayList<>(array.getLength());
        for (int i = 0; i < array.getLength(); ++i) {
            list.add(parseNativeJavascriptResult(array.item(i)));
        }
        return list;
    }

    @Override
    public TargetLocator switchTo() {
        return targetLocator;
    }

    @Override
    public Navigation navigate() {
        return new HtmlUnitNavigation();
    }

    protected HtmlUnitWebElement toWebElement(DomElement element) {
        return getElementsMap().addIfAbsent(this, element);
    }

    public boolean isJavascriptEnabled() {
        return getWebClient().getOptions().isJavaScriptEnabled();
    }

    public void setJavascriptEnabled(boolean enableJavascript) {
        getWebClient().getOptions().setJavaScriptEnabled(enableJavascript);
    }

    public boolean isDownloadImages() {
        return getWebClient().getOptions().isDownloadImages();
    }

    public void setDownloadImages(boolean downloadImages) {
        getWebClient().getOptions().setDownloadImages(downloadImages);
    }

    public void setAcceptSslCertificates(boolean accept) {
        getWebClient().getOptions().setUseInsecureSSL(accept);
    }

    public boolean isAcceptSslCertificates() {
        return getWebClient().getOptions().isUseInsecureSSL();
    }

    protected <X> X implicitlyWaitFor(Callable<X> condition) {
        long implicitWait = options.timeouts().getImplicitWaitTimeout().toMillis();

        if (implicitWait < sleepTime) {
            try {
                return condition.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new WebDriverException(e);
            }
        }

        long end = System.currentTimeMillis() + implicitWait;
        Exception lastException = null;

        do {
            X toReturn = null;
            try {
                toReturn = condition.call();
            } catch (Exception e) {
                lastException = e;
            }

            if (toReturn instanceof Boolean && !(Boolean) toReturn) {
                continue;
            }

            if (toReturn != null) {
                return toReturn;
            }

            sleepQuietly(sleepTime);
        } while (System.currentTimeMillis() < end);

        if (lastException != null) {
            if (lastException instanceof RuntimeException) {
                throw (RuntimeException) lastException;
            }
            throw new WebDriverException(lastException);
        }

        return null;
    }

    protected WebClient getWebClient() {
        if (webClient == null) {
            throw new NoSuchSessionException("Session is closed");
        }
        return webClient;
    }

    protected WebWindow getCurrentWindow() {
        WebWindow currentWindow = getWebClient().getCurrentWindow();
        if (currentWindow == null || currentWindow.isClosed()) {
            throw new NoSuchWindowException("Window is closed");
        }
        return currentWindow;
    }

    private class HtmlUnitNavigation implements Navigation {

        @Override
        public void back() {
            runAsync(() -> {
                try {
                    getCurrentWindow().getHistory().back();
                } catch (IOException e) {
                    throw new WebDriverException(e);
                }
            });
        }

        @Override
        public void forward() {
            runAsync(() -> {
                try {
                    getCurrentWindow().getHistory().forward();
                } catch (IOException e) {
                    throw new WebDriverException(e);
                }
            });
        }

        @Override
        public void to(String url) {
            get(url);
        }

        @Override
        public void to(URL url) {
            get(url);
        }

        @Override
        public void refresh() {
            if (windowManager.lastPage() instanceof HtmlPage) {
                runAsync(() -> {
                    try {
                        ((HtmlPage) windowManager.lastPage()).refresh();
                    } catch (SocketTimeoutException e) {
                        throw new TimeoutException(e);
                    } catch (IOException e) {
                        throw new WebDriverException(e);
                    }
                });
            }
        }
    }

    @Override
    public Options manage() {
        return options;
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    private enum PageLoadStrategy {
        NORMAL, EAGER, NONE;
    }

    protected static class ElementsMap {
        private final Map<SgmlPage, Map<DomElement, HtmlUnitWebElement>> elementsMap;
        private int idCounter;

        public ElementsMap() {
            elementsMap = new WeakHashMap<>();
            idCounter = 0;
        }

        public HtmlUnitWebElement addIfAbsent(final HtmlUnitDriver driver, final DomElement element) {
            Map<DomElement, HtmlUnitWebElement> pageMap = elementsMap.computeIfAbsent(element.getPage(),
                    k -> new HashMap<>());

            HtmlUnitWebElement e = pageMap.get(element);
            if (e == null) {
                idCounter++;
                e = new HtmlUnitWebElement(driver, idCounter, element);
                pageMap.put(element, e);
            }
            return e;
        }

        public void remove(final Page page) {
            elementsMap.remove(page);
        }
    }

    @Override
    public void perform(Collection<Sequence> sequences) {
        // see https://www.w3.org/TR/webdriver/#processing-actions

        for (final Sequence sequence : sequences) {
            Map<String, Object> encodedSeq = sequence.encode();

            // final String sequenceId = encodedSeq.get("id").toString();
            // final Object sequenceParameters = encodedSeq.get("parameters");

            // valid types are "key", "pointer", "wheel", or "none"
            // we have to check this
            final String sequenceType = encodedSeq.get("type").toString();

            final List<Map<String, Object>> actions = (List<Map<String, Object>>)encodedSeq.get("actions");
            for (Map<String, Object> action : actions) {
                switch (sequenceType) {
                case "pointer":
                    processPointerAction(action);
                    break;

                default:
                    throw new RuntimeException("Sequence type '" + sequenceType + "' is not supported so far");
                }
            }
        }
    }

    private void processPointerAction(Map<String, Object> action) {
        final String actionType = action.get("type").toString();

        switch (actionType) {
        case "pointerMove":
            HtmlUnitWebElement origin = (HtmlUnitWebElement) action.get("origin");
            mouseMove(origin.getElement());
            break;

        default:
            break;
        }

        System.out.println(action);
    }

    @Override
    public void resetInputState() {
        throw new RuntimeException(
                "org.openqa.selenium.interactions.Interactive.resetInputState() is not supported so far");

    }
}
