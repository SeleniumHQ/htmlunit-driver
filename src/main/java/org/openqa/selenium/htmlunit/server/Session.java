package org.openqa.selenium.htmlunit.server;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.ScriptTimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.BeanToJsonConverter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.ErrorCodes;
import org.openqa.selenium.remote.JsonToBeanConverter;

@Path("/session")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Session {

  private static Map<String, Session> sessions = new HashMap<>();
  private static SecureRandom random = new SecureRandom();

  private static List<HtmlUnitLocalDriver> lockedDrivers = Collections.synchronizedList(new ArrayList<>());
  private static Map<HtmlUnitLocalDriver, RuntimeException> exceptionsMap = Collections.synchronizedMap(new HashMap<>());

  private String id;
  private HtmlUnitLocalDriver driver;
  
  private static synchronized Session createSession(Capabilities desiredCapabilities, Capabilities requiredCapabilities) {
    String id;
    do {
      id = createSessionId();
    }
    while (sessions.containsKey(id));

    Session session = new Session();
    session.id = id;
    session.driver = new HtmlUnitLocalDriver(desiredCapabilities, requiredCapabilities);
    sessions.put(id, session);
    return session;
  }

  private static String createSessionId() {
    return new BigInteger(16 * 8, random).toString(16);
  }

  private static HtmlUnitLocalDriver getDriver(String sessionId) {
    return getDriver(sessionId, true);
  }

  private static HtmlUnitLocalDriver getDriver(String sessionId, boolean ensureUnLock) {
    HtmlUnitLocalDriver driver = sessions.get(sessionId).driver;
    if (ensureUnLock) {
      waitForUnlock(driver);
    }
    return driver;
  }


  @POST
  @SuppressWarnings("unchecked")
  public Response newSession(String content) {
    Map<String, ?> map = new JsonToBeanConverter().convert(Map.class, content);
    Capabilities desiredCapabilities = new DesiredCapabilities((Map<String, ?>) map.get("desiredCapabilities"));
    Capabilities requiredCapabilities = new DesiredCapabilities((Map<String, ?>) map.get("requiredCapabilities"));
    return getResponse(createSession(desiredCapabilities, requiredCapabilities).id, new HashMap<>());
  }

  private static Response getResponse(String sessionId, Object value) {
    Map<String, Object> map = new HashMap<>();
    map.put("value", value);
    map.put("sessionId", sessionId);
    map.put("status", ErrorCodes.SUCCESS);
    return Response.ok(new BeanToJsonConverter().convert(map), MediaType.APPLICATION_JSON).build();
  }

  @DELETE
  @Path("{session}")
  public Response deleteSession(@PathParam("session") String session) {
    HtmlUnitLocalDriver driver = getDriver(session, false);
    HtmlUnitAlert alert = (HtmlUnitAlert) driver.switchTo().alert();
    if (alert.isLocked()) {
      alert.accept();
    }
    alert.setAutoAccept(true);

    driver.quit();
    sessions.remove(session);
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/url")
  public static Response go(@PathParam("session") String session, String content) {
    String url = new JsonToBeanConverter().convert(Map.class, content).get("url").toString();

    HtmlUnitLocalDriver driver = getDriver(session, false);
    HtmlUnitAlert alert = (HtmlUnitAlert) driver.switchTo().alert();
    if (alert.isLocked()) {
      alert.dismiss();
    }
    runAsync(driver, () -> {
      driver.get(url);
    });

    waitForUnlockedOrAlert(driver);
    return getResponse(session, null);
  }

  private static void runAsync(HtmlUnitLocalDriver driver, Runnable runnable) {
    waitForUnlock(driver);
    new Thread(() -> {
      try {
        runnable.run();
        Thread.sleep(200);
        unlockDriver(driver);
      }
      catch (RuntimeException e) {
        exceptionsMap.put(driver, e);
      }
      catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).start();
    lockDriver(driver);
  }

  private static void lockDriver(HtmlUnitLocalDriver driver) {
    waitForUnlock(driver);
    lockedDrivers.add(driver);
  }

  private static void waitForUnlock(HtmlUnitLocalDriver driver) {
    while (lockedDrivers.contains(driver)) {
      RuntimeException t = exceptionsMap.get(driver);
      if (t != null) {
        exceptionsMap.remove(driver);
        unlockDriver(driver);
        if (!(t instanceof ScriptTimeoutException)) {
          throw t;
        }
      }
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private static void unlockDriver(HtmlUnitLocalDriver driver) {
    lockedDrivers.remove(driver);
  }

  @GET
  @Path("{session}/url")
  public static Response getCurrentUrl(@PathParam("session") String session) {
    String value = getDriver(session).getCurrentUrl();
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/source")
  public static Response getPageSource(@PathParam("session") String session) {
    String value = getDriver(session).getPageSource();
    return getResponse(session, value);
  }

  @SuppressWarnings("unchecked")
  private static <T> Map<String, T> getMap(String content) {
    return new JsonToBeanConverter().convert(Map.class, content);
  }

  @POST
  @Path("{session}/element")
  public static Response findElement(@PathParam("session") String session, String content) {
    By by = getBy(content);
    HtmlUnitWebElement e = (HtmlUnitWebElement) getDriver(session, false).findElement(by);
    return getResponse(session, toElement(e));
  }

  @POST
  @Path("{session}/elements")
  public static Response findElements(@PathParam("session") String session, String content) {
    By by = getBy(content);
    
    List<?> list = toElements(getDriver(session).findElements(by));
    return getResponse(session, list);
  }

  private static List<?> toElements(List<?> list) {
    return list.stream().map(i -> toElement(i)).collect(Collectors.toList());
  }

  private static Object toElement(Object object) {
    if (object instanceof HtmlUnitWebElement) {
      Map<String, Integer> map2 = new HashMap<>();
      map2.put("ELEMENT", ((HtmlUnitWebElement) object).id);
      return map2;
    }
    return object;
  }

  @POST
  @Path("{session}/element/{elementId}/element")
  public static Response findElementFromElement(@PathParam("session") String session,
      @PathParam("elementId") String elementId,
      String content) {
    By by = getBy(content);
    WebElement element = getDriver(session, false).getElementById(Integer.valueOf(elementId));

    HtmlUnitWebElement e = (HtmlUnitWebElement) element.findElement(by);
    return getResponse(session, toElement(e));
  }

  @POST
  @Path("{session}/element/{elementId}/elements")
  public static Response findElementsFromElement(@PathParam("session") String session,
      @PathParam("elementId") String elementId,
      String content) {
    By by = getBy(content);

    WebElement element = getDriver(session).getElementById(Integer.valueOf(elementId));
    List<?> list = toElements(element.findElements(by));
    return getResponse(session, list);
  }

  private static By getBy(final String content) {
    Map<String, String> map = getMap(content);
    String value = map.get("value");
    By by;
    switch (map.get("using")) {
      case "id":
        by = By.id(value);
        break;

      case "name":
        by = By.name(value);
        break;

      case "tag name":
        by = By.tagName(value);
        break;

      case "css selector":
        by = By.cssSelector(value);
        break;

      case "class name":
        by = By.className(value);
        break;

      case "link text":
        by = By.linkText(value);
        break;

      case "partial link text":
        by = By.partialLinkText(value);
        break;

      case "xpath":
        by = By.xpath(value);
        break;

      default:
        throw new IllegalArgumentException();
    }
    return by;
  }

  @POST
  @Path("{session}/moveto")
  public static Response moveTo(@PathParam("session") String session, String content) {
    Map<String, ?> map = getMap(content);
    Integer elementId = Integer.parseInt((String) map.get("element"));
    getDriver(session).moveTo(elementId);
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/click")
  public static Response click(@PathParam("session") String session, String content) {
    Map<String, ?> map = getMap(content);
    int button = ((Long) map.get("button")).intValue();
    getDriver(session).click(button);
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/doubleclick")
  public static Response doubleclick(@PathParam("session") String session) {
    getDriver(session).doubleclick();
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/keys")
  public static Response keys(@PathParam("session") String session, String content) {
    Map<String, List<String>> map = getMap(content);
    getDriver(session).keys(map.get("value").get(0));
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/element/{elementId}/value")
  public static Response elementSendKeys(@PathParam("session") String session, @PathParam("elementId") String elementId, String content) {
    Map<String, List<String>> map = getMap(content);
    List<String> keys = map.get("value");
    HtmlUnitWebElement element = getDriver(session).getElementById(Integer.parseInt(elementId));
    element.sendKeys(keys.toArray(new String[keys.size()]));
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/element/active")
  public static Response getActiveElement(@PathParam("session") String session) {
    HtmlUnitWebElement e = (HtmlUnitWebElement) getDriver(session).switchTo().activeElement();
    return getResponse(session, toElement(e));
  }

  @GET
  @Path("{session}/element/{elementId}/name")
  public static Response getElementTagName(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    String value = getDriver(session).getElementById(Integer.valueOf(elementId)).getTagName();
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/element/{elementId}/attribute/{name}")
  public static Response getElementAttribute(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId,
      @PathParam("name") String name) {
    String value = getDriver(session).getElementById(Integer.valueOf(elementId)).getAttribute(name);
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/element/{elementId}/text")
  public static Response getElementText(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    String value = getDriver(session).getElementById(Integer.valueOf(elementId)).getText();
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/element/{elementId}/css/{propertyName}")
  public static Response getElementCssValue(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId,
      @PathParam("propertyName") String propertyName) {
    String value = getDriver(session).getElementById(Integer.valueOf(elementId)).getCssValue(propertyName);
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/title")
  public static Response getTitle(@PathParam("session") String session) {
    String value = getDriver(session, false).getTitle();
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/element/{elementId}/displayed")
  public static Response elementDisplayed(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    boolean value = getDriver(session).getElementById(Integer.valueOf(elementId)).isDisplayed();
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/element/{elementId}/size")
  public static Response elementSize(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    Dimension value = getDriver(session).getElementById(Integer.valueOf(elementId)).getSize();
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/element/{elementId}/rect")
  public static Response elementRect(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    Rectangle value = getDriver(session).getElementById(Integer.valueOf(elementId)).getRect();
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/element/{elementId}/location")
  public static Response elementLocation(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    Point value = getDriver(session).getElementById(Integer.valueOf(elementId)).getLocation();
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/element/{elementId}/location_in_view")
  public static Response elementLocationInView(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    Point value = getDriver(session).getElementById(Integer.valueOf(elementId)).getCoordinates().inViewPort();
    return getResponse(session, value);
  }

  @POST
  @Path("{session}/element/{elementId}/click")
  public static Response elementClick(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    HtmlUnitLocalDriver driver = getDriver(session);
    runAsync(driver, () -> driver.click(driver.getElementById(Integer.valueOf(elementId))));
    waitForUnlockedOrAlert(driver);
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/element/{elementId}/clear")
  public static Response elementClear(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    getDriver(session).getElementById(Integer.valueOf(elementId)).clear();
    return getResponse(session, null);
  }

  @GET
  @Path("{session}/element/{elementId}/enabled")
  public static Response isElementEnabled(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    boolean value = getDriver(session).getElementById(Integer.valueOf(elementId)).isEnabled();
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/element/{elementId}/selected")
  public static Response isElementSelected(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    boolean value = getDriver(session).getElementById(Integer.valueOf(elementId)).isSelected();
    return getResponse(session, value);
  }

  @POST
  @Path("{session}/element/{elementId}/submit")
  public static Response elementSubmit(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    HtmlUnitLocalDriver driver = getDriver(session);
    runAsync(driver, () -> {
      driver.getElementById(Integer.valueOf(elementId)).submit();
    });
    waitForUnlockedOrAlert(driver);
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/execute")
  @SuppressWarnings("unchecked")
  public static Response execute(@PathParam("session") String session, String content) {
    Map<String, ?> map = getMap(content);
    String script = (String) map.get("script");
    HtmlUnitLocalDriver driver = getDriver(session);

    List<?> args = (ArrayList<?>) map.get("args");
    Object[] array = args.stream().map(i -> {
      if (i instanceof Map) {
        return driver.getElementById(Integer.parseInt(((Map<String, String>) i).get("ELEMENT")));
      }
      return i;
    }).toArray(size -> new Object[size]);

    Object value = driver.executeScript(script, array);
    return getResponse(session, toElement(value));
  }

  @POST
  @Path("{session}/execute_async")
  public static Response executeAsync(@PathParam("session") String session, String content) {
      Map<String, ?> map = getMap(content);
      String script = (String) map.get("script");
      HtmlUnitLocalDriver driver = getDriver(session);

      List<?> args = (ArrayList<?>) map.get("args");
      Object[] array = args.toArray(new Object[args.size()]);
      AtomicReference<Object> value = new AtomicReference<>();

      runAsync(driver, () -> {
        Object result = driver.executeAsyncScript(script, array);
        if (result instanceof List) {
          value.set(toElements((List<?>) result));
        }
        else {
          value.set(toElement(result));
        }
      });

      waitForUnlockedOrAlert(driver);
      HtmlUnitAlert alert = (HtmlUnitAlert) driver.switchTo().alert();
      
      if (alert.isLocked()) {
        String text = alert.getText();
        alert.accept();
        waitForUnlock(driver);
        throw new UnhandledAlertException(text);
      }

      return getResponse(session, value.get());
  }

  @GET
  @Path("{session}/window_handle")
  public static Response getWindowHandle(@PathParam("session") String session) {
    String value = getDriver(session).getWindowHandle();
    return getResponse(session, value);
  }

  @GET
  @Path("{session}/window_handles")
  public static Response getWindowHandles(@PathParam("session") String session) {
    Set<String> value = getDriver(session, false).getWindowHandles();
    return getResponse(session, value);
  }

  @POST
  @Path("{session}/window")
  public static Response switchToWindow(@PathParam("session") String session, String content) {
    Map<String, String> map = getMap(content);
    getDriver(session, false).switchTo().window(map.get("name"));
    return getResponse(session, null);
  }

  @DELETE
  @Path("{session}/window")
  public static Response closeWindow(@PathParam("session") String session) {
    getDriver(session).close();
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/frame")
  public static Response frame(@PathParam("session") String session, String content) {
    Map<String, Map<String, ?>> map = getMap(content);
    HtmlUnitLocalDriver driver = getDriver(session);
    Map<String, ?> subMap = map.get("id");
    if (subMap != null) {
      int id = Integer.parseInt((String) subMap.get("ELEMENT"));
      HtmlUnitWebElement frame = driver.getElementById(id);
      driver.switchTo().frame(frame);
    }
    else {
      driver.switchTo().defaultContent();
    }
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/buttondown")
  public static Response buttondown(@PathParam("session") String session) {
    getDriver(session).buttondown();
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/buttonup")
  public static Response buttonup(@PathParam("session") String session) {
    getDriver(session).buttonup();
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/timeouts")
  public static Response setTimeouts(@PathParam("session") String session, String content) {
    Map<String, ?> map = getMap(content);
    String type = (String) map.get("type"); 
    long millis = (Long) map.get("ms");
    Timeouts timeouts = getDriver(session).manage().timeouts();
    switch (type) {
      case "script":
        timeouts.setScriptTimeout(millis, TimeUnit.MILLISECONDS);
        break;
      
      case "page load":
        timeouts.pageLoadTimeout(millis, TimeUnit.MILLISECONDS);
        break;

      case "implicit":
        timeouts.implicitlyWait(millis, TimeUnit.MILLISECONDS);
        break;
    }
    return getResponse(session, null);
  }

  @GET
  @Path("{session}/alert_text")
  public static Response alertText(@PathParam("session") String session) {
    HtmlUnitLocalDriver driver = getDriver(session, false);
    HtmlUnitAlert alert = waitForUnlockedOrAlert(driver);
    String value = alert.getText();
    return getResponse(session, value);
  }

  private static HtmlUnitAlert waitForUnlockedOrAlert(HtmlUnitLocalDriver driver) {
    HtmlUnitAlert alert = (HtmlUnitAlert) driver.switchTo().alert();
    while (lockedDrivers.contains(driver) && !alert.isLocked()) {
      RuntimeException t = exceptionsMap.get(driver);
      if (t != null) {
        exceptionsMap.remove(driver);
        unlockDriver(driver);
        throw t;
      }
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
    return alert;
  }

  @POST
  @Path("{session}/alert_text")
  public static Response alertTextPost(@PathParam("session") String session, String content) {
    Map<String, String> map = getMap(content);
    getDriver(session, false).switchTo().alert().sendKeys(map.get("text"));
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/accept_alert")
  public static Response acceptAlert(@PathParam("session") String session) {
    getDriver(session, false).switchTo().alert().accept();
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/dismiss_alert")
  public static Response dismissAlert(@PathParam("session") String session) {
    getDriver(session, false).switchTo().alert().dismiss();
    return getResponse(session, null);
  }

  @POST
  @Path("{session}/back")
  public static Response back(@PathParam("session") String session) {
    HtmlUnitLocalDriver driver = getDriver(session);
    
    runAsync(driver, () -> {
      driver.navigate().back();
    });
    return getResponse(session, null);
  }

}
