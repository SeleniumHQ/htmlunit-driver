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
  public Response toResponse(Throwable throwable) {
    Map<String, Object> map = new HashMap<>();
    String message = throwable.getMessage();
    if (throwable instanceof UnhandledAlertException) {
      map.put("alertText", ((UnhandledAlertException) throwable).getAlertText());
    }
    map.put("message", message);
    map.put("sessionId", request.getPathInfo().split("/")[2]);
    map.put("status", new ErrorCodes().toStatusCode(throwable));
    return Response.ok(new BeanToJsonConverter().convert(map), MediaType.APPLICATION_JSON).build();
  }
}
