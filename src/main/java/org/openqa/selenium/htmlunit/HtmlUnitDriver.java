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

package org.openqa.selenium.htmlunit;

import static org.openqa.selenium.remote.CapabilityType.ACCEPT_INSECURE_CERTS;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import org.htmlunit.BrowserVersion;
import org.htmlunit.CookieManager;
import org.htmlunit.Page;
import org.htmlunit.ProxyConfig;
import org.htmlunit.ScriptResult;
import org.htmlunit.SgmlPage;
import org.htmlunit.TopLevelWindow;
import org.htmlunit.WaitingRefreshHandler;
import org.htmlunit.WebClient;
import org.htmlunit.WebClientOptions;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.WebWindow;
import org.htmlunit.WebWindowEvent;
import org.htmlunit.WebWindowListener;
import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.corejs.javascript.IdScriptableObject;
import org.htmlunit.corejs.javascript.NativeArray;
import org.htmlunit.corejs.javascript.NativeObject;
import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.corejs.javascript.Undefined;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.FrameWindow;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.host.Element;
import org.htmlunit.javascript.host.Location;
import org.htmlunit.javascript.host.html.DocumentProxy;
import org.htmlunit.javascript.host.html.HTMLCollection;
import org.htmlunit.javascript.host.html.HTMLElement;
import org.htmlunit.platform.AwtClipboardHandler;
import org.htmlunit.util.UrlUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.InvalidCookieDomainException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnableToSetCookieException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.htmlunit.logging.HtmlUnitLogs;
import org.openqa.selenium.htmlunit.options.HtmlUnitDriverOptions;
import org.openqa.selenium.htmlunit.w3.Action;
import org.openqa.selenium.htmlunit.w3.Algorithms;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * An implementation of {@link WebDriver} that drives
 * <a href="https://www.htmlunit.org">HtmlUnit</a>, which is a headless
 * (GUI-less) browser simulator.
 * <p>
 * The main supported browsers are Chrome, Edge, Firefox and Internet Explorer.
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Rafael Jimenez
 * @author Luke Inman-Semerau
 * @author Kay McCormick
 * @author Simon Stewart
 * @author Javier Neira
 * @author Ronald Brill
 * @author Rob Winch
 * @author Andrei Solntsev
 * @author Martin Bartoš
 * @author Scott Babcock
 */
public class HtmlUnitDriver implements WebDriver, JavascriptExecutor, HasCapabilities, Interactive {

    private static final int sleepTime = 200;

    private WebClient webClient_;
    private final HtmlUnitAlert alert_;
    private HtmlUnitWindow currentWindow_;
    private HtmlUnitKeyboard keyboard_;
    private HtmlUnitMouse mouse_;
    private final TargetLocator targetLocator_;
    private AsyncScriptExecutor asyncScriptExecutor_;
    private PageLoadStrategy pageLoadStrategy_ = PageLoadStrategy.NORMAL;
    private final ElementsMap elementsMap_ = new ElementsMap();
    private final Options options_;

    private final HtmlUnitElementFinder elementFinder_;
    private HtmlUnitInputProcessor inputProcessor_ = new HtmlUnitInputProcessor(this);

    /** BROWSER_LANGUAGE_CAPABILITY = "browserLanguage". */
    public static final String BROWSER_LANGUAGE_CAPABILITY = "browserLanguage";

    /** DOWNLOAD_IMAGES_CAPABILITY = "downloadImages". */
    public static final String DOWNLOAD_IMAGES_CAPABILITY = "downloadImages";

    /** JAVASCRIPT_ENABLED = "javascriptEnabled". */
    public static final String JAVASCRIPT_ENABLED = "javascriptEnabled";

    /**
     * The Lock for the {@link #mainCondition_}, which waits at the end of
     * {@link #runAsync(Runnable)} till either and alert is triggered, or
     * {@link Runnable} finishes.
     */
    private final Lock conditionLock_ = new ReentrantLock();
    private final Condition mainCondition_ = conditionLock_.newCondition();
    private boolean runAsyncRunning_;
    private RuntimeException exception_;
    private final ExecutorService defaultExecutor_;
    private Executor executor_;

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
    public HtmlUnitDriver(final BrowserVersion version) {
        this(version, false);
    }

    /**
     * Constructs a new instance, specify JavaScript support and using the
     * {@link BrowserVersion#getDefault() default} BrowserVersion.
     *
     * @param enableJavascript whether to enable JavaScript support or not
     */
    public HtmlUnitDriver(final boolean enableJavascript) {
        this(BrowserVersion.getDefault(), enableJavascript);
    }

    /**
     * Constructs a new instance with the specified {@link BrowserVersion} and the
     * JavaScript support.
     *
     * @param version          the browser version to use
     * @param enableJavascript whether to enable JavaScript support or not
     */
    public HtmlUnitDriver(final BrowserVersion version, final boolean enableJavascript) {
        this(new HtmlUnitDriverOptions(version, enableJavascript));
    }

    /**
     * Constructs a new instance with the specified desired and required {@link Capabilities}.
     * 
     * @param desiredCapabilities desired capabilities
     * @param requiredCapabilities required capabilities
     */
    public HtmlUnitDriver(final Capabilities desiredCapabilities, final Capabilities requiredCapabilities) {
        this(new DesiredCapabilities(desiredCapabilities, requiredCapabilities));
    }

    /**
     * The browserName is {@link Browser#HTMLUNIT} "htmlunit" and the
     * browserVersion denotes the required browser AND its version. For example
     * "chrome" for Chrome, "firefox-100" for Firefox 100.
     *
     * @param capabilities desired capabilities requested for the htmlunit driver
     *                     session
     */
    public HtmlUnitDriver(final Capabilities capabilities) {
        final HtmlUnitDriverOptions driverOptions = new HtmlUnitDriverOptions(capabilities);
        webClient_ = newWebClient(driverOptions.getWebClientVersion());

        setAcceptInsecureCerts(Boolean.FALSE != driverOptions.getCapability(ACCEPT_INSECURE_CERTS));

        final String pageLoadStrategyString = (String) driverOptions.getCapability(PAGE_LOAD_STRATEGY);
        if ("none".equals(pageLoadStrategyString)) {
            pageLoadStrategy_ = PageLoadStrategy.NONE;
        }
        else if ("eager".equals(pageLoadStrategyString)) {
            pageLoadStrategy_ = PageLoadStrategy.EAGER;
        }

        final WebClientOptions clientOptions = webClient_.getOptions();
        driverOptions.applyOptions(clientOptions);

        setProxySettings(Proxy.extractFrom(driverOptions));

        webClient_.setRefreshHandler(new WaitingRefreshHandler());
        webClient_.setClipboardHandler(new AwtClipboardHandler());

        elementFinder_ = new HtmlUnitElementFinder();

        alert_ = new HtmlUnitAlert(this);
        alert_.handleBrowserCapabilities(driverOptions);
        currentWindow_ = new HtmlUnitWindow(webClient_.getCurrentWindow());

        defaultExecutor_ = Executors.newCachedThreadPool();
        executor_ = defaultExecutor_;

        // Now put us on the home page, like a real browser
        get(clientOptions.getHomePage());

        options_ = new HtmlUnitWebDriverOptions(this);
        targetLocator_ = new HtmlUnitTargetLocator(this);

        webClient_.addWebWindowListener(new WebWindowListener() {
            @Override
            public void webWindowOpened(final WebWindowEvent webWindowEvent) {
                if (webWindowEvent.getWebWindow() instanceof TopLevelWindow) {
                    // use the first top level window we are getting aware of
                    if (currentWindow_ == null && webClient_.getTopLevelWindows().size() == 1) {
                        currentWindow_ = new HtmlUnitWindow(webClient_.getTopLevelWindows().get(0));
                    }
                }
            }

            @Override
            public void webWindowContentChanged(final WebWindowEvent event) {
                elementsMap_.remove(event.getOldPage());
                if (event.getWebWindow() != currentWindow_.getWebWindow()) {
                    return;
                }

                // Do we need to pick some new default content?
                switchToDefaultContentOfWindow(currentWindow_.getWebWindow());
            }

            @Override
            public void webWindowClosed(final WebWindowEvent event) {
                elementsMap_.remove(event.getOldPage());

                // the last window is gone
                if (getWebClient().getTopLevelWindows().size() == 0) {
                    currentWindow_ = null;
                    return;
                }

                // Check if the event window refers to us or one of our parent windows
                // setup the currentWindow appropriately if necessary
                WebWindow ourCurrentWindow = currentWindow_.getWebWindow();
                final WebWindow ourCurrentTopWindow = currentWindow_.getWebWindow().getTopWindow();
                do {
                    // Instance equality is okay in this case
                    if (ourCurrentWindow == event.getWebWindow()) {
                        setCurrentWindow(ourCurrentTopWindow);
                        return;
                    }
                    ourCurrentWindow = ourCurrentWindow.getParentWindow();
                }
                while (ourCurrentWindow != ourCurrentTopWindow);
            }
        });

        resetKeyboardAndMouseState();
        modifyWebClient(webClient_);
    }

    /**
     * @return to process or not to proceed
     */
    boolean isProcessAlert() {
        if (asyncScriptExecutor_ != null) {
            final String text = alert_.getText();
            alert_.dismiss();
            asyncScriptExecutor_.alertTriggered(text);
            return false;
        }
        conditionLock_.lock();
        try {
            mainCondition_.signal();
        }
        finally {
            conditionLock_.unlock();
        }
        return true;
    }

    /**
     * Executes the given task asynchronously on the driver's internal executor,
     * optionally blocking the calling thread until completion depending on the
     * configured {@link PageLoadStrategy}.
     *
     * <p>This method provides a unified mechanism for running mouse, keyboard,
     * and element operations in a background thread while preserving WebDriver's
     * synchronous execution semantics when required. The behavior differs based
     * on the configured page load strategy:</p>
     *
     * <ul>
     *   <li><b>{@code PageLoadStrategy.NONE}</b> — the task is dispatched
     *       asynchronously and this method returns immediately.</li>
     *
     *   <li><b>Any other strategy</b> — the calling thread waits until the task
     *       completes. The method blocks using an internal condition variable
     *       until the executed runnable signals completion.</li>
     * </ul>
     *
     * <p>Only one synchronous operation (i.e., one operation requiring waiting)
     * may run at a time. If another such operation is already executing, the
     * calling thread waits until the previous task finishes.</p>
     *
     * <p>Any {@link RuntimeException} thrown by the task is captured and
     * re-thrown on the calling thread after the task completes, preserving
     * WebDriver-consistent error reporting.</p>
     *
     * @param r the runnable task to execute; must not be {@code null}
     * @throws RuntimeException if the task throws a runtime exception during execution
     */
    protected void runAsync(final Runnable r) {
        final boolean loadStrategyWait = pageLoadStrategy_ != PageLoadStrategy.NONE;

        if (loadStrategyWait) {
            while (runAsyncRunning_) {
                try {
                    Thread.sleep(10);
                }
                catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            conditionLock_.lock();
            runAsyncRunning_ = true;
        }

        exception_ = null;
        final Runnable wrapped = () -> {
            try {
                r.run();
            }
            catch (final RuntimeException e) {
                exception_ = e;
            }
            finally {
                conditionLock_.lock();
                try {
                    runAsyncRunning_ = false;
                    mainCondition_.signal();
                }
                finally {
                    conditionLock_.unlock();
                }
            }
        };
        executor_.execute(wrapped);

        if (loadStrategyWait && runAsyncRunning_) {
            mainCondition_.awaitUninterruptibly();
            conditionLock_.unlock();
        }

        if (exception_ != null) {
            throw exception_;
        }
    }

    /**
     * Performs a click operation on the specified element.
     * <p>
     * This simulates a WebDriver pointer click by firing {@code mousedown},
     * {@code mouseup}, and {@code click} events on the target element. The action is
     * executed asynchronously.
     *
     * @param element
     *        the DOM element to receive the click
     * @param directClick
     *        if {@code true}, the click is dispatched directly without moving the
     *        mouse cursor; if {@code false}, a hover/move sequence may be performed
     *        before the click depending on HtmlUnit’s behavior
     */
    public void click(final DomElement element, final boolean directClick) {
        runAsync(() -> mouse_.click(element, directClick));
    }

    /**
     * Performs a double-click on the specified element.
     * <p>
     * This simulates a WebDriver double-click gesture by issuing two click
     * sequences in rapid succession and dispatching a {@code dblclick} event.
     * The action is executed asynchronously.
     *
     * @param element
     *        the DOM element to double-click
     */
    public void doubleClick(final DomElement element) {
        runAsync(() -> mouse_.doubleClick(element));
    }

    /**
     * Releases the pressed mouse button over the specified element.
     * <p>
     * This simulates a WebDriver {@code pointerUp} action and fires a
     * {@code mouseup} event on the element. The action is executed asynchronously.
     *
     * @param element
     *        the DOM element on which to release the mouse button
     */
    public void mouseUp(final DomElement element) {
        runAsync(() -> mouse_.mouseUp(element));
    }

    /**
     * Moves the virtual mouse cursor to the specified element.
     * <p>
     * This simulates a WebDriver {@code pointerMove} action. Moving the cursor
     * may trigger {@code mouseover}, {@code mouseenter}, and {@code mousemove}
     * events as appropriate. The action is executed asynchronously.
     *
     * @param element
     *        the DOM element to move the mouse to
     */
    public void mouseMove(final DomElement element) {
        runAsync(() -> mouse_.mouseMove(element));
    }

    /**
     * Presses the primary mouse button over the specified element.
     * <p>
     * This simulates a WebDriver {@code pointerDown} action and fires a
     * {@code mousedown} event. The action is executed asynchronously.
     *
     * @param element
     *        the DOM element on which to press the mouse button
     */
    public void mouseDown(final DomElement element) {
        runAsync(() -> mouse_.mouseDown(element));
    }

    /**
     * Submits a form associated with the specified element.
     * <p>
     * This behaves like WebDriver's form submission semantics: if the element is a
     * form, it is submitted directly; if it is a control inside a form, the
     * containing form is submitted. HTML5 form submission validation rules apply.
     * The action is executed asynchronously.
     *
     * @param element
     *        the HtmlUnitWebElement whose form should be submitted
     */
    public void submit(final HtmlUnitWebElement element) {
        runAsync(element::submitImpl);
    }

    /**
     * Sends keystrokes to the specified element.
     * <p>
     * This simulates WebDriver keyboard input, including dispatch of
     * {@code keydown}, {@code keypress}, and {@code keyup} events as appropriate.
     * Characters are inserted into the element based on standard DOM editing rules.
     * The action is executed asynchronously.
     *
     * @param element
     *        the element to receive keyboard input
     * @param value
     *        one or more sequences of characters to send
     */
    public void sendKeys(final HtmlUnitWebElement element, final CharSequence... value) {
        runAsync(() -> keyboard_.sendKeys(element, true, value));
    }

    /**
     * Get the simulated {@code BrowserVersion}.
     *
     * @return the used {@code BrowserVersion}
     */
    public BrowserVersion getBrowserVersion() {
        return webClient_.getBrowserVersion();
    }

    /**
     * Create the underlying WebClient, but don't set any fields on it.
     *
     * @param version Which browser to emulate
     * @return a new instance of WebClient.
     */
    protected WebClient newWebClient(final BrowserVersion version) {
        return new WebClient(version);
    }

    /**
     * Child classes can override this method to customize the WebClient that the
     * HtmlUnit driver uses.
     *
     * @param client The client to modify
     * @return The modified client
     */
    protected WebClient modifyWebClient(final WebClient client) {
        // Does nothing here to be overridden.
        return client;
    }

    /**
     * Returns the current {@link HtmlUnitAlert} associated with this context.
     *
     * @return the active alert instance, or {@code null} if no alert is present
     */
    public HtmlUnitAlert getAlert() {
        return alert_;
    }

    /**
     * Returns the {@link ElementsMap} that tracks elements known to this context.
     * This map typically provides lookup and indexing for DOM or component references.
     *
     * @return the elements map, never {@code null}
     */
    public ElementsMap getElementsMap() {
        return elementsMap_;
    }

    /**
     * Sets the current {@link WebWindow} for this context. If the provided window
     * differs from the currently tracked one, a new {@link HtmlUnitWindow}
     * wrapper is created and stored.
     *
     * @param window the WebWindow that should become the current window;
     *               must not be {@code null}
     */
    public void setCurrentWindow(final WebWindow window) {
        if (currentWindow_.getWebWindow() != window) {
            currentWindow_ = new HtmlUnitWindow(window);
        }
    }

    /**
     * Set proxy for WebClient using Proxy.
     *
     * @param proxy The proxy preferences.
     */
    public void setProxySettings(final Proxy proxy) {
        if (proxy == null || proxy.getProxyType() == Proxy.ProxyType.UNSPECIFIED) {
            return;
        }

        switch (proxy.getProxyType()) {
            case MANUAL:
                final List<String> noProxyHosts = new ArrayList<>();
                final String noProxy = proxy.getNoProxy();
                if (noProxy != null && !noProxy.isEmpty()) {
                    final String[] hosts = noProxy.split(",");
                    for (final String host : hosts) {
                        if (host.trim().length() > 0) {
                            noProxyHosts.add(host.trim());
                        }
                    }
                }

                final String httpProxy = proxy.getHttpProxy();
                if (httpProxy != null && !httpProxy.isEmpty()) {
                    String host = httpProxy;
                    int port = 0;

                    final int index = httpProxy.indexOf(":");
                    if (index != -1) {
                        host = httpProxy.substring(0, index);
                        port = Integer.parseInt(httpProxy.substring(index + 1));
                    }

                    setHTTPProxy(host, port, noProxyHosts);
                }

                final String socksProxy = proxy.getSocksProxy();
                if (socksProxy != null && !socksProxy.isEmpty()) {
                    String host = socksProxy;
                    int port = 0;

                    final int index = socksProxy.indexOf(":");
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
                final String pac = proxy.getProxyAutoconfigUrl();
                if (pac != null && !pac.isEmpty()) {
                    setAutoProxy(pac);
                }
                break;

            default:
                break;
        }
    }

    /**
     * Sets HTTP proxy for WebClient.
     *
     * @param host The hostname of HTTP proxy
     * @param port The port of HTTP proxy, 0 means HTTP proxy w/o port
     */
    public void setProxy(final String host, final int port) {
        setHTTPProxy(host, port, null);
    }

    /**
     * Sets HTTP proxy for WebClient with bypass proxy hosts.
     *
     * @param host         The hostname of HTTP proxy
     * @param port         The port of HTTP proxy, 0 means HTTP proxy w/o port
     * @param noProxyHosts The list of hosts which need to bypass HTTP proxy
     */
    public void setHTTPProxy(final String host, final int port, final List<String> noProxyHosts) {
        final ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setProxyHost(host);
        proxyConfig.setProxyPort(port);
        if (noProxyHosts != null && noProxyHosts.size() > 0) {
            for (final String noProxyHost : noProxyHosts) {
                proxyConfig.addHostsToProxyBypass(noProxyHost);
            }
        }
        getWebClient().getOptions().setProxyConfig(proxyConfig);
    }

    /**
     * Sets SOCKS proxy for WebClient.
     *
     * @param host The hostname of SOCKS proxy
     * @param port The port of SOCKS proxy, 0 means HTTP proxy w/o port
     */
    public void setSocksProxy(final String host, final int port) {
        setSocksProxy(host, port, null);
    }

    /**
     * Sets SOCKS proxy for WebClient with bypass proxy hosts.
     *
     * @param host         The hostname of SOCKS proxy
     * @param port         The port of SOCKS proxy, 0 means HTTP proxy w/o port
     * @param noProxyHosts The list of hosts which need to bypass SOCKS proxy
     */
    public void setSocksProxy(final String host, final int port, final List<String> noProxyHosts) {
        final ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setProxyHost(host);
        proxyConfig.setProxyPort(port);
        proxyConfig.setSocksProxy(true);
        if (noProxyHosts != null && noProxyHosts.size() > 0) {
            for (final String noProxyHost : noProxyHosts) {
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
    public void setExecutor(final Executor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor cannot be null");
        }
        executor_ = executor;
    }

    /**
     * Sets Proxy Autoconfiguration URL for WebClient.
     *
     * @param autoProxyUrl The Proxy Autoconfiguration URL
     */
    public void setAutoProxy(final String autoProxyUrl) {
        final ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setProxyAutoConfigUrl(autoProxyUrl);
        getWebClient().getOptions().setProxyConfig(proxyConfig);
    }

    @Override
    public Capabilities getCapabilities() {
        return new HtmlUnitDriverOptions(getBrowserVersion()).importOptions(webClient_.getOptions());
    }

    @Override
    public void get(final String url) {
        final URL fullUrl;
        try {
            // this takes care of data: and about:
            fullUrl = UrlUtils.toUrlUnsafe(url);
        }
        catch (final Exception e) {
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
    protected void get(final URL fullUrl) {
        getAlert().close();
        getAlert().setAutoAccept(false);
        try {
            // we can't use webClient.getPage(url) here because selenium has a different
            // idea of the current window and we like to load into to selenium current one
            final BrowserVersion browser = getBrowserVersion();
            final WebRequest request = new WebRequest(fullUrl, browser.getHtmlAcceptHeader(),
                    browser.getAcceptEncodingHeader());
            request.setCharset(StandardCharsets.UTF_8);
            getWebClient().getPage(getCurrentWindow().getWebWindow().getTopWindow(), request);

            // A "get" works over the entire page
            setCurrentWindow(getCurrentWindow().getWebWindow().getTopWindow());
        }
        catch (final UnknownHostException e) {
            throw new WebDriverException(e);
        }
        catch (final ConnectException e) {
            // This might be expected
        }
        catch (final SocketTimeoutException e) {
            throw new TimeoutException(e);
        }
        catch (final NoSuchSessionException e) {
            throw e;
        }
        catch (final NoSuchWindowException e) {
            throw e;
        }
        catch (final SSLHandshakeException e) {
            return;
        }
        catch (final Exception e) {
            throw new WebDriverException(e);
        }

        resetKeyboardAndMouseState();
    }

    private void resetKeyboardAndMouseState() {
        keyboard_ = new HtmlUnitKeyboard(this);
        mouse_ = new HtmlUnitMouse(this, keyboard_);
    }

    @Override
    public String getCurrentUrl() {
        getWebClient(); // check that session is active
        final Page page = getCurrentWindow().getWebWindow().getTopWindow().getEnclosedPage();
        if (page == null) {
            return null;
        }
        final URL url = page.getUrl();
        if (url == null) {
            return null;
        }
        return url.toString();
    }

    @Override
    public String getTitle() {
        alert_.ensureUnlocked();
        Page page = getCurrentWindow().lastPage();
        if (!(page instanceof HtmlPage)) {
            return null; // no page so there is no title
        }
        if (getCurrentWindow().getWebWindow() instanceof FrameWindow) {
            page = getCurrentWindow().getWebWindow().getTopWindow().getEnclosedPage();
        }

        return ((HtmlPage) page).getTitleText();
    }

    @Override
    public WebElement findElement(final By by) {
        alert_.ensureUnlocked();
        return implicitlyWaitFor(() -> elementFinder_.findElement(this, by));
    }

    @Override
    public List<WebElement> findElements(final By by) {
        final long implicitWait = options_.timeouts().getImplicitWaitTimeout().toMillis();
        if (implicitWait < sleepTime) {
            return elementFinder_.findElements(this, by);
        }

        final long end = System.currentTimeMillis() + implicitWait;
        List<WebElement> found;
        do {
            found = elementFinder_.findElements(this, by);
            if (!found.isEmpty()) {
                return found;
            }
            sleepQuietly(sleepTime);
        }
        while (System.currentTimeMillis() < end);

        return found;
    }

    /**
     * Locates a single {@link WebElement} using the given search context and selector.
     * <p>
     * This method applies the configured implicit wait timeout: if the element is not
     * immediately available, the lookup is repeatedly retried until it is found or
     * the timeout expires.
     * </p>
     *
     * @param element the search context, typically the parent {@link HtmlUnitWebElement}
     * @param by the locating mechanism to use
     * @return the located WebElement
     * @throws NoSuchElementException if the element cannot be found within the implicit wait
     */
    public WebElement findElement(final HtmlUnitWebElement element, final By by) {
        alert_.ensureUnlocked();
        return implicitlyWaitFor(() -> elementFinder_.findElement(element, by));
    }

    /**
     * Locates all {@link WebElement}s matching the given selector within the provided
     * search context.
     * <p>
     * If the configured implicit wait timeout exceeds the driver's polling interval,
     * the method repeatedly searches until at least one element is found or the timeout
     * expires. If the timeout is shorter than the polling interval, the search is
     * performed only once.
     * </p>
     *
     * @param element the search context, typically an {@link HtmlUnitWebElement}
     * @param by the locating strategy
     * @return a list of all matching elements; may be empty if none are found before timeout
     */
    public List<WebElement> findElements(final HtmlUnitWebElement element, final By by) {
        final long implicitWait = options_.timeouts().getImplicitWaitTimeout().toMillis();
        if (implicitWait < sleepTime) {
            return elementFinder_.findElements(element, by);
        }

        final long end = System.currentTimeMillis() + implicitWait;
        List<WebElement> found;
        do {
            found = elementFinder_.findElements(element, by);
            if (!found.isEmpty()) {
                return found;
            }
            sleepQuietly(sleepTime);
        }
        while (System.currentTimeMillis() < end);

        return found;
    }

    @Override
    public String getPageSource() {
        final Page page = getCurrentWindow().lastPage();
        if (page == null) {
            return null;
        }

        if (page instanceof SgmlPage) {
            return ((SgmlPage) page).asXml();
        }
        final WebResponse response = page.getWebResponse();
        return response.getContentAsString();
    }

    @Override
    public void close() {
        getWebClient(); // check that session is active
        if (getWebClient().getWebWindows().size() == 1) {
            // closing the last window is equivalent to quit
            quit();
        }
        else {
            final WebWindow thisWindow = getCurrentWindow().getWebWindow(); // check that the current window is active
            if (thisWindow != null) {
                alert_.close();
                ((TopLevelWindow) thisWindow.getTopWindow()).close();
            }
            if (getWebClient().getWebWindows().size() == 0) {
                quit();
            }
        }
    }

    @Override
    public void quit() {
        // closing the web client while some async processes are running
        // will produce strange effects; therefore wait until they are done
        while (runAsyncRunning_) {
            try {
                Thread.sleep(10);
            }
            catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        conditionLock_.lock();
        runAsyncRunning_ = true;
        try {
            if (webClient_ != null) {
                alert_.close();
                webClient_.close();
                webClient_ = null;
            }
            defaultExecutor_.shutdown();
        }
        finally {
            runAsyncRunning_ = false;
            conditionLock_.unlock();
        }
    }

    @Override
    public Set<String> getWindowHandles() {
        final Set<String> allHandles = new HashSet<>();
        for (final WebWindow window : getWebClient().getTopLevelWindows()) {
            allHandles.add(String.valueOf(System.identityHashCode(window)));
        }

        return allHandles;
    }

    @Override
    public String getWindowHandle() {
        final WebWindow topWindow = getCurrentWindow().getWebWindow().getTopWindow();
        if (topWindow.isClosed()) {
            throw new NoSuchWindowException("Window is closed");
        }
        return String.valueOf(System.identityHashCode(topWindow));
    }

    @Override
    public Object executeScript(String script, final Object... args) {
        final HtmlPage page = getPageToInjectScriptInto();

        script = "let huDriverFoo = function() {" + script + "\n}; huDriverFoo;";
        ScriptResult result = page.executeJavaScript(script);
        final Object function = result.getJavaScriptResult();

        final Object[] parameters = convertScriptArgs(page, args);

        try {
            result = page.executeJavaScriptFunction(function, getCurrentWindow().getWebWindow().getScriptableObject(),
                    parameters, page.getDocumentElement());

            return parseNativeJavascriptResult(result);
        }
        catch (final Throwable ex) {
            throw new WebDriverException(ex);
        }
    }

    @Override
    public Object executeAsyncScript(final String script, Object... args) {
        final HtmlPage page = getPageToInjectScriptInto();
        args = convertScriptArgs(page, args);

        asyncScriptExecutor_ = new AsyncScriptExecutor(page, options_.timeouts().getScriptTimeout().toMillis());
        try {
            final Object result = asyncScriptExecutor_.execute(script, args);

            alert_.ensureUnlocked();
            return parseNativeJavascriptResult(result);
        }
        finally {
            asyncScriptExecutor_ = null;
        }
    }

    private Object[] convertScriptArgs(final HtmlPage page, final Object[] args) {
        final HtmlUnitScriptable scope = page.getEnclosingWindow().getScriptableObject();

        if (scope == null) {
            return args;
        }

        final Object[] parameters = new Object[args.length];
        Context.enter();
        try {
            for (int i = 0; i < args.length; i++) {
                parameters[i] = parseArgumentIntoJavascriptParameter(scope, args[i]);
            }
        }
        finally {
            Context.exit();
        }
        return parameters;
    }

    private HtmlPage getPageToInjectScriptInto() {
        if (!isJavascriptEnabled()) {
            throw new UnsupportedOperationException("Javascript is not enabled for this HtmlUnitDriver instance");
        }

        final Page lastPage = getCurrentWindow().lastPage();
        if (!(lastPage instanceof HtmlPage)) {
            throw new UnsupportedOperationException("Cannot execute JS against a plain text page");
        }

        return (HtmlPage) lastPage;
    }

    private Object parseArgumentIntoJavascriptParameter(final Scriptable scope, Object arg) {
        while (arg instanceof WrapsElement) {
            arg = ((WrapsElement) arg).getWrappedElement();
        }

        if (!(arg instanceof HtmlUnitWebElement
                || arg instanceof HtmlElement
                || arg instanceof Number // special case the underlying type
                || arg instanceof String
                || arg instanceof Boolean

                || arg instanceof Object[]
                || arg instanceof int[]
                || arg instanceof long[]
                || arg instanceof float[]
                || arg instanceof double[]
                || arg instanceof boolean[]

                || arg instanceof Collection<?> || arg instanceof Map<?, ?>)) {
            throw new IllegalArgumentException(
                    "Argument must be a string, number, boolean or WebElement: " + arg + " (" + arg.getClass() + ")");
        }

        if (arg instanceof HtmlUnitWebElement) {
            final HtmlUnitWebElement webElement = (HtmlUnitWebElement) arg;
            assertElementNotStale(webElement.getElement());
            return webElement.getElement().getScriptableObject();
        }
        else if (arg instanceof HtmlElement) {
            final HtmlElement element = (HtmlElement) arg;
            assertElementNotStale(element);
            return element.getScriptableObject();
        }
        else if (arg instanceof Collection<?>) {
            final List<Object> list = new ArrayList<>();
            for (final Object o : (Collection<?>) arg) {
                list.add(parseArgumentIntoJavascriptParameter(scope, o));
            }
            return Context.getCurrentContext().newArray(scope, list.toArray());
        }

        else if (arg instanceof Object[]) {
            final List<Object> list = new ArrayList<>();
            for (final Object o : (Object[]) arg) {
                list.add(parseArgumentIntoJavascriptParameter(scope, o));
            }
            return Context.getCurrentContext().newArray(scope, list.toArray());
        }
        else if (arg instanceof int[]) {
            final List<Object> list = new ArrayList<>();
            for (final Object o : (int[]) arg) {
                list.add(parseArgumentIntoJavascriptParameter(scope, o));
            }
            return Context.getCurrentContext().newArray(scope, list.toArray());
        }
        else if (arg instanceof long[]) {
            final List<Object> list = new ArrayList<>();
            for (final Object o : (long[]) arg) {
                list.add(parseArgumentIntoJavascriptParameter(scope, o));
            }
            return Context.getCurrentContext().newArray(scope, list.toArray());
        }
        else if (arg instanceof float[]) {
            final List<Object> list = new ArrayList<>();
            for (final Object o : (float[]) arg) {
                list.add(parseArgumentIntoJavascriptParameter(scope, o));
            }
            return Context.getCurrentContext().newArray(scope, list.toArray());
        }
        else if (arg instanceof double[]) {
            final List<Object> list = new ArrayList<>();
            for (final Object o : (double[]) arg) {
                list.add(parseArgumentIntoJavascriptParameter(scope, o));
            }
            return Context.getCurrentContext().newArray(scope, list.toArray());
        }
        else if (arg instanceof boolean[]) {
            final List<Object> list = new ArrayList<>();
            for (final Object o : (boolean[]) arg) {
                list.add(parseArgumentIntoJavascriptParameter(scope, o));
            }
            return Context.getCurrentContext().newArray(scope, list.toArray());
        }

        else if (arg instanceof Map<?, ?>) {
            final Map<?, ?> map = (Map<?, ?>) arg;
            final Scriptable obj = Context.getCurrentContext().newObject(scope);
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                obj.put((String) entry.getKey(), obj, parseArgumentIntoJavascriptParameter(scope, entry.getValue()));
            }
            return obj;
        }

        else {
            return arg;
        }
    }

    /**
     * Verifies that the provided {@link DomElement} is still attached to the
     * current page and therefore safe for user interaction.
     *
     * <p>This check mirrors Selenium's {@code StaleElementReferenceException}
     * behavior. An element is considered stale if:</p>
     *
     * <ul>
     *   <li>the element belongs to a different page than the driver's current window, or</li>
     *   <li>the element is no longer part of the DOM tree (i.e., it has no ancestor
     *       that is an {@link SgmlPage}).</li>
     * </ul>
     *
     * <p>If either condition is detected, a {@link StaleElementReferenceException}
     * is thrown, indicating that the element cannot be interacted with.</p>
     *
     * @param element the element to validate; must not be {@code null}
     * @throws StaleElementReferenceException if the element is detached, removed,
     *                                        or belongs to a different page
     */
    protected void assertElementNotStale(final DomElement element) {
        final SgmlPage elementPage = element.getPage();
        final Page lastPage = getCurrentWindow().lastPage();

        if (!lastPage.equals(elementPage)) {
            throw new StaleElementReferenceException(
                "Element appears to be stale. Did you navigate away from the page that contained it? "
                + "And is the current window focused the same as the one holding this element?");
        }

        // We need to walk the DOM to determine if the element is actually attached
        DomNode parentElement = element;
        while (parentElement != null && !(parentElement instanceof SgmlPage)) {
            parentElement = parentElement.getParentNode();
        }

        if (parentElement == null) {
            throw new StaleElementReferenceException(
                "The element seems to be disconnected from the DOM. "
                + "This means that a user cannot interact with it.");
        }
    }

    /**
     * Returns the driver's keyboard implementation.
     *
     * <p>This object is used to simulate keyboard input, including sending keys
     * to elements and generating key press/release events.</p>
     *
     * @return the {@link HtmlUnitKeyboard} associated with this driver
     */
    public HtmlUnitKeyboard getKeyboard() {
        return keyboard_;
    }

    /**
     * Returns the driver's mouse implementation.
     *
     * <p>The returned object supports pointer-based interactions such as clicking,
     * moving the pointer, and pressing or releasing mouse buttons.</p>
     *
     * @return the {@link HtmlUnitMouse} associated with this driver
     */
    public HtmlUnitMouse getMouse() {
        return mouse_;
    }

    /**
     * Represents a JavaScript array-like result set returned from script execution.
     *
     * <p>This interface abstracts collection-like objects that may be returned
     * from JavaScript (e.g., arrays, NodeLists, or custom JS collections). It
     * provides minimal operations needed to iterate over such results.</p>
     */
    protected interface JavaScriptResultsCollection {

        /**
         * Returns the number of items in the collection.
         *
         * @return the collection length
         */
        int getLength();

        /**
         * Returns the item at the specified index within the collection.
         *
         * @param index the zero-based index of the item to retrieve
         * @return the object at the specified index, or {@code null} if none exists
         */
        Object item(int index);
    }

    private Object parseNativeJavascriptResult(final Object result) {
        final Object value;
        if (result instanceof ScriptResult) {
            value = ((ScriptResult) result).getJavaScriptResult();
        }
        else {
            value = result;
        }
        if (value instanceof HTMLElement) {
            return toWebElement(((HTMLElement) value).getDomNodeOrDie());
        }

        if (value instanceof DocumentProxy) {
            final Element element = ((DocumentProxy) value).getDelegee().getDocumentElement();
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
            final Map<String, Object> map = new HashMap<>((NativeObject) value);
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

            final JavaScriptResultsCollection collection = new JavaScriptResultsCollection() {
                @Override
                public int getLength() {
                    return (int) array.getLength();
                }

                @Override
                public Object item(final int index) {
                    return array.get(index);
                }
            };

            return parseJavascriptResultsList(collection);
        }

        if (value instanceof HTMLCollection) {
            final HTMLCollection array = (HTMLCollection) value;

            final JavaScriptResultsCollection collection = new JavaScriptResultsCollection() {
                @Override
                public int getLength() {
                    return array.getLength();
                }

                @Override
                public Object item(final int index) {
                    return array.get(index);
                }
            };

            return parseJavascriptResultsList(collection);
        }

        if (value instanceof IdScriptableObject && value.getClass().getSimpleName().equals("NativeDate")) {

            final long l = ((Number) getPrivateField(value, "date")).longValue();
            return Instant.ofEpochMilli(l).toString();
        }

        if (Undefined.isUndefined(value)) {
            return null;
        }

        return value;
    }

    private static Object getPrivateField(final Object o, final String fieldName) {
        try {
            final Field field = o.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(o);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Object> convertLocationToMap(final Location location) {
        final Map<String, Object> map = new HashMap<>();
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

    private List<Object> parseJavascriptResultsList(final JavaScriptResultsCollection array) {
        final List<Object> list = new ArrayList<>(array.getLength());
        for (int i = 0; i < array.getLength(); ++i) {
            list.add(parseNativeJavascriptResult(array.item(i)));
        }
        return list;
    }

    @Override
    public TargetLocator switchTo() {
        return targetLocator_;
    }

    @Override
    public Navigation navigate() {
        return new HtmlUnitNavigation();
    }

    /**
     * Wraps the given {@link DomElement} into a corresponding {@link HtmlUnitWebElement},
     * creating and caching the wrapper if it does not already exist.
     *
     * @param element the DOM element to wrap; must not be {@code null}
     * @return the associated {@link HtmlUnitWebElement}, never {@code null}
     */
    protected HtmlUnitWebElement toWebElement(final DomElement element) {
        return getElementsMap().addIfAbsent(this, element);
    }

    /**
     * Retrieves an existing {@link HtmlUnitWebElement} by its element ID.
     *
     * <p>This method does not search the DOM; it only returns an element previously
     * known and mapped by {@link ElementsMap}.</p>
     *
     * @param elementId the unique ID of the element to retrieve
     * @return the corresponding {@link HtmlUnitWebElement}, or {@code null} if it is not mapped
     */
    public HtmlUnitWebElement toWebElement(final String elementId) {
        return getElementsMap().getWebElement(elementId);
    }

    /**
     * Indicates whether JavaScript execution is enabled in the underlying {@link WebClient}.
     *
     * @return {@code true} if JavaScript is enabled, {@code false} otherwise
     */
    public boolean isJavascriptEnabled() {
        return getWebClient().getOptions().isJavaScriptEnabled();
    }

    /**
     * Enables or disables JavaScript execution in the underlying {@link WebClient}.
     *
     * @param enableJavascript {@code true} to enable JavaScript, {@code false} to disable it
     */
    public void setJavascriptEnabled(final boolean enableJavascript) {
        getWebClient().getOptions().setJavaScriptEnabled(enableJavascript);
    }

    /**
     * Indicates whether automatic image downloading is enabled.
     *
     * @return {@code true} if images will be downloaded, {@code false} otherwise
     */
    public boolean isDownloadImages() {
        return getWebClient().getOptions().isDownloadImages();
    }

    /**
     * Enables or disables automatic image downloading.
     *
     * @param downloadImages {@code true} to download images, {@code false} otherwise
     */
    public void setDownloadImages(final boolean downloadImages) {
        getWebClient().getOptions().setDownloadImages(downloadImages);
    }

    /**
     * Configures whether the driver should accept insecure SSL/TLS certificates.
     *
     * @param accept {@code true} to allow insecure certificates, {@code false} to reject them
     */
    public void setAcceptInsecureCerts(final boolean accept) {
        getWebClient().getOptions().setUseInsecureSSL(accept);
    }

    /**
     * Indicates whether insecure SSL/TLS certificates are accepted.
     *
     * @return {@code true} if insecure certificates are allowed, {@code false} otherwise
     */
    public boolean isAcceptInsecureCerts() {
        return getWebClient().getOptions().isUseInsecureSSL();
    }

    /**
     * Executes the given condition using Selenium-style implicit wait semantics.
     *
     * <p>The method repeatedly invokes the provided {@link Callable} until it either:
     * <ul>
     *   <li>returns a non-{@code null} value,</li>
     *   <li>returns {@code true} if the return type is {@link Boolean},</li>
     *   <li>or the implicit wait timeout expires.</li>
     * </ul>
     *
     * <p>If the implicit wait timeout is shorter than the configured sleep interval,
     * the condition is evaluated only once.</p>
     *
     * @param <X>       the return type of the condition
     * @param condition the operation to evaluate until it succeeds or times out
     * @return the successful result of the condition, or {@code null} if it timed out
     * @throws WebDriverException if the condition throws an exception
     *         that is not a {@link RuntimeException}
     * @throws RuntimeException if the last invocation of the condition throws one
     */
    protected <X> X implicitlyWaitFor(final Callable<X> condition) {
        final long implicitWait = options_.timeouts().getImplicitWaitTimeout().toMillis();

        if (implicitWait < sleepTime) {
            try {
                return condition.call();
            }
            catch (final RuntimeException e) {
                throw e;
            }
            catch (final Exception e) {
                throw new WebDriverException(e);
            }
        }

        final long end = System.currentTimeMillis() + implicitWait;
        Exception lastException = null;

        do {
            X toReturn = null;
            try {
                toReturn = condition.call();
            }
            catch (final Exception e) {
                lastException = e;
            }

            if (toReturn instanceof Boolean && !(Boolean) toReturn) {
                continue;
            }

            if (toReturn != null) {
                return toReturn;
            }

            sleepQuietly(sleepTime);
        }
        while (System.currentTimeMillis() < end);

        if (lastException != null) {
            if (lastException instanceof RuntimeException) {
                throw (RuntimeException) lastException;
            }
            throw new WebDriverException(lastException);
        }

        return null;
    }

    /**
     * Returns the underlying {@link WebClient} used by this driver.
     *
     * @return the active {@link WebClient}
     * @throws NoSuchSessionException if the session has been closed
     */
    public WebClient getWebClient() {
        if (webClient_ == null) {
            throw new NoSuchSessionException("Session is closed");
        }
        return webClient_;
    }

    /**
     * Returns the current {@link HtmlUnitWindow} used by this driver.
     *
     * @return the active window
     * @throws NoSuchSessionException if the session has been closed
     * @throws NoSuchWindowException if the current window reference is invalid or the window is closed
     */
    public HtmlUnitWindow getCurrentWindow() {
        if (webClient_ == null || currentWindow_ == null) {
            throw new NoSuchSessionException("Session is closed");
        }
        if (currentWindow_.getWebWindow().isClosed()) {
            throw new NoSuchWindowException("Window is closed");
        }
        return currentWindow_;
    }

    private final class HtmlUnitNavigation implements Navigation {

        @Override
        public void back() {
            runAsync(() -> {
                try {
                    getCurrentWindow().getWebWindow().getHistory().back();
                }
                catch (final IOException e) {
                    throw new WebDriverException(e);
                }
            });
        }

        @Override
        public void forward() {
            runAsync(() -> {
                try {
                    getCurrentWindow().getWebWindow().getHistory().forward();
                }
                catch (final IOException e) {
                    throw new WebDriverException(e);
                }
            });
        }

        @Override
        public void to(final String url) {
            get(url);
        }

        @Override
        public void to(final URL url) {
            get(url);
        }

        @Override
        public void refresh() {
            if (getCurrentWindow().lastPage() instanceof HtmlPage) {
                runAsync(() -> {
                    try {
                        ((HtmlPage) getCurrentWindow().lastPage()).refresh();
                    }
                    catch (final SocketTimeoutException e) {
                        throw new TimeoutException(e);
                    }
                    catch (final IOException e) {
                        throw new WebDriverException(e);
                    }
                });
            }
        }
    }

    @Override
    public Options manage() {
        return options_;
    }

    private static void sleepQuietly(final long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (final InterruptedException ignored) {
        }
    }

    private enum PageLoadStrategy {
        NORMAL, EAGER, NONE
    }

    /**
     * Maintains a bidirectional mapping between {@link DomElement} instances and their
     * corresponding {@link HtmlUnitWebElement} wrappers.
     *
     * <p>The mapping is stored per {@link SgmlPage}, allowing elements to be reused
     * across driver operations and ensuring that each DOM element has a stable,
     * unique {@code HtmlUnitWebElement} representation for the lifetime of its page.</p>
     *
     * <p>Element wrappers are automatically removed when the associated page is removed,
     * preventing memory leaks. Page-level maps are stored in a {@link WeakHashMap},
     * allowing them to be reclaimed when their pages are no longer referenced.</p>
     */
    protected static class ElementsMap {

        private final Map<SgmlPage, Map<DomElement, HtmlUnitWebElement>> elementsMapByPage_;
        private final Map<String, HtmlUnitWebElement> elementsMapById_;
        private int idCounter_;

        /**
         * Creates a new, empty {@code ElementsMap}.
         *
         * <p>Each instance assigns unique element IDs starting from 1 and maintains
         * internal lookup structures for both page-based mappings and global ID-based lookup.</p>
         */
        public ElementsMap() {
            elementsMapByPage_ = new WeakHashMap<>();
            elementsMapById_ = new HashMap<>();
            idCounter_ = 0;
        }

        /**
         * Returns an existing {@link HtmlUnitWebElement} wrapper for the given
         * {@link DomElement}, or creates and registers a new one if none exists.
         *
         * <p>The wrapper is stored in both:</p>
         * <ul>
         *   <li>the page-specific element map, keyed by the actual DOM element, and</li>
         *   <li>a global ID-based map used for lookup by element identifier.</li>
         * </ul>
         * <p>Each new wrapper is assigned a unique integer ID.</p>
         *
         * @param driver  the owning {@link HtmlUnitDriver}; must not be {@code null}
         * @param element the DOM element to wrap; must not be {@code null}
         * @return the existing or newly created {@link HtmlUnitWebElement}
         */
        public HtmlUnitWebElement addIfAbsent(final HtmlUnitDriver driver, final DomElement element) {
            final Map<DomElement, HtmlUnitWebElement> pageMap =
                    elementsMapByPage_.computeIfAbsent(element.getPage(), k -> new HashMap<>());

            HtmlUnitWebElement e = pageMap.get(element);
            if (e == null) {
                idCounter_++;
                e = new HtmlUnitWebElement(driver, idCounter_, element);
                pageMap.put(element, e);
                elementsMapById_.put(Integer.toString(idCounter_), e);
            }
            return e;
        }

        /**
         * Removes all element mappings associated with the specified {@link Page}.
         *
         * <p>This method is typically invoked when a page is navigated away from or discarded,
         * removing stale element references and freeing associated memory.</p>
         *
         * @param page the page whose element mappings should be removed;
         *             may be {@code null}, in which case nothing is removed
         */
        public void remove(final Page page) {
            final Map<DomElement, HtmlUnitWebElement> pageMap = elementsMapByPage_.remove(page);
            if (pageMap != null) {
                pageMap.values().forEach(element ->
                        elementsMapById_.remove(Integer.toString(element.getId())));
            }
        }

        /**
         * Retrieves a previously registered {@link HtmlUnitWebElement} by its element ID.
         *
         * <p>If the element is no longer valid—typically because the page was replaced—
         * this method throws a {@link StaleElementReferenceException}, matching Selenium
         * semantics.</p>
         *
         * @param elementId the string identifier of the element
         * @return the associated {@link HtmlUnitWebElement}; never {@code null}
         * @throws StaleElementReferenceException if no element is registered under the given ID
         */
        public HtmlUnitWebElement getWebElement(final String elementId) {
            final HtmlUnitWebElement webElement = elementsMapById_.get(elementId);
            if (webElement == null) {
                throw new StaleElementReferenceException(
                        "Failed finding web element associated with identifier: " + elementId);
            }
            return webElement;
        }
    }

    @Override
    public void perform(final Collection<Sequence> sequences) {
        // https://www.w3.org/TR/webdriver/#perform-actions

        // Let input state be the result of get the input state with current session and
        // current top-level browsing context.

        // Let actions by tick be the result of trying to extract an action sequence
        // given input state, and parameters.
        final List<List<Action>> actionsByTick = Algorithms.extractActionSequence(sequences);

        // If the current browsing context is no longer open, return error with error
        // code no such window.

        // Handle any user prompts. If this results in an error, return that error.

        // Dispatch actions given input state, actions by tick, and current browsing
        // context.
        // If this results in an error return that error.
        Algorithms.dispatchActions(/* inputState_, */ actionsByTick, inputProcessor_);

        // Return success with data null.
    }

    @Override
    public void resetInputState() {
        inputProcessor_ = new HtmlUnitInputProcessor(this);
    }

    /**
     * Switches the driver's context to the default content of the specified window.
     *
     * <p>This method inspects the provided {@link WebWindow}, retrieves its
     * enclosed {@link Page}, and—if that page is an {@link HtmlPage}—switches the
     * current window to the page's enclosing top-level window.</p>
     *
     * <p>In effect, this resets the driver's frame context for the given window,
     * analogous to Selenium's {@code switchTo().defaultContent()}.</p>
     *
     * @param window the {@link WebWindow} whose default content should become active;
     *               must not be {@code null}
     */
    protected void switchToDefaultContentOfWindow(final WebWindow window) {
        final Page page = window.getEnclosedPage();
        if (page instanceof HtmlPage) {
            setCurrentWindow(page.getEnclosingWindow());
        }
    }

    /**
     * Opens a new, blank browser window and sets it as the driver's current window.
     *
     * <p>This method calls {@code webClient_.openWindow()} using {@code about:blank}
     * as the initial page, then updates the driver's internal window reference to
     * point to the newly created window.</p>
     *
     * <p>Similar to Selenium's {@code driver.switchTo().newWindow(WindowType.WINDOW)},
     * but implemented using HtmlUnit's headless window model.</p>
     */
    public void openNewWindow() {
        final WebWindow newWindow = webClient_.openWindow(UrlUtils.URL_ABOUT_BLANK, "");
        currentWindow_ = new HtmlUnitWindow(newWindow);
    }

    /**
     * Implementation of {@link WebDriver.Options} for {@link HtmlUnitDriver}.
     *
     * <p>This inner class provides driver-scoped configuration objects such as
     * logging support ({@link HtmlUnitLogs}) and timeout management 
     * ({@link HtmlUnitTimeouts}).</p>
     *
     * <p>Instances are typically created by the enclosing {@link HtmlUnitDriver}
     * and exposed via {@code driver.manage()}.</p>
     */
    protected class HtmlUnitWebDriverOptions implements WebDriver.Options {

        private final HtmlUnitLogs logs_;
        private final HtmlUnitDriver driver_;
        private final HtmlUnitTimeouts timeouts_;

        /**
         * Creates a new {@code HtmlUnitWebDriverOptions} bound to the given driver.
         *
         * @param driver the owning {@link HtmlUnitDriver}; must not be {@code null}
         */
        protected HtmlUnitWebDriverOptions(final HtmlUnitDriver driver) {
            driver_ = driver;
            logs_ = new HtmlUnitLogs(getWebClient());
            timeouts_ = new HtmlUnitTimeouts(getWebClient());
        }

        @Override
        public Logs logs() {
            return logs_;
        }

        @Override
        public void addCookie(final Cookie cookie) {
            final Page page = window().lastPage();
            if (!(page instanceof HtmlPage)) {
                throw new UnableToSetCookieException("You may not set cookies on a page that is not HTML");
            }

            final String domain = getDomainForCookie();
            verifyDomain(cookie, domain);

            getWebClient().getCookieManager().addCookie(
                    new org.htmlunit.util.Cookie(
                            domain,
                            cookie.getName(),
                            cookie.getValue(),
                            cookie.getPath(),
                            cookie.getExpiry(),
                            cookie.isSecure(),
                            cookie.isHttpOnly(),
                            cookie.getSameSite()));
        }

        private void verifyDomain(final Cookie cookie, String expectedDomain) {
            String domain = cookie.getDomain();
            if (domain == null) {
                return;
            }

            if ("".equals(domain)) {
                throw new InvalidCookieDomainException(
                        "Domain must not be an empty string. Consider using null instead");
            }

            // Line-noise-tastic
            if (domain.matches(".*[^:]:\\d+$")) {
                domain = domain.replaceFirst(":\\d+$", "");
            }

            expectedDomain = expectedDomain.startsWith(".") ? expectedDomain : "." + expectedDomain;
            domain = domain.startsWith(".") ? domain : "." + domain;

            if (!expectedDomain.endsWith(domain)) {
                throw new InvalidCookieDomainException(
                        String.format("You may only add cookies that would be visible to the current domain: %s => %s",
                                domain, expectedDomain));
            }
        }

        @Override
        public Cookie getCookieNamed(final String name) {
            final Set<Cookie> allCookies = getCookies();
            for (final Cookie cookie : allCookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }

            return null;
        }

        @Override
        public void deleteCookieNamed(final String name) {
            final CookieManager cookieManager = getWebClient().getCookieManager();

            final URL url = getRawUrl();
            final Set<org.htmlunit.util.Cookie> rawCookies = getWebClient().getCookies(url);
            for (final org.htmlunit.util.Cookie cookie : rawCookies) {
                if (name.equals(cookie.getName())) {
                    cookieManager.removeCookie(cookie);
                }
            }
        }

        @Override
        public void deleteCookie(final Cookie cookie) {
            getWebClient().getCookieManager().removeCookie(convertSeleniumCookieToHtmlUnit(cookie));
        }

        @Override
        public void deleteAllCookies() {
            final CookieManager cookieManager = getWebClient().getCookieManager();

            final URL url = getRawUrl();
            final Set<org.htmlunit.util.Cookie> rawCookies = getWebClient().getCookies(url);
            for (final org.htmlunit.util.Cookie cookie : rawCookies) {
                cookieManager.removeCookie(cookie);
            }
        }

        @Override
        public Set<Cookie> getCookies() {
            final URL url = getRawUrl();

            // The about:blank URL (the default in case no navigation took place)
            // does not have a valid 'hostname' part and cannot be used for creating
            // cookies based on it - return an empty set.

            if (!url.toString().startsWith("http")) {
                return Collections.emptySet();
            }

            final Set<Cookie> result = new HashSet<>();
            for (final org.htmlunit.util.Cookie c : getWebClient().getCookies(url)) {
                result .add(
                        new Cookie.Builder(c.getName(), c.getValue())
                        .domain(c.getDomain())
                        .path(c.getPath())
                        .expiresOn(c.getExpires())
                        .isSecure(c.isSecure())
                        .isHttpOnly(c.isHttpOnly())
                        .sameSite(c.getSameSite())
                        .build());
            }

            return Collections.unmodifiableSet(result);
        }

        private org.htmlunit.util.Cookie convertSeleniumCookieToHtmlUnit(final Cookie cookie) {
            return new org.htmlunit.util.Cookie(
                    cookie.getDomain(),
                    cookie.getName(),
                    cookie.getValue(),
                    cookie.getPath(),
                    cookie.getExpiry(),
                    cookie.isSecure(),
                    cookie.isHttpOnly(),
                    cookie.getSameSite());
        }

        private String getDomainForCookie() {
            final URL current = getRawUrl();
            return current.getHost();
        }

        private WebClient getWebClient() {
            return driver_.getWebClient();
        }

        @Override
        public WebDriver.Timeouts timeouts() {
            return timeouts_;
        }

        @Override
        public HtmlUnitWindow window() {
            return driver_.getCurrentWindow();
        }

        private URL getRawUrl() {
            return Optional.ofNullable(window().lastPage()).map(Page::getUrl).orElse(null);
        }
    }
}
