package org.openqa.selenium.htmlunit.server;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.remote.BeanToJsonConverter;
import org.openqa.selenium.remote.ErrorCodes;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

  @Context
  private HttpServletRequest request;
  
  @Override
  public Response toResponse(Throwable exception) {
    Map<String, Object> map = new HashMap<>();
    String message = exception.getMessage();
    if (exception instanceof UnhandledAlertException) {
      map.put("alertText", ((UnhandledAlertException) exception).getAlertText());
    }
    map.put("message", message);
    map.put("sessionId", request.getPathInfo().split("/")[2]);
    map.put("status", new ErrorCodes().toStatusCode(exception));
    return Response.ok(new BeanToJsonConverter().convert(map), MediaType.APPLICATION_JSON).build();
  }
}
