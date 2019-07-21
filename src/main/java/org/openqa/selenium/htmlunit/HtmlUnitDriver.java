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

import static org.openqa.selenium.remote.CapabilityType.ACCEPT_SSL_CERTS;
import static org.openqa.selenium.remote.CapabilityType.PAGE_LOAD_STRATEGY;
import static org.openqa.selenium.remote.CapabilityType.SUPPORTS_FINDING_BY_CSS;
import static org.openqa.selenium.remote.CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLHandshakeException;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.InvalidCookieDomainException;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnableToSetCookieException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.internal.FindsByClassName;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.htmlunit.logging.HtmlUnitLogs;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.css.parser.CSSException;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.BrowserVersion.BrowserVersionBuilder;
import com.gargoylesoftware.htmlunit.CookieManager;
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
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.WebWindowNotFoundException;
import com.gargoylesoftware.htmlunit.html.BaseFrameElement;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHtml;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.Element;
import com.gargoylesoftware.htmlunit.javascript.host.Location;
import com.gargoylesoftware.htmlunit.javascript.host.html.DocumentProxy;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLCollection;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.IdScriptableObject;
import net.sourceforge.htmlunit.corejs.javascript.NativeArray;
import net.sourceforge.htmlunit.corejs.javascript.NativeObject;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.Undefined;

/**
 * An implementation of {@link WebDriver} that drives <a href="http://htmlunit.sourceforge.net/">HtmlUnit</a>,
 * which is a headless (GUI-less) browser simulator.
 * <p>The main supported browsers are Chrome, Firefox and Internet Explorer.
 */
public class HtmlUnitDriver implements WebDriver, JavascriptExecutor,
    FindsById, FindsByLinkText, FindsByXPath, FindsByName, FindsByCssSelector,
    FindsByTagName, FindsByClassName, HasCapabilities, HasInputDevices {

  private static final int sleepTime = 200;

  private WebClient webClient;
  private WebWindow currentWindow;
  private HtmlUnitAlert alert;

  // Fictive position just to implement the API
  private Point windowPosition = new Point(0, 0);
  private Dimension initialWindowDimension;

  private boolean enableJavascript;
  private ProxyConfig proxyConfig;
  private long implicitWait = 0;
  private long scriptTimeout = 0;
  private HtmlUnitKeyboard keyboard;
  private HtmlUnitMouse mouse;
  private boolean gotPage;
  private TargetLocator targetLocator = new HtmlUnitTargetLocator();
  private AsyncScriptExecutor asyncScriptExecutor;
  private UnexpectedAlertBehaviour unexpectedAlertBehaviour;
  private PageLoadStrategy pageLoadStrategy = PageLoadStrategy.NORMAL;
  private int elementsCounter;
  private Map<SgmlPage, Map<DomElement, HtmlUnitWebElement>> elementsMap = new WeakHashMap<>();
  private Options options;

  public static final String INVALIDXPATHERROR = "The xpath expression '%s' cannot be evaluated";
  public static final String INVALIDSELECTIONERROR =
      "The xpath expression '%s' selected an object of type '%s' instead of a WebElement";

  public static final String BROWSER_LANGUAGE_CAPABILITY = "browserLanguage";
  public static final String DOWNLOAD_IMAGES_CAPABILITY = "downloadImages";
  public static final String JAVASCRIPT_ENABLED = "javascriptEnabled";

  /** The Lock for the {@link #mainCondition}, which waits at the end of {@link #runAsync(Runnable)}
   * till either and alert is triggered, or {@link Runnable} finishes. */
  private Lock conditionLock = new ReentrantLock();
  private Condition mainCondition = conditionLock.newCondition();
  private boolean runAsyncRunning;
  private RuntimeException exception;
  private final ExecutorService defaultExecutor;
  private Executor executor;

  /**
   * Constructs a new instance with JavaScript disabled,
   * and the {@link BrowserVersion#getDefault() default} BrowserVersion.
   */
  public HtmlUnitDriver() {
    this(false);
  }

  /**
   * Constructs a new instance, specify JavaScript support
   * and using the {@link BrowserVersion#getDefault() default} BrowserVersion.
   *
   * @param enableJavascript whether to enable JavaScript support or not
   */
  public HtmlUnitDriver(boolean enableJavascript) {
    this(BrowserVersion.getDefault(), enableJavascript);
  }

  /**
   * Constructs a new instance with the specified {@link BrowserVersion} and the JavaScript support.
   *
   * @param version the browser version to use
   * @param enableJavascript whether to enable JavaScript support or not
   */
  public HtmlUnitDriver(BrowserVersion version, boolean enableJavascript) {
    this(version);
    setJavascriptEnabled(enableJavascript);
  }

  /**
   * Constructs a new instance with the specified {@link BrowserVersion}.
   *
   * @param version the browser version to use
   */
  public HtmlUnitDriver(BrowserVersion version) {
    webClient = createWebClient(version);
    alert = new HtmlUnitAlert(this);
    currentWindow = webClient.getCurrentWindow();
    initialWindowDimension = new Dimension(currentWindow.getOuterWidth(), currentWindow.getOuterHeight());
    unexpectedAlertBehaviour = UnexpectedAlertBehaviour.DISMISS_AND_NOTIFY;

    defaultExecutor = Executors.newCachedThreadPool();
    executor = defaultExecutor;


    webClient.addWebWindowListener(new WebWindowListener() {
      @Override
      public void webWindowOpened(WebWindowEvent webWindowEvent) {
        // Ignore
      }

      @Override
      public void webWindowContentChanged(WebWindowEvent event) {
        elementsMap.remove(event.getOldPage());
        if (event.getWebWindow() != currentWindow) {
          return;
        }

        // Do we need to pick some new default content?
        switchToDefaultContentOfWindow(currentWindow);
      }

      @Override
      public void webWindowClosed(WebWindowEvent event) {
        elementsMap.remove(event.getOldPage());
        // Check if the event window refers to us or one of our parent windows
        // setup the currentWindow appropriately if necessary
        WebWindow curr = currentWindow;
        do {
          // Instance equality is okay in this case
          if (curr == event.getWebWindow()) {
            currentWindow = currentWindow.getTopWindow();
            return;
          }
          curr = curr.getParentWindow();
        } while (curr != currentWindow.getTopWindow());
      }
    });

    // Now put us on the home page, like a real browser
    get(webClient.getOptions().getHomePage());
    gotPage = false;
    resetKeyboardAndMouseState();

    options = new HtmlUnitOptions();
  }

  /**
   * The browserName is {@link BrowserType#HTMLUNIT} "htmlunit" and the browserVersion
   * denotes the required browser AND its version.
   * For example "chrome" for Chrome, "firefox-45" for Firefox 45
   * or "internet explorer" for IE.
   *
   * @param capabilities desired capabilities requested for the htmlunit driver session
   */
  public HtmlUnitDriver(Capabilities capabilities) {
    this(determineBrowserVersion(capabilities));

    setJavascriptEnabled(capabilities.getCapability(JAVASCRIPT_ENABLED) == null
        || capabilities.is(JAVASCRIPT_ENABLED));

    setProxySettings(Proxy.extractFrom(capabilities));

    setDownloadImages(capabilities.is(DOWNLOAD_IMAGES_CAPABILITY));

    unexpectedAlertBehaviour = (UnexpectedAlertBehaviour) capabilities.getCapability(UNEXPECTED_ALERT_BEHAVIOUR);
    if (unexpectedAlertBehaviour == null) {
      unexpectedAlertBehaviour = UnexpectedAlertBehaviour.DISMISS_AND_NOTIFY;
    }

    Boolean acceptSslCerts = (Boolean) capabilities.getCapability(ACCEPT_SSL_CERTS);
    if (acceptSslCerts == null) {
      acceptSslCerts = true;
    }
    setAcceptSslCertificates(acceptSslCerts);

    String pageLoadStrategyString = (String) capabilities.getCapability(PAGE_LOAD_STRATEGY);
    if ("none".equals(pageLoadStrategyString)) {
      pageLoadStrategy = PageLoadStrategy.NONE;
    }
    else if ("eager".equals(pageLoadStrategyString)) {
      pageLoadStrategy = PageLoadStrategy.EAGER;
    }
  }

  public HtmlUnitDriver(Capabilities desiredCapabilities, Capabilities requiredCapabilities) {
    this(new DesiredCapabilities(desiredCapabilities, requiredCapabilities));
  }

  static BrowserVersion determineBrowserVersion(Capabilities capabilities) {
    String capBrowserName = capabilities.getBrowserName();
    if (!BrowserType.HTMLUNIT.equals(capBrowserName)) {
      throw new IllegalArgumentException("When building an HtmlUntDriver, the capability browser name must be set to '"
                          + BrowserType.HTMLUNIT + "' but was '" + capBrowserName + "'.");
    }

    String browserName;
    String browserVersion;

    String rawVersion = capabilities.getVersion();
    String[] splitVersion = rawVersion == null ? new String[0] : rawVersion.split("-");
    if (splitVersion.length > 1) {
      browserName = splitVersion[0];
      browserVersion = splitVersion[1];
    } else {
      browserName = capabilities.getVersion();
      browserVersion = null;
    }

    BrowserVersion browserVersionObject;
    switch (browserName) {
      case BrowserType.CHROME:
        browserVersionObject = BrowserVersion.CHROME;
        break;

      case BrowserType.IE:
        browserVersionObject = BrowserVersion.INTERNET_EXPLORER;
        break;

      case BrowserType.FIREFOX:
        try {
          int version = Integer.parseInt(browserVersion);
          switch (version) {
            case 52:
              browserVersionObject = BrowserVersion.FIREFOX_52;
              break;

            default:
              browserVersionObject = BrowserVersion.FIREFOX_60;
          }
        } catch (NumberFormatException e) {
            browserVersionObject = BrowserVersion.FIREFOX_60;
        }
        break;

      default:
        browserVersionObject = BrowserVersion.getDefault();
        break;
    }

    Object rawLanguage = capabilities.getCapability(BROWSER_LANGUAGE_CAPABILITY);
    if (rawLanguage instanceof String) {
      browserVersionObject = new BrowserVersionBuilder(browserVersionObject)
          .setBrowserLanguage((String) rawLanguage).build();
    }

    return browserVersionObject;
  }

  private WebClient createWebClient(BrowserVersion version) {
    WebClient client = newWebClient(version);

    final WebClientOptions clienOptions = client.getOptions();
    clienOptions.setHomePage(WebClient.URL_ABOUT_BLANK.toString());
    clienOptions.setThrowExceptionOnFailingStatusCode(false);
    clienOptions.setPrintContentOnFailingStatusCode(false);
    clienOptions.setJavaScriptEnabled(enableJavascript);
    clienOptions.setRedirectEnabled(true);
    clienOptions.setUseInsecureSSL(true);

    // Ensure that we've set the proxy if necessary
    if (proxyConfig != null) {
      clienOptions.setProxyConfig(proxyConfig);
    }

    client.setRefreshHandler(new WaitingRefreshHandler());

    return modifyWebClient(client);
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

  void runAsync(Runnable r) {
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
      }
      catch (RuntimeException e) {
        exception = e;
      }
      finally {
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

  void click(DomElement element, boolean directClick) {
    runAsync(() -> mouse.click(element, directClick));
  }

  void doubleClick(DomElement element) {
    runAsync(() -> mouse.doubleClick(element));
  }

  void mouseUp(DomElement element) {
    runAsync(() -> mouse.mouseUp(element));
  }

  void mouseMove(DomElement element) {
    runAsync(() -> mouse.mouseMove(element));
  }

  void mouseDown(DomElement element) {
    runAsync(() -> mouse.mouseDown(element));
  }

  void submit(HtmlUnitWebElement element) {
    runAsync(() -> element.submitImpl());
  }

  void sendKeys(HtmlUnitWebElement element, CharSequence... value) {
    runAsync(() -> keyboard.sendKeys(element, true, value));
  }

  /**
   * Get the simulated {@code BrowserVersion}.
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
   * Child classes can override this method to customize the WebClient that the HtmlUnit driver
   * uses.
   *
   * @param client The client to modify
   * @return The modified client
   */
  protected WebClient modifyWebClient(WebClient client) {
    // Does nothing here to be overridden.
    return client;
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
   * @param host The hostname of HTTP proxy
   * @param port The port of HTTP proxy, 0 means HTTP proxy w/o port
   * @param noProxyHosts The list of hosts which need to bypass HTTP proxy
   */
  public void setHTTPProxy(String host, int port, List<String> noProxyHosts) {
    proxyConfig = new ProxyConfig();
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
   * @param host The hostname of SOCKS proxy
   * @param port The port of SOCKS proxy, 0 means HTTP proxy w/o port
   * @param noProxyHosts The list of hosts which need to bypass SOCKS proxy
   */
  public void setSocksProxy(String host, int port, List<String> noProxyHosts) {
    proxyConfig = new ProxyConfig();
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
   * Sets the {@link Executor} to be used for submitting async tasks to.
   * You have to close this manually on {@link #quit()}
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
    proxyConfig = new ProxyConfig();
    proxyConfig.setProxyAutoConfigUrl(autoProxyUrl);
    getWebClient().getOptions().setProxyConfig(proxyConfig);
  }

  @Override
  public Capabilities getCapabilities() {
    DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();

    capabilities.setPlatform(Platform.getCurrent());
    capabilities.setJavascriptEnabled(isJavascriptEnabled());
    capabilities.setVersion(Version.getProductVersion());
    capabilities.setCapability(SUPPORTS_FINDING_BY_CSS, true);

    return capabilities;
  }

  @Override
  public void get(String url) {
    // Prevent the malformed URL exception.
    if (WebClient.URL_ABOUT_BLANK.toString().equals(url)) {
      get(WebClient.URL_ABOUT_BLANK);
      return;
    }

    URL fullUrl;
    try {
      fullUrl = new URL(url);
    } catch (Exception e) {
      throw new WebDriverException(e);
    }

    runAsync(() -> get(fullUrl));
  }

  /**
   * Allows HtmlUnit's about:blank to be loaded in the constructor, and may be useful for other
   * tests?
   *
   * @param fullUrl The URL to visit
   */
  protected void get(URL fullUrl) {
    alert.close();
    alert.setAutoAccept(false);
    try {
      // we can't use webClient.getPage(url) here because selenium has a different idea
      // of the current window and we like to load into to selenium current one
      final BrowserVersion browser = getBrowserVersion();
      final WebRequest request = new WebRequest(fullUrl, browser.getHtmlAcceptHeader(), browser.getAcceptEncodingHeader());
      request.setCharset(StandardCharsets.UTF_8);
      getWebClient().getPage(getCurrentWindow().getTopWindow(), request);

      // A "get" works over the entire page
      currentWindow = getCurrentWindow().getTopWindow();
    } catch (UnknownHostException e) {
      getCurrentWindow().getTopWindow().setEnclosedPage(new UnexpectedPage(
          new StringWebResponse("Unknown host", fullUrl),
          getCurrentWindow().getTopWindow()
          ));
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
    pickWindow();
    resetKeyboardAndMouseState();
  }

  private void resetKeyboardAndMouseState() {
    keyboard = new HtmlUnitKeyboard(this);
    mouse = new HtmlUnitMouse(this, keyboard);
  }

  protected void pickWindow() {
    // TODO(simon): HtmlUnit tries to track the current window as the frontmost. We don't
    if (currentWindow == null) {
      currentWindow = getWebClient().getCurrentWindow();
    }
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
    ensureAlertUnlocked();
    Page page = lastPage();
    if (page == null || !(page instanceof HtmlPage)) {
      return null; // no page so there is no title
    }
    if (getCurrentWindow() instanceof FrameWindow) {
      page = getCurrentWindow().getTopWindow().getEnclosedPage();
    }

    return ((HtmlPage) page).getTitleText();
  }

  private void ensureAlertUnlocked() {
    if (alert.isLocked()) {
      String text = alert.getText();
      switch (unexpectedAlertBehaviour) {
        case ACCEPT:
          alert.accept();
          return;

        case ACCEPT_AND_NOTIFY:
            alert.accept();
            break;

        case DISMISS:
          alert.dismiss();
          return;

        case DISMISS_AND_NOTIFY:
            alert.dismiss();
            break;

        case IGNORE:
            break;
      }
      throw new UnhandledAlertException("Alert found", text);
    }
  }

  @Override
  public WebElement findElement(By by) {
    return findElement(by, this);
  }

  @Override
  public List<WebElement> findElements(By by) {
    return findElements(by, this);
  }

  @Override
  public String getPageSource() {
    Page page = lastPage();
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
    WebWindow thisWindow = getCurrentWindow(); // check that the current window is active
    if (getWebClient().getWebWindows().size() == 1) {
      // closing the last window is equivalent to quit
      quit();
    } else {
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
    currentWindow = null;
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
      result = page.executeJavaScriptFunction(
          function,
          getCurrentWindow().getScriptableObject(),
          parameters,
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

    asyncScriptExecutor = new AsyncScriptExecutor(page, scriptTimeout);
    try {
      Object result = asyncScriptExecutor.execute(script, args);

      ensureAlertUnlocked();
      return parseNativeJavascriptResult(result);
    }
    finally {
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
    }
    finally {
      Context.exit();
    }
    return parameters;
  }

  private HtmlPage getPageToInjectScriptInto() {
    if (!isJavascriptEnabled()) {
      throw new UnsupportedOperationException(
          "Javascript is not enabled for this HtmlUnitDriver instance");
    }

    final Page lastPage = lastPage();
    if (!(lastPage instanceof HtmlPage)) {
      throw new UnsupportedOperationException("Cannot execute JS against a plain text page");
    } else if (!gotPage) {
      // just to make ExecutingJavascriptTest.testShouldThrowExceptionIfExecutingOnNoPage happy
      // but does this limitation make sense?
      throw new WebDriverException("Can't execute JavaScript before a page has been loaded!");
    }

    return (HtmlPage) lastPage;
  }

  private Object parseArgumentIntoJavascriptParameter(Scriptable scope, Object arg) {
    while (arg instanceof WrapsElement) {
      arg = ((WrapsElement) arg).getWrappedElement();
    }

    if (!(arg instanceof HtmlUnitWebElement ||
        arg instanceof HtmlElement || // special case the underlying type
        arg instanceof Number ||
        arg instanceof String ||
        arg instanceof Boolean ||
        arg.getClass().isArray() ||
        arg instanceof Collection<?> ||
        arg instanceof Map<?, ?>)) {
      throw new IllegalArgumentException(
          "Argument must be a string, number, boolean or WebElement: " +
              arg + " (" + arg.getClass() + ")");
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

    } else if (arg instanceof Map<?,?>) {
      Map<?,?> argmap = (Map<?,?>) arg;
      Scriptable map = Context.getCurrentContext().newObject(scope);
      for (Object key: argmap.keySet()) {
        map.put((String) key, map, parseArgumentIntoJavascriptParameter(scope,
            argmap.get(key)));
      }
      return map;

    } else {
      return arg;
    }
  }

  protected void assertElementNotStale(DomElement element) {
    SgmlPage elementPage = element.getPage();
    Page currentPage = lastPage();

    if (!currentPage.equals(elementPage)) {
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
      throw new StaleElementReferenceException(
          "The element seems to be disconnected from the DOM. "
              + " This means that a user cannot interact with it.");
    }
  }

  @Override
  public Keyboard getKeyboard() {
    return keyboard;
  }

  @Override
  public Mouse getMouse() {
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

    if (value instanceof IdScriptableObject
        && value.getClass().getSimpleName().equals("NativeDate")) {
      long l = ((Number) getPrivateField(value, "date")).longValue();
      return Instant.ofEpochMilli(l).toString();
    }

    if (value instanceof Undefined) {
      return null;
    }

    return value;
  }

  private static Object getPrivateField(Object o, String fieldName) {
    try {
      final Field field = o.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(o);
    }
    catch (Exception e) {
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

  private void switchToDefaultContentOfWindow(WebWindow window) {
    Page page = window.getEnclosedPage();
    if (page instanceof HtmlPage) {
      currentWindow = window;
    }
  }

  @Override
  public Navigation navigate() {
    return new HtmlUnitNavigation();
  }

  protected Page lastPage() {
    getWebClient(); // check that session is active
    return getCurrentWindow().getEnclosedPage();
  }

  @Override
  public WebElement findElementByLinkText(String selector) {
    if (!(lastPage() instanceof HtmlPage)) {
      throw new IllegalStateException("Cannot find links for " + lastPage());
    }

    String expectedText = selector.trim();

    List<HtmlAnchor> anchors = ((HtmlPage) lastPage()).getAnchors();
    for (HtmlAnchor anchor : anchors) {
      if (expectedText.equals(anchor.asText().trim())) {
        return toWebElement(anchor);
      }
    }
    throw new NoSuchElementException("No link found with text: " + expectedText);
  }

  protected HtmlUnitWebElement toWebElement(DomElement element) {
    Map<DomElement, HtmlUnitWebElement> pageMap = elementsMap.get(element.getPage());
    if (pageMap == null) {
        pageMap = new HashMap<DomElement, HtmlUnitWebElement>();
        elementsMap.put(element.getPage(), pageMap);
    }

    HtmlUnitWebElement e = pageMap.get(element);
    if (e == null) {
      e = new HtmlUnitWebElement(this, ++elementsCounter, element);
      pageMap.put(element, e);
    }
    return e;
  }

  public HtmlUnitWebElement getElementById(int id) {
    for (Map<DomElement, HtmlUnitWebElement> pageMap : elementsMap.values()) {
      for (HtmlUnitWebElement e : pageMap.values()) {
        if (e.id == id) {
          return e;
        }
      }
    }
    return null;
  }

  @Override
  public List<WebElement> findElementsByLinkText(String selector) {
    List<WebElement> elements = new ArrayList<>();

    if (!(lastPage() instanceof HtmlPage)) {
      return elements;
    }

    String expectedText = selector.trim();

    List<HtmlAnchor> anchors = ((HtmlPage) lastPage()).getAnchors();
    for (HtmlAnchor anchor : anchors) {
      if (expectedText.equals(anchor.asText().trim())) {
        elements.add(toWebElement(anchor));
      }
    }
    return elements;
  }

  @Override
  public WebElement findElementById(String id) {
    if (!(lastPage() instanceof HtmlPage)) {
      throw new NoSuchElementException("Unable to locate element by id for " + lastPage());
    }

    DomElement element = ((HtmlPage) lastPage()).getElementById(id);
    if (element == null) {
      throw new NoSuchElementException("Unable to locate element with ID: '" + id + "'");
    }
    return toWebElement(element);
  }

  @Override
  public List<WebElement> findElementsById(String id) {
    if (!(lastPage() instanceof HtmlPage)) {
      return new ArrayList<>();
    }

    List<DomElement> allElements = ((HtmlPage) lastPage()).getElementsById(id);
    return convertRawDomElementsToWebElements(allElements);
  }

  @Override
  public WebElement findElementByClassName(String className) {
    if (className.indexOf(' ') != -1) {
      throw new NoSuchElementException("Compound class names not permitted");
    }
    return findElementByCssSelector("." + className);
  }

  @Override
  public List<WebElement> findElementsByClassName(String className) {
    if (className.indexOf(' ') != -1) {
      throw new NoSuchElementException("Compound class names not permitted");
    }
    return findElementsByCssSelector("." + className);
  }

  @Override
  public WebElement findElementByCssSelector(String using) {
    if (!(lastPage() instanceof HtmlPage)) {
      throw new NoSuchElementException("Unable to locate element using css: " + lastPage());
    }

    DomNode node;
    try {
      node = ((HtmlPage) lastPage()).querySelector(using);
    } catch (CSSException ex) {
      throw new NoSuchElementException("Unable to locate element using css", ex);
    }

    if (node instanceof DomElement) {
      return toWebElement((DomElement) node);
    }

    throw new NoSuchElementException("Returned node (" + node + ") was not a DOM element");
  }

  @Override
  public List<WebElement> findElementsByCssSelector(String using) {
    if (!(lastPage() instanceof HtmlPage)) {
      throw new NoSuchElementException("Unable to locate element using css: " + lastPage());
    }

    DomNodeList<DomNode> allNodes;

    try {
      allNodes = ((HtmlPage) lastPage()).querySelectorAll(using);
    } catch (CSSException ex) {
      throw new NoSuchElementException("Unable to locate element using css", ex);
    }

    List<WebElement> toReturn = new ArrayList<>();

    for (DomNode node : allNodes) {
      if (node instanceof DomElement) {
        toReturn.add(toWebElement((DomElement) node));
      } else {
        throw new NoSuchElementException("Returned node was not a DOM element");
      }
    }

    return toReturn;
  }

  @Override
  public WebElement findElementByName(String name) {
    if (!(lastPage() instanceof HtmlPage)) {
      throw new IllegalStateException("Unable to locate element by name for " + lastPage());
    }

    List<DomElement> allElements = ((HtmlPage) lastPage()).getElementsByName(name);
    if (!allElements.isEmpty()) {
      return toWebElement(allElements.get(0));
    }

    throw new NoSuchElementException("Unable to locate element with name: " + name);
  }

  @Override
  public List<WebElement> findElementsByName(String name) {
    if (!(lastPage() instanceof HtmlPage)) {
      return new ArrayList<>();
    }

    List<DomElement> allElements = ((HtmlPage) lastPage()).getElementsByName(name);
    return convertRawDomElementsToWebElements(allElements);
  }

  @Override
  public WebElement findElementByTagName(String name) {
    if (!(lastPage() instanceof HtmlPage)) {
      throw new IllegalStateException("Unable to locate element by name for " + lastPage());
    }

    NodeList allElements = ((HtmlPage) lastPage()).getElementsByTagName(name);
    if (allElements.getLength() > 0) {
      return toWebElement((HtmlElement) allElements.item(0));
    }

    throw new NoSuchElementException("Unable to locate element with name: " + name);
  }

  @Override
  public List<WebElement> findElementsByTagName(String name) {
    if ("".equals(name)) {
      throw new InvalidSelectorException("Unable to locate element by xpath for " + lastPage());
    }

    if (!(lastPage() instanceof HtmlPage)) {
      return new ArrayList<>();
    }

    NodeList allElements = ((HtmlPage) lastPage()).getElementsByTagName(name);
    List<WebElement> toReturn = new ArrayList<>(allElements.getLength());
    for (int i = 0; i < allElements.getLength(); i++) {
      Node item = allElements.item(i);
      if (item instanceof DomElement) {
        toReturn.add(toWebElement((DomElement) item));
      }
    }
    return toReturn;
  }

  @Override
  public WebElement findElementByXPath(String selector) {
    if (!(lastPage() instanceof SgmlPage)) {
      throw new IllegalStateException("Unable to locate element by xpath for " + lastPage());
    }

    Object node;
    try {
      node = ((SgmlPage) lastPage()).getFirstByXPath(selector);
    } catch (Exception ex) {
      // The xpath expression cannot be evaluated, so the expression is invalid
      throw new InvalidSelectorException(
          String.format(INVALIDXPATHERROR, selector),
          ex);
    }
    if (node == null) {
      throw new NoSuchElementException("Unable to locate a node using " + selector);
    }
    if (node instanceof DomElement) {
      return toWebElement((DomElement) node);
    }
    // The xpath expression selected something different than a WebElement.
    // The selector is therefore invalid
    throw new InvalidSelectorException(
        String.format(INVALIDSELECTIONERROR, selector, node.getClass()));
  }

  @Override
  public List<WebElement> findElementsByXPath(String selector) {
    if (!(lastPage() instanceof SgmlPage)) {
      return new ArrayList<>();
    }

    List<?> nodes;
    try {
      nodes = ((SgmlPage) lastPage()).getByXPath(selector);
    } catch (RuntimeException ex) {
        // The xpath expression cannot be evaluated, so the expression is invalid
        throw new InvalidSelectorException(String.format(INVALIDXPATHERROR, selector), ex);
    }

    List<WebElement> elements = new ArrayList<>(nodes.size());
    for (Object node : nodes) {
      // There exist elements in the nodes list which could not be converted to WebElements.
      // A valid xpath selector should only select WebElements.
      if (!(node instanceof DomElement)) {
        // We only want to know the type of one invalid element so that we can give this
        // information in the exception. We can throw the exception immediately.
        throw new InvalidSelectorException(String.format(INVALIDSELECTIONERROR, selector, node.getClass()));
      }
      elements.add(toWebElement((DomElement) node));
    }

    return elements;
  }

  private List<WebElement> convertRawDomElementsToWebElements(List<DomElement> nodes) {
    List<WebElement> elements = new ArrayList<>(nodes.size());

    for (DomElement node : nodes) {
      elements.add(toWebElement(node));
    }

    return elements;
  }

  public boolean isJavascriptEnabled() {
    return getWebClient().getOptions().isJavaScriptEnabled();
  }

  public void setJavascriptEnabled(boolean enableJavascript) {
    this.enableJavascript = enableJavascript;
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

  private class HtmlUnitTargetLocator implements TargetLocator {

    @Override
    public WebDriver frame(int index) {
      Page page = lastPage();
      if (page instanceof HtmlPage) {
        try {
          currentWindow = ((HtmlPage) page).getFrames().get(index);
        } catch (IndexOutOfBoundsException ignored) {
          throw new NoSuchFrameException("Cannot find frame: " + index);
        }
      }
      return HtmlUnitDriver.this;
    }

    @Override
    public WebDriver frame(final String nameOrId) {
      Page page = lastPage();
      if (page instanceof HtmlPage) {
        // First check for a frame with the matching name.
        for (final FrameWindow frameWindow : ((HtmlPage) page).getFrames()) {
          if (frameWindow.getName().equals(nameOrId)) {
            currentWindow = frameWindow;
            return HtmlUnitDriver.this;
          }
        }
      }

      // Next, check for a frame with a matching ID. For simplicity, assume the ID is unique.
      // Users can still switch to frames with non-unique IDs using a WebElement switch:
      // WebElement frameElement = driver.findElement(By.xpath("//frame[@id=\"foo\"]"));
      // driver.switchTo().frame(frameElement);
      try {
        HtmlUnitWebElement element =
            (HtmlUnitWebElement) HtmlUnitDriver.this.findElementById(nameOrId);
        DomElement domElement = element.getElement();
        if (domElement instanceof BaseFrameElement) {
          currentWindow = ((BaseFrameElement) domElement).getEnclosedWindow();
          return HtmlUnitDriver.this;
        }
      } catch (NoSuchElementException ignored) {
      }

      throw new NoSuchFrameException("Unable to locate frame with name or ID: " + nameOrId);
    }

    @Override
    public WebDriver frame(WebElement frameElement) {
      while (frameElement instanceof WrapsElement) {
        frameElement = ((WrapsElement) frameElement).getWrappedElement();
      }

      HtmlUnitWebElement webElement = (HtmlUnitWebElement) frameElement;
      webElement.assertElementNotStale();

      DomElement domElement = webElement.getElement();
      if (!(domElement instanceof BaseFrameElement)) {
        throw new NoSuchFrameException(webElement.getTagName() + " is not a frame element.");
      }

      currentWindow = ((BaseFrameElement) domElement).getEnclosedWindow();
      return HtmlUnitDriver.this;
    }

    @Override
    public WebDriver parentFrame() {
      currentWindow = currentWindow.getParentWindow();
      return HtmlUnitDriver.this;
    }

    @Override
    public WebDriver window(String windowId) {
      try {
        WebWindow window = getWebClient().getWebWindowByName(windowId);
        return finishSelecting(window);
      } catch (WebWindowNotFoundException e) {

        List<WebWindow> allWindows = getWebClient().getWebWindows();
        for (WebWindow current : allWindows) {
          WebWindow top = current.getTopWindow();
          if (String.valueOf(System.identityHashCode(top)).equals(windowId)) {
            return finishSelecting(top);
          }
        }
        throw new NoSuchWindowException("Cannot find window: " + windowId);
      }
    }

    private WebDriver finishSelecting(WebWindow window) {
      getWebClient().setCurrentWindow(window);
      currentWindow = window;
      pickWindow();
      alert.setAutoAccept(false);
      return HtmlUnitDriver.this;
    }

    @Override
    public WebDriver defaultContent() {
      switchToDefaultContentOfWindow(getCurrentWindow().getTopWindow());
      return HtmlUnitDriver.this;
    }

    @Override
    public WebElement activeElement() {
      Page page = lastPage();
      if (page instanceof HtmlPage) {
        DomElement element = ((HtmlPage) page).getFocusedElement();
        if (element == null || element instanceof HtmlHtml) {
          List<? extends HtmlElement> allBodies =
              ((HtmlPage) page).getDocumentElement().getElementsByTagName("body");
          if (!allBodies.isEmpty()) {
            return toWebElement(allBodies.get(0));
          }
        } else {
          return toWebElement(element);
        }
      }

      throw new NoSuchElementException("Unable to locate element with focus or body tag");
    }

    @Override
    public Alert alert() {
      if (!alert.isLocked()) {
        for (int i = 0; i < 5; i++) {
          if (!alert.isLocked()) {
            try {
              Thread.sleep(50);
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
        }
        if (!alert.isLocked()) {
          getCurrentWindow();
          throw new NoAlertPresentException();
        }
      }
      WebWindow alertWindow = alert.getWebWindow();
      if (alertWindow != currentWindow
          && !isChild(currentWindow, alertWindow)
          && !isChild(alertWindow, currentWindow)) {
          throw new TimeoutException();
      }
      return alert;
    }
  }

  private static boolean isChild(WebWindow parent, WebWindow potentialChild) {
    for (WebWindow child = potentialChild; child != null ; child = child.getParentWindow()) {
      if (child == parent) {
        return true;
      }
      if (child == child.getTopWindow()) {
        break;
      }
    }
    return false;
  }

  protected <X> X implicitlyWaitFor(Callable<X> condition) {
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
    if (currentWindow == null || currentWindow.isClosed()) {
      throw new NoSuchWindowException("Window is closed");
    }
    return currentWindow;
  }

  private URL getRawUrl() {
    // TODO(simon): I can see this being baaad.
    Page page = lastPage();
    if (page == null) {
      return null;
    }

    return page.getUrl();
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
      if (lastPage() instanceof HtmlPage) {
        runAsync(() -> {
          try {
            ((HtmlPage) lastPage()).refresh();
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

  private class HtmlUnitOptions implements Options {
    private final HtmlUnitLogs logs;

    public HtmlUnitOptions() {
      logs = new HtmlUnitLogs(getWebClient());
    }

    @Override
    public Logs logs() {
      return logs;
    }

    @Override
    public void addCookie(Cookie cookie) {
      Page page = lastPage();
      if (!(page instanceof HtmlPage)) {
        throw new UnableToSetCookieException("You may not set cookies on a page that is not HTML");
      }

      String domain = getDomainForCookie();
      verifyDomain(cookie, domain);

      getWebClient().getCookieManager().addCookie(
          new com.gargoylesoftware.htmlunit.util.Cookie(domain, cookie.getName(),
              cookie.getValue(),
              cookie.getPath(), cookie.getExpiry(), cookie.isSecure()));
    }

    private void verifyDomain(Cookie cookie, String expectedDomain) {
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
            String.format(
                "You may only add cookies that would be visible to the current domain: %s => %s",
                domain, expectedDomain));
      }
    }

    @Override
    public Cookie getCookieNamed(String name) {
      Set<Cookie> allCookies = getCookies();
      for (Cookie cookie : allCookies) {
        if (name.equals(cookie.getName())) {
          return cookie;
        }
      }

      return null;
    }

    @Override
    public void deleteCookieNamed(String name) {
      CookieManager cookieManager = getWebClient().getCookieManager();

      URL url = getRawUrl();
      Set<com.gargoylesoftware.htmlunit.util.Cookie> rawCookies = getWebClient().getCookies(url);
      for (com.gargoylesoftware.htmlunit.util.Cookie cookie : rawCookies) {
        if (name.equals(cookie.getName())) {
          cookieManager.removeCookie(cookie);
        }
      }
    }

    @Override
    public void deleteCookie(Cookie cookie) {
      getWebClient().getCookieManager().removeCookie(convertSeleniumCookieToHtmlUnit(cookie));
    }

    @Override
    public void deleteAllCookies() {
      CookieManager cookieManager = getWebClient().getCookieManager();

      URL url = getRawUrl();
      Set<com.gargoylesoftware.htmlunit.util.Cookie> rawCookies = getWebClient().getCookies(url);
      for (com.gargoylesoftware.htmlunit.util.Cookie cookie : rawCookies) {
        cookieManager.removeCookie(cookie);
      }
    }

    @Override
    public Set<Cookie> getCookies() {
      URL url = getRawUrl();

      // The about:blank URL (the default in case no navigation took place)
      // does not have a valid 'hostname' part and cannot be used for creating
      // cookies based on it - return an empty set.

      if (!url.toString().startsWith("http")) {
        return Sets.newHashSet();
      }

      return ImmutableSet.copyOf(Collections2.transform(
          getWebClient().getCookies(url),
          htmlUnitCookieToSeleniumCookieTransformer));
    }

    private com.gargoylesoftware.htmlunit.util.Cookie convertSeleniumCookieToHtmlUnit(Cookie cookie) {
      return new com.gargoylesoftware.htmlunit.util.Cookie(
          cookie.getDomain(),
          cookie.getName(),
          cookie.getValue(),
          cookie.getPath(),
          cookie.getExpiry(),
          cookie.isSecure(),
          cookie.isHttpOnly()
          );
    }

    private final com.google.common.base.Function<? super com.gargoylesoftware.htmlunit.util.Cookie, org.openqa.selenium.Cookie> htmlUnitCookieToSeleniumCookieTransformer =
        new com.google.common.base.Function<com.gargoylesoftware.htmlunit.util.Cookie, org.openqa.selenium.Cookie>() {
      @Override
      public org.openqa.selenium.Cookie apply(com.gargoylesoftware.htmlunit.util.Cookie c) {
        return new Cookie.Builder(c.getName(), c.getValue())
            .domain(c.getDomain())
            .path(c.getPath())
            .expiresOn(c.getExpires())
            .isSecure(c.isSecure())
            .isHttpOnly(c.isHttpOnly())
            .build();
      }
    };

    private String getDomainForCookie() {
      URL current = getRawUrl();
      return current.getHost();
    }

    @Override
    public Timeouts timeouts() {
      return new HtmlUnitTimeouts();
    }

    @Override
    public ImeHandler ime() {
      throw new UnsupportedOperationException("Cannot input IME using HtmlUnit.");
    }

    @Override
    public Window window() {
      return new HtmlUnitWindow();
    }
  }

  class HtmlUnitTimeouts implements Timeouts {

    @Override
    public Timeouts implicitlyWait(long time, TimeUnit unit) {
      HtmlUnitDriver.this.implicitWait =
          TimeUnit.MILLISECONDS.convert(Math.max(0, time), unit);
      return this;
    }

    @Override
    public Timeouts setScriptTimeout(long time, TimeUnit unit) {
      HtmlUnitDriver.this.scriptTimeout = TimeUnit.MILLISECONDS.convert(time, unit);
      return this;
    }

    @Override
    public Timeouts pageLoadTimeout(long time, TimeUnit unit) {
      int timeout = (int) TimeUnit.MILLISECONDS.convert(time, unit);
      getWebClient().getOptions().setTimeout(timeout > 0 ? timeout : 0);
      return this;
    }
  }

  public class HtmlUnitWindow implements Window {

    private int SCROLLBAR_WIDTH = 8;
    private int HEADER_HEIGHT = 150;

    @Override
    public void setSize(Dimension targetSize) {
      WebWindow topWindow = getCurrentWindow().getTopWindow();

      int width = targetSize.getWidth();
      if (width < SCROLLBAR_WIDTH) width = SCROLLBAR_WIDTH;
      topWindow.setOuterWidth(width);
      topWindow.setInnerWidth(width - SCROLLBAR_WIDTH);

      int height = targetSize.getHeight();
      if (height < HEADER_HEIGHT) height = HEADER_HEIGHT;
      topWindow.setOuterHeight(height);
      topWindow.setInnerHeight(height - HEADER_HEIGHT);
    }

    @Override
    public void setPosition(Point targetPosition) {
      windowPosition = targetPosition;
    }

    @Override
    public Dimension getSize() {
      WebWindow topWindow = getCurrentWindow().getTopWindow();
      return new Dimension(topWindow.getOuterWidth(), topWindow.getOuterHeight());
    }

    @Override
    public Point getPosition() {
      return windowPosition;
    }

    @Override
    public void maximize() {
      setSize(initialWindowDimension);
      setPosition(new Point(0, 0));
    }

    @Override
    public void fullscreen() {
      maximize();
    }
  }

  @Override
  public WebElement findElementByPartialLinkText(String using) {
    if (!(lastPage() instanceof HtmlPage)) {
      throw new IllegalStateException("Cannot find links for " + lastPage());
    }

    List<HtmlAnchor> anchors = ((HtmlPage) lastPage()).getAnchors();
    for (HtmlAnchor anchor : anchors) {
      if (anchor.asText().contains(using)) {
        return toWebElement(anchor);
      }
    }
    throw new NoSuchElementException("No link found with text: " + using);
  }

  @Override
  public List<WebElement> findElementsByPartialLinkText(String using) {
    List<HtmlAnchor> anchors = ((HtmlPage) lastPage()).getAnchors();
    List<WebElement> elements = new ArrayList<>();
    for (HtmlAnchor anchor : anchors) {
      if (anchor.asText().contains(using)) {
        elements.add(toWebElement(anchor));
      }
    }
    return elements;
  }

  WebElement findElement(final By locator, final SearchContext context) {
    ensureAlertUnlocked();
    return implicitlyWaitFor(new Callable<WebElement>() {

      @Override
      public WebElement call() throws Exception {
        return locator.findElement(context);
      }
    });
  }

  List<WebElement> findElements(final By by, final SearchContext context) {
    if (implicitWait < sleepTime) {
        return by.findElements(context);
    }

    long end = System.currentTimeMillis() + implicitWait;
    List<WebElement> found;
    do {
      found = by.findElements(context);
      if (!found.isEmpty()) {
        return found;
      }
      sleepQuietly(sleepTime);
    } while (System.currentTimeMillis() < end);

    return found;
  }

  private static void sleepQuietly(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ignored) {
    }
  }

  private static enum PageLoadStrategy {
    NORMAL, EAGER, NONE;
  }
}
