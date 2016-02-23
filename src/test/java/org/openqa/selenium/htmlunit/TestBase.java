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

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.ExternalResource;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.EmbeddedResourceHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class TestBase {

  public static class TestServer extends ExternalResource {

    private WebServer webServer;

    public String page(String pageAddress) {
      return webServer.getUri().toString() + pageAddress;
    }

    public String domain() {
      return webServer.getUri().getHost();
    }

    @Override
    protected void before() throws Throwable {
      super.before();
      Executor immediateExecutor = new Executor() {
        @Override
        public void execute(Runnable command) {
          command.run();
        }
      };
      webServer = WebServers.createWebServer(2713)
          .add(new EmbeddedResourceHandler("web", immediateExecutor, getClass()))
          .start()
          .get();
    }

    @Override
    protected void after() {
      try {
        webServer.stop().get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
      super.after();
    }
  }

  @ClassRule
  public static TestServer testServer = new TestServer();

  public HtmlUnitDriver driver;

  @Before
  public void initDriver() {
    driver = new HtmlUnitDriver(true);
    driver.get(testServer.page(""));
  }

  @After
  public void stopDriver() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Rule
  public ExpectedException thrown= ExpectedException.none();

}
