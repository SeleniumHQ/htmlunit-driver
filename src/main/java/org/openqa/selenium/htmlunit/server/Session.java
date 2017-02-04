package org.openqa.selenium.htmlunit.server;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.BeanToJsonConverter;
import org.openqa.selenium.remote.JsonToBeanConverter;

@Path("/session")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Session {

  private static Map<String, Session> sessions = new HashMap<>();
  private static SecureRandom random = new SecureRandom();

  private String id;
  private HtmlUnitServerDriver driver;
  
  private static synchronized Session createSession() {
    String id;
    do {
      id = createSessionId();
    }
    while (sessions.containsKey(id));

    Session session = new Session();
    session.id = id;
    session.driver = new HtmlUnitServerDriver();
    sessions.put(id, session);
    return session;
  }
  
  private static String createSessionId() {
    return new BigInteger(16 * 8, random).toString(16);
  }

  private static HtmlUnitServerDriver getDriver(String sessionId) {
    return sessions.get(sessionId).driver;
  }

  @POST
  public Response newSession() {
    return getResponse(createSession().id, 0, new HashMap<>());
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
    return getResponse(session, 0, new HashMap<>());
  }

  @SuppressWarnings("unchecked")
  private static <T> Map<String, T> getMap(String content) {
    return new JsonToBeanConverter().convert(Map.class, content);
  }

  @POST
  @Path("{session}/element")
  public static Response findElement(@PathParam("session") String session, String content) {
    Map<String, String> map = getMap(content);
    String value = map.get("value");
    By by;
    switch (map.get("using")) {
      case "id":
        by = By.id(value);
        break;

      default:
        throw new IllegalArgumentException();
    }
    HtmlUnitWebElement e = (HtmlUnitWebElement) getDriver(session).findElement(by);
    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("ELEMENT", e.id);
    return getResponse(session, 0, valueMap);
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
  public static Response keys(@PathParam("session") String session, @PathParam("elementId") String elementId,
          @PathParam("name") String name) {
    String value = getDriver(session).getElementById(Integer.valueOf(elementId)).getAttribute(name);
    return getResponse(session, 0, value);
  }
}
