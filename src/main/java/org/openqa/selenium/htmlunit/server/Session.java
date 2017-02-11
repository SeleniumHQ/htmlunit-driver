package org.openqa.selenium.htmlunit.server;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.BeanToJsonConverter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.JsonToBeanConverter;

@Path("/session")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Session {

  private static Map<String, Session> sessions = new HashMap<>();
  private static SecureRandom random = new SecureRandom();

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
    return sessions.get(sessionId).driver;
  }

  @POST
  @SuppressWarnings("unchecked")
  public Response newSession(String content) {
    Map<String, ?> map = new JsonToBeanConverter().convert(Map.class, content);
    Capabilities desiredCapabilities = new DesiredCapabilities((Map<String, ?>) map.get("desiredCapabilities"));
    Capabilities requiredCapabilities = new DesiredCapabilities((Map<String, ?>) map.get("requiredCapabilities"));
    return getResponse(createSession(desiredCapabilities, requiredCapabilities).id, 0, new HashMap<>());
  }

  private static Response getResponse(String sessionId, int status, Object value){
    Map<String, Object> map = new HashMap<>();
    map.put("sessionId", sessionId);
    map.put("status", status);
    map.put("value", value);
    return Response.ok(new BeanToJsonConverter().convert(map), MediaType.APPLICATION_JSON).build();
  }

  @POST
  @Path("{session}/url")
  public static Response go(@PathParam("session") String session, String content) {
    String url = new JsonToBeanConverter().convert(Map.class, content).get("url").toString();
    getDriver(session).get(url);
    return getResponse(session, 0, null);
  }

  @SuppressWarnings("unchecked")
  private static <T> Map<String, T> getMap(String content) {
    return new JsonToBeanConverter().convert(Map.class, content);
  }

  @POST
  @Path("{session}/element")
  public static Response findElement(@PathParam("session") String session, String content) {
    By by = getBy(content);
    HtmlUnitWebElement e = (HtmlUnitWebElement) getDriver(session).findElement(by);
    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("ELEMENT", e.id);
    return getResponse(session, 0, valueMap);
  }

  @POST
  @Path("{session}/elements")
  public static Response findElements(@PathParam("session") String session, String content) {
    By by = getBy(content);
    
    List<Map<String, Integer>> list = getElements(getDriver(session).findElements(by));
    return getResponse(session, 0, list);
  }

  private static List<Map<String, Integer>> getElements(List<WebElement> list) {
    return list.stream().map(i -> {
      Map<String, Integer> map2 = new HashMap<>();
      map2.put("ELEMENT", ((HtmlUnitWebElement) i).id);
      return map2;
    }).collect(Collectors.toList());
  }

  @POST
  @Path("{session}/element/{elementId}/elements")
  public static Response findElementsFromElement(@PathParam("session") String session,
      @PathParam("elementId") String elementId,
      String content) {
    By by = getBy(content);

    WebElement element = getDriver(session).getElementById(Integer.valueOf(elementId));
    List<Map<String, Integer>> list = getElements(element.findElements(by));
    return getResponse(session, 0, list);
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
    return getResponse(session, 0, null);
  }

  @POST
  @Path("{session}/click")
  public static Response click(@PathParam("session") String session, String content) {
    Map<String, ?> map = getMap(content);
    @SuppressWarnings("unused")
    int button = ((Long) map.get("button")).intValue();
    getDriver(session).click();
    return getResponse(session, 0, null);
  }

  @POST
  @Path("{session}/keys")
  public static Response keys(@PathParam("session") String session, String content) {
    Map<String, List<String>> map = getMap(content);
    getDriver(session).keys(map.get("value").get(0));
    return getResponse(session, 0, null);
  }

  @GET
  @Path("{session}/element/{elementId}/attribute/{name}")
  public static Response getElementAttribute(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId,
      @PathParam("name") String name) {
    String value = getDriver(session).getElementById(Integer.valueOf(elementId)).getAttribute(name);
    return getResponse(session, 0, value);
  }

  @GET
  @Path("{session}/element/{elementId}/text")
  public static Response getElementText(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    String value = getDriver(session).getElementById(Integer.valueOf(elementId)).getText();
    return getResponse(session, 0, value);
  }

  @GET
  @Path("{session}/element/{elementId}/css/{propertyName}")
  public static Response getElementCssValue(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId,
      @PathParam("propertyName") String propertyName) {
    String value = getDriver(session).getElementById(Integer.valueOf(elementId)).getCssValue(propertyName);
    return getResponse(session, 0, value);
  }

  @GET
  @Path("{session}/title")
  public static Response getTitle(@PathParam("session") String session) {
    String value = getDriver(session).getTitle();
    return getResponse(session, 0, value);
  }

  @GET
  @Path("{session}/element/{elementId}/displayed")
  public static Response elementDisplayed(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    boolean value = getDriver(session).getElementById(Integer.valueOf(elementId)).isDisplayed();
    return getResponse(session, 0, value);
  }

  @POST
  @Path("{session}/element/{elementId}/click")
  public static Response elementClick(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    getDriver(session).getElementById(Integer.valueOf(elementId)).click();
    return getResponse(session, 0, null);
  }

  @POST
  @Path("{session}/element/{elementId}/clear")
  public static Response elementClear(
      @PathParam("session") String session,
      @PathParam("elementId") String elementId) {
    getDriver(session).getElementById(Integer.valueOf(elementId)).clear();
    return getResponse(session, 0, null);
  }

  @POST
  @Path("{session}/execute")
  public static Response execute(@PathParam("session") String session, String content) {
    Map<String, ?> map = getMap(content);
    String script = (String) map.get("script");
    HtmlUnitLocalDriver driver = getDriver(session);

    @SuppressWarnings("unchecked")
    ArrayList<Map<String, String>> args = (ArrayList<Map<String, String>>) map.get("args");
    HtmlUnitWebElement[] array =
        args.stream().map(i -> Integer.valueOf(i.get("ELEMENT")))
        .map(driver::getElementById).toArray(size -> new HtmlUnitWebElement[size]);

    driver.executeScript(script, (Object[]) array);
    return getResponse(session, 0, null);
  }

  @GET
  @Path("{session}/window_handles")
  public static Response getWindowHandles(@PathParam("session") String session) {
    Set<String> value = getDriver(session).getWindowHandles();
    return getResponse(session, 0, value);
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
      driver.switchTo().frame(frame.getAttribute("id"));
    }
    else {
      driver.switchTo().parentFrame();
    }
    return getResponse(session, 0, null);
  }

}
