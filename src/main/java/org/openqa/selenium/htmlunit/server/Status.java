package org.openqa.selenium.htmlunit.server;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/status")
public class Status {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public static Object status() {
    Map<String, Object> map = new HashMap<>();
    map.put("sessionId", "");
    map.put("status", "0");
    Map<String, Object> value = new HashMap<>();
    map.put("value", value);
    return map;
  }
}
